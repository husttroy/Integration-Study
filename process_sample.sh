#!/bin/bash

# change the following directory of sampled clones to the one in your own machine
DIR="/home/troy/research/Integration-Study/dataset/sample"

i=0
for f in $DIR/*
do
	echo "Processing $f"
	temp=(${f//// })
	fName=${temp[-1]}
	echo "File name $fName"
	cd $f
	for f1 in $f/so-*.txt
	do	
		temp1=(${f1//// })
		fName1=${temp1[-1]}
		echo "Adding class headers to $fName1"
		sed -i '1ipublic class foo{' $fName1
		echo "}" >> $fName1
		ss=(${fName1//-/ })
		num=${ss[1]}
		num=${num%.*} # strip off .txt			
		echo "Renaming $fName1 to $fName-$num.java"
		mv $fName1 "$fName-$num.java"
	done
	for f2 in $f/gh*.txt
	do
		temp2=(${f2//// })
		fName2=${temp2[-1]}
		#echo "Carving GitHub file $fName2 to carved-$fName2"
		#echo "File name $fName2"
		arr=(${fName2//-/ })
		#echo "${arr[2]}"
		#echo "${arr[3]%.*}"
		sed -n "${arr[3]}","${arr[4]%.*}"p $f2 > "carved-$fName2"
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
