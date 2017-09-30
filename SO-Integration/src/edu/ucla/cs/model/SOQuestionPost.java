package edu.ucla.cs.model;

import java.util.HashSet;

public class SOQuestionPost {
	public int id;
	public int acceptedPostId;
	public HashSet<String> tags;
	public int viewCount;
	
	public SOQuestionPost(String id, String acceptedPostId, String tags, String viewCount) {
		this.id = Integer.parseInt(id);
		if(acceptedPostId == null || acceptedPostId.equals("null")) {
			this.acceptedPostId = -1;
		} else {
			this.acceptedPostId = Integer.parseInt(acceptedPostId);
		}
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
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SOQuestionPost) {
			SOQuestionPost other = (SOQuestionPost)obj;
			return this.id == other.id;
		} else {
			return false;
		}
	}
}
