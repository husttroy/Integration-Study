package edu.ucla.cs.utils;

import java.io.File;

public class RealCloneFinder {
	public static void main(String[] args) {
		String path = "/home/troy/research/Integration-Study/dataset/clone-codes";
		
		File rootDir = new File(path);
		for(File dir : rootDir.listFiles()) {
			String dirName = dir.getName();
			if(dirName.startsWith("so-")) {
				for(File file : dir.listFiles()) {
					String fileName = file.getName();
					if(!fileName.equals("gh-urls.txt") && fileName.startsWith("gh-")) {
						String code = FileUtils.readFileToString(file.getAbsolutePath());
						if(code.contains("stackoverflow.com")) {
							System.out.println(file.getAbsolutePath());
							String[] ss = code.split(System.lineSeparator());
							for(String s : ss) {
								if(s.contains("stackoverflow.com")) {
									System.out.println(s);
								}
							}
							System.out.println("===");
						}
					}
				}
			}
		}
	}
}
