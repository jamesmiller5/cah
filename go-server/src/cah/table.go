package cah

import (
	"crypto/rand"
	"encoding/base64"
	"encoding/json"
	"io"
	"log"
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
	Id      string
}

const TABLE_TIMEOUT = 1 * time.Minute

type NetEncoder struct {
	*json.Encoder
	net.Conn
}

type NetDecoder struct {
	*json.Decoder
	net.Conn
}

var tables = map[string]*Table{}
var tableLock *sync.RWMutex = new(sync.RWMutex)

func HandleNewClient(conn net.Conn) {
	defer conn.Close()
	defer func() {
		if r := recover(); r != nil {
			log.Println("Recovered in HandleNewClients:", r)
		}
	}()

	dec := &NetDecoder{json.NewDecoder(conn), conn}
	enc := &NetEncoder{json.NewEncoder(conn), conn}
	for {
		var msg TableDelta

		dec.SetDeadline(time.Now().Add(TABLE_TIMEOUT))
		if err := dec.Decode(&msg); err != nil && err != io.EOF {
			panic("Decode Error")
		} else if err == io.EOF {
			log.Println("Client disconnected before joining a table.")
			break
		}
		dec.SetDeadline(time.Time{})

		handler, exists := handlers[msg.Command]
		if !exists {
			panic("Unknown Command: " + msg.Command)
		}
		if handler(dec, enc, &msg) {
			break
		}
	}
}

//Map of handler names in messages to handler functions
var handlers map[string]func(*NetDecoder, *NetEncoder, *TableDelta) bool

func init() {
	handlers = map[string]func(*NetDecoder, *NetEncoder, *TableDelta) bool{
		"join": func(dec *NetDecoder, enc *NetEncoder, msg *TableDelta) bool {
			if len(msg.Id) > 6 {
				panic("Message Id too long")
			}

			tab, exists := LookUpTable(msg.Id)
			if exists == false || tab == nil {
				enc.Encode(&TableDelta{Command: "nope", Id: msg.Id})
			} else {
				enc.Encode(&TableDelta{Command: "ok", Id: msg.Id})
				tab.game.HandlePlayer(dec, enc)
				return true
			}

			return false
		},
		"new": func(dec *NetDecoder, enc *NetEncoder, msg *TableDelta) bool {
			log.Println("New table created")

			tab := NewTable(nil)
			go tab.PlayGame()
			msg.Id = tab.id
			return handlers["join"](dec, enc, msg)
		},
	}
}

func NewTable(id *string) *Table {
	t := &Table{game: NewGame()}

	//If an id is provided, try and use that
	if id != nil {
		t.id = *id
		if !t.AddToListing() {
			panic("Couldn't add table with id:" + *id)
		}
	} else {
		//try and add the table to the list using a random id avoiding collisions
		for {
			b := make([]byte, 4)
			rand.Read(b)
			d := make([]byte, base64.StdEncoding.EncodedLen(len(b)))
			base64.StdEncoding.Encode(d, b)
			t.id = string(d)[0:6]

			if t.AddToListing() {
				break
			}

			log.Println("Had a collision of table ID's, probably not supposed to happen")
		}
	}

	return t
}

func (t *Table) PlayGame() {
	t.game = NewGame()
	t.game.Play()
}

func LookUpTable(id string) (tab *Table, exists bool) {
	tableLock.RLock()
	defer tableLock.RUnlock()

	tab, exists = tables[id]
	return
}

func (t *Table) AddToListing() bool {
	tableLock.Lock()
	defer tableLock.Unlock()

	if _, exists := tables[t.id]; exists != true {
		tables[t.id] = t
		return true
	}

	return false
}

func (t *Table) Delete() {
	tableLock.Lock()
	defer tableLock.Unlock()

	if _, exists := tables[t.id]; exists != true {
		panic("Tried to remove a non-existant table")
	}

	delete(tables, t.id)
}
