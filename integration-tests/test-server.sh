#!/bin/bash
PROJ_PATH=$( cd "$( dirname "$0" )/.." && pwd )

function start {
	GOPATH="$GOPATH:$PROJ_PATH/go-server"
	go build -o server cmd
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
	setup_test
	#for infile in client-*.json; do
		#cat $infile > server-input
		#echo "Diffing"
		#diff server-output server-${infile:7}
		#if [[ $? != 0 ]]; then 
		#	echo "=== Diff of $infile failed! ==="
		#	stop
		#	exit 1
		#fi
	#done
	cat client-table-new.json > server-input
	diff server-output server-table-new.json
	echo '{"Keepalive":true}' > server-input
}

function setup_test {
	rm -f server-{input,output}
	mkfifo server-{input,output}
	nc localhost 41337 -q 10 < server-input > server-output &
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
