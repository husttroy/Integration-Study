import sys, os

# sys.argv[1] may be either clone-output.txt or clone-pair-200.txt

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
		
