package edu.ucla.cs.so.stats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import edu.ucla.cs.utils.MyFileUtils;

public class FilterSizeEditPair {
	public static void main(String[] args) throws IOException {
		List<String> data = Files.readAllLines(Paths.get("/home/troy/research/Integration-Study/log/correlation_dist.csv"));
		String output = "/home/troy/research/Integration-Study/log/correlation_dist_short.csv";
		for(String s : data) {
			int size = Integer.parseInt(s.split(",")[0]);
			int edit = Integer.parseInt(s.split(",")[1]);
			if(size < 400 && edit < 150) {
				MyFileUtils.appendStringToFile(size + "," + edit + System.lineSeparator(), output);
			}
		}
	}
}
