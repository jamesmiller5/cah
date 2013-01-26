package cah

import (
	"net"
	"fmt"
	"encoding/json"
)

func HandleNewClient(conn net.Conn) {
	defer conn.Close()

	type Animal struct {
			Name string
			Order string
	}
	var animals []Animal
	err := json.Unmarshal(conn, &animals)
	if err != nil {
		fmt.Println("error:", err)
	}
	fmt.Printf("%+v", animals)
}
