package client

import (
	"bytes"
	"lcmtest"
	"lcmtest2"
	"math/rand"
	"reflect"
	"strconv"
	"testing"
	"time"

	lcm "../../lcm-go/lcm"
)

// 1. Echo test
type echo struct {
	data []byte
}

func (e *echo) Encode() ([]byte, error) {
	return e.data, nil
}
func (e *echo) Decode(d []byte) error {
	e.data = d
	return nil
}
func echoGenerate(n int) (tester, error) {
	rand.Seed(int64(n))
	datalen := rand.Intn(10000) + 10
	data := make([]byte, datalen)
	_, _ = rand.Read(data)

	return &echo{data: data}, nil
}

// 2. primitives_t test
func primitiveTGenerate(n int) (tester, error) {
	ranges := make([]int16, n)
	for i := 0; i < n; i++ {
		ranges[i] = int16(i)
	}
	data := lcmtest.LcmtestPrimitivesT{
		I8:          int8(n % 100),
		I16:         int16(n) * 10,
		I64:         int64(n) * 10000,
		Position:    [3]float32{float32(n), float32(n), float32(n)},
		Orientation: [4]float64{float64(n), float64(n), float64(n), float64(n)},
		Ranges:      ranges,
		Name:        strconv.Itoa(n),
		Enabled:     (n % 2) != 0,
	}
	if _, err := data.NumRanges(); err != nil {
		return nil, err
	}
	return &data, nil
}

// 3. primitives_list_t
func primitivesListTGenerate(n int) (tester, error) {
	primitivesT := make([]lcmtest.LcmtestPrimitivesT, n)
	for i := 0; i < n; i++ {
		ranges := make([]int16, i)
		for j := 0; j < i; j++ {
			ranges[j] = -int16(j)
		}
		primitivesT[i] = lcmtest.LcmtestPrimitivesT{
			I8:          -int8(i % 100),
			I16:         -(int16(i) * 10),
			I64:         -(int64(i) * 10000),
			Position:    [3]float32{float32(-i), float32(-i), float32(-i)},
			Orientation: [4]float64{float64(-i), float64(-i), float64(-i), float64(-i)},
			Ranges:      ranges,
			Name:        strconv.Itoa(-i),
			Enabled:     (i % 2) == 0,
		}
		if _, err := primitivesT[i].NumRanges(); err != nil {
			return nil, err
		}
	}
	data := lcmtest.LcmtestPrimitivesListT{
		Items: primitivesT,
	}
	if _, err := data.NumItems(); err != nil {
		return nil, err
	}
	return &data, nil
}

// 4. node_t
func nodeTGenerate(n int) (tester, error) {
	data := lcmtest.LcmtestNodeT{}
	for i := 0; i < n; i++ {
		children := make([]lcmtest.LcmtestNodeT, i+1)
		for j := 0; j <= i; j++ {
			children[j] = data.Copy()
		}

		data = lcmtest.LcmtestNodeT{
			Children: children,
		}

		if _, err := data.NumChildren(); err != nil {
			return nil, err
		}
	}
	return &data, nil
}

// 5. multidim_array_t
func multidimArrayTGenerate(n int) (tester, error) {
	data := lcmtest.LcmtestMultidimArrayT{
		Data: make([][][]int32, n),
	}
	for i := 0; i < n; i++ {
		data.Data[i] = make([][]int32, n)
		for j := 0; j < n; j++ {
			data.Data[i][j] = make([]int32, n)
			for k := 0; k < n; k++ {
				data.Data[i][j][k] = int32(i*n*n + j*n + k)
			}
		}
	}
	for p := 0; p < 2; p++ {
		data.Strarray[p] = make([]string, n)
		for q := 0; q < n; q++ {
			data.Strarray[p][q] = strconv.Itoa(p*n + q)
		}
	}

	if _, err := data.SizeA(); err != nil {
		return nil, err
	}
	if _, err := data.SizeB(); err != nil {
		return nil, err
	}
	if _, err := data.SizeC(); err != nil {
		return nil, err
	}

	return &data, nil
}

// 6. cross_package_t
func crossPackageTGenerate(n int) (tester, error) {
	another := lcmtest2.Lcmtest2AnotherTypeT{
		Val: int32(n),
	}

	primitives, err := primitiveTGenerate(n)
	if err != nil {
		return nil, err
	}

	data := lcmtest2.Lcmtest2CrossPackageT{
		Primitives: *primitives.(*lcmtest.LcmtestPrimitivesT),
		Another:    another,
	}

	return &data, nil
}

