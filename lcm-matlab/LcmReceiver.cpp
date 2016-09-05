/**
 * \brief This is an LCM receiver which works together with the Autonomos Orocos framework.
 *
 * This receiver connects via LCM/tcpq to the Autonomos LogPlayer. It then syncronizes the time of the
 * Simulink simulation with that of the LogPlayer by exchanging LCM messages with the next log time.
 * It uses a variable step simulation time where the time for the next step is received from the LogPlayer.
 *
 * \author Georg Bremer
 */


#define S_FUNCTION_LEVEL 2
#define S_FUNCTION_NAME  LcmReceiver

#include "simstruc.h"

#include <stdint.h>
#include <ws2tcpip.h>
#pragma comment(lib, "Ws2_32.lib")
#include <stdexcept>
#include <sstream>

#define MAGIC_SERVER 0x287617fa      // first word sent by server
#define MAGIC_CLIENT 0x287617fb      // first word sent by client
#define PROTOCOL_VERSION 0x0100               // what version do we implement?
#define MESSAGE_TYPE_PUBLISH     1
#define MESSAGE_TYPE_SUBSCRIBE   2
#define MESSAGE_TYPE_UNSUBSCRIBE 3

#define MAX_CHANNEL_SIZE 256
#define LOG_TIME_IN "AutonomosGetLogTime"
#define LOG_TIME_OUT "AutonomosSetLogTime"
#define LOG_TIME_IN_LEN strlen(LOG_TIME_IN)
#define LOG_TIME_OUT_LEN strlen(LOG_TIME_OUT)


class WSAInit {
	WSADATA wsaData;
public:
	WSAInit() {
		int err = WSAStartup(MAKEWORD(2, 2), &wsaData);
		if (err) {
			throw std::logic_error("WSAStartup failed");
		}

		if (LOBYTE(wsaData.wVersion) != 2 || HIBYTE(wsaData.wVersion) != 2) {
			throw std::logic_error("Could not find a usable version of Winsock.dll");
		}
	}

	~WSAInit() {
		WSACleanup();
	}

} WSAInitiator;

class WSAConnection {
	std::string mError;
	SOCKET mSocket;
public:
	WSAConnection(char const * address, char const * port)
		: mSocket(INVALID_SOCKET)
	{
		//connect
		addrinfo * result;
		addrinfo hints;
		ZeroMemory(&hints, sizeof(hints));
		hints.ai_family = AF_UNSPEC;
		hints.ai_socktype = SOCK_STREAM;
		hints.ai_protocol = IPPROTO_TCP;

		int err = getaddrinfo(address, port, &hints, &result);
		if (err) {
			printf("Address: %s:%s\n", address, port);
			mError = gai_strerror(err);
			return;
		}

		for (addrinfo * ptr = result; ptr != NULL; ptr = ptr->ai_next)
		{
			mSocket = socket(ptr->ai_family, ptr->ai_socktype, ptr->ai_protocol);

			if (INVALID_SOCKET == mSocket) {
				mError = "Socket failed";
				return;
			}

			if (SOCKET_ERROR == connect(mSocket, ptr->ai_addr, ptr->ai_addrlen)) {
				closesocket(mSocket);
				mSocket = INVALID_SOCKET;
				continue;
			}
			break;
		}
		freeaddrinfo(result);

		if (INVALID_SOCKET == mSocket) {
			mError = "Could not connect to server";
			return;
		}

	}

	operator SOCKET & () {
		return mSocket;
	}

	bool isValid() const {
		return INVALID_SOCKET != mSocket;
	}

	char const * error() const {
		return mError.c_str();
	}

	~WSAConnection() {
		closesocket(mSocket);
	}
};

class LogTime {
	uint64_t const mHash;
	int64_t mTime;

