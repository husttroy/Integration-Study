package edu.ucla.cs.so.process;

import java.util.ArrayList;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.Test;

public class PartialProgramParserTest {
	@Test
	public void test() {
		// this is the case where no parsing error is too restrict and we miss some interesting methods
		String code = FileUtils.readFileToString("./test/edu/ucla/cs/so/process/snippet-9288544.txt");
		code = StringEscapeUtils.unescapeHtml4(code);
		PartialProgramParser parser = new PartialProgramParser();
		try {
			ArrayList<String> methods = parser.extractMethod(code);
			for(String m : methods) {
				System.out.println(m);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
