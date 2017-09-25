package edu.ucla.cs.so.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.io.Files;

import edu.ucla.cs.database.MySQLAccess;
import edu.ucla.cs.model.SOPost;

public class SOMetaDataAnalysis {
	public int total;
	public int accepted;
	public int total_cloned;
	public int accepted_cloned;
	public HashMap<Integer, Integer> cloned_scores;
	public HashMap<Integer, Integer> total_scores;

	public HashMap<String, Integer> total_tags;
	public HashMap<String, Integer> cloned_tags;
	
	public String path_to_all_posts;
	public String path_to_cloned_posts;
	
	public SOMetaDataAnalysis(String allPosts, String clonedPosts) {
		this.total = 0;
		this.accepted = 0;
		this.total_cloned = 0;
		this.accepted_cloned = 0;
		this.cloned_scores = new HashMap<Integer, Integer>();
		this.total_scores = new HashMap<Integer, Integer>();
		this.total_tags = new HashMap<String, Integer>();
		this.cloned_tags = new HashMap<String, Integer>();
		this.path_to_all_posts = allPosts;
		this.path_to_cloned_posts = clonedPosts;
	}
	
	public void analyze() {
		// load all SO snippets
		try (BufferedReader br = new BufferedReader(new FileReader(new File(path_to_all_posts)))) {
			String line = null;
			while((line = br.readLine()) != null) {
				if(line.equals("===UCLA@@@UCI===")) {
					this.total ++;
				} else if (line.startsWith("Accepted: ")) {
					String sub = line.substring(10);
					if(sub.equals("1")) {
						this.accepted ++; 
					}
				} else if (line.startsWith("Score: ")) {
					String sub = line.substring(7);
					Integer score = Integer.parseInt(sub);
					if(total_scores.containsKey(score)) {
						total_scores.put(score, total_scores.get(score) + 1);
					} else {
						total_scores.put(score, 1);
					}
				} else if (line.startsWith("Tags: ") && line.length() > 7) {
					String sub = line.substring(7, line.length() - 1);
					String[] tags = sub.split("><");
					for(String tag : tags) {
						if(total_tags.containsKey(tag)) {
							total_tags.put(tag, total_tags.get(tag) + 1);
						} else {
							total_tags.put(tag, 1);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// load all cloned SO snippets 
		try {
			List<String> posts = Files.readLines(new File(path_to_cloned_posts), Charset.defaultCharset());
			MySQLAccess access = new MySQLAccess();
			access.connect();
			for(String post : posts) {
				String id = post.split(",")[0];
				int num = Integer.parseInt(post.split(",")[1]);
				this.total_cloned += num;
				
				SOPost p = access.getPost(id);
				if(p != null) {
					if(p.isAccepted) accepted_cloned += num;
					
					if(cloned_scores.containsKey(p.score)) {
						cloned_scores.put(p.score, cloned_scores.get(p.score) + num);
					} else {
						cloned_scores.put(p.score, num);
					}
					
					for(String tag : p.tags) {
						if(cloned_tags.containsKey(tag)) {
							cloned_tags.put(tag, cloned_tags.get(tag) + num);
						} else {
							cloned_tags.put(tag, num);
						}
					}
				}
			}
			access.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		SOMetaDataAnalysis analyzer = new SOMetaDataAnalysis("/home/troy/research/Integration-Study/dataset/so-more-than-10-lines.txt", "/home/troy/research/Integration-Study/dataset/post-id-list.txt");
		analyzer.analyze();
		System.out.println("Total SO snippets: "  + analyzer.total);
		System.out.println("Accepted SO snippets: "  + analyzer.accepted);
		System.out.println("Total cloned SO snippets: "  + analyzer.total_cloned);
		System.out.println("Accepted cloned SO snippets: " + analyzer.accepted_cloned);
		LinkedList<Map.Entry<Integer, Integer>> sortedMap1 = new LinkedList<Map.Entry<Integer, Integer>>(analyzer.total_scores.entrySet());
		Collections.sort(sortedMap1, new Comparator<Map.Entry<Integer, Integer>>() {

			@Override
			public int compare(Entry<Integer, Integer> o1,
					Entry<Integer, Integer> o2) {
				if(o1.getKey() > o2.getKey()) {
					return 1;
				} else if (o1.getKey() < o2.getKey()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		
		System.out.println("Ranked Scores: ");
		for(Map.Entry<Integer, Integer> entry : sortedMap1) {
			int score = entry.getKey();
			double ratio;
			if(analyzer.cloned_scores.containsKey(score)) {
				ratio = ((double) analyzer.cloned_scores.get(score)) / entry.getValue();
			} else {
				ratio = 0;
			}
			
			System.out.println(entry.getKey() + "," + ratio * 100);
		}
		
		
		
		LinkedList<Map.Entry<String, Integer>> sortedMap2 = new LinkedList<Map.Entry<String,Integer>>(analyzer.cloned_tags.entrySet());
		Collections.sort(sortedMap2, new Comparator<Map.Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				if(o1.getValue() > o2.getValue()) {
					return -1;
				} else if (o1.getValue() < o2.getValue()) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		System.out.println("Tags ranked by cloning num:");
		for(Map.Entry<String, Integer> entry : sortedMap2) {
			String tag = entry.getKey();
			int num = entry.getValue();
			System.out.println(tag + "," + num);
		}

		// get the cloning ratio of SO snippets related to a tag
		HashMap<String, Double> clone_tag_ratios = new HashMap<String, Double>();
		for(String tag : analyzer.cloned_tags.keySet()) {
			if(analyzer.total_tags.containsKey(tag)) {
				int total = analyzer.total_tags.get(tag);
				if(total > 3) {
					clone_tag_ratios.put(tag, (double) analyzer.cloned_tags.get(tag) / total);
				}
			}
		}
		
		LinkedList<Map.Entry<String, Double>> sortedMap3 = new LinkedList<Map.Entry<String,Double>>(clone_tag_ratios.entrySet());
		Collections.sort(sortedMap3, new Comparator<Map.Entry<String, Double>>() {

			@Override
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				if(o1.getValue() > o2.getValue()) {
					return -1;
				} else if (o1.getValue() < o2.getValue()) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		System.out.println("Tags ranked by cloning ratio:");
		for(Map.Entry<String, Double> entry : sortedMap3) {
			String tag = entry.getKey();
			double ratio = entry.getValue();
			System.out.println(tag + "," + ratio * 100);
		}
		
		LinkedList<Map.Entry<String, Integer>> sortedMap4 = new LinkedList<Map.Entry<String,Integer>>(analyzer.total_tags.entrySet());
		Collections.sort(sortedMap4, new Comparator<Map.Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				if(o1.getValue() > o2.getValue()) {
					return -1;
				} else if (o1.getValue() < o2.getValue()) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		System.out.println("Tags ranked by total num:");
		for(Map.Entry<String, Integer> entry : sortedMap4) {
			String tag = entry.getKey();
			int num = entry.getValue();
			System.out.println(tag + "," + num);
		}
	}
}
