package io.antmedia.demo_client;

import java.io.File;

public class Utils {
	private static File tempDir;
	
	public static File getTempDir() {
		if(tempDir == null) {
			tempDir = new File("temp");
			if(!tempDir.exists()) {
				tempDir.mkdir();
			}
		}
		return tempDir;
	}
}
