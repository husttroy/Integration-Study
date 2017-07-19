import os, sys
import random
import shutil

'''
random_list = random.sample(range(1, 284487), 200)

count = 1
with open("clone-output.txt", 'r') as f:
	with open("clone-output-200.txt", 'w') as fw:
		for line in f:
			if count in random_list:
				fw.write(line)
			count += 1
'''

random_list = random.sample(os.listdir("clone-codes"), 200)

for item in random_list:
	shutil.copytree("clone-codes/"+item, "clone-codes-200/"+item)

