package edu.ucla.cs.utils;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import edu.ucla.cs.parse.CalibrateMethodRange;

/**
 * This program is the replacement of the preprocess shell script. Instead of just 
 * renaming files and extracting GitHub clones based on line numbers, this program
 * also calibrates the line numbers of the GitHub clones and includes the leading 
 * comments as well as Java annotations in the code.
 * 
 * @author Tianyi Zhang
 *
 */
public class Preprocess {
	public static void main(String[] args) throws IOException {
		String path = "/home/troy/research/Integration-Study/dataset/clone-codes-new-sample";
		
		// load the post ids in the set of real clones we found previously
		// when preprocessing this new set of clones, do not include the previously found clones
//		String path2 = "/home/troy/research/Integration-Study/dataset/real-clones";
//		File dir2 = new File(path2);
//		ArrayList<String> excludePostIds = new ArrayList<String>();
//		for(File dir : dir2.listFiles()) {
//			excludePostIds.add(dir.getName().substring(3, dir.getName().lastIndexOf('-')));
//		}

		File rootDir = new File(path);
		int count = 0;
		for (File dir : rootDir.listFiles()) {
			String dirName = dir.getName();
			if (dirName.startsWith("so-")) {
				String pid_mid = dirName.substring(3);
//				String pid = pid_mid.substring(0, pid_mid.indexOf("-"));
//				if(excludePostIds.contains(pid)) {
//					FileUtils.deleteDirectory(dir);
//					continue;
//				}
				
				// find the url file first since we will update it later
				File urlFile = new File(dir.getAbsolutePath() + File.separator + "urls.txt");
				if(!urlFile.exists()) {
					urlFile = new File(dir.getAbsolutePath() + File.separator + "gh-urls.txt");
				}
				// read urls
				HashMap<Integer, String> urls = MyFileUtils.getCloneURL(urlFile);
				HashMap<Integer, String> new_urls = new HashMap<Integer, String>();
				
				for (File file : dir.listFiles()) {
					String fileName = file.getName();
					if (fileName.equals("urls.txt") || fileName.equals("gh-urls.txt")) {
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
						String ghIndex = ss[1]; // ss[0] is 'gh', ss[1] is the index of the GitHub clone
						String ghCount = ss[2]; // ss[2] is the number of the occurrence of the same GitHub clone
						String initial_startLine = ss[3];
						String initial_endLine = ss[4];
						int startLine = Integer.parseInt(initial_startLine);
						int endLine = Integer.parseInt(initial_endLine);
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

						File carvedFile = new File(dir.getAbsolutePath()
								+ File.separator
								+ "carved-gh-"
								+ ghIndex + "-" 
								+ ghCount + "-"
								+ startLine + "-"
								+ endLine
								+ ".java");
						if (!carvedFile.exists()) {
							carvedFile.createNewFile();
						}
						MyFileUtils.writeStringToFile(sb.toString(),
								carvedFile.getAbsolutePath());
						
						// rename the original github file based on re-calibrated method line numbers
						file.renameTo(new File(dir.getAbsolutePath() + File.separator
								+ "gh-" + ghIndex + "-" + ghCount + "-" + startLine + "-" + endLine + ".txt"));
						// also update the url in the urls.txt
						int gh_index = Integer.parseInt(ghIndex);
						String url = urls.get(gh_index);
						if(url != null) {
							String new_url = url.substring(0, url.indexOf("#L")) + "#L" + startLine + "-L" + endLine;
							new_urls.put(gh_index, new_url);
						}
						
					}
				}
				
				// rewrite the url file
				String content = FileUtils.readFileToString(urlFile, Charset.defaultCharset());
				for(Integer gh_index : urls.keySet()) {
					String url = urls.get(gh_index);
					String new_url = new_urls.get(gh_index);
					content = content.replace(gh_index + "\t" + url, gh_index + "\t" + new_url);
				}
				FileUtils.writeStringToFile(urlFile, content, Charset.defaultCharset());
				
				
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
