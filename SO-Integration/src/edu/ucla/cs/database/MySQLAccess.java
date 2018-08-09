package edu.ucla.cs.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;

import edu.ucla.cs.model.SOAnswerPost;
import edu.ucla.cs.model.SOQuestionPost;
import edu.ucla.cs.parse.PartialProgramParser;
import edu.ucla.cs.utils.MyFileUtils;

public class MySQLAccess {
	final String url = "jdbc:mysql://localhost:3306/stackoverflow";
	final String username = "root";
	final String password = "5887526";
	
	Connection connect = null;
	ResultSet result = null;
	PreparedStatement prep = null;
	
	public void connect() {
		try {
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			connect = DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	public SOAnswerPost getAnswerPost(String id) {
		String query = "select * from answers where Id = " + id + ";";
		SOAnswerPost post = null;
		try {
			prep = connect.prepareStatement(query);
			result = prep.executeQuery();
			if (result.next()) {
				String parentId = result.getString("ParentId");
				String body = result.getString("Body");
				String score = result.getString("Score");
				String isAccepted = result.getString("IsAccepted");
				String tags = result.getString("Tags");
				String viewCount = result.getString("ViewCount");
				post = new SOAnswerPost(id, parentId, body, score, isAccepted, tags, viewCount);
			}
			
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return post;
	}
	
	public String getTimeStampOfAnswerPost(String id) {
		String query = "select CreationDate from answers where Id = " + id + ";";
		try {
			prep = connect.prepareStatement(query);
			result = prep.executeQuery();
			if (result.next()) {
				String date = result.getString("CreationDate");
				return date;
			}
			
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public ArrayList<SOAnswerPost> getAnswerPostsByQuestionId(String id) {
		String query = "select * from answers where ParentId = " + id + ";";
		ArrayList<SOAnswerPost> posts = new ArrayList<SOAnswerPost>();
		try {
			prep = connect.prepareStatement(query);
			result = prep.executeQuery();
			while (result.next()) {
				String answerId = result.getString("id");
				String parentId = result.getString("ParentId");
				String body = result.getString("Body");
				String score = result.getString("Score");
				String isAccepted = result.getString("IsAccepted");
				String tags = result.getString("Tags");
				String viewCount = result.getString("ViewCount");
				posts.add(new SOAnswerPost(answerId, parentId, body, score, isAccepted, tags, viewCount));
			}
			
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return posts;
	}
	
	public SOQuestionPost getQuestionPost(String id) {
		String query = "select * from questions where Id = " + id + ";";
		SOQuestionPost post = null;
		try {
			prep = connect.prepareStatement(query);
			result = prep.executeQuery();
			if (result.next()) {
				String acceptedId = result.getString("AcceptedAnswerId");
				String tags = result.getString("Tags");
				String viewCount = result.getString("ViewCount");
				post = new SOQuestionPost(id, acceptedId, tags, viewCount);
			}
			
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return post;
	}
	
	public int countNumOfSOSnippets() {
		String query = "select * from answers;";
		int count = 0;
		if (connect != null) {
			try {
				prep = connect.prepareStatement(query);
				result = prep.executeQuery();
				while(result.next()) {
					// get the snippet
					String body = result.getString("Body");
					ArrayList<String> snippets = MyFileUtils.getCode(body);
					for(String snippet: snippets) {
						snippet = StringEscapeUtils.unescapeHtml4(snippet);
						int len = snippet.split(System.lineSeparator()).length;
						if(len > 1) {
							PartialProgramParser parser = new PartialProgramParser();
							boolean flag = false;
							try {
								ArrayList<String> methods = parser.extracMethod(snippet);
								// print methods to the output file
								for(String method : methods) {
									int len2 = method.split(System.lineSeparator()).length; 
//									if(len2 >= 10 || len2 < 4) {
										// only consider methods with less than 10 lines of code
									if(len2 < 10) {
										// only consider methods with more than 10 lines of code
										continue;
									} else {
										flag = true;
										break;
									}
								}
								
								if(flag) {
									count ++;
								}
							} catch (Exception e) {
								// suppress the exception
								continue;
							}
						}
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return count;
	}
	
	public void close() {
		try {
			if (result != null)
				result.close();
			if (prep != null)
				prep.close();
			if (connect != null)
				connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
