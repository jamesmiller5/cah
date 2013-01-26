package cah

import (
	"log"
	"net"
	"os"
)

func Serve() {
	tcpAddr, err := net.ResolveTCPAddr("tcp", "0.0.0.0:41337")
	checkError(err, true)

	listener, err := net.ListenTCP("tcp", tcpAddr)
	checkError(err, true)

	for {
		conn, err := listener.Accept()
		if err != nil {
			checkError(err, false)
			continue
		}
		go HandleNewClient(conn)
	}
}

func CheckError(err error, fatal bool) {
	if err != nil {
		if fatal {
			log.Println("Fatal error ", err.Error())
			os.Exit(1)
		} else {
			log.Println("Error ", err.Error())
		}
	}
}
