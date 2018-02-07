package edu.ucla.cs.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.ucla.cs.database.MySQLAccess;
import edu.ucla.cs.model.SOAnswerPost;

public class GrepClonedMethodsWithSOLinks {
	public static void main(String[] args) throws IOException {
		String rootPath = "/home/troy/research/Integration-Study/dataset/clone-codes-screened/GH-files-with-so-links";
		String target = "/home/troy/research/Integration-Study/dataset/clone-codes-screened/real-clones";
		String identicalPostPath = "/home/troy/research/Integration-Study/dataset/clone-codes-screened/SO-token-groups.txt";
		String logPath = "/home/troy/research/Integration-Study/dataset/clone-codes-screened/clones-undetected-by-scc.log";
		
		File logFile = new File(logPath);
		if(logFile.exists()) {
			logFile.delete();
		}
		
		// load the post ids with identical SO methods
		// to distinguish different methods in the same post, we use 
		// the post id and also the method id as the key in the format of 'postId-methodId'
		HashMap<String, HashSet<String>> identicalPosts = new HashMap<String, HashSet<String>>();
		try(BufferedReader br = new BufferedReader(new FileReader(identicalPostPath))) {
			String line;
			HashSet<String> group = new HashSet<String>();
			while((line = br.readLine()) != null) { 
				if(line.equals("===UCLA@@@UCI===")) {
					// new group starts 
					if(!group.isEmpty()) {
						for(String id : group) {
							// add all but not this id to the value
							HashSet<String> set = new HashSet<String>(group);
							set.remove(id);
							identicalPosts.put(id, set);
						}
						group.clear();
					}
				} else {
					// post id
					group.add(line.replace(",", "-"));
				}
			}
		}
		
		File rootDir = new File(rootPath);
		MySQLAccess db = new MySQLAccess();
		db.connect();
		for(File dir : rootDir.listFiles()) {
			String dirName = dir.getName();
			String postId = dirName.split("-")[1];
			String methodId = dirName.split("-")[2];
			File soFile = null;
			for(File file : dir.listFiles()) {
				String fileName = file.getName();
				if (fileName.startsWith("so-")) {
					soFile = file;
					break;
				}
			}
			
			// get the question id of this answer post
			SOAnswerPost post = db.getAnswerPost(postId);
			HashSet<SOAnswerPost> postSet = new HashSet<SOAnswerPost>();
			postSet.add(post);
			HashSet<Integer> answerIdSet = new HashSet<Integer>();
			answerIdSet.add(post.id);
			HashSet<Integer> questionIdSet = new HashSet<Integer>();
			questionIdSet.add(post.parentId);
			// look up for all identical posts
			if(identicalPosts.containsKey(postId + "-" + methodId)) {
				HashSet<String> set = identicalPosts.get(postId + "-" + methodId);
				for(String id : set) {
					String pid = id.split("-")[0];
					SOAnswerPost dup = db.getAnswerPost(pid);
					postSet.add(dup);
					answerIdSet.add(dup.id);
					questionIdSet.add(dup.parentId);
				}
			}
			
			ArrayList<File> realClones = new ArrayList<File>();
			for(File file : dir.listFiles()) {
				String fileName = file.getName();
				if(fileName.startsWith("gh")) {
					String code = MyFileUtils.readFileToString(file.getAbsolutePath());
					if(code.contains("stackoverflow.com")) {
						String[] ss = code.split(System.lineSeparator());
						for(String s : ss) {
							if(s.contains("stackoverflow.com/questions/")) {
								// get the question id
								String temp = extractPostId(s, "stackoverflow.com/questions/");
								
								int questionId = Integer.parseInt(temp);
								if(questionIdSet.contains(questionId) || answerIdSet.contains(questionId)) {
									// this is a real clone
									realClones.add(file);
									
									// if this GitHub clone is copied from another identical post but not 
									// the one detected by SourcererCC, rename the folder name with the correct 
									// post and method id
									if(questionId != post.id && questionId != post.parentId) {
										int correctPostId = -1;
										int correctMethodId = -1;
										if(answerIdSet.contains(questionId)) {
											correctPostId = questionId;
										} else if (questionIdSet.contains(questionId)) {
											// iterate through the post set and find the corresponding answer id
											for(SOAnswerPost p : postSet) {
												if(p.parentId == questionId) {
													correctPostId = p.id;
													break;
												}
											}
										}
										
										if(correctPostId != -1) {
											// find the method id
											HashSet<String> set = identicalPosts.get(postId + "-" + methodId);
											for(String id : set) {
												String pid = id.split("-")[0];
												String mid = id.split("-")[1];
												if(Integer.parseInt(pid) == correctPostId) {
													correctMethodId = Integer.parseInt(mid);
													break;
												}
											}
											
											// rename the folder name virtually
											// do not physically rename it since it will invalidate the paths of the GitHub files in realClones array list
											if(correctMethodId != -1) {
												dirName = "so-" + correctPostId + "-" + correctMethodId;
											} else {
												System.out.println("Cannot find the corresponding identical post:" + file.getAbsolutePath());
											}
										} else {
											System.out.println("Cannot find the corresponding identical post:" + file.getAbsolutePath());
										}
									}
									
									break;
								} else {
									// this post is referenced but not detected by SourcererCC
									logUndetectedClones(logPath, dir, postId,
											file, temp);
								}
							} else if (s.contains("stackoverflow.com/a/")) {
								// get the answer id
								String temp = extractPostId(s, "stackoverflow.com/a/");
								
								int answerId = Integer.parseInt(temp);
								if(answerIdSet.contains(answerId)) {
									realClones.add(file);
									
									// if this GitHub clone is copied from another identical post but not 
									// the one detected by SourcererCC, rename the folder name with the correct 
									// post and method id
									if(answerId != post.id) {
										int correctPostId = answerId;
										int correctMethodId = -1;
										// find the method id
										HashSet<String> set = identicalPosts.get(postId + "-" + methodId);
										for(String id : set) {
											String pid = id.split("-")[0];
											String mid = id.split("-")[1];
											if(Integer.parseInt(pid) == correctPostId) {
												correctMethodId = Integer.parseInt(mid);
												break;
											}
										}
										
										// rename the folder name virtually
										// do not physically rename it since it will invalidate the paths of the GitHub files in realClones array list
										if(correctMethodId != -1) {
											dirName = "so-" + correctPostId + "-" + correctMethodId;
										} else {
											System.out.println("Cannot find the corresponding identical post:" + file.getAbsolutePath());
										}
									}
									
									break;
								} else {
									// this post is referenced but not detected by SourcererCC
									logUndetectedClones(logPath, dir, postId,
											file, temp);
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
					destDir.mkdirs();
				}
				
				// copy the stack overflow file
				if(soFile != null) {
					FileUtils.copyFileToDirectory(soFile, destDir);
				}
				
				// only copy the GitHub file that contains the SO link and the so snippet to the target directory
				for(File file : realClones) {
					FileUtils.copyFileToDirectory(file, destDir);
				}
				
				// only copy the GitHub links of those GitHub files that contain validated, real clones
				List<String> urls = FileUtils.readLines(new File(dir.getAbsolutePath() + File.separator + "urls.txt"),
						Charset.defaultCharset());
				for (File file : realClones) {
					String fName = file.getName();
					String fNameNoExtension = fName.substring(0, fName.indexOf("."));
					String startLineS = fNameNoExtension.split("-")[3];
					String endLineS = fNameNoExtension.split("-")[4];
					String range = "#L" + startLineS + "-L" + endLineS;
					for (String url : urls) {
						if (url.endsWith(range)) {
							MyFileUtils.appendStringToFile(
									url + System.lineSeparator(),
									destDir.getAbsolutePath() + File.separator
											+ "urls.txt");
						}
					}
				}
			}
		}
		db.close();
	}

	private static void logUndetectedClones(String logPath, File dir,
			String detectedId, File file, String referencedId) throws IOException {
		String ref_url = "https://stackoverflow.com/questions/" + referencedId;
		String det_url = "https://stackoverflow.com/questions/" + detectedId;
		MyFileUtils.appendStringToFile("===UCLA@@@UCI===" + System.lineSeparator(), logPath);
		MyFileUtils.appendStringToFile("Detected Post: " + det_url + System.lineSeparator(), logPath);
		List<String> urls = FileUtils.readLines(new File(dir.getAbsolutePath() + File.separator + "urls.txt"),
				Charset.defaultCharset());
		String fName = file.getName();
		String fNameNoExtension = fName.substring(0, fName.indexOf("."));
		String startLineS = fNameNoExtension.split("-")[3];
		String endLineS = fNameNoExtension.split("-")[4];
		String range = "#L" + startLineS + "-L" + endLineS;
		for (String url : urls) {
			if (url.endsWith(range)) {
				MyFileUtils.appendStringToFile(
						"Detected GitHub File: " + url.substring(url.indexOf("http")) + System.lineSeparator(),
						logPath);
				break;
			}
		}
		MyFileUtils.appendStringToFile("Referenced Post: " + ref_url + System.lineSeparator(), logPath);
	}
	
	private static String extractPostId(String text, String prefix) {
		String sub = text.substring(text.indexOf(prefix) + prefix.length());
		char[] arr = sub.toCharArray();
		StringBuilder sb = new StringBuilder();
		for(char c : arr) {
			if(c >= '0' && c <= '9') {
				sb.append(c);
			} else {
				break;
			}
		}
		
		return sb.toString();
	}
}
