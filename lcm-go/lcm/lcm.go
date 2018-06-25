package lcm

// #cgo LDFLAGS: -llcm
//
// #include <stdlib.h>
// #include <string.h>
// #include <lcm/lcm.h>
//
// extern void goLCMCallbackHandler(void *, int, char *);
//
// static void lcm_msg_handler(const lcm_recv_buf_t *buffer,
//                                   const char *channel, void *userdata) {
//      (void)userdata;
//      goLCMCallbackHandler((void *)buffer->data, (int)buffer->data_size,
//                           (char *)channel);
// }
//
// static lcm_subscription_t * lcm_go_subscribe(lcm_t *lcm,
//                                              const char *channel) {
//     return lcm_subscribe(lcm, channel, &lcm_msg_handler, NULL);
// }
import "C"

import (
	"errors"
	"unsafe"
)

// Subscription is a wrapper type for a subscription to an LCM channel.
type Subscription struct {
	ReceiveChan chan []byte // Channel on which messages to the subscribed channel are forwarded
	Drops       int         // The number of dropped packages so far
	channel     string
	cPtr        *C.lcm_subscription_t
}

var subscriptions map[LCM]map[string]Subscription

func init() {
	subscriptions = make(map[LCM]map[string]Subscription)
}

// Channel returns the LCM channel name of the subscription.
func (subscription Subscription) Channel() string {
	return subscription.channel
}

// LCM is a wrapper type for all things LCM.
type LCM struct {
	Errors chan error // Errors when handling incoming LCM messages
	closed bool
	cPtr   *C.lcm_t
}

// New returns a new LCM instance. In case of a failure, an error is returned.
func New() (LCM, error) {
	cPtr := C.lcm_create(nil)
	if cPtr == nil {
		return LCM{}, errors.New("could not create c pointer to lcm_t")
	}

	lcm := LCM{
		Errors: make(chan error),
		cPtr:   cPtr,
	}

	go lcm.handle()

	subscriptions[lcm] = make(map[string]Subscription)

	return lcm, nil
}

// Subscribe subscribes to an LCM channel with the given buffer size.
// Whenever an incoming message is handled by LCM the encoded data is sent in
// the form of a []byte down the ReceiveChan member of the returned
// Subscription.
func (lcm LCM) Subscribe(channel string, size int) (Subscription, error) {
	cChannel := C.CString(channel)
	defer C.free(unsafe.Pointer(cChannel))

	cPtr := C.lcm_go_subscribe(lcm.cPtr, cChannel)
	if cPtr == nil {
		return Subscription{}, errors.New("could not subscribe to channel " +
			channel)
	}

	subscription := Subscription{
		ReceiveChan: make(chan []byte, size),
		channel:     channel,
		cPtr:        cPtr,
	}
	subscriptions[lcm][channel] = subscription

	return subscription, nil
}

// Unsubscribe removes an Subscription from an LCM object.
func (lcm LCM) Unsubscribe(subscription Subscription) error {
	status := C.lcm_unsubscribe(lcm.cPtr, subscription.cPtr)
	if status != 0 {
		return errors.New("could not unsubsribe from " + subscription.channel)
	}

	close(subscription.ReceiveChan)

	if _, ok := subscriptions[lcm][subscription.channel]; ok {
		delete(subscriptions[lcm], subscription.channel)
	}

	return nil
}

// UnsubscribeAll removes all Subscriptions from an LCM object.
func (lcm LCM) UnsubscribeAll() error {
	for _, sub := range subscriptions[lcm] {
		if err := lcm.Unsubscribe(sub); err != nil {
			return err
		}
	}
	return nil
}

// Publisher returns a channel that the caller can send data to, which is
// sent out to the channel passed as the argument. Furthermoe, an error channel
// is returned that allows the caller to listen for and handle any errors.
func (lcm LCM) Publisher(channel string) (chan<- []byte, <-chan error) {
	publisher := make(chan []byte)
	errs := make(chan error)

	go func() {
		defer close(errs)
	FOR_SELECT:
		for {
			select {
			case data, ok := <-publisher:
				if !ok {
					break FOR_SELECT
				}

				dataSize := C.size_t(len(data))

				buffer := C.malloc(dataSize)
				if buffer == nil {
					errs <- errors.New("could not malloc memory for lcm message")
				}
				defer C.free(buffer)
				C.memcpy(buffer, unsafe.Pointer(&data[0]), dataSize)

				cChannel := C.CString(channel)
				defer C.free(unsafe.Pointer(cChannel))

				status := C.lcm_publish(lcm.cPtr, cChannel, buffer,
					C.uint(dataSize))
				if status == -1 {
					errs <- errors.New("could not publish lcm message")
				}
			}
		}
	}()

	return publisher, errs
}

// Destroy destroys an LCM object, so that it can be picked up by the garbage
// collector. This method also unsubscribes to all channels that lcm previously
// subscribed to.
func (lcm LCM) Destroy() error {
	lcm.closed = true

	C.lcm_destroy(lcm.cPtr)

	if err := lcm.UnsubscribeAll(); err != nil {
		return err
	}

	delete(subscriptions, lcm)

	return nil
}

func (lcm LCM) handle() {
	defer close(lcm.Errors)

	for !lcm.closed {
		if status := C.lcm_handle(lcm.cPtr); status != 0 {
			lcm.Errors <- errors.New("could not call lcm_handle")
		}
	}
}

//export goLCMCallbackHandler
func goLCMCallbackHandler(data unsafe.Pointer, size C.int, name *C.char) {
	channel := C.GoString(name)
	buffer := C.GoBytes(data, size)

	for _, subs := range subscriptions {
		if sub, ok := subs[channel]; ok {
			select {
			case sub.ReceiveChan <- buffer:
				break
			default:
				sub.Drops++
			}
		} else {
			panic("LCM gave us a msg on a non-existing channel")
		}
	}
}
