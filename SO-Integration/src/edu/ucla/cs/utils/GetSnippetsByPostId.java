package edu.ucla.cs.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import edu.ucla.cs.database.MySQLAccess;
import edu.ucla.cs.model.SOAnswerPost;
import edu.ucla.cs.model.SOQuestionPost;

public class GetSnippetsByPostId {
	
	public static void main(String[] args) {
		String input = "/home/troy/research/Integration-Study/dataset/gh-post-ids.txt";
		String targetDir = "/home/troy/research/Integration-Study/dataset/snippets";
		String log = "/home/troy/research/Integration-Study/dataset/gh-post-ids(unfound).txt";
		File rootDir = new File(targetDir);
		if(!rootDir.exists()) {
			rootDir.mkdirs();
		}
		
		File logFile = new File(log);
		if(logFile.exists()) {
			logFile.delete();
		}
		
		ArrayList<String> ids = loadIds(input);
		MySQLAccess db = new MySQLAccess();
		db.connect();
		
		for(String id : ids) {
			System.out.println("processing https://stackoverflow.com/questions/" + id);
			id = id.trim();
			if(id.isEmpty()) {
				continue;
			}
			File dir = new File(targetDir + File.separator + id);
			if(!dir.exists()) {
				dir.mkdirs();
			}
			SOQuestionPost qPost = db.getQuestionPost(id);
			if(qPost != null) {
				ArrayList<SOAnswerPost> posts = db.getAnswerPostsByQuestionId(id);
				for(SOAnswerPost aPost : posts) {
					writeSnippetsToDir(aPost, dir);
				}
			} else {
				// this id refers to an answer post
				SOAnswerPost aPost = db.getAnswerPost(id);
				if(aPost != null) {
					writeSnippetsToDir(aPost, dir);
				} else {
					MyFileUtils.appendStringToFile(id + System.lineSeparator(), log);
				}
			}
		}
		
		db.close();
	}
	
	private static ArrayList<String> loadIds(String input) {
		ArrayList<String> ids = new ArrayList<String>();
		File file = new File(input);
		List<String> lines;
		try {
			lines = FileUtils.readLines(file, Charset.defaultCharset());
			for(String line : lines) {
				if(line.contains("@@##@@")) {
					String id = line.substring(line.indexOf("@@##@@") + 6);
					if(id.contains(",")) {
						for(String s : id.split(",")) {
							ids.add(s);
						}
					} else {
						ids.add(id);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ids;
	}

	private static void writeSnippetsToDir(SOAnswerPost aPost, File dir) {
		ArrayList<String> snippets = MyFileUtils.getCode(aPost.body);
		StringBuilder sb = new StringBuilder();
		sb.append("PostId:" + aPost.id + System.lineSeparator());
		sb.append("ParentId:" + aPost.parentId + System.lineSeparator());
		sb.append("Score:" + aPost.score + System.lineSeparator());
		sb.append("Accepted:" + (aPost.isAccepted?"1":"0") + System.lineSeparator());
		sb.append("ViewCount:" + aPost.viewCount + System.lineSeparator());
		sb.append("Tags:" + aPost.tags + System.lineSeparator());
		sb.append("Snippets:");
		for(int i = 0; i < snippets.size(); i++) {
			String snippet = snippets.get(i);
			snippet = StringEscapeUtils.unescapeHtml4(snippet);
			if(snippet.split(System.lineSeparator()).length > 1 || snippet.endsWith(";")) {
				sb.append(snippet + System.lineSeparator());
				if(i != snippets.size() - 1) {
					sb.append("===UCLA@@UCI===" + System.lineSeparator());
				}
				
			}
		}
		
		MyFileUtils.writeStringToFile(sb.toString(), dir.getAbsolutePath() + File.separator + aPost.id + ".txt");
	}
}
