package edu.ucla.cs.so.process;

import java.util.ArrayList;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class JavaMethodExtractor extends ASTVisitor{
	
	CompilationUnit cu;
	String src;
	ArrayList<String> methods;
	
	public JavaMethodExtractor(String code, CompilationUnit cu) {
		this.src = code;
		this.cu = cu;
		this.methods = new ArrayList<String>();
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		int startLine = cu.getLineNumber(node
				.getStartPosition()) - 1;
		int endLine = cu.getLineNumber(node.getStartPosition()
				+ node.getLength()) - 1;
		
		// check whether there is a syntax error in this method
		IProblem[] errors = cu.getProblems();
		for(IProblem error : errors) {
			int line = error.getSourceLineNumber() - 1;
			if(line <= endLine && line >= startLine) {
				// there is a syntax error in this method
				return false;
			}
		}
		
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
}
