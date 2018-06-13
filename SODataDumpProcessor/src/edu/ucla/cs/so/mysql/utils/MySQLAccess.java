package edu.ucla.cs.so.mysql.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

import edu.ucla.cs.so.data.model.Answer;
import edu.ucla.cs.so.data.model.Question;

public class MySQLAccess {
	final String url = "jdbc:mysql://localhost:3306/stackoverflow";
	final String username = "root";
	final String password = "5887526";
	String table;
	Connection connect = null;
	Statement statement = null;
	public ResultSet result = null;
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

	public void insertQuestionPost(String id, String accept, String view,
			String tags) {
		if (connect != null) {
			try {
				prep = connect
						.prepareStatement("insert into questions (Id, AcceptedAnswerId, ViewCount, Tags) values (?, ?, ?, ?)");
				prep.setString(1, id);
				prep.setString(2, accept);
				prep.setString(3, view);
				prep.setString(4, tags);
				prep.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void insertAnswerPost(String id, String parentId, String body,
			String score, String accept, String tags, String view) {
		if (connect != null) {
			try {
				prep = connect
						.prepareStatement("insert into answers "
								+ "(Id, ParentId, Body, Score, "
								+ "IsAccepted, Tags, ViewCount) "
								+ "values (?, ?, ?, ?, ?, ?, ?)");
				prep.setString(1, id);
				prep.setString(2, parentId);
				prep.setString(3, body);
				prep.setString(4, score);
				prep.setString(5, accept);
				prep.setString(6, tags);
				prep.setString(7, view);
				prep.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Question selectQuestionPosts(String id) {
		if (connect != null) {
			try {
				prep = connect
						.prepareStatement("select * from questions where id = "
								+ id + ";");
				result = prep.executeQuery();
				Question q;
				if (result.next()) {
					q = new Question(id, result.getString("AcceptedAnswerId"),
							result.getString("Tags"),
							result.getString("ViewCount"));
				} else {
					q = null;
				}

				result.close();
				return q;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
	
	public boolean isAnswerExistInDatabase(String id) {
		if (connect != null) {
			try {
				prep = connect
						.prepareStatement("select * from answers where id = "
								+ id + ";");
				result = prep.executeQuery();
				boolean exists = false;
				if (result.next()) {
					exists = true;
				}

				result.close();
				return exists;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public ArrayList<Answer> searchCodeSnippets(HashSet<String> keywords) {
		ArrayList<Answer> answers = new ArrayList<Answer>();
		
		if (connect != null) {
			try {
				// construct the query
				String query = "select * from answers";
				if(!keywords.isEmpty()) {
					query += " where";
					for(String keyword : keywords) {
						query += " body like \"%" + keyword + "%\" and";
					}
					query = query.substring(0, query.length() - 4);
				}
				query += ";";
				
				prep = connect.prepareStatement(query);
				result = prep.executeQuery();
				while(result.next()) {
					String id = result.getString("Id");
					String parentId = result.getString("ParentId");
					String body = result.getString("Body");
					String score = result.getString("Score");
					String isAccepted = result.getString("IsAccepted");
					String tags = result.getString("Tags");
					String viewCount = result.getString("ViewCount");
					Answer answer = new Answer(id, parentId, body, score, isAccepted, tags, viewCount);
					answers.add(answer);
				}
				
				result.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		return answers;
	}

	public void close() {
		try {
			if (result != null)
				result.close();
			if (statement != null)
				statement.close();
			if (prep != null)
				prep.close();
			if (connect != null)
				connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