type tester interface {
	Encode() ([]byte, error)
	Decode([]byte) error
}

// Generator function that generates the data for a specific iteration of a
// TC and send that data on the specific publish channel for a TC
type generatorFunc func(n int) (tester, error)

// Test is the high level test implementation
func test(lc lcm.LCM, iterations int, pub, sub string,
	generate generatorFunc, typ tester, step int) func(t *testing.T) {
	return func(t *testing.T) {
		t.Log("Subscribing to", sub)
		subs, err := lc.Subscribe(sub, 1)
		if err != nil {
			t.Fatal(err)
		}

		publisher, errs := lc.Publisher(pub)
		defer close(publisher)

		for i := 0; i < iterations; i++ {
			// Generate data
			data, err := generate(i)
			if err != nil {
				t.Fatal(err)
			}

			var encoded []byte
			encoded, err = data.Encode()
			if err != nil {
				t.Fatal(err)
			}

			// Send and receive data
			t.Log("Sent data on", pub)
			publisher <- encoded

			t.Log("Receiving data on", sub)
			validate(t, subs, i+step, typ, generate)
		}

		// Check for any leftovers or errors
		for {
			select {
			case data := <-subs.ReceiveChan:
				t.Fatal("Leftover data found", data)
			case err := <-errs:
				t.Fatal("Error during publish", err)
			case err := <-lc.Errors:
				t.Fatal("LCM error", err)
			default:
				return
			}
		}
	}
}

func validate(t *testing.T, subs lcm.Subscription, i int, data tester, generate generatorFunc) {
	select {
	case <-time.After(1 * time.Second):
		t.Fatal("Timeout")
	case received, ok := <-subs.ReceiveChan:
		if !ok {
			t.Fatal("Receive channel closed")
		}

		// Validate data
		if err := data.Decode(received); err != nil {
			t.Fatal(err)
		}
		// Expected data
		expected, err := generate(i)
		if err != nil {
			t.Fatalf("Error: %v\nduring generation of expected data for:\n%v",
				err, data)
		}

		var match bool
		if d, ok := data.(*echo); ok {
			// Echo comparison is slow when using reflect, since it only
			// contains bytes, compare them instead
			match = bytes.Equal(d.data, expected.(*echo).data)
		} else {
			match = reflect.DeepEqual(data, expected)
		}

		if !match {
			t.Fatalf("Received:\n\t%v\ndoes not equal expected:\n\t%v"+
				"\nfor iteration: %d\non channel: %s",
				data, expected, i, subs.Channel())
		}
	}
}

func TestAll(t *testing.T) {
	// Setup
	lc, err := lcm.New()
	if err != nil {
		t.Fatal(err)
	}
	defer lc.Destroy()

	// The 5 LCM test cases
	testcases := []struct {
		name       string // Name of testcase
		iterations int    // Number of iterations TC will do
		publish    string
		subscribe  string
		generate   generatorFunc
		typ        tester
		step       int
	}{
		{
			"echo test",
			10000,
			"TEST_ECHO",
			"TEST_ECHO_REPLY",
			echoGenerate,
			&echo{},
			0,
		},
		{
			"primitive_t test",
			1000,
			"test_lcmtest_primitives_t",
			"test_lcmtest_primitives_t_reply",
			primitiveTGenerate,
			&lcmtest.LcmtestPrimitivesT{},
			1,
		},
		{
			"primitives_list_t",
			100,
			"test_lcmtest_primitives_list_t",
			"test_lcmtest_primitives_list_t_reply",
			primitivesListTGenerate,
			&lcmtest.LcmtestPrimitivesListT{},
			1,
		},
		{
			"node_t",
			7,
			"test_lcmtest_node_t",
			"test_lcmtest_node_t_reply",
			nodeTGenerate,
			&lcmtest.LcmtestNodeT{},
			1,
		},
		{
			"multidim_array_t",
			5,
			"test_lcmtest_multidim_array_t",
			"test_lcmtest_multidim_array_t_reply",
			multidimArrayTGenerate,
			&lcmtest.LcmtestMultidimArrayT{},
			1,
		},
		{
			"cross_package_t",
			1000,
			"test_lcmtest2_cross_package_t",
			"test_lcmtest2_cross_package_t_reply",
			crossPackageTGenerate,
			&lcmtest2.Lcmtest2CrossPackageT{},
			1,
		},
	}

	// Run the table tests
	for _, tc := range testcases {
		t.Run(tc.name, test(lc, tc.iterations, tc.publish, tc.subscribe,
			tc.generate, tc.typ, tc.step))
	}
}
