import sys, os

def isGH(id):
	if len(id)>6:
		return True
	else:
		return False

so_output_dict() = {}
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
			so_block_stats[block_id] = (method_id, loc)


get_output_dict()
print len(so_output_dict)
load_SO_stats()
print len(so_block_stats)

post_id_list = {}
loc_all = []
for block_id in so_block_stats:
	(method_id, loc) = so_block_stats[block_id]
	loc_all.append(loc) 

loc_has_clone = []

for block_id in so_output_dict:
	proj_id = so_output_dict[block_id]
	post_id = proj_id[1:]

	(method_id, loc) = so_block_stats[block_id]
	loc_has_clone.append(loc)
	if post_id in post_id_list:
		post_id_list[post_id] += 1
	else:
		post_id_list[post_id] = 1

with open("loc-all.csv", 'w') as fw:
	for item in loc_all:
		fw.write(item + "\n")

with open("lock-has-clone.csv", 'w') as fw:
	for item in loc_has_clone:
		fw.write(item + "\n")

with open("post-id-list.txt", 'w') as fw:
	for postid in post_id_list:
		fw.write(postid + ',' + post_id_list[post_id] + '\n')



	
	
