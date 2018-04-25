package main

import (
	"fmt"

	"../exlcm"
	golcm "github.com/lcm-proj/lcm/lcm-go/lcm"
)

func sendData(publisher chan<- []byte) {
	defer close(publisher)

	// Let's send 100 times some data.
	for i := 0; i < 100; i++ {
		example := &exlcm.ExlcmExampleT{
			Name: fmt.Sprintf("Datapoint %d", i),
		}

		encData, err := example.Encode()
		if err != nil {
			panic(err)
		}

		fmt.Println("sending data no", i)
		publisher <- encData
	}
}

func main() {
	// Create a new LCM instance.
	lcm, err := golcm.New()
	if err != nil {
		panic(err)
	}
	defer lcm.Destroy()

	publisher, errs := lcm.Publisher("EXAMPLE")
	go sendData(publisher)

FOR_SELECT:
	for {
		select {
		case err, ok := <-errs:
			if !ok {
				break FOR_SELECT
			}
			panic(err)
		}
	}
}
