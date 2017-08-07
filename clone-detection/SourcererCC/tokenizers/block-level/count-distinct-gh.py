import os

count_gh = 0
count_so = 0
with open("distinct_token_hashes/distinct-files.tokens", 'r') as f:
	for line in f:
		if line.startswith('1'):
			count_gh += 1
		if line.startswith('2'):
			count_so += 1

print count_gh
print count_so
	
