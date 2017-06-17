package edu.ucla.cs.so.process;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;

public class SOProcess {
	final String url = "jdbc:mysql://localhost:3306/stackoverflow";
	final String username = "root";
	final String password = "5887526";
	final String query = "select * from answers;";
	String table;
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
	
	/**
	 * 1. Get all Java snippets in the database
	 * 2. Compile each snippet 
	 * 3. Process a snippet to a set of methods
	 * 		a. if the snippet is an individual method, do nothing
	 *      b. if the snippet is a list of statements, add a method header
	 *      c. if the snippet is a class or a list of methods, truncate it to individual methods
	 * 4. Output the processed results (i.e., a set of methods) to the given text file. 
	 *    Each method is associated with the post id and its method id (starting from 0).    
	 */
	public void processAll(String output) {
		if (connect != null) {
			try {
				prep = connect.prepareStatement(query);
				result = prep.executeQuery();
				while(result.next()) {
					// get the metadata
					String id = result.getString("Id");
					String score = result.getString("Score");
					String isAccepted = result.getString("IsAccepted");
					String viewCount = result.getString("ViewCount");
					
					// get the snippet
					String body = result.getString("Body");
					ArrayList<String> snippets = getCode(body);
					for(String snippet: snippets) {
						snippet = StringEscapeUtils.unescapeHtml4(snippet);
						int len = snippet.split(System.lineSeparator()).length;
						if(len > 1) {
							PartialProgramParser parser = new PartialProgramParser();
							try {
								ArrayList<String> methods = parser.extracMethod(snippet);
								// print methods to the output file
								for(int i = 0; i < methods.size(); i++) {
									String method = methods.get(i);
									int len2 = method.split(System.lineSeparator()).length; 
									if(len2 >= 10 || len2 < 4) {
										// only consider methods with less than 10 lines of code
										continue;
									}
									String s = "===" + System.lineSeparator();
									s += "PostId: " + id + System.lineSeparator();
									s += "Score: " + score + System.lineSeparator();
									s += "Accepted: " + isAccepted + System.lineSeparator();
									s += "ViewCount: " + viewCount + System.lineSeparator();
									s += "MethodId: " + i + System.lineSeparator();
									s += method + System.lineSeparator();
									FileUtils.appendStringToFile(s, output);
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
	
	public ArrayList<String> getCode(String body) {
		ArrayList<String> codes = new ArrayList<>();
		String start = "<code>", end = "</code>";
		int s = 0;
		while (true) {
			s = body.indexOf(start, s);
			if (s == -1)
				break;
			s += start.length();
			int e = body.indexOf(end, s);
			if (e == -1)
				break;
			codes.add(body.substring(s, e).trim());
			s = e + end.length();
		}
		return codes;
	}
	
	public static void main(String[] args) {
		SOProcess p = new SOProcess();
		p.connect();
		p.processAll("./so-less-than-10-lines.txt");
		p.close();
	}
}
