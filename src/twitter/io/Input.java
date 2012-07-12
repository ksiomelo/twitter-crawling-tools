package twitter.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import crawler.Constants;

import twitter.TwitterRetry;
import twitter.model.StatusImpl;
import twitter.model.UserImpl;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

public class Input {
	
	private final int NETWORK_FILE_FORMAT_CSV_WEIGHTED = 1;
	private final int NETWORK_FILE_FORMAT_CSV_ADJACENCY = 2;
	
	private static User getUserByScreename(List<User> users, String screename){
		
		for(User u : users) {
			if (u.getScreenName().equals(screename)) return u;
		}
		return null;
	}
	
	private static User getUserById(List<User> users, Long uId){
		
		for(User u : users) {
			if (u.getId() == uId) return u;
		}
		return null;
	}
	
	/*
	 * Read network file in this format
	 * 
	 * ;vicegandako;sharon_cuneta12;RafaelRosell;
	 * vicegandako;0;1.0;1.0;1.0
	 * sharon_cuneta12;0;0;0;0
	 * ...
	 */
	public static HashMap<Long,HashSet<Long>> readNetworkCSVWeighted(TwitterRetry twitter, String netFile) throws IOException, TwitterException{
		HashMap<Long,HashSet<Long>> ret = new HashMap<Long,HashSet<Long>>();
		
		
		// read input file line by line
		BufferedReader reader = new BufferedReader(new FileReader(netFile));
		String line = null;

		line = reader.readLine();
		String [] usersArray = line.substring(1).split(";"); // skips the first ;
		
		ResponseList<User> users = null;
		if (usersArray.length > 100) {
			int lastIdx = 0;
			for (int i = 0; i < usersArray.length; i += 100) {
				String [] subArray = Arrays.copyOfRange(usersArray, i, i+100);
				
				
				
				ResponseList<User> users2 = twitter.lookupUsers(subArray);
				System.out.println("input: "+subArray.length + " | output: "+users2.size());
				if (users == null) users = users2;
				else users.addAll(users2);
				
				lastIdx = i;
			}
			String [] rest = Arrays.copyOfRange(usersArray, lastIdx, usersArray.length);
			ResponseList<User> users2 = twitter.lookupUsers(rest);
			users.addAll(users2);
			System.out.println("input: "+rest.length + " | output: "+users2.size());
			
		} else users = twitter.lookupUsers(usersArray);
			
		
		
		
		line = reader.readLine();
		while (line != null) {
			String [] lineArray = line.split(";");
			
			// gets user
			User u = getUserByScreename(users, lineArray[0]);
			
			if (u == null) {
				System.err.println("User not found: "+lineArray[0] +" - SKIPPING");
				line = reader.readLine();
				continue;
			}
			
			// gets his connections
			HashSet<Long> links = new HashSet<Long>();
			
			for (int i = 1; i < lineArray.length; i++) {
				
				if (!lineArray[i].equals("0")) {
					User u2 = getUserByScreename(users, usersArray[i-1]);
					
					if (u2 == null) {
						System.err.println("User not found: "+usersArray[i-1] +" - SKIPPING");
						continue;
					}
					
					links.add(u2.getId());
				}
			}
			
			ret.put(u.getId(), links);
			 
			line = reader.readLine();
		}
		
		
		return ret;
	}
	
	public static HashMap<Long,HashSet<Long>> readNetwork(List<User> users, String netFile) throws IOException{
		HashMap<Long,HashSet<Long>> ret = new HashMap<Long,HashSet<Long>>();
		
		
		// read input file line by line
		BufferedReader reader = new BufferedReader(new FileReader(netFile));
		String line = null;

		line = reader.readLine();
		while (line != null) {
			String [] lineArray = line.split(";");
			
			// gets user
			User u = getUserById(users, Long.parseLong(lineArray[0]));//getUserByScreename(users, lineArray[0]);
			
			if (u == null) {
				System.err.println("User not found: "+lineArray[0] +" - SKIPPING");
				line = reader.readLine();
				continue;
			}
			
			// gets his connections
			HashSet<Long> links = new HashSet<Long>();
			
			for (int i = 1; i < lineArray.length; i++) {
				User u2 = getUserById(users, Long.parseLong(lineArray[i].trim()));
				
				
				if (u2 == null) {
					System.err.println("User not found in : "+lineArray[i] +" - SKIPPING");
					continue;
				}
				
				links.add(u2.getId());
			}
			
			ret.put(u.getId(), links);
			 
			line = reader.readLine();
		}
		
		
		return ret;
	}
	
