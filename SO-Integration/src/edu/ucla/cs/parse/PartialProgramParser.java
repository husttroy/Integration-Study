package edu.ucla.cs.parse;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class PartialProgramParser extends JavaParser{
	public int cutype;
	private int flag = 0;

	public ArrayList<String> extracMethod(String code)
			throws Exception {
ArrayList<String> methods = new ArrayList<String>();
		
		ASTParser parser = getASTParser(code);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		// System.out.println(cu);
		cutype = 0;
		if (((CompilationUnit) cu).types().isEmpty()) {
			flag = 1;
			cutype = 1;
			// no class header, try to parse
			String s1 = "public class sample{\n" + code + "\n}";
			parser = getASTParser(s1);
			try {
				cu = (CompilationUnit) parser.createAST(null);
			} catch(Exception e) {
				// parse error
				return new ArrayList<String>();
			}
			
			cu.accept(new ASTVisitor() {
				public boolean visit(MethodDeclaration node) {
					// find the method header
					flag = 2;
					return false;
				}
			});
			
			if (flag == 1) {
				// this code snippet has no method header nor class header
				s1 = "public class sample{\n public void foo(){\n" + code
						+ "\n}\n}";
				cutype = 2;
				parser = getASTParser(s1);
				try {
					cu = (CompilationUnit) parser.createAST(null);
				} catch(Exception e) {
					// parse error
					return new ArrayList<String>();
				}
				
				if(cu.toString().isEmpty() || cu.getProblems().length > 0) {
					// parse error
					return new ArrayList<String>();
				}
				
				methods.add("public void foo(){\n" + code + "\n}");
			} else if (flag == 2) {
				// this code snippet has at least one method but has no class header
				// extract methods from the snippet
				if(cu.getProblems().length > 0) {
					// parse error
					return new ArrayList<String>();
				}
			
				final String src = s1;
				final CompilationUnit cu2 = cu;
				cu.accept(new ASTVisitor() {
					@Override
					public boolean visit(MethodDeclaration node) {
						int startLine = cu2.getLineNumber(node
								.getStartPosition()) - 1;
						int endLine = cu2.getLineNumber(node.getStartPosition()
								+ node.getLength()) - 1;
						String s = "";
						String[] ss = src.split(System.lineSeparator());
						for (int i = startLine; i <= endLine; i++) {
							if(i == endLine) {
								s += ss[i];
							} else {
								s += ss[i] + System.lineSeparator();
							}
						}
						methods.add(s);
						return false;
					}
				});
			}
		} else {
			// this code snippet has both class header and method header
			cutype = 0;
			parser = getASTParser(code);
			try {
				cu = (CompilationUnit) parser.createAST(null);
			} catch(Exception e) {
				// parse error
				return new ArrayList<String>();
			}
			
			if(cu.getProblems().length > 0) {
				// parse error
				return new ArrayList<String>();
			}
			
			// extract methods from the snippet
			final String src = code;
			final CompilationUnit cu2 = cu;
			cu.accept(new ASTVisitor() {
				@Override
				public boolean visit(MethodDeclaration node) {
					int startLine = cu2.getLineNumber(node
							.getStartPosition()) - 1;
					int endLine = cu2.getLineNumber(node.getStartPosition()
							+ node.getLength()) - 1;
					String s = "";
					String[] ss = src.split(System.lineSeparator());
					for (int i = startLine; i <= endLine; i++) {
						if(i == endLine) {
							s += ss[i];
						} else {
							s += ss[i] + System.lineSeparator();
						}
					}
					methods.add(s);
					return false;
				}
			});
		}
		return methods;
	}
}
