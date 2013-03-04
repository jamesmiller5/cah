#!/bin/bash
PROJ_PATH=$( cd "$( dirname "$0" )/.." && pwd )

function start_server {
	GOPATH="$GOPATH:$PROJ_PATH/go-server"
	go build -o server cmd
	if [[ $? != 0 ]]; then
		echo "Build failed, halting test"
		exit 1
	fi

	echo "Server Built"
	(./server 2>&1 & echo $! > pid) | while read line; do echo "[SERVER] $line"; done &
	sleep 1
	echo "Server Running with PID $(< pid)"
}

function stop_server {
	kill $(< pid)
}

function clean_server {
	rm -f pid server
}
