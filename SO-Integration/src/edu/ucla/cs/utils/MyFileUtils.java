package edu.ucla.cs.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class MyFileUtils {
	public static void appendStringToFile(String s, String path) {
		try {
			File f = new File(path);
			if(!f.exists()) {
				f.createNewFile();
			}
			FileWriter w = new FileWriter(f, true);
			BufferedWriter writer = new BufferedWriter(w, 8192);
			writer.write(s);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeStringToFile(String s, String path) {
		try {
			File f = new File(path);
			if(!f.exists()) {
				f.createNewFile();
			}
			FileWriter w = new FileWriter(f, false);
			BufferedWriter writer = new BufferedWriter(w, 8192);
			writer.write(s);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String readFileToString(String path){
		String content = "";
		try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))){
			String line = null;
			while((line = br.readLine()) != null) {
				content += line + System.lineSeparator();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return content;
	}
	
	public static ArrayList<String> getCode(String body) {
		ArrayList<String> codes = new ArrayList<>();
		String start = "<code>", end = "</code>";
		int s = 0;
		while (true) {
			s = body.indexOf(start, s);
			if (s == -1)
				break;
			s += start.length();
			int e = body.indexOf(end, s);
			if (e == -1)
				break;
			codes.add(body.substring(s, e).trim());
			s = e + end.length();
		}
		return codes;
	}
	
	public static HashMap<Integer, String> getCloneURL(File file) {
		HashMap<Integer, String> urls = new HashMap<Integer, String>();
		try {
			List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
			for(String line : lines) {
				if(line.isEmpty()) continue;
				String[] ss = line.split("\t");
				int index = Integer.parseInt(ss[0]);
				String url  = ss[1];
				urls.put(index, url);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return urls;
	}

	public static HashMap<Integer, String> getCloneTimestamp(File file) {
		HashMap<Integer, String> timestamps = new HashMap<Integer, String>();
		try {
			List<String> lines = FileUtils.readLines(file, Charset.defaultCharset());
			for(String line : lines) {
				if(line.isEmpty()) continue;
				String[] ss = line.split("\t");
				int index = Integer.parseInt(ss[0]);
				String url  = ss[2];
				timestamps.put(index, url);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return timestamps;
	}
}
