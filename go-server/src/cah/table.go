package cah

import (
	"crypto/rand"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"net"
	"sync"
)

type Table struct {
	id        string
	deltaChan chan * PlayerDelta
}

type TableDelta struct {
	Command string
	ID      string
}

var tables = map[string]*Table{}
var tableLock *sync.Mutex = new(sync.Mutex)
var handlers = map[string]func(dec *json.Decoder, enc *json.Encoder, msg *TableDelta){
	"new":  handleNewTable,
	"join": handleJoinTable,
}

func HandleNewClient(conn net.Conn) {
	defer conn.Close()
	defer func() {
		if r := recover(); r != nil {
			fmt.Println("Recovered in HandleNewClients:", r)
		}
	}()

	dec := json.NewDecoder(conn)
	enc := json.NewEncoder(conn)
	for {
		var msg TableDelta

		if err := dec.Decode(&msg); err != nil {
			panic("Decode Error")
		}

		handler, exists := handlers[msg.Command]

		if !exists {
			panic("Unknown Command: " + msg.Command)
		}

		handler(dec, enc, &msg)
	}
}

func handleNewTable(dec *json.Decoder, enc *json.Encoder, msg *TableDelta) {
	tab := NewTable()
	go tab.PlayGame()
	msg.ID = tab.id
	handleJoinTable(dec, enc, msg)
}

func handleJoinTable(dec *json.Decoder, enc *json.Encoder, msg *TableDelta) {
	if len(msg.ID) > 6 {
		panic("Message ID too long")
	}

	tab := LookUpTable(msg.ID)
	if tab == nil {
		enc.Encode(&TableDelta{Command: "nope", ID: msg.ID})
	} else {
		enc.Encode(&TableDelta{Command: "ok", ID: msg.ID})
	}

	for {
		var msg TableDelta
		if err := dec.Decode(&msg); err != nil {
			panic("Decode Error")
		}

		tab.deltaChan <- "Sup?"
	}
}

func NewTable() *Table {
	t := &Table{deltaChan: make(chan * PlayerDelta)}

	//try and add the table to the list using a random id avoiding collisions
	for {
		b := make([]byte, 4)
		rand.Read(b)
		d := make([]byte, base64.StdEncoding.EncodedLen(len(b)))
		base64.StdEncoding.Encode(d, b)
		id := string(d)[0:6]

		//add our new table to the map in a safe manner
		tableLock.Lock()
		if _, exists := tables[id]; exists != true {
			t.id = id
			tables[id] = t
			tableLock.Unlock()
			break
		}
		tableLock.Unlock()
	}

	return t
}

func LookUpTable(id string) *Table {
	tableLock.Lock()
	defer tableLock.Unlock()

	return tables[id]
}

func (t *Table) Delete() {
	tableLock.Lock()
	defer tableLock.Unlock()

	if _, exists := tables[t.id]; exists != true {
		panic("Tried to remove a non-existant table")
	}

	delete(tables, t.id)
}

func (t *Table) PlayGame() {
	for {
		fmt.Println("Play Game: ", <-t.deltaChan)
	}
}
