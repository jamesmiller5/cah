package cah

import (
	"encoding/json"
	"fmt"
)

type PlayerDelta struct {
	num     int
	message string
}

type Player struct {
	playerDeltas chan *PlayerDelta
}

func NewPlayer() *Player {
	return &Player{playerDeltas: make(chan *PlayerDelta)}
}

type Game struct {
	dealer *Player
}

func NewGame() *Game {
	return &Game{dealer: NewPlayer()}
}

func (game *Game) Play() {
	for {
		select {
		case pd := <-game.dealer.playerDeltas:
			fmt.Println("got a player delta", pd)
		}
	}
}

func (game *Game) HandlePlayer(dec *json.Decoder, enc *json.Encoder) {
	//me := NewPlayer()
	incoming := make(chan *PlayerDelta)
	//We want 2 slots in case both goroutines quit at the same time
	quit := make(chan bool, 2)
	defer func() { quit <- true }()

	//Decode goroutine
	go func() {
		defer func() { quit <- true }()

		for {
			select {
				case <-quit:
					//Should quit
					fmt.Println("Decode got the message to quit")
					goto exit
				default:
					//dummy test message
					var m struct {
						Message string
					}

					if err := dec.Decode(&m); err != nil {
						fmt.Println("HandlePlayer decode error")
						goto exit
					}

					fmt.Println("Got a message", m)
			}
		}
		exit:
	}()

	//
	for {
		select {
		//pd update from dealer
		case pd := <-game.dealer.playerDeltas:
			fmt.Println("client got pd update from dealer", pd)
			//encode and send

			//pd update from our client
		case pd := <-incoming:
			game.dealer.playerDeltas <- pd

		case <-quit:
			fmt.Println("PlayerHandler got the message to quit")
			goto exit
		}
	}
exit:
}
