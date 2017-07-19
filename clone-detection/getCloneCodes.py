import sys, os
import zipfile


def isGH(id):
	if len(id)>6:
		return True
	else:
		return False


output_dict = {}
def get_output_dict():

	global output_dict

	with open("clone-output.txt", 'r') as f:
		for line in f:
			line = line.strip()
			ids = line.split(',')
			proj_id_1 = ids[0]
			block_id_1 = ids[1]
			proj_id_2 = ids[2]
			block_id_2 = ids[3]

			if isGH(block_id_1): # first item is a GH block, second is SO
				if (proj_id_2, block_id_2) in output_dict:
					output_dict[(proj_id_2, block_id_2)].append((proj_id_1, block_id_1))
				else:
					output_dict[(proj_id_2, block_id_2)] = [(proj_id_1, block_id_1)]

			else: # first item is SO, second is GH
				if (proj_id_1, block_id_1) in output_dict:
					output_dict[(proj_id_1, block_id_1)].append((proj_id_2, block_id_1))
				else:
					output_dict[(proj_id_1, block_id_1)] = [(proj_id_2, block_id_2)]


so_block_stats = {}
gh_block_stats = {}
gh_file_stats = {}

def load_SO_stats():
	global so_block_stats
	with open("so-more-than-10-blocks-stats.stats", 'r') as f:
		for line in f:
			line = line.strip()
			items = line.split(',')
			proj_id = items[0]
			block_id = items[1]
			method_id = items[3]
			so_block_stats[block_id] = method_id



def load_GH_stats():
	global gh_block_stats
	global gh_file_stats
	with open("gh-blocks-stats.txt", 'r') as f:
		for line in f:
			line = line.strip()
			items = line.split(',')
			block_id = items[1]
			start_line = items[-2]
			end_line = items[-1]
			gh_block_stats[block_id] = (start_line, end_line)

	with open("gh-files-stats.txt", 'r') as f:
		for line in f:
			line = line.strip()
			items = line.split(',')
			file_id = items[1]
			file_path = items[2]
			gh_file_stats[file_id] = file_path


block_location = {}
def getBlockLocation():
	global block_location
	print len(output_dict)

	for (so_proj_id, so_block_id) in output_dict:
		so_post_id = so_proj_id[1:]
		so_method_id = so_block_stats[so_block_id]

		clone_list = output_dict[(so_proj_id, so_block_id)]
		for tuple in clone_list:
			(gh_proj_id, gh_block_id) = tuple
			(start_line, end_line) = gh_block_stats[gh_block_id] 
			file_path = gh_file_stats[gh_block_id[5:]]
			if (so_post_id, so_method_id) in block_location:
				block_location[(so_post_id, so_method_id)].append((start_line, end_line, file_path))	
			else:
				block_location[(so_post_id, so_method_id)] = [(start_line, end_line, file_path)]


def write_files():
	snippet_dict = {}
	with open("so-more-than-10-lines.txt", "r") as f:	
		all_snippets = f.read()
		blocks = all_snippets.split("===UCLA@@@UCI===")
		for item in blocks:
			item = item.strip()
			if len(item)==0:
				continue
			else:
				lines = item.split("\n")
				if len(lines) < 6:
					print "cannot find a valid snippet", item
					continue 
				post_id = (lines[0].split())[1]
				method_id = (lines[4].split())[1]
				snippet = "\n".join(lines[5:])
				snippet_dict[(post_id, method_id)] = snippet


	for (so_post_id, so_method_id) in block_location:
		newdir = "clone-codes/so-"+ so_post_id + "-" + so_method_id
		if not os.path.exists(newdir):
			os.makedirs(newdir)
		with open(newdir + "/so.txt", 'w') as fw:
			fw.write(snippet_dict[(so_post_id, so_method_id)])
		
		for i, (start_line, end_line, file_path) in enumerate(block_location[(so_post_id, so_method_id)]):
			file_path = file_path.strip('"')
			zip_path = '/'.join(file_path.split("/")[0:10])
			subfile_path = '/'.join(file_path.split("/")[10:])
			archive = zipfile.ZipFile(zip_path, 'r')
			code = archive.read(subfile_path)
			with open(newdir + "/gh-" + str(i) + "-" + start_line + "-" + end_line + ".txt", 'w') as fw:
				fw.write(code)


def main():
#	try:
		get_output_dict()
		print len(output_dict)
		load_SO_stats()
		print len(so_block_stats)
		load_GH_stats()
		print len(gh_block_stats), len(gh_file_stats)
		getBlockLocation()
		print len(block_location)
		write_files()
#	except  Exception as e:
#		print e
main()
