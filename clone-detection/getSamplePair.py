import os, sys
import random

random_list = random.sample(range(1, 283787), 200)

count = 1
with open("clone-output.txt", 'r') as f:
	with open("clone-output-200.txt", 'w') as fw:
		for line in f:
			if count in random_list:
				fw.write(line)
			count += 1


