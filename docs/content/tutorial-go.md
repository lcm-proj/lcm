Go Tutorial {#tut_go}
====
\brief Sending and receiving LCM messages using Go

# Introduction {#tut_go_intro}

This tutorial will walk you through the main tasks for exchanging LCM messages
using the Go LCM library. Throughout this tutorial, we will look at how to:

\li Initialize an LCM instance.
\li Publish a message.
\li Subscribe to a channel and receive a message over it.

Like the other tutorials, this one uses the \p example_t message type defined in
the \ref tut_lcmgen "type definition tutorial", and assumes that you have
generated the Go bindings by executing for it:

\code
cd examples/go
go generate
\endcode

This should produce a few go files in `exlcm`, we will use the file called
`exlcm/example_t.go`.

The file contains everything you need to encode and decode data of type
example_t, which is necessary in order to use LCM.

# Initializing LCM {#tut_go_initialize}

That was not too difficult, right? Time to actually get our hands dirty with
some actual Go code.

The first thing that you will need to do, is to fetch the actual library. You
can use `go get` or any other dependency manager of your choice:
`go get github.com/lcm-proj/lcm/lcm-go/lcm`

After you have successfully executed that, you are able to use and import the
just fetched library. Make sure that the following code is inside a subdirectory
of `$GOPATH/src`.

\code
package main

import "github.com/lcm-proj/lcm/lcm-go/lcm"

func main() {
    lc, err := lcm.New()
    if err != nil {
        panic(err)
    }
    defer lc.Destroy()
}
\endcode

This will result in a new lcm instance by the name `lc` that is ready for use.

As we are dealing with Go bindings towards the C library, it is quite easy to
use. Maybe even easier than the C library itself. `lc` contains all the methods
that you will need to utilize LCM based communication.

Compile and run the code to see, if it works: `go run path/to/subdirectory`

As you can see, we are also using `defer` to destroy the LCM instance just right
before main is done executing.

# Publishing a message {#tut_go_publish}

Now that you have successfully created an LCM instance in Go, it is time to
actually send some data to a channel. This is done by sending data down a Golang
channel that the `Publisher(string)` method of the instance that we just created
returns.

The passed string is the channel's name that you want to send the data to.
Alongside the actual Go channel that you can send the data to, another channel
is returned. This is an error channel that you have to check for any errors that
could potentially occur.

Finally, make sure that you are actually sending (LCM) encoded data down the
channel. So where do you get that from? *From the Go bindings that you have
generated earlier!*

\code
    // Skipping the code above...
    // Create a pointer to a new ExampleT object.
    example := &exlcm.ExampleT{}

    publisher, errs := lc.Publisher("EXAMPLE")

    go func() {
        defer close(publisher)

        // Let's send that a couple of times
        for i := 0; i < 100; i++ {            
            // Encode the data.
            encEx, err := example.Encode()
            if err != nil {
                panic(err)
            }

            // Send down a channel.
            publisher <- encEx
        }
    }()

FOR_SELECT:
    for {
        select {
            err, ok := <- errs:
            if !ok {
                break FOR_SELECT
            }
            panic(err)
        }
    }
\endcode

As you can see, it does not require much code to send something through LCM
using Go. Obviously, in a real-world case, one would need to initialize
`example` with appropriate values, instead of initializing everything to default
values.

An important note: as soon as you are done with sending to a channel, you should
close the corresponding publisher channel, as we are doing in the `defer`
statement. This is necessary, so that you do not end up in a potential deadlock.
The errs channel is only closed *after* the publisher one is.

# Receiving LCM Messages {#tut_go_receive}

Receiving messages is just as simple as sending them out. It's just the other
way around. Needless to say, it is important that the sender and receiver
"know" about the channel that is used for communication. Therefore, we will
also use the channel called **EXAMPLE** that we used earlier to send out
information, as the receiving channel.

In order to receive data, you will need to check for data that comes down a
Go channel. Usually, deserialize the received data back into a suitable object
is a reasonable way to do here.

\code
    // Skipping the code from the initialization part.
    // Subscribe to the previously used EXAMPLE channel. The subscription Go
    // channel has a buffer size 5, whenever more messages are pending than the
    // buffer size they are dropped.
    if subscription, err := lc.Subscribe("EXAMPLE", 5); if err != nil {
        panic(err)
    }
    // Not explicitly necessary, as Destroy() also unsubscribes to all
    // subscriptions.
    defer lc.Unsubscribe(subscription)

FOR:
    for {
        select {
        // Usually you have a timeout to not deadlock.
        case <-time.After(5 * time.Second):
            break FOR

        case data, ok := <- subscription.ReceiveChan:
            if !ok {
                // That means that the channel was closed.
                break FOR
            }
            fmt.Println("Received information on", channel)

            // Empty example_t
            exType := &exlcm.ExampleT{}

            // Decode the received data
            if err := exType.Decode(data); err != nil {
                panic(err)
            }

            // Simple print out of the whole object.
            fmt.Println(exType)

        }
    }
\endcode

Done!

As you can see, we are also unsubscribing right before we are done. This is only
possible because we are timing out (and returning) after five seconds.
ReceiveChan is only closed whenever one unsubscribes. Therefore:: **be aware of
potential deadlocks that you could introduce to your application**.

# Conclusion and Important Information {#tu_go_concl}

Obviously, this tutorial outlines just a really simple use-case of LCM in Go.
Some of the practices used here ought to be avoided in a proper production
application (such as the use of panic for error handling).

Throughout the tutorial you might have noticed that the original type name which
was called `example_t` got transferred into something like `ExampleT`. This is
was an actively chosen design decision. As it is Go best-practice to use
Camel-Casing, every _ will be removed and the following character will be
capitalized. Furthermore, the casing describes if a member or function gets
exported or not in Go: capitalized member/function name = exported function;
small-cased member/function name = unexported function. Therefore, every type
gets automatically capitalized by the code generator.

You can find more complete and compilable executables here:
[listener/main.go](https://github.com/lcm-proj/lcm/blob/master/examples/go/listener/main.go) | [sender/main.go](https://github.com/lcm-proj/lcm/blob/master/examples/go/sender/main.go)

If you need more information about the language itself, you should check out
[the official Go web pages](https://golang.org/), which are usually of great
help.
