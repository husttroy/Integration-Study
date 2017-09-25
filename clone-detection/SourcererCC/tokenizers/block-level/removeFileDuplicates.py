import sys, os

# from file_stats folder, get the list of distinct files
# iterate through block_stats and block_tokens folders, remove the blocks that doesn't belong to the distinct file list

distinct_file_hashes = set()
file_ids = set()

for file in os.listdir("files_stats"):
	print 'Reading', file
	readfile = os.path.join("files_stats", file)
	with open(readfile, 'r') as f:
		with open(os.path.join("distinct_files_stats", file), 'w') as fw:
			for line in f:
				file_id = line.split(',')[1]
				file_hash = line.split(',')[4]
				if file_hash not in distinct_file_hashes:
					distinct_file_hashes.add(file_hash)
					file_ids.add(file_id)
					fw.write(line)				

print "num of distinct file hashes: ", len(distinct_file_hashes)


for file in os.listdir("blocks_stats"):
	print 'Reading', file
	readfile = os.path.join("blocks_stats", file)
	with open(readfile, 'r') as f:
		with open(os.path.join("distinct_file_blocks_stats", file), 'w') as fw:
			for line in f:
				block_id = line.split(',')[1]
				file_id = block_id[5:]
				if file_id in file_ids:
					fw.write(line)



for file in os.listdir("blocks_tokens"):
	print 'Reading', file
	readfile = os.path.join("blocks_tokens", file)
	with open(readfile, 'r') as f:
		with open(os.path.join("distinct_file_blocks_tokens", file), 'w') as fw:
			for line in f:
				block_id = line.split(',')[1]
				file_id = block_id[5:]
				if file_id in file_ids:
					fw.write(line)			
	
				
