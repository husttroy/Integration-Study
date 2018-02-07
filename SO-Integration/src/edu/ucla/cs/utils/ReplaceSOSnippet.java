package edu.ucla.cs.utils;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;

import edu.ucla.cs.database.MySQLAccess;
import edu.ucla.cs.model.SOAnswerPost;
import edu.ucla.cs.parse.PartialProgramParser;

public class ReplaceSOSnippet {
	public static void main(String[] args) {
		String path = "/home/troy/research/Integration-Study/dataset/real-clones-reindex";
		File rootDir = new File(path);
		MySQLAccess access = new MySQLAccess();
		access.connect();
		for(File dir : rootDir.listFiles()) {
			File soFile = null;
			for(File file : dir.listFiles()) {
				if(file.getName().startsWith("so-")) {
					soFile = file;
					break;
				}
			}
			
			String dirName = dir.getName();
			String postId = dirName.split("-")[1];
			int methodId = Integer.parseInt(dirName.split("-")[2]);
			SOAnswerPost post = access.getAnswerPost(postId);
			ArrayList<String> snippets = MyFileUtils.getCode(post.body);
			int method_count = -1;
			for(String snippet: snippets) {
				snippet = StringEscapeUtils.unescapeHtml4(snippet);
				int len = snippet.split(System.lineSeparator()).length;
				if(len > 1) {
					PartialProgramParser parser = new PartialProgramParser();
					try {
						ArrayList<String> methods = parser.extracMethod(snippet);
						// print methods to the output file
						for(String method : methods) {
							method_count ++;
							int len2 = method.split(System.lineSeparator()).length;
							if(len2 < 10) {
								// only consider methods with more than 10 lines of code
								continue;
							}
							if(method_count == methodId) {
								// this is the method
								MyFileUtils.writeStringToFile(method, soFile.getAbsolutePath());
								break;
							}
						}
					} catch (Exception e) {
						// suppress the exception
						continue;
					}	
				}
			}
		}
		access.close();
	}
}
