package edu.ucla.cs.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

public class RealCloneFinder {
	public static void main(String[] args) throws IOException {
		String path = "/home/troy/research/Integration-Study/dataset/clone-codes";
		String target = "/home/troy/research/Integration-Study/dataset/GH-files-with-so-links";
		
		File rootDir = new File(path);
		for(File dir : rootDir.listFiles()) {
			String dirName = dir.getName();
			if(dirName.startsWith("so-")) {
				ArrayList<File> filesWithSOLinks = new ArrayList<File>();
				File soFile = null;
				for(File file : dir.listFiles()) {
					String fileName = file.getName();
					if(!fileName.equals("gh-urls.txt") && fileName.startsWith("gh-")) {
						String code = MyFileUtils.readFileToString(file.getAbsolutePath());
						if(code.contains("stackoverflow.com")) {
							filesWithSOLinks.add(file);
							System.out.println(file.getAbsolutePath());
							String[] ss = code.split(System.lineSeparator());
							for(String s : ss) {
								if(s.contains("stackoverflow.com")) {
									System.out.println(s);
								}
							}
							
							System.out.println("===");
						}
					} else if (fileName.startsWith("so-")){
						soFile = file;
					}
				}
				
				if(!filesWithSOLinks.isEmpty()) {
					// create a new folder with the same group index in the target directory
					File destDir = new File(target + File.separator + dirName);
					if(!destDir.exists()) {
						destDir.mkdir();
					}
					
					// copy the stack overflow file
					if(soFile != null) {
						FileUtils.copyFileToDirectory(soFile, destDir);
					}
					
					// only copy the GitHub file that contains the SO link and the so snippet to the target directory
					for(File file : filesWithSOLinks) {
						FileUtils.copyFileToDirectory(file, destDir);
					}
				}
			}
		}
	}
}
