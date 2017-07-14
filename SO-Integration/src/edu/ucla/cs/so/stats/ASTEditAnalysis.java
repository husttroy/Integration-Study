package edu.ucla.cs.so.stats;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;

import edu.ucla.cs.so.diff.GumTreeDiff;

public class ASTEditAnalysis {
	String root;
	int cloneGroups;
	long totalEdits;
	HashMap<String, Integer> cluster;
	
	public ASTEditAnalysis(String dir) {
		this.root = dir;
		this.cloneGroups = 0;
		this.totalEdits = 0;
		this.cluster = new HashMap<String, Integer>();
	}
	
	public void process() {
		// traverse all clone groups in the root folder
		File rootDir = new File(this.root);
		if(!rootDir.exists() || !rootDir.isDirectory()) {
			System.err.println("The specified folder " + root + " does not exist or is not a folder.");
		}
		
		for(File f : rootDir.listFiles()) {
			if(f.isDirectory() && f.getName().startsWith("so-")) {
				File so_file = null;
				ArrayList<File> gh_files = new ArrayList<File>();
				for(File f2 : f.listFiles()) {
					if(f2.getName().startsWith("so-")) {
						so_file = f2;
					} else if (f2.getName().startsWith("carved-gh-")) {
						gh_files.add(f2);
					}
				}
				
				if(so_file != null && !gh_files.isEmpty()) {
					this.cloneGroups ++;
					for (File gh_file : gh_files) {
						GumTreeDiff differ = new GumTreeDiff(so_file.getAbsolutePath(), gh_file.getAbsolutePath());
						try {
							List<Action> edits = differ.diff();
							this.totalEdits += edits.size();
							
							// cluster these AST edits based on their operations and the types of AST nodes they operate on
							for(Action edit : edits) {
								String s = null;
								if(edit instanceof Insert) {
									Insert ins = (Insert)edit;
									ITree node = ins.getNode();
									String label = node.toPrettyString(differ.dstContext); 
									if(label.contains(":")) {
										s = "INS:" + label.substring(0, label.indexOf(':'));
									} else {
										s = "INS:" + label;
									}
								} else if (edit instanceof Delete) {
									Delete del = (Delete)edit;
									ITree node = del.getNode();
									String label = node.toPrettyString(differ.srcContext);
									if(label.contains(":")) {
										s = "DEL:" + label.substring(0, label.indexOf(':'));
									} else {
										s = "DEL:" + label;
									}
								} else if (edit instanceof Move) {
									Move mov = (Move)edit;
									ITree node = mov.getNode();
									String label = node.toPrettyString(differ.srcContext);
									if(label.contains(":")) {
										s = "MOV:" + label.substring(0, label.indexOf(':'));
									} else {
										s = "MOV:" + label;
									}
									
								} else if (edit instanceof Update) {
									Update upd = (Update)edit;
									ITree node = upd.getNode();
									String label = node.toPrettyString(differ.srcContext);
									if(label.contains(":")) {
										s = "UPD:" + label.substring(0, label.indexOf(':'));
									} else {
										s = "UPD:" + label;
									}
								}
								if(s != null) {
									if(cluster.containsKey(s)) {
										cluster.put(s, cluster.get(s) + 1);
									} else {
										cluster.put(s, 1);
									}
								}
							}
						} catch (UnsupportedOperationException | IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		ASTEditAnalysis counter = new ASTEditAnalysis("/home/troy/research/Integration-Study/sample");
		counter.process();
		double ave_edits = counter.totalEdits / (double) counter.cloneGroups;
		System.out.println("Total groups: " + counter.cloneGroups);
		System.out.println("Total edits: " + counter.totalEdits);
		System.out.println("Average edits: " + ave_edits);
		// sort the cluster by value in a descending order
		LinkedList<Map.Entry<String, Integer>> sortedMap = new LinkedList<Map.Entry<String, Integer>>(counter.cluster.entrySet());
		Collections.sort(sortedMap, new Comparator<Map.Entry<String, Integer>>() {

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
		for(Map.Entry<String, Integer> entry : sortedMap) {
			System.out.println(entry.getKey() + " occurs in " + entry.getValue() + " times");
		}
	}
}
