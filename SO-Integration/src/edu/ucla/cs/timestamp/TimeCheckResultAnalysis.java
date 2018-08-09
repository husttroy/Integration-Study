package edu.ucla.cs.timestamp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class TimeCheckResultAnalysis {
	final static String csv_file_path = "./result/time_order_result.tsv"; 

	public static void main(String[] args) throws IOException {
		File csvFile = new File(csv_file_path);
		List<String> lines = FileUtils.readLines(csvFile, Charset.defaultCharset());
		int beforeSOFileCount = 0;
		int afterSOFileCount = 0;
		int noTimestampFileCount = 0;
		int remainingGroupCount = 0;
		HashSet<String> seenGroupIds = new HashSet<String>();
		for(String line : lines) {
			if(line.isEmpty()) continue;
			String[] ss = line.split("\t");
			if(ss[2].equals("AFTER")) {
				// the GitHub clone is created after the SO post, we need to keep this clone
				afterSOFileCount++;
				if(!seenGroupIds.contains(ss[0])) {
					// this group has at least one GitHub clone that is created after the SO post
					remainingGroupCount ++;
					seenGroupIds.add(ss[0]);
				}
			} else if(ss[2].equals("BEFORE")) {
				// the GitHub clone is created before the SO post, we need to remove this clone
				beforeSOFileCount++;
			} else if(ss[2].equals("NO_TIMESTAMP")) {
				// the GitHub repo is either private or deleted
				noTimestampFileCount++;
			}
		}
		
		System.out.println("THe number of GitHub clones that are created after the corresponding SO post: " + afterSOFileCount);
		System.out.println("THe number of GitHub clones that are created before the corresponding SO post: " + beforeSOFileCount);
		System.out.println("THe number of GitHub clones whose repository is either deleted or private: " + noTimestampFileCount);
		System.out.println("THe number of clone groups with at least one GitHub clone that is created after the SO post: " + remainingGroupCount);
	}	
}
