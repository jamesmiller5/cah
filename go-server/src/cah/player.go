package cah

import (
	"fmt"
	"time"
)

type PlayerDelta struct {
	Id      int
	Message string
}

var playerDeltaMessages = map[string]bool{
	"connect": true,
	"leave":   true,
}

type DeckDelta struct {
	Player   int
	DeckTo   string
	DeckFrom string
	Amount   int
}

//table of available
var deckDeltaDecks = map[string]bool{
	"draw": true,
	"hand": true,
	"play": true,
}

type Player struct {
	Id           int
	quit         chan bool
	playerDeltas chan *PlayerDelta
	deckDeltas   chan *DeckDelta
	dec          *NetDecoder
	enc          *NetEncoder
}

const PLAYER_TIMEOUT = 30 * time.Second

func NewPlayer(dec *NetDecoder, enc *NetEncoder, id int) *Player {
	return &Player{
		Id:           id,
		dec:          dec,
		enc:          enc,
		playerDeltas: make(chan *PlayerDelta),
		deckDeltas:   make(chan *DeckDelta),
		//We want 2 slots in case both goroutines quit at the same time
		quit: make(chan bool, 2),
	}
}

func (p *Player) LeaveMessage() *PlayerDelta {
	return &PlayerDelta{Id: p.Id, Message: "leave"}
}

func (p *Player) DecodeMessages(playerDeltas chan *PlayerDelta, deckDeltas chan *DeckDelta) {
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
				Deck   DeckDelta
				Player PlayerDelta
				Keepalive	bool
			}

			//decode either as a DeckDelta or PlayerDelta
			p.dec.net.SetDeadline(time.Now().Add(PLAYER_TIMEOUT))
			if err := p.dec.Decode(&delta); err != nil {
				fmt.Println("Player.DecodeMessages decode error")
				//send a "leave" command
				goto exit
			}
			p.dec.net.SetDeadline(time.Time{})

			//not zero makes this a Deck
			if delta.Deck.Player != 0 {
				if delta.Deck.Player < 1 || delta.Deck.Amount < 1 || len(delta.Deck.DeckTo) == 0 || len(delta.Deck.DeckFrom) == 0 {
					fmt.Println("Deck.Player missing fields")
					goto exit
				}
				if true != deckDeltaDecks[delta.Deck.DeckTo] || true != deckDeltaDecks[delta.Deck.DeckFrom] {
					fmt.Println("Deck.DeckTo or .DeckFrom illegal")
					goto exit
				}
				deckDeltas <- &delta.Deck
			} else if delta.Player.Id != 0 {
				if delta.Player.Id < 1 || len(delta.Player.Message) > 10 || true != playerDeltaMessages[delta.Player.Message] {
					fmt.Println("delta.Player data error")
					goto exit
				}
				playerDeltas <- &delta.Player
			} else if delta.Keepalive == true {
				//keep alive, igonre
				fmt.Println("Keepalived")
			} else {
				fmt.Println("Error decoding")
				goto exit
			}
		}
	}
exit:
	playerDeltas <- p.LeaveMessage()
}

func (p *Player) EncodeMessages() {
	defer func() { p.quit <- true }()

	//message pump for this player
	for {
		select {
		//pd update from dealer
		case pd := <-p.playerDeltas:
			p.enc.Encode(pd)
		case dd := <-p.deckDeltas:
			p.enc.Encode(dd)
		case <-p.quit:
			fmt.Println("PlayerHandler got the message to quit")
			goto exit
		}
	}
exit:
}
