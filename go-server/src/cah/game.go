package cah

import (
	"encoding/json"
	"fmt"
	"sync"
)

type Game struct {
	sync.Mutex
	dealer *Player
	playerInc int
}

func NewGame() *Game {
	//dealer has an id of 0
	return &Game{dealer: NewPlayer(nil, nil, 0), playerInc: 1}
}

func (game *Game) Play() {
	for {
		select {
		case pd := <-game.dealer.playerDeltas:
			fmt.Println("Dealer a player delta", pd)
		case dd := <-game.dealer.deckDeltas:
			fmt.Println("Dealer a deck delta", dd)
		}
	}
}

func (game *Game) HandlePlayer(dec *json.Decoder, enc *json.Encoder) {
	game.Lock()
	me := NewPlayer(dec, enc, game.playerInc)
	game.playerInc++
	game.Unlock()

	//Decode goroutine, sends messages to dealer
	go me.DecodeMessages(game.dealer.playerDeltas, game.dealer.deckDeltas)
	//Encode our messages
	me.EncodeMessages()
}
