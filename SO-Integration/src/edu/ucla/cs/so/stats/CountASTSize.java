package edu.ucla.cs.so.stats;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import edu.ucla.cs.utils.FileUtils;

public class CountASTSize {
	final String csv_file = "/home/troy/research/Integration-Study/log/size_dist.csv";
	
	String root;
	int totalSnippets;
	int totalNodes;
	HashMap<Integer, Integer> cluster;

	public CountASTSize(String dir) {
		this.root = dir;
		this.totalSnippets = 0;
		this.totalNodes = 0;
		this.cluster = new HashMap<Integer, Integer>();
	}

	public void process() {
		// traverse all clone groups in the root folder
		File rootDir = new File(this.root);
		if (!rootDir.exists() || !rootDir.isDirectory()) {
			System.err.println("The specified folder " + root
					+ " does not exist or is not a folder.");
		}

		for (File f : rootDir.listFiles()) {
			if (f.isDirectory() && f.getName().startsWith("so-")) {
				File so_file = null;
				for(File f2 : f.listFiles()) {
					if(f2.getName().startsWith("so-")) {
						so_file = f2;
					}
				}
				
				if(so_file != null) {
					String so_name = so_file.getName();
					String temp = so_name.substring(0, so_name.lastIndexOf(".java"));
					String[] ss = temp.split("-");
					int so_num = Integer.parseInt(ss[3]);
					totalSnippets += so_num;
					try {
						Run.initGenerators();
						TreeContext tc = Generators.getInstance().getTree(so_file.getAbsolutePath());
						ITree node = tc.getRoot();
						int size = countNumOfChildren(node) + 1;
						
						// output the number of AST nodes to a csv file
						for(int i = 0; i < so_num; i++) {
							FileUtils.appendStringToFile(size + System.lineSeparator(), csv_file);
						}
						
						totalNodes += so_num * size;
						if(cluster.containsKey(size)) {
							cluster.put(size, cluster.get(size) + so_num);
						} else {
							cluster.put(size, so_num);
						}
					} catch (UnsupportedOperationException | IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static int countNumOfChildren(ITree node) {
		int num = 0;
		for(ITree child : node.getChildren()) {
			num += 1 + countNumOfChildren(child);
		}
		return num;
	}

	public static void main(String[] args) {
		CountASTSize counter = new CountASTSize("/home/troy/research/Integration-Study/dataset/clone-groups");
		counter.process();
		System.out.println("Total SO snippets:" + counter.totalSnippets);
		System.out.println("Average AST nodes:" + counter.totalNodes/counter.totalSnippets);
		LinkedList<Map.Entry<Integer, Integer>> sortedMap1 = new LinkedList<Map.Entry<Integer, Integer>>(counter.cluster.entrySet());
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
		
		for(Map.Entry<Integer, Integer> entry : sortedMap1) {
			System.out.println(entry.getKey() + "," + entry.getValue());
		}
	}
}
