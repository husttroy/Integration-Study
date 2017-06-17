package edu.ucla.cs.so.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
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
}
