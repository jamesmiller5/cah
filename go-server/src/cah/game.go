package cah

type PlayerDelta struct {
	num     int
	message string
}

type Player struct {
}

type Game struct {
	dealer Player
}

func NewGame() *Game {
	return new(Game)
}

func (*Game) play() {
	for {
	}
}
