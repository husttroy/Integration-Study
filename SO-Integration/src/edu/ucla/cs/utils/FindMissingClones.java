package edu.ucla.cs.utils;

import java.io.File;
import java.util.ArrayList;

public class FindMissingClones {
	public static void main(String[] args) {
		String path1 = "/home/troy/research/Integration-Study/dataset/real-clones";
		File dir1 = new File(path1);
		String path2 = "/home/troy/research/Integration-Study/dataset/clone-codes-screened/real-clones";
		File dir2 = new File(path2);
		
		ArrayList<String> cloneNameSet1 = new ArrayList<String>(); 
		ArrayList<String> copy = new ArrayList<String>();
		for(File dir : dir1.listFiles()) {
			copy.add(dir.getName());
			cloneNameSet1.add(dir.getName().substring(0, dir.getName().lastIndexOf('-')));
		}
		
		ArrayList<String> cloneNameSet2 = new ArrayList<String>(); 
		for(File dir : dir2.listFiles()) {
			cloneNameSet2.add(dir.getName().substring(0, dir.getName().lastIndexOf('-')));
		}
		
		cloneNameSet1.removeAll(cloneNameSet2);
		
		for(String name : cloneNameSet1) {
			for(String name2 : copy) {
				if(name2.contains(name)) {
					System.out.println(name2);
				}
			}
		}
	}
}
