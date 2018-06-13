package edu.ucla.cs.so.mysql.utils;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.ucla.cs.so.data.model.Answer;
import edu.ucla.cs.so.data.model.Question;

public class MySQLAccessTest {
	static MySQLAccess access = null;

	@BeforeClass
	public static void setup() {
		access = new MySQLAccess();
		access.connect();
	}

	@Test
	public void testSelectEmpty() {
		Question q = access.selectQuestionPosts("1");
		assertNull(q);
	}

	@Test
	public void testSelectNonempty() {
		Question q = access.selectQuestionPosts("123");
		assertNotNull(q);
		assertEquals("183", q.accepted);
		assertEquals("<java><xml><csv><data-conversion>", q.tags);
		assertEquals("57044", q.view);
	}
	
	@Test
	public void testSearchWithOneKeyword() {
		HashSet<String> keywords = new HashSet<String>();
		keywords.add("createNewFile");
		ArrayList<Answer> answers = access.searchCodeSnippets(keywords);
		assertEquals(1330, answers.size());
	}
	
	@Test
	public void testSearchWithMultipleKeywords() {
		HashSet<String> keywords = new HashSet<String>();
		keywords.add("createNewFile");
		keywords.add("new File");
		ArrayList<Answer> answers = access.searchCodeSnippets(keywords);
		assertEquals(1168, answers.size());
	}

	@AfterClass
	public static void teardown() {
		access.close();
	}
}
