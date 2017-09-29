package edu.ucla.cs.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import edu.ucla.cs.database.MySQLAccess;
import edu.ucla.cs.model.SOPost;

public class GrepClonedMethodsWithSOLinks {
	public static void main(String[] args) throws IOException {
		String rootPath = "/home/troy/research/Integration-Study/dataset/GH-files-with-so-links";
		String target = "/home/troy/research/Integration-Study/dataset/real-clones";
		
		File rootDir = new File(rootPath);
		MySQLAccess db = new MySQLAccess();
		db.connect();
		for(File dir : rootDir.listFiles()) {
			String dirName = dir.getName();
			String postId = dirName.split("-")[1];
			File soFile = null;
			for(File file : dir.listFiles()) {
				String fileName = file.getName();
				if (fileName.startsWith("so-")) {
					soFile = file;
					break;
				}
			}
			
			// get the question id of this answer post
			SOPost post = db.getPost(postId);
			
			ArrayList<File> realClones = new ArrayList<File>();
			for(File file : dir.listFiles()) {
				String fileName = file.getName();
				if(fileName.startsWith("gh") && !fileName.equals("gh-urls")) {
					String code = MyFileUtils.readFileToString(file.getAbsolutePath());
					if(code.contains("stackoverflow.com")) {
						String[] ss = code.split(System.lineSeparator());
						for(String s : ss) {
							if(s.contains("stackoverflow.com/questions/")) {
								// get the question id
								String temp = s.substring(s.indexOf("stackoverflow.com/questions/") + 28);
								if(temp.contains(" ")) {
									temp = temp.substring(0, temp.indexOf(" "));
								}
								
								if(temp.contains("/")) {
									temp = temp.substring(0, temp.indexOf("/"));
								}
								
								int questionId = Integer.parseInt(temp);
								if(questionId == post.parentId || temp.equals(postId)) {
									// this is a real clone
									realClones.add(file);
									break;
								}
							} else if (s.contains("stackoverflow.com/a")) {
								// get the answer id
								String temp = s.substring(s.indexOf("stackoverflow.com/a/") + 20);
								if(temp.contains(" ")) {
									temp = temp.substring(0, temp.indexOf(" "));
								}
								
								if(temp.contains("/")) {
									temp = temp.substring(0, temp.indexOf("/"));
								}
								
								if(temp.equals(postId)) {
									realClones.add(file);
									break;
								}
							}
						}
					}
				}
			}
			
			if(!realClones.isEmpty()) {
				// create a new folder with the same group index in the target directory
				File destDir = new File(target + File.separator + dirName);
				if(!destDir.exists()) {
					destDir.mkdir();
				}
				
				// copy the stack overflow file
				if(soFile != null) {
					FileUtils.copyFileToDirectory(soFile, destDir);
				}
				
				// only copy the GitHub file that contains the SO link and the so snippet to the target directory
				for(File file : realClones) {
					FileUtils.copyFileToDirectory(file, destDir);
				}
			}
		}
		db.close();
	}
}
