package edu.ucla.cs.so.stats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.ucla.cs.utils.MyFileUtils;

public class ClusterSizeEditPair {
	public static void main(String[] args) throws IOException {
		List<String> data = Files.readAllLines(Paths.get("/home/troy/research/Integration-Study/log/correlation_dist.csv"));
		String output = "/home/troy/research/Integration-Study/log/correlation_dist_clustered.csv";
		HashMap<ArrayList<Integer>, Integer> cluster = new HashMap<ArrayList<Integer>, Integer>();
		for(String s : data) {
			int size = Integer.parseInt(s.split(",")[0]);
			int edit = Integer.parseInt(s.split(",")[1]);
			if(size < 400 && edit < 150) {
				ArrayList<Integer> pair = new ArrayList<Integer>();
				//pair.add((size/10) * 10 + 5);
				//pair.add((edit/10) * 10 + 5);
				pair.add(size);
				pair.add(edit);
				if(cluster.containsKey(pair)) {
					cluster.put(pair, cluster.get(pair) + 1);
				} else {
					cluster.put(pair, 1);
				}
			}
		}
		
		MyFileUtils.appendStringToFile("size, edit, num" + System.lineSeparator(), output);
		for(ArrayList<Integer> pair : cluster.keySet()) {
			MyFileUtils.appendStringToFile(pair.get(0) + "," + pair.get(1) + "," + cluster.get(pair) + System.lineSeparator(), output);
		}
	}
}