	static void byteSwap(int64_t * out, int64_t const * in) {
		char * bout = (char *) out;
		char const * bin = (char const *) in;
		bout[0] = bin[7];
		bout[1] = bin[6];
		bout[2] = bin[5];
		bout[3] = bin[4];
		bout[4] = bin[3];
		bout[5] = bin[2];
		bout[6] = bin[1];
		bout[7] = bin[0];
	}

public:
	LogTime()
		: mHash(0x59d123c63d809135ll)
		, mTime(0)
	{
	}

	int64_t time() const {
		int64_t t;
		byteSwap(&t, &mTime);
		return t;
	}
};

class LCM {
	WSAConnection mConnection;

	uint32_t mMaxDataLength;

	LogTime mLogTime;

	std::stringstream mError;

public:
	char mChannel[MAX_CHANNEL_SIZE];
	uint32_t mChannelLength;
	char * mData;
	uint32_t mDataLength;

	LCM(char const * address, char const * port, uint32_t maxDataLength)
		: mConnection(address, port)
		, mData(new char[maxDataLength])
		, mMaxDataLength(maxDataLength)
	{
		if (!mConnection.isValid()) {
			return;
		}

		if (_send_uint32(mConnection, MAGIC_CLIENT)
			|| _send_uint32(mConnection, PROTOCOL_VERSION)) {
			return;
		}

		uint32_t server_magic;
		uint32_t server_version;
		if (_recv_uint32(mConnection, &server_magic)
			|| _recv_uint32(mConnection, &server_version)) {
			return;
		}

		if (MAGIC_SERVER != server_magic) {
			mError << "Invalid response from server" << std::endl;
			return;
		}
	}

	~LCM() {
		delete[] mData;
	}

	bool subscribe(char const * channel) {
		if (_sub_unsub_helper(mConnection, channel, MESSAGE_TYPE_SUBSCRIBE)) {
			mError << "Error subscribing" << std::endl;
			return false;
		}
		return true;
	}

	bool subscribeTime() {
		return subscribe(LOG_TIME_IN);
	}

	bool sendTime() {
		if(_send_uint32(mConnection, MESSAGE_TYPE_PUBLISH)
		    || _send_uint32(mConnection, LOG_TIME_OUT_LEN)
		    || (LOG_TIME_OUT_LEN != _send_fully(mConnection, LOG_TIME_OUT, LOG_TIME_OUT_LEN))
		    || _send_uint32(mConnection, sizeof(LogTime))
		    || (sizeof(LogTime) != _send_fully(mConnection, &mLogTime, sizeof(LogTime)))
		    ) {
		    mError << "Error sending log time - disconnected" << std::endl;
		    return false;
		}
		printf("Sent %lluns\n", mLogTime.time());
		return true;
	}

	int mOutputMessages;
	int mTotalMessages;
	bool receiveMessage() {
		uint32_t msg_type;
		if (_recv_uint32(mConnection, &msg_type) || _recv_uint32(mConnection, &mChannelLength)) {
			mError << "Error receiving message header - disconnected" << std::endl;
			return false;
		}

		if (mChannelLength > MAX_CHANNEL_SIZE) {
			mError << "Channel name to long: " << mChannelLength << " > " << MAX_CHANNEL_SIZE;
			return false;
		}

		if (mChannelLength != _recv_fully(mConnection, mChannel, mChannelLength)
			|| _recv_uint32(mConnection, &mDataLength)
		   ){
			mError << "Error receiving channel - disconnected";
			return false;
		}
		mChannel[mChannelLength] = 0;

		if (mDataLength > mMaxDataLength) {
			mError << "Message to big: " << mDataLength << " > " << mMaxDataLength;
			return false;
		}

		if (mDataLength != _recv_fully(mConnection, mData, mDataLength)) {
			mError << "Error receiving data - disconnected";
			return false;
		}
		++mTotalMessages;
		printf("Total: %i, outputted: %i\n", mTotalMessages, mOutputMessages);
		return true;
	}

