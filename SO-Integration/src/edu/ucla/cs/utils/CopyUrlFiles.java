package edu.ucla.cs.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class CopyUrlFiles {
	public static void main(String[] args) throws IOException {
		String srcPath = "/home/troy/research/Integration-Study/dataset/clone-codes";
//		String destPath = "/home/troy/research/Integration-Study/dataset/real-clones";
		String destPath = "/home/troy/research/Integration-Study/dataset/GH-files-with-so-links";

		File destDir = new File(destPath);
		for (File dir : destDir.listFiles()) {
			String dirName = dir.getName();
			String urlPath = srcPath + File.separator + dirName
					+ File.separator + "urls.txt";
			List<String> urls = FileUtils.readLines(new File(urlPath),
					Charset.defaultCharset());
			for (String fName : dir.list()) {
				if (fName.startsWith("gh")) {
					String fNameNoExtension = fName.substring(0, fName.indexOf("."));
					String startLineS = fNameNoExtension.split("-")[3];
					String endLineS = fNameNoExtension.split("-")[4];
					String range = "#L" + startLineS + "-L" + endLineS;
					for (String url : urls) {
						if (url.endsWith(range)) {
							MyFileUtils.appendStringToFile(
									url + System.lineSeparator(),
									dir.getAbsolutePath() + File.separator
											+ "gh-urls.txt");
						}
					}
				}
			}
		}
	}
}
