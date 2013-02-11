package cah

import (
	"fmt"
	"sync"
)

type Game struct {
	sync.Mutex
	dealer    *Player
	players   map[int]*Player
	playerInc int
}

func NewGame() *Game {
	//dealer has an id of 0
	return &Game{
		dealer:    NewPlayer(nil, nil, 0),
		players:   make(map[int]*Player),
		playerInc: 1,
	}
}

func (game *Game) Play() {
	for {
		select {
		case pd := <-game.dealer.playerDeltas:
			fmt.Println("Dealer a player delta", pd)
			game.Lock()
			if pd.Message == "leave" {
				//remove from list
				delete(game.players, pd.Id)
			}
			for _, p := range game.players {
				p.playerDeltas <- pd
			}
			game.Unlock()
		case dd := <-game.dealer.deckDeltas:
			fmt.Println("Dealer a deck delta", dd)
			game.Lock()
			for _, p := range game.players {
				p.deckDeltas <- dd
			}
			game.Unlock()
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
	go me.DecodeMessages(game.dealer.playerDeltas, game.dealer.deckDeltas)
	//Encode our messages
	me.EncodeMessages()
}