	bool isLastMessageTime() {
		//printf("is channel \"%s\" == \"%s\"?\n", mChannel, LOG_TIME_IN);
		return !strcmp(mChannel, LOG_TIME_IN);
	}

	bool readTime() {
		if (isLastMessageTime()) {
			if (sizeof(LogTime) != mDataLength) {
				mError << "Wrong size for recieved time: " << mDataLength << " != " << sizeof(LogTime) << std::endl;
				return false;
			}
			memcpy(&mLogTime, mData, sizeof(LogTime));
			//printf("Read %lluns\n", mLogTime.time());
			return true;
		}
		return false;
	}

	int64_t time() const {
		return mLogTime.time();
	}

	bool isValid() {
		return mConnection.isValid() && mError.str().empty();
	}

	char const * error() {
		if (mError.str().empty()) {
			return mConnection.error();
		}
		return mError.str().c_str();
	}

private:
	//these functions are directly copied from LCM
	static int
	_recv_fully(int fd, void *b, int len)
	{
	    int cnt=0;
	    int thiscnt;
	    char *bb=(char*) b;

	    while(cnt<len) {
		thiscnt=recv(fd, &bb[cnt], len-cnt, 0);
		if(thiscnt<0) {
		    perror("_recv_fully");
		    return -1;
		}
		if(thiscnt == 0) {
		    return -1;
		}
		cnt+=thiscnt;
	    }
	    return cnt;
	}

	static int
	_recv_uint32(int fd, uint32_t *result)
	{
	    uint32_t v;
	    if(_recv_fully(fd, &v, 4) != 4)
		return -1;
	    *result = ntohl(v);
	    return 0;
	}

	static int
	_send_fully(int fd, const void *b, int len)
	{
	    int cnt=0;
	    int thiscnt;
	    char *bb=(char*) b;

	    while(cnt<len) {
		thiscnt=send(fd, &bb[cnt], len-cnt, 0);
		if(thiscnt<0) {
		    perror("_send_fully");
		    return -1;
		}
		if(thiscnt == 0) {
		    return -1;
		}
		cnt+=thiscnt;
	    }
	    return cnt;
	}

	static int
	_send_uint32(int fd, uint32_t v)
	{
	    uint32_t n = htonl(v);
	    return (_send_fully(fd, &n, 4) == 4) ? 0 : -1;
	}

	static int
	_sub_unsub_helper(int fd, const char *channel, uint32_t msg_type)
	{
	    uint32_t channel_len = strlen(channel);
	    if(fd < 0) {
		//fprintf(stderr, "LCM not connected (%d)\n", fd);
		return -1;
	    }

	    if(_send_uint32(fd, msg_type) ||
	       _send_uint32(fd, channel_len) ||
	       (channel_len != _send_fully(fd, channel, channel_len)))
	    {
		perror("LCM tcpq");
	//        dbg(DBG_LCM, "Disconnected!\n");
		return -1;
	    }

	    return 0;
	}
};

static void errorOut(SimStruct * S, char const * text, ...) {
	static char msg[256];
	va_list args;
	va_start(args, text);
	vsprintf(msg, text, args);
	ssSetErrorStatus(S, msg);
	return;
}

/*====================*
 * S-function methods *
 *====================*/

#define MDL_CHECK_PARAMETERS
#if defined(MDL_CHECK_PARAMETERS)  && defined(MATLAB_MEX_FILE)
/*
 * Check to make sure that each parameter is 1-d and positive
 */
static void mdlCheckParameters(SimStruct *S)
{

    const mxArray * address = ssGetSFcnParam(S, 0);
    const mxArray * port = ssGetSFcnParam(S, 1);
    const mxArray * channel = ssGetSFcnParam(S, 2);
    const mxArray * maxMessageSize = ssGetSFcnParam(S, 3);

    if (!mxIsChar(address)) {
        ssSetErrorStatus(S, "Address must be a string host");
        return;
    }

    if (!mxIsChar(port)) {
        ssSetErrorStatus(S, "Port must be a string");
        return;
    } 

    if (!mxIsChar(channel)) {
        ssSetErrorStatus(S, "Channel must be a string");
        return;
    } 

    if (!mxIsNumeric(maxMessageSize) || mxIsComplex(maxMessageSize)) {
        ssSetErrorStatus(S, "MaxMessageSize must be a real integer");
        return;
    }
}
#endif


