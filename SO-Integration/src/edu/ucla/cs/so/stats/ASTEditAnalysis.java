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
import edu.ucla.cs.utils.MyFileUtils;

public class ASTEditAnalysis {
	final String edit_csv_file = "/home/troy/research/Integration-Study/log/edit_dist.csv";
	final String corr_csv_file = "/home/troy/research/Integration-Study/log/correlation_dist.csv";
	final String insert_csv_file = "/home/troy/research/Integration-Study/log/insert_edit_dist.csv";
	final String delete_csv_file = "/home/troy/research/Integration-Study/log/delete_edit_dist.csv";
	final String move_csv_file = "/home/troy/research/Integration-Study/log/move_edit_dist.csv";
	final String update_csv_file = "/home/troy/research/Integration-Study/log/update_edit_dist.csv";
	
	String root;
	int clonePairs;
	long totalEdits;
	long totalInsert;
	long totalDelete;
	long totalUpdate;
	long totalMove;
	HashMap<String, Integer> types; // cluster edits based on edit types
	HashMap<Integer, Integer> efforts; // cluster clone groups based on the number of edits
	
	public ASTEditAnalysis(String dir) {
		this.root = dir;
		this.clonePairs = 0;
		this.totalEdits = 0;
		this.totalInsert = 0;
		this.totalDelete = 0;
		this.totalUpdate = 0;
		this.totalMove = 0;
		this.types = new HashMap<String, Integer>();
		this.efforts = new HashMap<Integer, Integer>();
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
					// get the number of exact clones for this so snippet
					String so_name = so_file.getName();
					String temp = so_name.substring(0, so_name.lastIndexOf(".java"));
					String[] ss = temp.split("-");
					int so_num = Integer.parseInt(ss[3]);
					for (File gh_file : gh_files) {
						// similarly, get the number of exact clones for this github fragment
						String gh_name = gh_file.getName();
						String temp2 = gh_name.substring(0, gh_name.lastIndexOf(".java"));
						String[] ss2 = temp2.split("-");
						int gh_num = Integer.parseInt(ss2[3]);
						
						this.clonePairs += gh_num * so_num;
						GumTreeDiff differ = new GumTreeDiff(so_file.getAbsolutePath(), gh_file.getAbsolutePath());
						try {
							List<Action> edits = differ.diff();
							
							// output the number of edits to a csv file
							for(int i = 0; i < gh_num * so_num; i++) {
								MyFileUtils.appendStringToFile(edits.size() + System.lineSeparator(), edit_csv_file);
							}
							
							// output the size of the SO snippit and the number of edits to a csv file
							int size = CountASTSize.countNumOfChildren(differ.srcTree) + 1;
							for(int i = 0; i < gh_num * so_num; i++) {
								MyFileUtils.appendStringToFile(size + "," + edits.size() + System.lineSeparator(), corr_csv_file);
							}
							
							// cluster clone groups based on the number of edits
							int n = edits.size() / 10;
							if(efforts.containsKey(n)) {
								efforts.put(n, efforts.get(n) + gh_num * so_num);
							} else {
								efforts.put(n, gh_num * so_num);
							}
							
							this.totalEdits += gh_num * so_num * edits.size();
							
							// cluster these AST edits based on their operations and the types of AST nodes they operate on
							int localInsert = 0;
							int localDelete = 0;
							int localMove = 0;
							int localUpdate = 0;
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
									this.totalInsert += gh_num * so_num;
									localInsert += gh_num * so_num;
								} else if (edit instanceof Delete) {
									Delete del = (Delete)edit;
									ITree node = del.getNode();
									String label = node.toPrettyString(differ.srcContext);
									if(label.contains(":")) {
										s = "DEL:" + label.substring(0, label.indexOf(':'));
									} else {
										s = "DEL:" + label;
									}
									this.totalDelete += gh_num * so_num;
									localDelete += gh_num * so_num;
								} else if (edit instanceof Move) {
									Move mov = (Move)edit;
									ITree node = mov.getNode();
									String label = node.toPrettyString(differ.srcContext);
									if(label.contains(":")) {
										s = "MOV:" + label.substring(0, label.indexOf(':'));
									} else {
										s = "MOV:" + label;
									}
									this.totalMove += gh_num * so_num;
									localMove += gh_num * so_num;
								} else if (edit instanceof Update) {
									Update upd = (Update)edit;
									ITree node = upd.getNode();
									String label = node.toPrettyString(differ.srcContext);
									if(label.contains(":")) {
										s = "UPD:" + label.substring(0, label.indexOf(':'));
									} else {
										s = "UPD:" + label;
									}
									this.totalUpdate += gh_num * so_num;
									localUpdate += gh_num * so_num;
								}
								if(s != null) {
									if(types.containsKey(s)) {
										types.put(s, types.get(s) + gh_num * so_num);
									} else {
										types.put(s, gh_num * so_num);
									}
								}
							}
							
							// output the edit number to different csv files based on action types
							for(int i = 0; i < gh_num * so_num; i++) {
								MyFileUtils.appendStringToFile(localInsert + System.lineSeparator(), insert_csv_file);
								MyFileUtils.appendStringToFile(localDelete + System.lineSeparator(), delete_csv_file);
								MyFileUtils.appendStringToFile(localMove + System.lineSeparator(), move_csv_file);
								MyFileUtils.appendStringToFile(localUpdate + System.lineSeparator(), update_csv_file);
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
		ASTEditAnalysis counter = new ASTEditAnalysis("/home/troy/research/Integration-Study/dataset/clone-groups");
		counter.process();
		double ave_edits = counter.totalEdits / (double) counter.clonePairs;
		System.out.println("Total pairs: " + counter.clonePairs);
		System.out.println("Total edits: " + counter.totalEdits);
		System.out.println("Average edits: " + ave_edits);
		System.out.println("Total insertion: " + counter.totalInsert);
		System.out.println("Average insertion: " + counter.totalInsert / (double) counter.clonePairs);
		System.out.println("Total deletion: " + counter.totalDelete);
		System.out.println("Average deletion: " + counter.totalDelete / (double) counter.clonePairs);
		System.out.println("Total update: " + counter.totalUpdate);
		System.out.println("Average update: " + counter.totalUpdate / (double) counter.clonePairs);
		System.out.println("Total move: " + counter.totalMove);
		System.out.println("Average move: " + counter.totalMove / (double) counter.clonePairs);
		
		// sort the cluster2 by key in an ascending orders
		LinkedList<Map.Entry<Integer, Integer>> sortedMap1 = new LinkedList<Map.Entry<Integer, Integer>>(counter.efforts.entrySet());
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
		
		
		// sort the cluster by value in a descending order
		LinkedList<Map.Entry<String, Integer>> sortedMap2 = new LinkedList<Map.Entry<String, Integer>>(counter.types.entrySet());
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
		for(Map.Entry<String, Integer> entry : sortedMap2) {
			System.out.println(entry.getKey() + " occurs in " + entry.getValue() + " times");
		}
	}
}