	public static HashMap<Long,List<Status>> readStatuses(String statusesFile) throws IOException {
		HashMap<Long,List<Status>> ret = new HashMap<Long,List<Status>>();
		
		// read input file line by line
		BufferedReader reader = new BufferedReader(new FileReader(statusesFile));
		String line = null;

		line = reader.readLine();
		
		while (line != null){
		
		String userId = line.split(Constants.NEW_IDENTIFIER)[1];
		int statusesCount = Integer.parseInt(line.split(Constants.NEW_IDENTIFIER)[3]);
		line = reader.readLine();
		
		
		// gets user
		//User u = getUserByScreename(users, screename);
		//User u = users.get(users.indexOf(new UserImpl(userId)));
		
		// gets his statuses
		ArrayList<Status> statuses = new ArrayList<Status>();
		
		for (int i = 0; i < statusesCount; i++) {
			
			// hack to fix unexpected line breaks
			boolean merged = false;
			String proxline = reader.readLine();
			while (proxline != null && !proxline.contains(Constants.NEW_IDENTIFIER)) { 
				line += " " + proxline;
				merged = true;
				proxline = reader.readLine();
			}
			
			try {
			StatusImpl s = new StatusImpl(line);//new UserImpl();
			statuses.add(s);
			
			} catch (Exception e) {
				System.out.println("empty status, skipping...");
				//System.out.println();
			}
			
//			if (merged)
//				line = reader.readLine();
//			else
				line = proxline;
		}
		
		ret.put(Long.parseLong(userId), statuses);
		
		}		
				
				
		return ret;
	}
	
	
	
	public static List<User> readOnlyUsersWithTheirNetwork(String usersFile, String networkFile) throws IOException{
		List<User> users = new ArrayList<User>();
		
		List<User> allUsers = readUsers(usersFile);
		
		BufferedReader reader = new BufferedReader(new FileReader(networkFile));
		String line = null;
	
		while ((line = reader.readLine()) != null){
			String [] idsArray = line.split(";");
			
			User u = getUserById(allUsers, Long.parseLong(idsArray[0].trim()));
			if (u != null)
				users.add(u);
			else
				System.err.println("User: "+idsArray[0]+" found in the network file (column 0) but not found in the users file");
		}
		
		return users;
		
	}
	
	
	
	public static List<User> readUsersFromCSVFile(String usersFile) throws IOException{
		List<User> users = new ArrayList<User>();
		
		BufferedReader reader = new BufferedReader(new FileReader(usersFile));
		String line = null;
	
		line = reader.readLine();
		String [] idsArray = line.split(";");
		
		for (int i = 1; i < idsArray.length; i++) {
			User u = new UserImpl(Long.parseLong(idsArray[i]));
			users.add(u);
		}
		
		return users;
		
	}
	
	
	public static List<User> readUsers(String usersFile) throws IOException{
		ArrayList<User> ret = new ArrayList<User>();
		
		
//	    List<String> lines = FileUtils.readLines(new File(usersFile));
//	    for (String line : lines) {
//	        // handle one line
//	    	if (line)
//	    	
//	    	 UserImpl u = new UserImpl(line);//new UserImpl();
//				
//				ret.add(u);
//	    }

		
		
		BufferedReader in = new BufferedReader(
				   new InputStreamReader(
		                      new FileInputStream(usersFile), "UTF8"));
		 
				String str;
		 
				while ((str = in.readLine()) != null) {
				   // System.out.println(str);
					String [] lineArray = str.split(Constants.NEW_IDENTIFIER);
					while (lineArray.length < 9) {
						str += in.readLine();
						
						lineArray = str.split(Constants.NEW_IDENTIFIER); 
					}
					
				    UserImpl u = new UserImpl(str);//new UserImpl();
					
					ret.add(u);
					
				}
		 
		in.close();
		
		
//		// read input file line by line
//		BufferedReader reader = new BufferedReader(new FileReader(usersFile));
//		String line = null;
//
//		line = reader.readLine();
//		while(line != null) {
//			
//			UserImpl u = new UserImpl(line);//new UserImpl();
//			
//			ret.add(u);
//			
//			line = reader.readLine();
//			
//		}
		
		
		return ret;
	}

}
