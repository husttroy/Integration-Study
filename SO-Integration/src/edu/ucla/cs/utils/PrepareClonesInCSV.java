package edu.ucla.cs.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.github.gumtreediff.actions.model.Action;

import edu.ucla.cs.so.diff.GumTreeDiff;

public class PrepareClonesInCSV {
	public static void main(String[] args)
			throws UnsupportedOperationException, IOException {
		String path = "/home/troy/research/Integration-Study/dataset/clone-codes-new-sample";
		String csvPath = "manual-inspection/scc-sample.csv";
		
		File csvFile = new File(csvPath);
		if(csvFile.exists()) {
			csvFile.delete();
		}
		
		GumTreeDiff.init();

		File rootDir = new File(path);
		for (File dir : rootDir.listFiles()) {
			String groupId = dir.getName().split("-")[1];
			File soFile = null;
			ArrayList<File> carvedGHFiles = new ArrayList<File>();
			File urlFile = null;
			for (File file : dir.listFiles()) {
				String fName = file.getName();
				if(fName.endsWith("~")) {
					continue;
				} else if (fName.startsWith("so-") && fName.endsWith(".java")) {
					soFile = file;
				} else if (fName.startsWith("carved-gh-") && fName.endsWith(".java")) {
					carvedGHFiles.add(file);
				} else if (fName.equals("urls.txt") || fName.equals("gh-urls.txt")) {
					urlFile = file;
				}
			}

			if (soFile != null && !carvedGHFiles.isEmpty() && urlFile != null) {
				String postId = soFile.getName().split("-")[1];
				String link = "https://stackoverflow.com/questions/" + postId;
				HashMap<Integer, String> urls = MyFileUtils.getCloneURL(urlFile);
				for (File ghFile : carvedGHFiles) {
					String ghFileName = ghFile.getName();
					int ghIndex = Integer.parseInt(ghFileName.split("-")[2]);
					String url =  urls.get(ghIndex);
					if(url != null) {
						// ignore those that do not have urls
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
}
