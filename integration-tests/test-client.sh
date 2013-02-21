pushd .

cd ../android-client
javac -cp libs/gson-2.2.2.jar:src/ src/com/cah/CahClient.java

if [[ $? != 0 ]]; then
	echo "Build Error"
	exit 1
fi

#nc -l -p 41337 -w 5 &
go build -o server cmd
./server &
sleep 1

java -cp libs/gson-2.2.2.jar:src/ com.cah.CahClient

popd
