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
	"join":    true,
	"leave":   true,
	"my-id?":  true,
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
	"white-draw": true,
	"white-discard": true,
	"black-draw": true,
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
	hand                 *Deck
}

const PLAYER_TIMEOUT = 10 * time.Minute

func NewPlayer(dec *NetDecoder, enc *NetEncoder) *Player {
	return &Player{
		dec:                  dec,
		enc:                  enc,
		outgoingPlayerDeltas: make(chan *PlayerDelta),
		outgoingDeckDeltas:   make(chan *DeckDelta),
		//We want 2 slots in case both goroutines quit at the same time
		quit: make(chan bool, 2),
		hand: NewDeck("hand", nil),
	}
}

func (p *Player) Shutdown() {
	p.quit <- true
}

func (p *Player) Leave() *PlayerDelta {
	return &PlayerDelta{Id: p.Id, Message: "leave", Delta: Delta{fromPlayer: p}}
}

//Send decoded messages to arguments
func (p *Player) DecodeDeltas(incomingPlayerDeltas chan<- *PlayerDelta, incomingDeckDeltas chan<- *DeckDelta) {
	defer func() { p.quit <- true }()

	for {
		select {
		case <-p.quit:
			//Should quit silently
			goto silent_exit
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
silent_exit:
}

func (p *Player) sendHand(white_card *Deck) {
	log.Println("Sending player hand for", p.Id)
	// Give the new player a new hand of 7 cards
	cards := []*Card{}

	have := len(p.hand.cards)
	println("----HV:",have)
	if have < 7 {
		//random white cards
		randomList := white_card.randomCards()[0:7-have]


		delta, err := TransferSome(white_card, p.hand, randomList)

		log.Printf("----DELAT:%v\n",delta)
		if err {
			println("Oh noes! Error")
		}
	}

	for key, _ := range p.hand.cards {
		cards = append(cards, key)
	}

	log.Printf("----CARDS:%v\n",cards)

	p.outgoingDeckDeltas <- &DeckDelta{Player: p.Id, DeckTo: "hand", DeckFrom: "white-draw", Cards: cards}
}

func (p *Player) sendPlayers(players map[int]*Player) {
	log.Println("Sending players")
	for id, _ := range players {
		if id != p.Id {
			p.outgoingPlayerDeltas <- &PlayerDelta{Id: id, Message: "join"}
		}
	}
}

func (p *Player) sendYourId(id int) {
	p.Id = id
	p.outgoingPlayerDeltas <- &PlayerDelta{Id: id, Message: "your-id"}
}

func (p *Player) czarify() *PlayerDelta {
	return &PlayerDelta{Id: p.Id, Message: "is-czar"}
}

func (p *Player) EncodeDeltas() {
	defer func() { p.quit <- true }()

	for {
		select {
		//pd update from dealer
		case pd := <-p.outgoingPlayerDeltas:
			p.enc.Encode(pd)
		case dd := <-p.outgoingDeckDeltas:
			//hack
			rdd := struct {
				Player int
				DeckTo string
				DeckFrom string
				Cards 	[]string
			}{
				dd.Player,
				dd.DeckTo,
				dd.DeckFrom,
				string_it(dd.Cards),
			}
			p.enc.Encode(rdd)
		case <-p.quit:
			goto exit
		}
	}
exit:
}
