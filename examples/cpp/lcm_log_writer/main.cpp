#include <lcm/lcm.h>
#include <sys/time.h>
#include <unistd.h>

#include <cmath>
#include <fstream>
#include <iostream>
#include <lcm/lcm-cpp.hpp>
#include <pronto/joint_state_t.hpp>
#include <string>

using namespace std;

int64_t utime_now()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (int64_t) tv.tv_sec * 1000000 + tv.tv_usec;
}

class LcmLogWriter {
  public:
    LcmLogWriter(std::string filename);
    ~LcmLogWriter();
    void run();

  private:
    ifstream is;
    uint64_t eventnum;
    std::string filename_;

    lcm::LogFile *lf;
    lcm::LogEvent le;
    lcm::LCM mylcm;
    pronto::joint_state_t test_msg;
};

LcmLogWriter::LcmLogWriter(std::string filename) : filename_(filename), eventnum(0), lf(NULL)
{
    is.open(filename_.c_str());

    // we set them now forever
    test_msg.num_joints = 12;

    test_msg.joint_name.push_back("lf_haa_joint");
    test_msg.joint_name.push_back("lf_hfe_joint");
    test_msg.joint_name.push_back("lf_kfe_joint");

    test_msg.joint_name.push_back("rf_haa_joint");
    test_msg.joint_name.push_back("rf_hfe_joint");
    test_msg.joint_name.push_back("rf_kfe_joint");

    test_msg.joint_name.push_back("lh_haa_joint");
    test_msg.joint_name.push_back("lh_hfe_joint");
    test_msg.joint_name.push_back("lh_kfe_joint");

    test_msg.joint_name.push_back("rh_haa_joint");
    test_msg.joint_name.push_back("rh_hfe_joint");
    test_msg.joint_name.push_back("rh_kfe_joint");

    test_msg.joint_position = std::vector<float>(test_msg.num_joints, 0.0);
    test_msg.joint_velocity = std::vector<float>(test_msg.num_joints, 0.0);
    test_msg.joint_effort = std::vector<float>(test_msg.num_joints, 0.0);

    std::cout << "Preparing to write to logfile \"" << filename_ << "\"" << std::endl;

    lf = new lcm::LogFile(filename_, "w");

    if (!lf->good()) {
        std::cerr << "ERROR: logfile not ready!" << std::endl;
    }
}

LcmLogWriter::~LcmLogWriter()
{
    delete lf;
    is.close();
}

void LcmLogWriter::run()
{
    while (eventnum < 2500) {
        std::cout << "Eventnum: " << eventnum << std::endl;
        struct timeval tv;
        gettimeofday(&tv, NULL);
        test_msg.utime = utime_now();
        double f1 = 1;
        // 1 Hz
        double f2 = 2;
        // 2 Hz
        double f3 = 3;
        // 3 Hz
        test_msg.joint_position[0] = sin(2 * M_PI * f1 * (double) test_msg.utime / 1000000.0);
        // we want seconds inside
        test_msg.joint_velocity[0] = sin(2 * M_PI * f2 * (double) test_msg.utime / 1000000.0);
        // we want seconds inside
        test_msg.joint_effort[0] = sin(2 * M_PI * f3 * (double) test_msg.utime / 1000000.0);
        // we want seconds inside
        lcm::LogEvent le;
        le.channel = "JOINT_TEST";
        le.datalen = test_msg.getEncodedSize();
        le.data = malloc(le.datalen);
        le.eventnum = eventnum;
        le.timestamp = test_msg.utime;
        test_msg.encode(le.data, 0, le.datalen);
        lf->writeEvent(&le);
        mylcm.publish(le.channel, &test_msg);
        eventnum++;
        usleep(1000);
    }
}

int main(int argc, char *argv[])
{
    if (argc >= 2) {
        LcmLogWriter stl(argv[1]);
        stl.run();
    } else {
        std::cout << "Usage: " << argv[0] << " <path-to-file>/<filename>.lcmlog" << std::endl;
    }

    return 0;
}
