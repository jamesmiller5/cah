package cah

import (
	"fmt"
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
			fmt.Println("Dealer a player delta", pd)
			multicast := false
			p := pd.fromPlayer
			switch pd.Message {
			case "my-id?":
				//let this client know their id so they can join
				p.toClientPlayerDeltas <- &PlayerDelta{Id: p.Id, Message: "your-id"}
			case "join":
				//let this client join the game
				//if they already existed, send them their cards
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
			fmt.Println("Dealer a deck delta", dd)
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
