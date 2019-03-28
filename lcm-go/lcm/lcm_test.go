package lcm

import (
	"testing"
	"time"
)

const (
	// The amount of messages we want to exchange here.
	messageGoal = 10000
	// Max time we want to wait for executing to be done.
	timeout = 1 * time.Second
)

var (
	// Some sample data.
	someBytes = []byte("abcdefghijklmnopqrstuvwxyz")
)

func runTest(t *testing.T, provider string) {
	// The amount of messages that we received.
	messages := 1
	var lcm LCM
	var err error

	// New LCM
	if len(provider) != 0 {
		lcm, err = NewProvider(provider)
	} else {
		lcm, err = New()
	}

	if err != nil {
		t.Fatal(err)
	}

	defer func() {
		err = lcm.Destroy()
		if err != nil {
			t.Fatal(err)
		}
	}()

	// Subscribe to "TEST" LCM channel using Go
	subscription, err := lcm.Subscribe("TEST", messageGoal)
	if err != nil {
		t.Fatal(err)
	}
	defer func() {
		if err = lcm.Unsubscribe(subscription); err != nil {
			t.Error(err)
		}
	}()

	// Send messageGoal messages
	go func() {
		t.Log("sending", "amount", messageGoal)
		publishChan, _ := lcm.Publisher("TEST")
		for i := 1; i < messageGoal; i++ {
			publishChan <- someBytes
		}
		close(publishChan)
	}()

FOR_SELECT:
	for {
		select {
		case <-time.After(timeout):
			t.Log("timed out after", timeout)
			break FOR_SELECT
		case _, ok := <-subscription.ReceiveChan:
			if !ok {
				break FOR_SELECT
			}
			messages++
		}

		// Stop, if we are already there...
		if messages == messageGoal {
			break
		}
	}

	// Did we receive messageGoal messages?
	if messages != messageGoal {
		t.Fatalf("Expected %d but received %d messages.", messageGoal, messages)
	}

	t.Log("received", "amount", messages)
}

func TestLCMDefaultProvider(t *testing.T) {
	runTest(t, "")
}

func TestLCMProviderUDPM(t *testing.T) {
	runTest(t, "udpm://239.255.76.67:7667?ttl=1")
}

func TestLCMProviderMemQ(t *testing.T) {
	runTest(t, "memq://")
}