#define MDL_INITIAL_SIZES
/* Function: mdlInitializeSizes ===============================================
 * Abstract:
 *    The sizes information is used by Simulink to determine the S-function
 *    block's characteristics (number of inputs, outputs, states, etc.).
 */
static void mdlInitializeSizes(SimStruct *S)
{
    ssSetNumSFcnParams(S, 4);  /* Number of expected parameters */
#if defined(MATLAB_MEX_FILE)
    if (ssGetNumSFcnParams(S) == ssGetSFcnParamsCount(S)) {
        mdlCheckParameters(S);
        if (ssGetErrorStatus(S) != NULL) {
            return;
        }
    } else {
        return; /* Parameter mismatch will be reported by Simulink */
    }
#endif
    ssSetSFcnParamTunable(S, 0, 0);
    ssSetSFcnParamTunable(S, 1, 0);
    ssSetSFcnParamTunable(S, 2, 0);
    ssSetSFcnParamTunable(S, 3, 0);

    ssSetNumContStates(S, 0);
    ssSetNumDiscStates(S, 0);

    if (!ssSetNumInputPorts(S, 0)) return;
    
    if (!ssSetNumOutputPorts(S, 4)) return;

    mxArray const * maxMessageSize = ssGetSFcnParam(S, 3);
    ssSetOutputPortWidth(S, 0, (int) mxGetScalar(maxMessageSize));

    ssSetOutputPortWidth(S, 1, 1);
    ssSetOutputPortWidth(S, 2, MAX_CHANNEL_SIZE);
    ssSetOutputPortWidth(S, 3, 1);

    ssSetOutputPortDataType(S, 0, SS_UINT8);
    ssSetOutputPortDataType(S, 2, SS_UINT8);

    ssSetNumRWork(S, 0);
    ssSetNumIWork(S, 0);
    ssSetNumPWork(S, 1); // storage for lcm
    ssSetNumModes(S, 0);
    ssSetNumNonsampledZCs(S, 0);

    ssSetOptions(S, 0);//TODO
}


/* Function: mdlInitializeSampleTimes =========================================
 * Abstract:
 *    This function is used to specify the sample time(s) for your
 *    S-function. You must register the same number of sample times as
 *    specified in ssSetNumSampleTimes.
 */
static void mdlInitializeSampleTimes(SimStruct *S)
{
    //ssSetSampleTime(S, 0, INHERITED_SAMPLE_TIME);
    ssSetSampleTime(S, 0, VARIABLE_SAMPLE_TIME);
    ssSetOffsetTime(S, 0, 0);//FIXED_IN_MINOR_STEP_OFFSET);
    ssSetModelReferenceSampleTimeDefaultInheritance(S);
}

#define MDL_START
  /* Function: mdlStart =======================================================
   * Abstract:
   *    This function is called once at start of model execution. If you
   *    have states that should be initialized once, this is the place
   *    to do it.
   */
static void mdlStart(SimStruct *S)
{
	char const * address = mxArrayToString(ssGetSFcnParam(S, 0));
	char const * port = mxArrayToString(ssGetSFcnParam(S, 1));
	char const * channel = mxArrayToString(ssGetSFcnParam(S, 2));
	uint32_t const maxDataLength = (uint32_t) mxGetScalar(ssGetSFcnParam(S, 3));


	LCM * lcm = new LCM(address, port, maxDataLength);
	ssGetPWork(S)[0] = (void *) lcm;

	lcm->subscribeTime();
	lcm->sendTime();
	if (lcm->receiveMessage()) {
		//printf("received: %s\n", lcm->mChannel);
		//printf("hash: %llx\n", *(uint64_t*)lcm->mData);
		if (!lcm->readTime()) {
			printf(lcm->error());
			ssSetErrorStatus(S, "Received something unexpected");
			return;
		}
	}
	lcm->subscribe(channel);

	if (!lcm->isValid()) {
		ssSetErrorStatus(S, lcm->error());
	}
	return;

}


