package lcm

import (
	"runtime"
	"strconv"
	"strings"
	"testing"
	"time"
)

const (
	// The amount of messages we want to exchange here.
	messageGoal = 10000
	// Max time we want to wait for executing to be done.
	timeout = 4 * time.Second
)

var (
	// Some sample data, prefixed with '<message index>:' when sent.
	someBytes = []byte("abcdefghijklmnopqrstuvwxyz")
	someBytesLen = len(someBytes)
)

func runTest(t *testing.T, provider string) {
	// The amount of messages that we received.
	messages := 0
	// The amount of dropped messages.
	drops := 0
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
		publishChan, errChan := lcm.Publisher("TEST")
		for i := 1; i <= messageGoal; i++ {
			// Prefix with message #
			data := make([]byte, 0, 8+someBytesLen)
			data = append(data, strconv.Itoa(i)...)
			data = append(data, ": "...)
			data = append(data, someBytes...)

			publishChan <- data

			// Give the backend some slack so we don't have drops
			if i%99 == 0 {
				time.Sleep(1 * time.Millisecond)
			}
		}
		close(publishChan)

		// Check for and fail if errors
		select {
		case err := <-errChan:
			t.Error(err)
		default:
			t.Log("sending successful")
		}
	}()

	timer := time.After(timeout)
FOR_SELECT:
	for {
		select {
		case <-timer:
			t.Log("timed out after", timeout)
			break FOR_SELECT
		case data, ok := <-subscription.ReceiveChan:
			if !ok {
				break FOR_SELECT
			}
			messages++

			// Verify message sequence order
			id, err := strconv.Atoi(strings.Split(string(data), ":")[0])
			if err != nil {
				t.Fatal(err)
			}
			expected := messages+drops
			if expected != id {
				t.Errorf("%d messages dropped, expected %d, got %d",
					id-expected, expected, id)
				drops += id-expected
			}
		}

		// Stop, if we are already there...
		if messages+drops == messageGoal {
			break
		}
	}

	// Did we receive messageGoal messages?
	if messages != messageGoal {
		t.Fatalf("Expected %d but received %d messages, %d drops, go-backend drops %d.",
			messageGoal, messages, drops, subscription.Drops)
	}

	t.Log("received", "amount", messages)
}

func TestLCMDefaultProvider(t *testing.T) {
	if runtime.GOOS == "darwin" {
		t.Skip("Test disabled on macOS as it is failing")
	}
	runTest(t, "")
}

func TestLCMProviderUDPM(t *testing.T) {
	if runtime.GOOS == "darwin" {
		t.Skip("Test disabled on macOS as it is failing")
	}
	runTest(t, "udpm://239.255.76.67:7667?ttl=1")
}

func TestLCMProviderMemQ(t *testing.T) {
	if runtime.GOOS == "darwin" {
		t.Skip("Test disabled on macOS as it is failing")
	}
	runTest(t, "memq://")
}
