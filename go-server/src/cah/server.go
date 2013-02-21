package cah

import (
	"log"
	"net"
)

func Serve() {
	tcpAddr, err := net.ResolveTCPAddr("tcp", "0.0.0.0:41337")
	if err != nil {
		log.Println("Error ", err.Error())
	}

	listener, err := net.ListenTCP("tcp", tcpAddr)
	if err != nil {
		log.Println("Error ", err.Error())
	}
	log.Println("Server started with binding", tcpAddr)

	for {
		conn, err := listener.AcceptTCP()
		if err != nil {
			log.Println("Fatal error ", err.Error())
			continue
		}
		conn.SetKeepAlive(true)
		go HandleNewClient(conn)
	}
}
