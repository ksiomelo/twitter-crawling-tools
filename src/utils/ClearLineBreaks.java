package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileChannel;

import crawler.Constants;



public class ClearLineBreaks {
	
	public static void clearLineBreaks(String file) throws IOException {
		File temp = File.createTempFile("clearlinebreaks", Long.toString(System.nanoTime()));
		File orig = new File(file);
		
		// read input file line by line
		BufferedReader reader = new BufferedReader(new FileReader(orig));
				
		FileOutputStream fout = new FileOutputStream(temp);
		PrintStream ps = new PrintStream(fout);
		 
		 
		String line = null;

		line = reader.readLine();
		while (line != null) {
			
			ps.println(line.replace("\n", "").replace("\r", ""));
			line = reader.readLine();
		}
	
		 
		

		 ps.close();
		 fout.close();
		 
		 
		// Copy the contents from temp to original file  
		  FileChannel src = new FileInputStream(temp).getChannel();
		  FileChannel dest = new FileOutputStream(orig).getChannel();
		  dest.transferFrom(src, 0, src.size());

		 
	}
	
	public static void fixLineBreaks(String file) throws IOException {
		File temp = File.createTempFile("fixlinebreaks", Long.toString(System.nanoTime()));
		File orig = new File(file);
		
		// read input file line by line
		BufferedReader reader = new BufferedReader(new FileReader(orig));
				
		FileOutputStream fout = new FileOutputStream(temp);
		PrintStream ps = new PrintStream(fout);
		 
		 
		String line = null;

		line = reader.readLine();
		while (line != null) {
			String [] lineArray = line.split(Constants.IDENTIFIER); 
			
			if (lineArray.length < 9) {
				
				String lineCont = reader.readLine();
				ps.println(line+lineCont);
				
			} else {
				ps.println(line);
			}
			
			line = reader.readLine();
		}
	
		 
		

		 ps.close();
		 fout.close();
		 
		 
		// Copy the contents from temp to original file  
		  FileChannel src = new FileInputStream(temp).getChannel();
		  FileChannel dest = new FileOutputStream(orig).getChannel();
		  dest.transferFrom(src, 0, src.size());

		 
	}
	
	public static void fixLineBreaksByScreename(String file, String screename) throws IOException {
		File temp = File.createTempFile("fixlinebreaks", Long.toString(System.nanoTime()));
		File orig = new File(file);
		
		// read input file line by line
		BufferedReader reader = new BufferedReader(new FileReader(orig));
				
		FileOutputStream fout = new FileOutputStream(temp);
		PrintStream ps = new PrintStream(fout);
		 
		 
		String line = null;

		line = reader.readLine();
		while (line != null) {
			String [] lineArray = line.split(Constants.IDENTIFIER); 
			
			if (lineArray.length > 2 && screename.equalsIgnoreCase(lineArray[2])) {
				
				String lineCont = reader.readLine();
				ps.println(line+lineCont);
				
			} else {
				ps.println(line);
			}
			
			line = reader.readLine();
		}
	
		 
		

		 ps.close();
		 fout.close();
		 
		 
		// Copy the contents from temp to original file  
		  FileChannel src = new FileInputStream(temp).getChannel();
		  FileChannel dest = new FileOutputStream(orig).getChannel();
		  dest.transferFrom(src, 0, src.size());

		 
	}
	
	
	//B_BENNYBANAN
	
	public static void main(String[] args) {
		try {
			//ClearLineBreaks.fixLineBreaksByScreename("C:\\data\\twitter3-users.txt", "FtGtJH");
			ClearLineBreaks.clearLineBreaks("C:\\data\\twitter4-users.txt");
			//ClearLineBreaks.clearLineBreaks("C:\\data\\twitter3-users.txt");
			//ClearLineBreaks.clearLineBreaks("C:\\data\\twitter3-statuses.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
