package main

import (
	"fmt"
	"time"

	"../exlcm"
	golcm "github.com/lcm-proj/lcm/lcm-go/lcm"
)

func main() {
	// Create a new LCM instance.
	lcm, err := golcm.New()
	if err != nil {
		panic(err)
	}

	// Subscripe to the previously used EXAMPLE channel.
	subscription, err := lcm.Subscribe("EXAMPLE", 5)
	if err != nil {
		panic(err)
	}

FOR_SELECT:
	for {
		select {
		case <-time.After(15 * time.Second):
			break FOR_SELECT

		case data, ok := <-subscription.ReceiveChan:
			if !ok {
				break FOR_SELECT
			}

			fmt.Println("received information on")

			// Empty example_t
			exType := &exlcm.ExlcmExampleT{}

			// Decode the received data
			if err := exType.Decode(data); err != nil {
				panic(err)
			}

			// Simple print out of the whole object.
			fmt.Println(exType)
		}
	}
}
