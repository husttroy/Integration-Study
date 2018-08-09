package edu.ucla.cs.timestamp;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class CrawlGitHubTimestampOnly {
	public static void main(String[] args) throws IOException, ParseException {
		String rootPath = "/home/troy/research/Integration-Study/dataset/clone-codes-new-sample";
		File rootDir = new File(rootPath);
				
		for(File cloneGroup : rootDir.listFiles()) {
			String dirName = cloneGroup.getName();
			if(dirName.startsWith("so-")) {
				String groupId = dirName.split("-")[1];
				System.out.println("Processing Clone Group " + groupId);
				
				File soFile = null;
				ArrayList<File> carvedGHFiles = new ArrayList<File>();
				File urlFile = null;
				File timestampFile = null;
				for (File file : cloneGroup.listFiles()) {
					String fName = file.getName();
					if(fName.endsWith("~")) {
						continue;
					} else if (fName.startsWith("so-") && fName.endsWith(".java")) {
						soFile = file;
					} else if (fName.startsWith("carved-gh-") && fName.endsWith(".java")) {
						carvedGHFiles.add(file);
					} else if (fName.equals("urls.txt") || fName.equals("gh-urls.txt")) {
						urlFile = file;
					} else if (fName.equals("timestamps.txt")) {
						timestampFile = file;
					}
				}
				
				if (soFile != null && !carvedGHFiles.isEmpty() && urlFile != null) {
					if(timestampFile == null) {
						String output = cloneGroup.getAbsolutePath() + File.separator + "timestamps.txt";
						GitHubCrawlUtils.crawlTimestamp(urlFile, carvedGHFiles, output);
					}
				}
			}
		}
	}
}
