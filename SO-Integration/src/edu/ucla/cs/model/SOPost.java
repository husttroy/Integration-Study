package edu.ucla.cs.model;

import java.util.HashSet;

public class SOPost {
	public int id;
	public int parentId;
	public String body;
	public int score;
	public boolean isAccepted;
	public HashSet<String> tags;
	public int viewCount;
	
	public SOPost(String id, String parentId, String body, String score,
			String isAccepted, String tags, String viewCount) {
		this.id = Integer.parseInt(id);
		this.parentId = Integer.parseInt(id);
		this.body = body;
		this.score = Integer.parseInt(score);
		this.isAccepted = Integer.parseInt(isAccepted) == 0 ? false : true;
		this.tags = new HashSet<String>();
		if(tags != null) {
			if(!tags.trim().isEmpty()) {
				// tags are stored in a format of <a><b><c>, we need to split them
				tags = tags.substring(1, tags.length() - 1);
				String[] ss = tags.split("><");
				for(String tag : ss) {
					this.tags.add(tag);
				}
			}
		}
		
		if(viewCount != null) {
			this.viewCount = Integer.parseInt(viewCount);
		} else {
			this.viewCount = 0;
		}
	}
	
	@Override
	public int hashCode() {
		int hash = 37;
		hash += 31 * id;
//		hash += 31 * parentId;
//		hash += 31 * viewCount;
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SOPost) {
			SOPost other = (SOPost)obj;
			return this.id == other.id;
		} else {
			return false;
		}
	}
}
