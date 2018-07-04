package edu.ucla.cs.so.process;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommentRemoveTest {
	@Test
	public void testSingleLineComment() {
		String code = "public void foo { // comment here \n"
				+ " int i = 0;\n}";
		String code2 = CommentRemoveUtils.removeComments(code);
		assertEquals(code2, "public void foo { \n int i = 0;\n}");
	}
	
	@Test
	public void testSingleLineComment2() {
		String code = "public void foo {\n // comment here \n"
				+ " int i = 0;\n}";
		String code2 = CommentRemoveUtils.removeComments(code);
		assertEquals(code2, "public void foo {\n \n int i = 0;\n}");
	}
	
	@Test
	public void testMultiLineComment() {
		String code = "/* abc */ public void foo {\n"
				+ " int i = 0;\n}";
		String code2 = CommentRemoveUtils.removeComments(code);
		assertEquals(code2, " public void foo {\n int i = 0;\n}");
	}
	
	@Test
	public void testMultiLineComment2() {
		String code = "/* abc \n def */\n public void foo {\n"
				+ " int i = 0;\n}";
		String code2 = CommentRemoveUtils.removeComments(code);
		assertEquals(code2, "\n\n public void foo {\n int i = 0;\n}");
	}
	
	@Test
	public void testMultiLineComment3() {
		String code = "public void foo {\n /* abc \n def */\n"
				+ " int i = 0;\n}";
		String code2 = CommentRemoveUtils.removeComments(code);
		assertEquals(code2, "public void foo {\n \n\n int i = 0;\n}");
	}
	
	@Test
	public void testSingleLineCommentSymbolInStringLiteral() {
		String code = "public void foo {\n"
				+ " int i = 0;\n"
				+ " System.out.println(\"//abc\");\n"
				+ "}";
		String code2 = CommentRemoveUtils.removeComments(code);
		assertEquals(code2, "public void foo {\n int i = 0;\n System.out.println(\"//abc\");\n}");
	}
	
	@Test
	public void testMultiLineCommentSymbolInStringLiteral() {
		String code = "public void foo {\n"
				+ " int i = 0;\n"
				+ " System.out.println(\"/*abc*/\");\n"
				+ "}";
		String code2 = CommentRemoveUtils.removeComments(code);
		assertEquals(code2, "public void foo {\n int i = 0;\n System.out.println(\"/*abc*/\");\n}");
	}
}
