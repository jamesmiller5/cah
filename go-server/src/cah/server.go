package cah

import (
	"log"
	"net"
	"os"
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

	for {
		conn, err := listener.Accept()
		if err != nil {
			log.Println("Fatal error ", err.Error())
			os.Exit(1)
			continue
		}
		go HandleNewClient(conn)
	}
}
