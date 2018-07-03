package edu.ucla.cs.so.process;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class PartialProgramParser {
	public int cutype;
	private int flag = 0;

	public ArrayList<String> extractMethod(String code)
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
				
				// If there is any syntax error in a method body, GumTree will not be able to generate the AST node of the method 
				if(cu.toString().isEmpty() || cu.getProblems().length > 0) {
					return new ArrayList<String>();
				}
				
				methods.add("public void foo(){\n" + code + "\n}");
			} else if (flag == 2) {
				// this code snippet has at least one method but has no class header
				// extract methods from the snippet
				
				// tolerate SO snippets with some syntax errors 
				if(cu.getProblems().length > 10) {
					// if there are more than 10 syntax errors, this is unlikely to be a valid Java snippet
					return new ArrayList<String>();
				}
			
				JavaMethodExtractor extractor = new JavaMethodExtractor(s1, cu);
				cu.accept(extractor);
				methods.addAll(extractor.methods);
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
			
			// [July 3] decide to tolerate SO snippets with some syntax errors if not many (use 10 as the threshold
			if(cu.getProblems().length > 10) {
				// if there are more than 10 syntax errors, this is unlikely to be a valid Java snippet
				return new ArrayList<String>();
			}
			
			// extract methods from the snippet
			JavaMethodExtractor extractor = new JavaMethodExtractor(code, cu);
			cu.accept(extractor);
			methods.addAll(extractor.methods);
		}
		return methods;
	}

	private ASTParser getASTParser(String sourceCode) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setStatementsRecovery(false);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(sourceCode.toCharArray());
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
		parser.setCompilerOptions(options);
		return parser;
	}
}
