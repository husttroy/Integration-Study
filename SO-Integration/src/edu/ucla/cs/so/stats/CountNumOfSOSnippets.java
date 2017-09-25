package edu.ucla.cs.so.stats;

import edu.ucla.cs.database.MySQLAccess;

public class CountNumOfSOSnippets {
	public static void main(String[] args) {
		MySQLAccess access = new MySQLAccess();
		access.connect();
		System.out.println(access.countNumOfSOSnippets());
		access.close();
	}
}
