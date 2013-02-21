#!/bin/bash
PROJ_PATH=$( cd "$( dirname "$0" )/.." && pwd )

function start {
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

function stop {
	kill $(< pid)
}

#run tests
function run_tests {
	#We are testing the server, therefore read *client* input into the test buffer
	for testset in "test*-client-player1.json"; do
		echo "Testing ${testset:0:5}"

		rm -f server-{input1,input2,input3,output}
		mkfifo server-{input1,input2,input3,output}

		#read input into a buffer, skipping comments
		cat $testset | grep -v "^//.*$" > server-input &

		#connect to the server and read buffer, capturing server response
		nc localhost 41337 --interval=2 --wait=5 < server-input1 > server-output &

		#TODO: table should be random
		head -n 1 server-output

		echo "Diffing..."
		diff server-output server-${testset:7}
		if [[ $? != 0 ]]; then 
			echo "=== Diff of $testset failed! ==="
			#run stop function
			stop
			exit 1
		fi
	done
}

function clean {
	rm -f server-{input,output} server pid
}

function run {
	start
	run_tests
	stop
	clean
}

if [ "_$1" = "_" ]; then
	run
else
	set -e # Tells bash to stop executing if there's an error.
	$1
fi
