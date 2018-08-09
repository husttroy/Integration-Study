package edu.ucla.cs.timestamp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import edu.ucla.cs.database.MySQLAccess;
import edu.ucla.cs.utils.MyFileUtils;

public class TimeOrderingCheck {
	final static String csv_file_path = "./result/time_order_result(scc_sample).tsv"; 
	
	public static void main(String[] args) throws IOException, ParseException {
		String rootPath = "/home/troy/research/Integration-Study/dataset/clone-codes-new-sample";
		File rootDir = new File(rootPath);
		File csvFile = new File(csv_file_path);
		if(csvFile.exists()) {
			csvFile.delete();
		}
		csvFile.createNewFile();
		
		MySQLAccess db = new MySQLAccess();
		db.connect();
		
		SimpleDateFormat soTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat ghTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
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
					String postId = soFile.getName().split("-")[1];
					String soTimestamp = db.getTimeStampOfAnswerPost(postId);
					Date soDate = soTimeFormatter.parse(soTimestamp);
					
					if(timestampFile == null) {
						String output = cloneGroup.getAbsolutePath() + File.separator + "timestamps.txt";
						GitHubCrawlUtils.crawlTimestamp(urlFile, carvedGHFiles, output);
						timestampFile = new File(output);
					}
					
					HashMap<Integer, String> timestamps = MyFileUtils.getCloneTimestamp(timestampFile);
					for(File ghFile : carvedGHFiles) {
						String ghFileName = ghFile.getName();
						int ghIndex = Integer.parseInt(ghFileName.split("-")[2]);
						String ghTimestamp = timestamps.get(ghIndex);
						if(ghTimestamp == null) {
							FileUtils.writeStringToFile(csvFile, groupId + "\t" + ghIndex + "\t" + "NO_TIMESTAMP" + System.lineSeparator(), Charset.defaultCharset(), true);
							System.err.println("Cannot find timestamp for " + dirName + "/" + ghIndex);
							continue;
						}
						
						Date ghDate = ghTimeFormatter.parse(ghTimestamp);
						if(ghDate.after(soDate)) {
							// The GitHub clone occurs after the SO post
							// We need to keep this clone
							FileUtils.writeStringToFile(csvFile, groupId + "\t" + ghIndex + "\t" + "AFTER" + System.lineSeparator(), Charset.defaultCharset(), true);
							System.out.println(groupId + " + " + ghIndex);
						} else {
							FileUtils.writeStringToFile(csvFile, groupId + "\t" + ghIndex + "\t" + "BEFORE" + System.lineSeparator(), Charset.defaultCharset(), true);
							System.out.println(groupId + " - " + ghIndex);
						}
					}
				}
			}
		}
		
		db.close();
	}
}