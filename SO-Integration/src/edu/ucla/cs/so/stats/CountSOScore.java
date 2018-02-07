package edu.ucla.cs.so.stats;

import java.io.File;

import edu.ucla.cs.database.MySQLAccess;
import edu.ucla.cs.model.SOAnswerPost;
import edu.ucla.cs.utils.MyFileUtils;

public class CountSOScore {
	public static void main(String[] args) {
		String path = "/home/troy/research/Integration-Study/dataset/real-clones-reindex";
		String logFile = "/home/troy/research/Integration-Study/log/score-real-clones.csv";
		
		MySQLAccess access = new MySQLAccess();
		access.connect();
		
		int numOfAccepted = 0;
		File rootDir = new File(path);
		for(File dir : rootDir.listFiles()) {
			File soFile = null;
			for(File file : dir.listFiles()) {
				if(file.getName().startsWith("so-") && !file.getName().endsWith("~")) {
					soFile = file;
					break;
				}
			}
			
			if(soFile != null) {
				String fName = soFile.getName();
				String postId = fName.split("-")[1];
				SOAnswerPost post = access.getAnswerPost(postId);
				if(post.isAccepted) {
					numOfAccepted ++;
				}
				
				MyFileUtils.appendStringToFile(post.score + System.lineSeparator(), logFile);
			}
		}
		
		access.close();
		
		System.out.println("Number of accepted posts: " + numOfAccepted);
	}
}
