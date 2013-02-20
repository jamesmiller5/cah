if [ "_$1" = "_" ]; then
	run
else
	set -e # Tells bash to stop executing if there's an error.
	$1
fi
