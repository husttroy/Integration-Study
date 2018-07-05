package edu.ucla.cs.so.process;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class SourceCodeTokenizer {
	final static String[] separators = "; . [ ] ( ) ~ ! - + & * / % < > ^ | ? { } = # , \" \\ : $ ' ` @".split(" ");
	
	public static ArrayList<String> tokenize(String code) {
		String codeWithOutComments = CommentRemoveUtils.removeComments(code);
		for(String separator : separators) {
			codeWithOutComments = codeWithOutComments.replaceAll(Pattern.quote(separator), " ");
		}
		
		codeWithOutComments = codeWithOutComments.replaceAll("\n", "");
		codeWithOutComments = codeWithOutComments.replaceAll("\t", "");
		codeWithOutComments = codeWithOutComments.replaceAll("\r", "");
		
		String[] tokens = codeWithOutComments.split(" ");
		ArrayList<String> ts = new ArrayList<String>();
		for(String t : tokens) {
			if(!t.isEmpty()) {
				ts.add(t);
			}
		}
		
		return ts;
	}
	
	public static int CountTokens(String code) {
		return tokenize(code).size();
	}
}
