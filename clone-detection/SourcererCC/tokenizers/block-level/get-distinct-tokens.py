# Creates a group of files whose token-hash is representative of a group


import sys
import os

PATH_distinct_token_hashes = 'distinct_token_hashes'

if os.path.exists(PATH_distinct_token_hashes):
	print 'ERROR - Folder ['+PATH_distinct_token_hashes+'] already exists!'
	sys.exit()
else:
	os.makedirs(PATH_distinct_token_hashes)

gh_hashes = set()
so_hashes = set()

with open(os.path.join(PATH_distinct_token_hashes,'distinct-files.tokens'),'w') as result_tokens:
	for file in os.listdir("distinct_file_blocks_tokens"):
		file = os.path.join("distinct_file_blocks_tokens",file)
		print 'Searching on ',file
		with open(file,'r') as file_book:
			for line in file_book:
				token_hash = (line.split('@#@')[0]).split(',')[-1]
				if token_hash not in gh_hashes:
					gh_hashes.add(token_hash)
					result_tokens.write(line)

	for file in os.listdir("so_blocks_tokens"):
		file = os.path.join("so_blocks_tokens", file)
		print 'Searching on ', file
		with open(file, 'r') as file_book:
			for line in file_book:
				token_hash = (line.split('@#@')[0]).split(',')[-1]
				if token_hash not in so_hashes:
					so_hashes.add(token_hash)
					result_tokens.write(line)

print len(gh_hashes)
print len(so_hashes)

