#!/bin/bash
PROJ_PATH=$( cd "$( dirname "$0" )/.." && pwd )

source functions.bash

function run {
	stop_server
	clean_server
	start_server

	client_only

	stop_server
}

function client_only {
	pushd .

	cd $PROJ_PATH/android-client/
	export CLASSPATH="`pwd`/libs/gson-2.2.2.jar:`pwd`/src/"

	javac -g src/com/cah/CahClient.java
	if [[ $? != 0 ]]; then
		echo "Build Error"
		exit 1
	fi

	java -ea:com.cah... com.cah.CahClient

	popd
}

if [ -z "$1" ]; then
	run
else
	set -e #Tells bash to stop executing if there's an error.
	$1
fi
