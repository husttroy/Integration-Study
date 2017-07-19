import os

count = 0
with open("gh-count.txt", 'w') as fw:
	for folder in os.listdir("clone-codes"):
		num = len(os.listdir("clone-codes/" + folder))
		count += num - 1	
		fw.write(str(num-1)) # minus 1 is to eliminate the count of so.txt file
		fw.write("\n")

print count	
