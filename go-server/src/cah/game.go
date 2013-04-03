package cah

import (
	"sync"
)

type Game struct {
	sync.RWMutex
	players      map[int]*Player
	playerInc    int
	playerDeltas chan *PlayerDelta
	deckDeltas   chan *DeckDelta
}

func NewGame() *Game {
	//dealer has an id of 0
	return &Game{
		players:      make(map[int]*Player),
		playerInc:    1,
		playerDeltas: make(chan *PlayerDelta),
		deckDeltas:   make(chan *DeckDelta),
	}
}

func (game *Game) Play() {
	for {
		select {
		case pd := <-game.playerDeltas:
			multicast := false
			p := pd.fromPlayer
			switch pd.Message {
			case "my-id?":
				//let this client know their id so they can join
				p.toClientPlayerDeltas <- &PlayerDelta{Id: p.Id, Message: "your-id"}
			case "join":
				//let this client join the game
				//TODO: if they already existed, send them their cards

				// Give the new player a new hand of 7 cards
				cards := []string{}
				for i:= 0; i < 7; i++ {
					cards = append(cards,GetNewWhiteCard())
				}
					p.toClientDeckDeltas <- &DeckDelta{Player: p.Id, DeckTo: "hand", DeckFrom: "draw", Amount: 7, Cards: cards}
			case "leave":
				//remove from player list and reflect to others
				game.Lock()
				delete(game.players, pd.Id)
				game.Unlock()
				multicast = true
			default:
				//error, should have been something above
			}

			//If we should reflect this message to all players
			if multicast {
				for _, p := range game.players {
					p.toClientPlayerDeltas <- pd
				}
			}
		case dd := <-game.deckDeltas:
			game.RLock()
			for _, p := range game.players {
				p.toClientDeckDeltas <- dd
			}
			game.RUnlock()
		}
	}
}

func (game *Game) HandlePlayer(dec *NetDecoder, enc *NetEncoder) {
	game.Lock()
	me := NewPlayer(dec, enc, game.playerInc)
	game.players[game.playerInc] = me
	game.playerInc++
	game.Unlock()

	//Decode goroutine, sends messages to dealer
	go me.DecodeMessages(game.playerDeltas, game.deckDeltas)
	//Encode our messages
	me.EncodeMessages()
}
