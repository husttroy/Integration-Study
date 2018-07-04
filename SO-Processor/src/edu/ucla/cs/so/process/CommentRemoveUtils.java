package edu.ucla.cs.so.process;

import java.util.Scanner;

public class CommentRemoveUtils {
	enum State { outsideComment, insideLineComment, insideblockComment, insideblockComment_noNewLineYet, insideString };

	/**
	 * Build an automaton to remove comments
	 * @param code
	 * @return
	 */
	public static String removeComments(String code) {
	  State state = State.outsideComment;
	  StringBuilder result = new StringBuilder();
	  Scanner s = new Scanner(code);
	  s.useDelimiter("");
	  while (s.hasNext()) {
	    String c = s.next();
	    switch (state) {
	      case outsideComment:
	        if (c.equals("/") && s.hasNext()) {
	          String c2 = s.next();
	          if (c2.equals("/"))
	            state = State.insideLineComment;
	          else if (c2.equals("*")) {
	            state = State.insideblockComment_noNewLineYet;
	          } else {
	            result.append(c).append(c2);
	          }
	        } else {
	          result.append(c);
	          if (c.equals("\"")) {
	            state = State.insideString;
	          }
	        }
	        break;
	      case insideString:
	        result.append(c);
	        if (c.equals("\"")) {
	          state = State.outsideComment;
	        } else if (c.equals("\\") && s.hasNext()) {
	          result.append(s.next());
	        }
	        break;
	      case insideLineComment:
	        if (c.equals("\n")) {
	          state = State.outsideComment;
	          result.append("\n");
	        }
	        break;
	      case insideblockComment_noNewLineYet:
	        if (c.equals("\n")) {
	          result.append("\n");
	          state = State.insideblockComment;
	        }
	      case insideblockComment:
	        while (c.equals("*") && s.hasNext()) {
	          String c2 = s.next();
	          if (c2.equals("/")) {
	            state = State.outsideComment;
	            break;
	          }
	        }
	    }
	  }
	  s.close();
	  return result.toString();
	}
}
