package cah

import (
	"crypto/rand"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"net"
	"sync"
	"time"
)

type Table struct {
	id   string
	game *Game
}

type TableDelta struct {
	Command string
	ID      string
}

type NetEncoder struct {
	*json.Encoder
	net net.Conn
}

type NetDecoder struct {
	*json.Decoder
	net net.Conn
}

var tables = map[string]*Table{}
var tableLock *sync.Mutex = new(sync.Mutex)

func HandleNewClient(conn net.Conn) {
	defer conn.Close()
	defer func() {
		if r := recover(); r != nil {
			fmt.Println("Recovered in HandleNewClients:", r)
		}
	}()

	dec := &NetDecoder{ json.Decoder: json.NewDecoder(conn), net: conn }
	enc := &NetEncoder{ json.Encoder: json.NewEncoder(conn), net: conn }
	for {
		var msg TableDelta

		dec.net.SetDeadline(time.Now().Add(30*time.Second))
		if err := dec.Decode(&msg); err != nil {
			panic("Decode Error")
		}
		dec.net.SetDeadline(time.Time{})

		handler, exists := handlers[msg.Command]

		if !exists {
			panic("Unknown Command: " + msg.Command)
		}

		if handler(dec, enc, &msg) {
			break
		}
	}
}


func handleJoinTable(dec *NetDecoder, enc *NetEncoder, msg *TableDelta) bool {
	if len(msg.ID) > 6 {
		panic("Message ID too long")
	}

	tab := LookUpTable(msg.ID)
	if tab == nil {
		enc.Encode(&TableDelta{Command: "nope", ID: msg.ID})
	} else {
		enc.Encode(&TableDelta{Command: "ok", ID: msg.ID})
		tab.game.HandlePlayer(dec, enc)
		return true
	}

	return false
}

//Map of handler names in messages to handler functions
var handlers = map[string]func(*NetDecoder, *NetEncoder, *TableDelta) bool{
	"join": handleJoinTable,
	"new":  func(dec *NetDecoder, enc *NetEncoder, msg *TableDelta) bool {
		tab := NewTable()
		go tab.PlayGame()
		msg.ID = tab.id
		return handleJoinTable(dec, enc, msg)
	},
}

func NewTable() *Table {
	t := &Table{game: NewGame()}

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

	fmt.Printf("New Table %+v\n", t)

	return t
}

func (t *Table) PlayGame() {
	t.game = NewGame()
	t.game.Play()
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
