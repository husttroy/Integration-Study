#!/bin/bash

DIR=$1

if [[ "${DIR: -1}" = / ]]
then 
	DIR="$DIR*"
else 
	DIR="$DIR/*"
fi

#echo $DIR

i=0
for f in $DIR
do
	temp=(${f//// })
	fName=${temp[-1]}
	if [[ "$fName" = so-*.java ]] 
	then
		#echo "Stack Overflow file $f"
		SO=$f
	elif [[ "$fName" = carved-gh-*.java ]]
	then
		#echo "GitHub file $f"
		GH[$i]=$f
		i=$(($i+1))
	fi
done

#echo $SO
PORT=4567
for j in "${GH[@]}"
do
	echo "Differencing $SO and $j at port $PORT" 
	gumtree webdiff $SO $j --port $PORT&
	sleep 2
	echo "Open browser at localhost:$PORT"
	google-chrome http://localhost:$PORT&
        sleep 2
	echo "Killing the gumtree server at port $PORT"
	kill $(lsof -i:$PORT -a -c java -t)
	PORT=$(($PORT+1))
done

