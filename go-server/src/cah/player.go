package cah

import (
	"io"
	"log"
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
	if pd.Id < 0 || len(pd.Message) > 10 || playerDeltaMessages[pd.Message] != true {
		log.Println("PlayerDelta is not clean")
		return false
	}

	return true
}

var playerDeltaMessages = map[string]bool{
	"join":   true,
	"leave":  true,
	"my-id?": true,
	"is-czar": true,
}

type DeckDelta struct {
	Player   int
	DeckTo   string
	DeckFrom string
	Cards    []*Card
	Delta
}

func (dd *DeckDelta) isClean() bool {
	if dd.Player < 1 || len(dd.DeckTo) == 0 || len(dd.DeckFrom) == 0 || len(dd.Cards) == 0 {
		log.Println("DeckDelta is not clean (missing fields)")
		return false
	}
	if true != deckDeltaDecks[dd.DeckTo] || true != deckDeltaDecks[dd.DeckFrom] {
		log.Println("DeckDelta .DeckTo or .DeckFrom illegal")
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
	outgoingPlayerDeltas chan *PlayerDelta
	outgoingDeckDeltas   chan *DeckDelta
	dec                  *NetDecoder
	enc                  *NetEncoder
	hand				 *Deck
}

const PLAYER_TIMEOUT = 10 * time.Minute

func NewPlayer(dec *NetDecoder, enc *NetEncoder, id int) *Player {
	return &Player{
		Id:                   id,
		dec:                  dec,
		enc:                  enc,
		outgoingPlayerDeltas: make(chan *PlayerDelta),
		outgoingDeckDeltas:   make(chan *DeckDelta),
		//We want 2 slots in case both goroutines quit at the same time
		quit: make(chan bool, 2),
		hand: 				  NewDeck("hand", nil),
	}
}

func (p *Player) Shutdown() {
	p.quit <- true;
}

func (p *Player) Leave() *PlayerDelta {
	return &PlayerDelta{ Id: p.Id, Message: "leave", Delta:Delta{ fromPlayer: p }}
}

//Send decoded messages to arguments
func (p *Player) DecodeDeltas(incomingPlayerDeltas chan *PlayerDelta, incomingDeckDeltas chan *DeckDelta) {
	defer func() { p.quit <- true }()

	for {
		select {
		case <-p.quit:
			//Should quit
			goto exit
		default:
			//Decode a delta
			var delta struct {
				Deck      *DeckDelta
				Player    *PlayerDelta
				Keepalive bool
			}

			//decode either as a DeckDelta or PlayerDelta
			p.dec.SetDeadline(time.Now().Add(PLAYER_TIMEOUT))
			if err := p.dec.Decode(&delta); err != nil {
				if err == io.EOF {
					goto exit
				} else if err, ok := err.(net.Error); ok && err.Timeout() {
					goto exit
				}

				log.Println("Unknown Player.DecodeCommands decode error: " + err.Error())
				goto exit
			}
			p.dec.SetDeadline(time.Time{})

			if delta.Deck != nil {
				if delta.Deck.isClean() != true {
					goto exit
				}
				delta.Deck.fromPlayer = p
				incomingDeckDeltas <- delta.Deck
			} else if delta.Player != nil {
				if delta.Player.isClean() != true {
					goto exit
				}
				delta.Player.fromPlayer = p
				incomingPlayerDeltas <- delta.Player
			} else if delta.Keepalive == true {
				//keep alive, reset counter and ignore
			} else {
				log.Println("Player.DecodeCommands: Unexpected delta recieved")
				goto exit
			}
		}
	}
exit:
	incomingPlayerDeltas <- p.Leave()
}

func (p *Player) sendCards() {
	// Give the new player a new hand of 7 cards
	cards := []*Card{}
	for i := 0; i < 7; i++ {
	    cards = append(cards, nil)
	}
	p.outgoingDeckDeltas <- &DeckDelta{Player: p.Id, DeckTo: "hand", DeckFrom: "draw", Cards: cards}
}

func (p *Player) EncodeDeltas() {
	defer func() { p.quit <- true }()

	for {
		select {
		//pd update from dealer
		case pd := <-p.outgoingPlayerDeltas:
			p.enc.Encode(pd)
		case dd := <-p.outgoingDeckDeltas:
			p.enc.Encode(dd)
		case <-p.quit:
			goto exit
		}
	}
exit:
}
