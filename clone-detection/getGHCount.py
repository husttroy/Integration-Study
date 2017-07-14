import sys, os

with open("gh-count.txt", 'w') as fw:
	for folder in os.listdir("clone-codes"):
		fw.write(len( os.listdir(folder))
		fw.write("\n")

	
