import os

count = 0
with open("gh-count.txt", 'w') as fw:
	for folder in os.listdir("clone-codes"):
		so_dup = 0
		gh_count = 0
		for file in os.listdir("clone-codes/" + folder):
			if file.startswith('so'):
				so_dup = int((file.split('-')[1])[:-4])
			if file.startswith('gh'):
				gh_dup = int(file.split('-')[2])
				gh_count += gh_dup
			
		count += so_dup
		while so_dup > 0:
			fw.write(str(gh_count) + "\n")
			so_dup -= 1
			

print count	
