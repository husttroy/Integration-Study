#!/bin/bash

# change the following directory of sampled clones to the one in your own machine
DIR="/home/troy/research/Integration-Study/dataset/new-gh-with-so-links"

i=0
for f in $DIR/*
do
	echo "Processing $f"
	temp=(${f//// })
	fName=${temp[-1]}
	echo "File name $fName"
	cd $f
	
	# process the SO snippet
	echo "Adding class headers to so.txt"
	sed -i '1ipublic class foo{' so.txt
	printf "\n}" >> so.txt			
	echo "Renaming so.txt to $fName.java"
	mv so.txt "$fName.java"
	
	# process GitHub clones
	for f2 in $f/gh*.txt
	do
		temp2=(${f2//// })
		fName2=${temp2[-1]}
		#echo "Carving GitHub file $fName2 to carved-$fName2"
		#echo "File name $fName2"
		arr=(${fName2//-/ })
		#echo "${arr[2]}"
		#echo "${arr[3]%.*}"
		### no need to carve in the new dataset, since the clones have already been carved
		# sed -n "${arr[3]}","${arr[4]%.*}"p $f2 > "carved-$fName2"
		cp "$fName2" "carved-$fName2"
		sed -i '1ipublic class foo{' "carved-$fName2"
		printf "\n}" >> "carved-$fName2"
		#rename txt file to java file
		fName3=${fName2%.*}
		mv "carved-$fName2" "carved-$fName3.java"
	done
	cd ..
	mv $f "$DIR/so-$i"
	i=$(($i+1))
done
