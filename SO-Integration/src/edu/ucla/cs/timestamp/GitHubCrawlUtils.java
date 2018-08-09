package edu.ucla.cs.timestamp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.ucla.cs.utils.MyFileUtils;

public class GitHubCrawlUtils {
	
	public static ArrayList<GitCommit> getSHAfromCommitHistoryPage(String url) {
		try {
			ArrayList<GitCommit> shas = new ArrayList<GitCommit>();
			// escape empty space in the url
			String encodedURL = url.replaceAll(" ", "%20");
			Document doc = Jsoup.connect(encodedURL).get();
			Elements commits = doc.getElementsByClass("commit");
			for(Element commit : commits) {
				Element copyboard = commit.select("clipboard-copy").first();
				String sha_value = copyboard.attr("value");
				Element timetag = commit.select("relative-time").first();
				String timestamp = timetag.attr("datetime");
				GitCommit git_commit = new GitCommit(sha_value, timestamp);
				shas.add(git_commit);
			}
			
			return shas;
		} catch (IOException e) {
			if(e instanceof HttpStatusException && ((HttpStatusException)e).getStatusCode() == 404) {
				// 404 error: url may be broken, html page may be deleted from the server, etc.
				System.err.println("404 Not Found: " + url);
			} else {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static String downloadAsRaw(String link) {
		URL url= null;
		BufferedReader in = null;
		try {
			// escape empty space in the url
			String encodedLink = link.replaceAll(" ", "%20");
	        url = new URL(encodedLink);
			in = new BufferedReader(
			        new InputStreamReader(url.openStream()));
			String content = "";
			String inputLine;
		    while ((inputLine = in.readLine()) != null) {
		    	content += inputLine + System.lineSeparator();
		    }
			return content;
		} catch (IOException e) {
			// fail safe
			System.err.println("Fails to download from " + link);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return "";
	}
	
	public static String getTimeStampFromGitHubPage(String url) {
		try {
			// escape empty space in the url
			String encodedURL = url.replaceAll(" ", "%20");
			Document doc = Jsoup.connect(encodedURL).get();
			Elements elems = doc.getElementsByClass("commit-tease");
			Elements timetags = elems.select("relative-time");
			Element timetag = timetags.first();
			String timestamp = timetag.attr("datetime");
			
			return timestamp;
		} catch (IOException e) {
			if(e instanceof HttpStatusException && ((HttpStatusException)e).getStatusCode() == 404) {
				// 404 error: url may be broken, html page may be deleted from the server, etc.
				System.err.println("404 Not Found: " + url);
			} else {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static void crawlTimestamp(File urlFile, ArrayList<File> carvedGHFiles, String output) throws IOException {
		// crawl timestamp
		File timestampFile = new File(output);
		timestampFile.createNewFile();
		
		HashMap<Integer, String> urls = MyFileUtils.getCloneURL(urlFile);
		for (File ghFile : carvedGHFiles) {
			String ghClone = FileUtils.readFileToString(ghFile, Charset.defaultCharset());
			// remove the class header
			ghClone = ghClone.substring(ghClone.indexOf("\n") + 1, ghClone.lastIndexOf("\n"));
			
			String ghFileName = ghFile.getName();
			int ghIndex = Integer.parseInt(ghFileName.split("-")[2]);
			String url = urls.get(ghIndex);
			
			if(url.startsWith("https://github.com/")) {
				url = url.substring(19);
			}
			
			// change the url to the commit history url of the file
			String[] ss = url.split("/");
			String usr_project = ss[0] + "/" + ss[1];
			String branch = ss[2] + "/" + ss[3]; // like blob/master or tree/master
			String file_path = url.substring(url.indexOf(branch) + branch.length(), url.indexOf("#"));
			
			String commit_history_url = "https://github.com/" + usr_project + "/commits/" + ss[3] + file_path;
			
			ArrayList<GitCommit> commits = GitHubCrawlUtils.getSHAfromCommitHistoryPage(commit_history_url);
			if(commits == null || commits.isEmpty()) {
				// no commits, something goes wrong
				System.err.println("Cannot find any commits based on this commit history url --- " + commit_history_url);
			} else {
				GitCommit first_clone_commit = null;
				for(int i = commits.size() - 1; i >= 0; i--) {
					GitCommit commit = commits.get(i);
					String raw_file_url = "https://raw.githubusercontent.com/" + usr_project + "/" + commit.sha_value + file_path;
					String raw_file = GitHubCrawlUtils.downloadAsRaw(raw_file_url);
					if(raw_file.contains(ghClone)) {
						// this is the first commit that introduces the clone
						first_clone_commit = commit;
						break;
					}
				}
				
				if(first_clone_commit != null) {
					// save the timestamp in timestamps.txt
					String link = "https://github.com/" + usr_project + "/blob/" + first_clone_commit.sha_value + file_path + url.substring(url.indexOf("#"));
					FileUtils.writeStringToFile(timestampFile, ghIndex + "\t" + link + "\t" + first_clone_commit.timestamp + System.lineSeparator(), Charset.defaultCharset(), true);
				}
			}
		}
	}
}
