package edu.ucla.cs.so.data.model;

import java.util.HashSet;

public class Answer {
	public int id;
	public int parentId;
	public String body;
	public int score;
	public boolean isAccepted;
	public HashSet<String> tags;
	public int viewCount;

	public Answer(String id, String parentId, String body, String score,
			String isAccepted, String tags, String viewCount) {
		this.id = Integer.parseInt(id);
		this.parentId = Integer.parseInt(id);
		this.body = body;
		this.score = Integer.parseInt(score);
		this.isAccepted = Integer.parseInt(isAccepted) == 0 ? false : true;
		this.tags = new HashSet<String>();
		if(tags != null) {
			String[] ss = tags.split(",");
			for(String s : ss) {
				this.tags.add(s);
			}
		}
		
		
		if(viewCount != null) {
			this.viewCount = Integer.parseInt(viewCount);
		} else {
			this.viewCount = 0;
		}
	}
}
