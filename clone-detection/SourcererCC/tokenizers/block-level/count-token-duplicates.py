import os

gh_hash_set = set()
so_hash_set = set()
for file in os.listdir("distinct_file_blocks_tokens"):
	# this is a GH token file
	file = os.path.join("distinct_file_blocks_tokens", file)
	print 'Searching on ', file
	with open(file, 'r') as f:
		for line in f:
			token_hash = (line.split('@#@')[0]).split(',')[-1]
			if token_hash not in gh_hash_set:
				gh_hash_set.add(token_hash)

for file in os.listdir("so_blocks_tokens"):
	# this is a SO token file
	file = os.path.join("so_blocks_tokens", file)
	print 'Searching on ', file
	with open(file, 'r') as f:
		for line in f:
			token_hash = (line.split('@#@')[0]).split(',')[-1]
			if token_hash not in so_hash_set:
				so_hash_set.add(token_hash)

print len(gh_hash_set)
print len(so_hash_set)
common_set = set()
for item in gh_hash_set:
	if item in so_hash_set:
		common_set.add(item)

print len(common_set)


count_common_gh = 0
count_common_so = 0
for file in os.listdir("distinct_file_blocks_tokens"):
	file = os.path.join("distinct_file_blocks_tokens", file)
	print 'Searching on ', file
	with open(file, 'r') as f:
		for line in f:
			token_hash = (line.split('@#@')[0]).split(',')[-1]
			if token_hash in common_set:
				count_common_gh += 1

for file in os.listdir("so_blocks_tokens"):
	file = os.path.join("so_blocks_tokens", file)
	print 'Searching on ', file
	with open(file, 'r') as f:
		for line in f:
			token_hash = (line.split('@#@')[0]).split(',')[-1]
			if token_hash in common_set:
				count_common_so += 1

print count_common_gh
print count_common_so
