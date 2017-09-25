import sys, os

# sys.argv[1] may be either clone-output.txt

bad_pair = 0
gh_blocks = set()
so_blocks = set()


def isGH(id):
	if len(id)>6:
		return True
	else:
		return False
file = sys.argv[1]
with open(file, 'r') as f:
	for line in f:
		line = line.strip()
		ids = line.split(',')
		proj_id_1 = ids[0]
		block_id_1 = ids[1]
		proj_id_2 = ids[2]
		block_id_2 = ids[3]
		if isGH(block_id_1):
			if isGH(block_id_2):
				bad_pair += 1
			else:
				gh_blocks.add(block_id_1)
				so_blocks.add(block_id_2)
		else:
			if isGH(block_id_2):
				gh_blocks.add(block_id_2)
				so_blocks.add(block_id_1)
			else:
				bad_pair += 1

print bad_pair
print len(gh_blocks)
print len(so_blocks)

# get all GH blocks that have clones in SO
num_dup = {}
with open("numTokenDuplicates.txt", 'r') as f:
	for line in f:
		line = line.strip()
		items = line.split(',')
		block_id = items[0]
		dup = items[1]
		num_dup[block_id] = dup

all_gh_count = 0
for block in gh_blocks:
	dup = num_dup[block]
	all_gh_count += int(dup)
print "all GH blocks that have clones in SO: ", all_gh_count
			
