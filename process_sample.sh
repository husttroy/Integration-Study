#!/bin/bash

# change the following directory of sampled clones to the one in your own machine
DIR="/home/troy/research/Integration-Study/sample"

i=0
for f in $DIR/*
do
	echo "Processing $f"
	temp=(${f//// })
	fName=${temp[-1]}
	echo "File name $fName"
	cd $f
	echo "Renaming so.txt to $fName.txt"
	sed -i '1ipublic class foo{' so.txt
	echo "}" >> so.txt
	mv so.txt "$fName.java"
	for f2 in $f/gh*.txt
	do
		temp2=(${f2//// })
		fName2=${temp2[-1]}
		echo "Carving GitHub file $fName2 to carved-$fName2"
		#echo "File name $fName2"
		arr=(${fName2//-/ })
		#echo "${arr[1]}"
		#echo "${arr[2]%.*}"
		sed -n "${arr[1]}","${arr[2]%.*}"p $f2 > "carved-$fName2"
		sed -i '1ipublic class foo{' "carved-$fName2"
		echo "}" >> "carved-$fName2"
		#rename txt file to java file
		fName3=${fName2%.*}
		mv "carved-$fName2" "carved-$fName3.java"
	done
	cd ..
	mv $f "$DIR/so-$i"
	i=$(($i+1))
done
