package edu.ucla.cs.so.stats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import edu.ucla.cs.utils.MyFileUtils;

public class GenerateSizeDistCSV {
	public static void main(String[] args) throws IOException {
		List<String> data = Files.readAllLines(Paths.get("/home/troy/research/Integration-Study/log/size_dist_cluster.log"));
		String output = "/home/troy/research/Integration-Study/log/size_dist_clustered.csv";
		for(String s : data) {
			int size_num = Integer.parseInt(s.split(",")[0]);
			int edit_freq = Integer.parseInt(s.split(",")[1]);
			while(edit_freq > 0) {
				MyFileUtils.appendStringToFile(size_num + System.lineSeparator(), output);
				edit_freq --;
			}
		}
	}
}
