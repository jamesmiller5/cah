package cah

import (
	"encoding/json"
	"fmt"
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
var deckDeltaDecks = map[string]bool {
	"draw": true,
	"hand": true,
	"play": true,
}

type Player struct {
	Id           int
	quit         chan bool
	playerDeltas chan *PlayerDelta
	deckDeltas   chan *DeckDelta
	dec          *json.Decoder
	enc          *json.Encoder
}

func NewPlayer(dec *json.Decoder, enc *json.Encoder, id int) *Player {
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
			}

			//decode either as a DeckDelta or PlayerDelta
			if err := p.dec.Decode(&delta); err != nil {
				fmt.Println("Player.DecodeMessages decode error")
				//send a "leave" command
				playerDeltas <- p.LeaveMessage()
				goto exit
			}

			//not zero makes this a Deck
			if delta.Deck.Player != 0 {
				if delta.Deck.Player < 1 || delta.Deck.Amount < 1 || len(delta.Deck.DeckTo) == 0 || len(delta.Deck.DeckFrom) == 0 {
					fmt.Println("Deck.Player missing fields")
					goto exit
				}
				deckDeltas <- &delta.Deck
			} else if delta.Player.Id != 0 {
				if delta.Player.Id < 1 || len(delta.Player.Message) == 0 || len(delta.Player.Message) > 10 {
					fmt.Println("delta.Player data error")
					goto exit
				}
				playerDeltas <- &delta.Player
			} else {
				fmt.Println("Error decoding")
				goto exit
			}
		}
	}
exit:
}

func (p *Player) EncodeMessages() {
	defer func() { p.quit <- true }()

	//message pump for this player
	for {
		select {
		//pd update from dealer
		case pd := <-p.playerDeltas:
			fmt.Println("client got player delta from dealer", pd)
			//encode and send
			p.enc.Encode(pd)
		case dd := <-p.deckDeltas:
			fmt.Println("client got deck delta from dealer", dd)
			//encode and send
			p.enc.Encode(dd)
		case <-p.quit:
			fmt.Println("PlayerHandler got the message to quit")
			goto exit
		}
	}
exit:
}
