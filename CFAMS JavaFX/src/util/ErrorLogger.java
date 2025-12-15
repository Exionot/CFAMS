package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorLogger {
	private static final String LOG_DIR;
	static {
	    try {
	        String userHome = System.getProperty("user.home");
	        File uploadDir = new File(userHome, "CFAMS");
	        if (!uploadDir.exists()) {
	            uploadDir.mkdirs();
	        }
	        LOG_DIR = uploadDir.getAbsolutePath() + File.separator;
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to initialize upload directory", e);
	    }
	}
	
	public static void logError(String message) {
		try {
			File errorFile = new File(LOG_DIR + "error-log.txt");
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(errorFile, true));
			
			writer.newLine();
			writer.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")).toString());
			writer.newLine();
			writer.write(message);
			writer.newLine();
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
