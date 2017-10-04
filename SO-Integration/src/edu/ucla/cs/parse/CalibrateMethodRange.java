package edu.ucla.cs.parse;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class CalibrateMethodRange {

	public Point calibrate(String code, int startLine, int endLine) {
		ASTParser parser = getASTParser(code);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		// in case of inner method declaration
		ArrayList<Point> ranges = new ArrayList<Point>();
		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodDeclaration node) {
				int start = cu.getLineNumber(node.getStartPosition());
				int end = cu.getLineNumber(node.getStartPosition() + node.getLength());
				if(start <= startLine && end >= endLine) {
					ranges.add(new Point(start, end));
				}
				return true;
			}
		});
		
		// figure out the closest method node
		Point theClosestRange = null;
		for(Point range : ranges) {
			if(theClosestRange == null) {
				theClosestRange = range;
			} else {
				if(theClosestRange.x < range.x) {
					theClosestRange = range;
				}
			}
		}
		
		return theClosestRange;
	}
	
	private ASTParser getASTParser(String sourceCode) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setStatementsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(sourceCode.toCharArray());
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
		parser.setCompilerOptions(options);
		return parser;
	}
}
