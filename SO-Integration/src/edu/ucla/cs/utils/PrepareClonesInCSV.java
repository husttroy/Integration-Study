package edu.ucla.cs.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.github.gumtreediff.actions.model.Action;

import edu.ucla.cs.so.diff.GumTreeDiff;

public class PrepareClonesInCSV {
	public static void main(String[] args)
			throws UnsupportedOperationException, IOException {
		String path = "/home/troy/research/Integration-Study/dataset/clone-codes-screened/real-clones-reindex";
		String csvPath = "/home/troy/research/Integration-Study/log/real-clones2.csv";
		
		File csvFile = new File(csvPath);
		if(csvFile.exists()) {
			csvFile.delete();
		}

		File rootDir = new File(path);
		for (File dir : rootDir.listFiles()) {
			String groupId = dir.getName().split("-")[1];
			File soFile = null;
			ArrayList<File> carvedGHFiles = new ArrayList<File>();
			File urlFile = null;
			for (File file : dir.listFiles()) {
				String fName = file.getName();
				if (fName.startsWith("so-") && !fName.endsWith("~")) {
					soFile = file;
				} else if (fName.startsWith("carved-gh-")
						&& !fName.endsWith("~")) {
					carvedGHFiles.add(file);
				} else if (fName.equals("urls.txt")) {
					urlFile = file;
				}
			}

			if (soFile != null && !carvedGHFiles.isEmpty() && urlFile != null) {
				String postId = soFile.getName().split("-")[1];
				String link = "https://stackoverflow.com/questions/" + postId;
				for (File ghFile : carvedGHFiles) {
					String ghFileName = ghFile.getName();
					String ghFileNameNoExt = ghFileName.substring(0,
							ghFileName.indexOf("."));
					String startLineS = ghFileNameNoExt.split("-")[4];
					String endLineS = ghFileNameNoExt.split("-")[5];
					String range = "#L" + startLineS + "-L" + endLineS;
					List<String> urls = FileUtils.readLines(urlFile,
							Charset.defaultCharset());
					String url = null;
					for (String s : urls) {
						if (s.endsWith(range)) {
							url = s.substring(s.indexOf("https"));
						}
					}
					GumTreeDiff differ = new GumTreeDiff(
							soFile.getAbsolutePath(), ghFile.getAbsolutePath());
					List<Action> edits = differ.diff();
					MyFileUtils.appendStringToFile(groupId + "," + link + ","
							+ url + "," + edits.size() + System.lineSeparator(), csvPath);
				}
			}
		}
	}
}
