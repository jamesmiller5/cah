package cah

import (
	"log"
	"sync"
	"time"
)

const MIN_PLAYERS = 2

type Game struct {
	sync.RWMutex
	players      map[int]*Player
	playerInc    int
	playerDeltas chan *PlayerDelta
	deckDeltas   chan *DeckDelta
	czar         *Player
	white_draw, black_draw *Deck
	white_discard *Deck
}

func NewGame() *Game {
	//dealer has an id of 0
	return &Game{
		players:      make(map[int]*Player),
		playerInc:    1,
		playerDeltas: make(chan *PlayerDelta),
		deckDeltas:   make(chan *DeckDelta),
		white_draw:   NewDeck("white-draw", wcList),
		black_draw:   NewDeck("black-draw", bcList),
		white_discard: NewDeck("white-discard", nil),
	}
}

func (game *Game) playRound(pd_filtered <-chan *PlayerDelta) {
	//deal up to x cards
	//pick czar : Emit PlayerDelta{ "is-czar" }
	//pick black card : Emit DeckDelta{ }
	//wait for player input : foreach player-czar { get DeckDelta }
	//wait for czar input : czar { get DeckDelta }
	//send czar's choice : Emit DeckDelta { }
	//win?

	//wait for MIN_PLAYERS
	start:
	for {
		l := game.playerCount()
		if l < MIN_PLAYERS {
			pd, ok := <-pd_filtered
			println("released", pd.Message, ok)
		} else {
			break
		}
	}

	game.RLock()

	println("WE HAVE ENOUGH", len(game.players))

	//ugly hack to make sure czar is not b4 join on client
	time.Sleep(1)
	//next czar
	czar_id := 0
	if game.czar != nil {
		czar_id = game.czar.Id
	}
	next_id := int( ^uint(0) >> 1 )
	max_id := next_id
	min_id := next_id

	for key := range game.players {
		//find the key closest to the czars but at least 1 greater
		if key < next_id && key > czar_id {
			next_id = key
		}

		if key < min_id {
			min_id = key
		}
	}

	if next_id == max_id {
		next_id = min_id
	}

	game.playerDeltas <- game.players[next_id].czarify()
	game.RUnlock()

	for {
		pd, ok := <-pd_filtered
		if !ok {
			return
		}
		if pd.Message == "leave" {
			if game.playerCount() < MIN_PLAYERS {
				goto start
			}
		} else {
			goto black
		}
	}

	black:
	//pick bla
}

func (game *Game) playerCount() (l int) {
	game.RLock()
	l = len(game.players)
	game.RUnlock();

	return
}

func (game *Game) Play() {
	pd_filtered := make(chan *PlayerDelta)

	go func() {
		for {
			game.playRound(pd_filtered)
		}
	}()

	//filter PlayerDeltas
	for pd := range game.playerDeltas {
		multicast, me_to := false, false
		to_filter := false
		player := pd.fromPlayer
		switch pd.Message {
		case "my-id?":
			//assign an id to this player and add them
			//to the game list
			id := player.Id
			if id == 0 {
				game.Lock()
				id = game.playerInc
				game.playerInc++
				game.Unlock()
			}
			player.sendYourId(id)
			log.Println("Assigned a new id", id)
		case "join":
			multicast = true
			to_filter = true

			//add to game board
			log.Println("Join start")
			game.Lock()
			game.players[player.Id] = player
			game.Unlock()

			player.sendHand(game.white_draw)
			player.sendPlayers(game.players)
		case "leave":
			to_filter = true
			multicast = true

			game.Lock()
			delete(game.players, player.Id)
			game.Unlock()
			player.Shutdown()
		case "is-czar":
			multicast = true
			me_to = true
		default:
			to_filter = true
		}

		//If we should reflect this message to all players
		if multicast {
			game.RLock()
			for _, p := range game.players {
				if me_to {
					println("METO: %v",p)
				}
				if !me_to && p == player {
					continue
				}
				p.outgoingPlayerDeltas <- pd
			}
			game.RUnlock()
		}

		if to_filter {
			pd_filtered <- pd
		}
	}
}

func (game *Game) HandlePlayer(dec *NetDecoder, enc *NetEncoder) {
	me := NewPlayer(dec, enc)
	//Decode goroutine, sends messages to dealer
	go me.DecodeDeltas(game.playerDeltas, game.deckDeltas)
	//Encode our messages
	me.EncodeDeltas()
}
