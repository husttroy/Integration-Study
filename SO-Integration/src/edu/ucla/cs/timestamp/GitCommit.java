package edu.ucla.cs.timestamp;

public class GitCommit {
	public String sha_value;
	public String timestamp;
	
	public GitCommit(String sha, String time) {
		this.sha_value = sha;
		this.timestamp = time;
	}
}
