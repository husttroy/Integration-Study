package edu.ucla.cs.utils;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import edu.ucla.cs.parse.CalibrateMethodRange;

public class Preprocess {
	public static void main(String[] args) throws IOException {
		String path = "/home/troy/research/Integration-Study/dataset/clone-codes-screened/real-clones-reindex";
		
		// load the post ids in the set of real clones we found previously
		// when preprocessing this new set of clones, do not include the previously found clones
//		String path2 = "/home/troy/research/Integration-Study/dataset/real-clones";
//		File dir2 = new File(path2);
//		ArrayList<String> excludePostIds = new ArrayList<String>();
//		for(File dir : dir2.listFiles()) {
//			excludePostIds.add(dir.getName().substring(3, dir.getName().lastIndexOf('-')));
//		}

		File rootDir = new File(path);
		int count = 83;
		for (File dir : rootDir.listFiles()) {
			String dirName = dir.getName();
			if (dirName.startsWith("so-")) {
				String pid_mid = dirName.substring(3);
				String pid = pid_mid.substring(0, pid_mid.indexOf("-"));
//				if(excludePostIds.contains(pid)) {
//					FileUtils.deleteDirectory(dir);
//					continue;
//				}
				for (File file : dir.listFiles()) {
					String fileName = file.getName();
					if (fileName.equals("gh-urls.txt")) {
						continue;
					} else if (fileName.startsWith("so-")) {
						String occurence = fileName.substring(3);
						occurence = occurence.substring(0,
								occurence.indexOf("."));
						String newName = "so-" + pid_mid + "-" + occurence
								+ ".java";
						String newPath = file.getParentFile().getAbsolutePath()
								+ File.separator + newName;
						// rename the stack overflow snippet
						file.renameTo(new File(newPath));

						// add dummy class header to the file
						addClassHeader(newPath);
					} else if (fileName.startsWith("gh-")) {
						String[] ss = fileName.substring(0,
								fileName.indexOf(".txt")).split("-");
						int startLine = Integer.parseInt(ss[3]);
						int endLine = Integer.parseInt(ss[4]);
						String code = MyFileUtils.readFileToString(file
								.getAbsolutePath());
						
						// calibrate the start and end line
						CalibrateMethodRange cmr = new CalibrateMethodRange();
						Point range = cmr.calibrate(code, startLine, endLine);
						if(range != null) {
							startLine = range.x;
							endLine = range.y;
						}
						
						String[] lines = code.split(System.lineSeparator());
						StringBuilder sb = new StringBuilder();
						sb.append("public class foo{" + System.lineSeparator());
						if (lines[startLine - 2].trim().startsWith("@")) {
							sb.append(lines[startLine - 2]
									+ System.lineSeparator());
						}
						for (int i = startLine - 1; i < endLine && i < lines.length; i++) {
							sb.append(lines[i] + System.lineSeparator());
						}

						sb.append("}");

						File carvedFile = new File(file.getParentFile()
								.getAbsolutePath()
								+ File.separator
								+ "carved-"
								+ fileName.substring(0, fileName.indexOf("."))
								+ ".java");
						if (!carvedFile.exists()) {
							carvedFile.createNewFile();
						}
						MyFileUtils.writeStringToFile(sb.toString(),
								carvedFile.getAbsolutePath());
					}
				}

				// rename the name of the folder to "so-$count"
				dir.renameTo(new File(dir.getParentFile().getAbsolutePath()
						+ File.separator + "so-" + count));
				count++;
			}
		}
	}

	public static void addClassHeader(String path) {
		String s = MyFileUtils.readFileToString(path);
		s = "public class foo {" + System.lineSeparator() + s;
		s += "}";
		MyFileUtils.writeStringToFile(s, path);
	}
}
