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
	czar *Player
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

func (game *Game) playRound() {

	//pick czar : Emit PlayerDelta{ "is-czar" }
	//pick black card : Emit DeckDelta{ }
	//wait for player input : foreach player-czar { get DeckDelta }
	//wait for czar input : czar { get DeckDelta }
	//send czar's choice : Emit DeckDelta { }
	//win?
	//deal up to x cards

	select {
	case pd := <-game.playerDeltas:
		multicast := false
		player := pd.fromPlayer
		switch pd.Message {
		case "my-id?":
			//let this client know their id so they can join
			player.outgoingPlayerDeltas <- &PlayerDelta{Id: player.Id, Message: "your-id"}
		case "join":
			//let this client join the game

			//TODO: if they already existed, send them their cards

			game.RLock()
			player.sendCards()
			game.RUnlock()

			//TODO: send them the player list

			multicast = true
		case "leave":
			player.Shutdown();
			multicast = true
		default:
		}

		//If we should reflect this message to all players
		if multicast {
			for _, p := range game.players {
				p.outgoingPlayerDeltas <- pd
			}
		}
	case dd := <-game.deckDeltas:
		game.RLock()
		for _, p := range game.players {
			p.outgoingDeckDeltas <- dd
		}
		game.RUnlock()
	}
}

func (game *Game) Play() {
	for {
		game.playRound();
	}
}

func (game *Game) HandlePlayer(dec *NetDecoder, enc *NetEncoder) {
	game.Lock()
	me := NewPlayer(dec, enc, game.playerInc)
	game.players[game.playerInc] = me
	game.playerInc++
	game.Unlock()

	//Decode goroutine, sends messages to dealer
	go me.DecodeDeltas(game.playerDeltas, game.deckDeltas)
	//Encode our messages
	me.EncodeDeltas()
}
