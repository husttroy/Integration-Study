package edu.ucla.cs.so.stats;

import java.io.File;

import edu.ucla.cs.utils.MyFileUtils;

public class GeneratePostLinkCSV {
	
	public void generateLinks(String input, String output) {
		String links = "";
		File dir = new File(input);
		for(File f : dir.listFiles()) {
			String fname = f.getName();
			if(!fname.startsWith("so")) {
				continue;
			}			
			String tmp = fname.split("-")[1];
			int id = Integer.parseInt(tmp);
			for(File f2 : f.listFiles()) {
				String fname2 = f2.getName();
				if(fname2.startsWith("so")) {
					String tmp2 = fname2.split("-")[1];
					String tmp3 = fname2.split("-")[2];
					int postId = Integer.parseInt(tmp2);
					int num = Integer.parseInt(tmp3);
					links += id + ",https://stackoverflow.com/questions/" + postId + "," + num + System.lineSeparator();
				}
			}
		}
		
		MyFileUtils.writeStringToFile(links, output);
	}
	
	public static void main(String[] args) {
		String input = "/home/troy/research/Integration-Study/dataset/sample";
		String output = "/home/troy/research/Integration-Study/log/links.csv";
		GeneratePostLinkCSV g = new GeneratePostLinkCSV();
		g.generateLinks(input, output);
	}
}
 