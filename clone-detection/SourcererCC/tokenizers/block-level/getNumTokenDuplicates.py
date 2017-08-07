import sys, os

gh_token_hashes = {}

for file in os.listdir("distinct_file_blocks_tokens"):
	file = os.path.join("distinct_file_blocks_tokens", file)
	print 'Searching on ', file
	with open(file, 'r') as f:
		for line in f:
			token_hash = (line.split('@#@')[0]).split(',')[-1]
			if token_hash in gh_token_hashes:
				gh_token_hashes[token_hash] += 1
			else:
				gh_token_hashes[token_hash] = 1

so_token_hashes = {}

for file in os.listdir("so_blocks_tokens"):
	file = os.path.join("so_blocks_tokens", file)
	print 'Searching on ', file
	with open(file, 'r') as f:
		for line in f:
			token_hash = (line.split('@#@')[0]).split(',')[-1]
			if token_hash in so_token_hashes:
				so_token_hashes[token_hash] += 1
			else:
				so_token_hashes[token_hash] = 1


with open("distinct_token_hashes/distinct-files.tokens", 'r') as f:
	with open("numTokenDuplicates.txt", 'w') as fw:
		for line in f:
			token_hash = (line.split('@#@')[0]).split(',')[-1]
			block_id = line.split(',')[1]
			if len(block_id)>6:
				# this is a GH block
				fw.write(block_id)
				fw.write(',')
				fw.write(str(gh_token_hashes[token_hash]))
				fw.write('\n')

			else:
				# this is a SO block
				fw.write(block_id)
				fw.write(',')	
				fw.write(str(so_token_hashes[token_hash]))
				fw.write('\n')


