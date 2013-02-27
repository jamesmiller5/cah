#!/bin/bash
PROJ_PATH=$( cd "$( dirname "$0" )/.." && pwd )

source functions.bash

function run {
	stop_server
	clean_server
	start_server
	pushd .

	cd $PROJ_PATH/android-client/

	javac -cp libs/gson-2.2.2.jar:src/ src/com/cah/CahClient.java
	if [[ $? != 0 ]]; then
		echo "Build Error"
		exit 1
	fi
	java -ea:com.cah... -cp libs/gson-2.2.2.jar:src/ com.cah.CahClient

	popd
	stop_server
}

if [ -z "$1" ]; then
	run
else
	set -e # Tells bash to stop executing if there's an error.
	$1
fi
