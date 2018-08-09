package edu.ucla.cs.timestamp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FilterClonesByTimestamp {
	public static void main(String[] args) throws IOException {
		// read the time checking log
		String csv_file_path = "./result/time_order_result(scc_sample).tsv";
		File csvFile = new File(csv_file_path);
		List<String> lines = FileUtils.readLines(csvFile, Charset.defaultCharset());
		HashMap<String, ArrayList<String>> groups = new HashMap<String, ArrayList<String>>();
		for(String line : lines) {
			String[] tmp = line.split("\t");
			if(tmp[2].equals("AFTER")) {
				String groupId = tmp[0];
				// keep this clone since it occurs after the SO post
				ArrayList<String> clones;
				if(groups.containsKey(groupId)) {
					clones = groups.get(groupId);
				} else {
					clones = new ArrayList<String>();
				}
				clones.add(tmp[1]);
				groups.put(groupId, clones);
			}
		}
		
		String rootPath = "/home/troy/research/Integration-Study/dataset/clone-codes-new-sample";
		String outputPath = "/home/troy/research/Integration-Study/dataset/clone-codes-new-sample-filtered";
		File rootDir = new File(rootPath);
		if(rootDir.exists()) {
			rootDir.delete();
		}
		rootDir.mkdir();
		
		// copy these clones to the target directory
		for(String groupId : groups.keySet()) {
			// make the group folder in the target directory 
			File groupDir = new File(outputPath + File.separator + "so-" + groupId);
			groupDir.mkdir();
			
			// copy urls.txt
			File urlFile1 = new File(rootPath + File.separator + "so-" + groupId + File.separator + "urls.txt");
			File urlFile2 = new File(outputPath + File.separator + "so-" + groupId + File.separator + "urls.txt");
			FileUtils.copyFile(urlFile1, urlFile2);
			
			// copy timestamps.txt
			File timeFile1 = new File(rootPath + File.separator + "so-" + groupId + File.separator + "timestamps.txt");
			File timeFile2 = new File(outputPath + File.separator + "so-" + groupId + File.separator + "timestamps.txt");
			FileUtils.copyFile(timeFile1, timeFile2);
			
			// copy clone files
			ArrayList<String> clones = groups.get(groupId);
			File dir = new File(rootPath + File.separator + "so-" + groupId);
			for(File file : dir.listFiles()) {
				String name = file.getName();
				if(name.startsWith("gh-")) {
					String cloneId = name.split("-")[1];
					if(clones.contains(cloneId)) {
						File file2 = new File(outputPath + File.separator + "so-" + groupId + File.separator + name);
						FileUtils.copyFile(file, file2);
					}
				} else if (name.startsWith("carved-gh-")) {
					String cloneId = name.split("-")[2];
					if(clones.contains(cloneId)) {
						File file2 = new File(outputPath + File.separator + "so-" + groupId + File.separator + name);
						FileUtils.copyFile(file, file2);
					}
				} else if (name.startsWith("so-")) {
					// copy the so snippet to the target dir
					File file2 = new File(outputPath + File.separator + "so-" + groupId + File.separator + name);
					FileUtils.copyFile(file, file2);
				}
			}
		}
	}
}