/* Function: mdlOutputs =======================================================
 * Abstract:
 *    In this function, you compute the outputs of your S-function
 *    block.
 */
static void mdlOutputs(SimStruct *S, int_T tid)
{
	UNUSED_ARG(tid);

	uint8_t * data_buf = (uint8_t *) ssGetOutputPortSignal(S,0);
	real_T * data_buf_len = ssGetOutputPortRealSignal(S,1);

	uint8_t * channel_buf = (uint8_t *) ssGetOutputPortSignal(S,2);
	real_T * channel_buf_len = ssGetOutputPortRealSignal(S,3);

	LCM * lcm = (LCM *) ssGetPWork(S)[0];

	//printf("Update\n");

	//This would be the place for syncronizing if we would be periodic

	lcm->sendTime();
	while (1) {
		if (!lcm->receiveMessage()) {
			ssSetErrorStatus(S, lcm->error());
			return;
		}

		//printf("Received - ");
		if (lcm->readTime()) {
			//printf("time\n");
			break;
		}
		else {
			memcpy(channel_buf, lcm->mChannel, lcm->mChannelLength);
			*channel_buf_len = lcm->mChannelLength;
			memcpy(data_buf, lcm->mData, lcm->mDataLength);
			*data_buf_len = lcm->mDataLength;
			//printf("%s\n", lcm->mChannel);
		}
	}
	++lcm->mOutputMessages;
}

#define MDL_GET_TIME_OF_NEXT_VAR_HIT
#if defined(MDL_GET_TIME_OF_NEXT_VAR_HIT) && (defined(MATLAB_MEX_FILE) || \
                                              defined(NRT))
  /* Function: mdlGetTimeOfNextVarHit =========================================
   * Abstract:
   *    This function is called to get the time of the next variable sample
   *    time hit. This function is called once for every major integration time
   *    step. It must return time of next hit by using ssSetTNext. The time of
   *    the next hit must be greater than ssGetT(S).
   *
   *    Note, the time of next hit can be a function of the input signal(s).
   */

static void mdlGetTimeOfNextVarHit(SimStruct *S)
{
	//time_T timeOfNextHit = ssGetT(S) /* + offset */ ;
	LCM * lcm = (LCM *) ssGetPWork(S)[0];
	time_T offset = lcm->time() * 1e-9;
	ssSetTNext(S, ssGetTStart(S) + offset);
	//printf("next var ticks, now=%f, next=%f\n", ssGetT(S), ssGetTNext(S));
}
#endif /* MDL_GET_TIME_OF_NEXT_VAR_HIT */


/* Function: mdlTerminate =====================================================
 * Abstract:
 *    In this function, you should perform any actions that are necessary
 *    at the termination of a simulation.  For example, if memory was
 *    allocated in mdlStart, this is the place to free it.
 */
static void mdlTerminate(SimStruct *S)
{
	LCM * lcm = (LCM *)ssGetPWork(S)[0];
	delete lcm;
}
/*======================================================*
 * See sfuntmpl.doc for the optional S-function methods *
 *======================================================*/

/*=============================*
 * Required S-function trailer *
 *=============================*/

#ifdef  MATLAB_MEX_FILE    /* Is this file being compiled as a MEX-file? */
#include "simulink.c"      /* MEX-file interface mechanism */
#else
#include "cg_sfun.h"       /* Code generation registration function */
#endif

