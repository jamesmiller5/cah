package cah

import (
	"encoding/json"
	"fmt"
	"net"
)

type TableDelta struct {
	Command string
	ID      string
}

var handlers = map[string]func(json.Decoder, json.Encoder){
	"new": func(dec json.Decoder, enc json.Encoder) {
		fmt.Println("I'm New!")
	},
	"old": func(dec json.Decoder, enc json.Encoder) {
		fmt.Println("I'm Old :(")
	},
}

func HandleNewClient(conn net.Conn) {
	defer conn.Close()

	dec := json.NewDecoder(conn)
	enc := json.NewEncoder(conn)
	for {
		var t TableDelta
		if err := dec.Decode(&t); err != nil {
			fmt.Println(err)
			return
		}

		if err := enc.Encode(&t); err != nil {
			fmt.Println(err)
		}
	}
}
