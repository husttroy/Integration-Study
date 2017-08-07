import sys, os

def isGH(id):
	if len(id)>6:
		return True
	else:
		return False

so_output_dict = {}
def get_output_dict():
	
	global so_output_dict
	
	with open("clone-output.txt", 'r') as f:
		for line in f:
			line = line.strip()
			ids = line.split(',')
			proj_id_1 = ids[0]
			block_id_1 = ids[1]
			proj_id_2 = ids[2]
			block_id_2 = ids[3]
			
			if isGH(block_id_1): # first item is a GH block, second is SO
				so_output_dict[block_id_2] = proj_id_2
			else: # first item is SO, second is GH
				so_output_dict[block_id_1] = proj_id_1



so_block_stats = {}

def load_SO_stats():
	global so_block_stats
	with open("so-more-than-10-blocks-stats.stats", 'r') as f:
		for line in f:
			line = line.strip()
			items = line.split(',')
			proj_id = items[0]
			block_id = items[1]
			method_id = items[3]
			loc = items[4]
			so_block_stats[block_id] = (proj_id, method_id, loc)


so_tokenhash = {}
def load_SO_tokenhash():
	global so_tokenhash
	with open("so-more-than-10-blocks-tokens.tokens", 'r') as f:
		for line in f:
			items = (line.split('@#@')[0]).split(',')
			proj_id = items[0]
			block_id = items[1]
			token_hash = items[-1]
			so_tokenhash[block_id] = token_hash  

get_output_dict()
print len(so_output_dict)
load_SO_stats()
print len(so_block_stats)
load_SO_tokenhash()
print len(so_tokenhash)

cloned_tokenhash = set()
def getClonedTokenhash():
	global cloned_tokenhash
	for block_id in so_output_dict:
		token_hash = so_tokenhash[block_id]
		cloned_tokenhash.add(token_hash)

all_cloned_blocks = set()
def getAllClonedBlocks():
	global all_cloned_blocks
	with open("so-more-than-10-blocks-tokens.tokens", 'r') as f:
		for line in f:
			items = (line.split('@#@')[0]).split(',')
			token_hash = items[-1]
			block_id = items[1]
			if token_hash in cloned_tokenhash:
				all_cloned_blocks.add(block_id)
	
getClonedTokenhash()
print len(cloned_tokenhash)
getAllClonedBlocks()
print len(all_cloned_blocks)

post_id_list = {}
loc_all = []
for block_id in so_block_stats:
	(proj_id, method_id, loc) = so_block_stats[block_id]
	loc_all.append((block_id,loc)) 

loc_has_clone = []

for block_id in all_cloned_blocks:

	(proj_id, method_id, loc) = so_block_stats[block_id]
	post_id = proj_id[1:]

	loc_has_clone.append((block_id,loc))

	if post_id in post_id_list:
		post_id_list[post_id] += 1
	else:
		post_id_list[post_id] = 1

print len(loc_all)
print len(loc_has_clone)
print len(post_id_list)

with open("loc-all.csv", 'w') as fw:
	for item in loc_all:
		fw.write(item[0] + ',' + item[1] + "\n")

with open("loc-has-clone.csv", 'w') as fw:
	for item in loc_has_clone:
		fw.write(item[0] + ',' + item[1] +  "\n")

with open("post-id-list.txt", 'w') as fw:
	for postid in post_id_list:
		fw.write(str(postid) + ',' + str(post_id_list[postid]) + '\n')



	
	
