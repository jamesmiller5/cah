package cah

import (
	"fmt"
	"net"
	"time"
)

type Delta struct {
	fromPlayer *Player `json:"-"` //Don't let json parse this field
}

type PlayerDelta struct {
	Id      int
	Message string
	Delta
}

func (pd *PlayerDelta) isClean() bool {
	if pd.Id < 0 ||
		len(pd.Message) > 10 ||
		playerDeltaMessages[pd.Message] != true {
		fmt.Println("PlayerDelta data error")
		return false
	}

	return true
}

var playerDeltaMessages = map[string]bool{
	"join":   true,
	"leave":  true,
	"my-id?": true,
}

type DeckDelta struct {
	Player   int
	DeckTo   string
	DeckFrom string
	Amount   int
	Delta
}

func (dd *DeckDelta) isClean() bool {
	if dd.Player < 1 || dd.Amount < 1 || len(dd.DeckTo) == 0 || len(dd.DeckFrom) == 0 {
		fmt.Println("Deck.Player missing fields")
		return false
	}
	if true != deckDeltaDecks[dd.DeckTo] || true != deckDeltaDecks[dd.DeckFrom] {
		fmt.Println("Deck.DeckTo or .DeckFrom illegal")
		return false
	}

	return true
}

//table of available
var deckDeltaDecks = map[string]bool{
	"draw": true,
	"hand": true,
	"play": true,
}

type Player struct {
	Id                   int
	quit                 chan bool
	toClientPlayerDeltas chan *PlayerDelta
	toClientDeckDeltas   chan *DeckDelta
	dec                  *NetDecoder
	enc                  *NetEncoder
}

const PLAYER_TIMEOUT = 30 * time.Second

func NewPlayer(dec *NetDecoder, enc *NetEncoder, id int) *Player {
	return &Player{
		Id:                   id,
		dec:                  dec,
		enc:                  enc,
		toClientPlayerDeltas: make(chan *PlayerDelta),
		toClientDeckDeltas:   make(chan *DeckDelta),
		//We want 2 slots in case both goroutines quit at the same time
		quit: make(chan bool, 2),
	}
}

func (p *Player) LeaveMessage() *PlayerDelta {
	return &PlayerDelta{Id: p.Id, Message: "leave"}
}

//Send decoded messages to arguments
func (p *Player) DecodeMessages(toServerPlayerDeltas chan *PlayerDelta, toServerDeckDeltas chan *DeckDelta) {
	defer func() { p.quit <- true }()

	for {
		select {
		case <-p.quit:
			//Should quit
			fmt.Println("Player.DecodeMessages told to quit")
			goto exit
		default:
			//Decode a delta
			var delta struct {
				Deck      *DeckDelta
				Player    *PlayerDelta
				Keepalive bool
			}

			//decode either as a DeckDelta or PlayerDelta
			p.dec.net.SetDeadline(time.Now().Add(PLAYER_TIMEOUT))
			if err := p.dec.Decode(&delta); err != nil {
				if err, ok := err.(net.Error); ok && err.Timeout() {
					fmt.Println("Player timout")
					goto exit
				}

				fmt.Println("Unknown Player.DecodeMessages decode error")
				goto exit
			}
			p.dec.net.SetDeadline(time.Time{})

			fmt.Printf("Dec:%v\n", delta)

			if delta.Deck != nil {
				if delta.Deck.isClean() != true {
					goto exit
				}
				delta.Deck.fromPlayer = p
				toServerDeckDeltas <- delta.Deck
			} else if delta.Player != nil {
				if delta.Player.isClean() != true {
					goto exit
				}
				delta.Player.fromPlayer = p
				toServerPlayerDeltas <- delta.Player
			} else if delta.Keepalive == true {
				//keep alive, igonre
				fmt.Println("Keepalived")
			} else {
				fmt.Println("Player.DecodeMessages: Unexpected message recieved")
				goto exit
			}
		}
	}
exit:
	toServerPlayerDeltas <- p.LeaveMessage()
}

func (p *Player) EncodeMessages() {
	defer func() { p.quit <- true }()

	//message pump for this player
	for {
		select {
		//pd update from dealer
		case pd := <-p.toClientPlayerDeltas:
			p.enc.Encode(pd)
		case dd := <-p.toClientDeckDeltas:
			p.enc.Encode(dd)
		case <-p.quit:
			fmt.Println("PlayerHandler got the message to quit")
			goto exit
		}
	}
exit:
}
