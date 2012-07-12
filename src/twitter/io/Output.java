package twitter.io;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import crawler.Constants;

import twitter4j.Status;
import twitter4j.User;

public class Output {
	
	
	public static void saveUsers(List<User> users, String usersOutputFile) throws IOException{
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(usersOutputFile),"UTF8")); 
		
		//FileOutputStream fout = new FileOutputStream(new File(usersOutputFile));
		 //PrintStream ps = new PrintStream(fout);
		 
		 for (User u : users) {
			 
				// ps.println(
				// );
			 
			 	try {
				 
			 
				 String userString = Constants.NEW_IDENTIFIER + u.getId() + 
				 Constants.NEW_IDENTIFIER + u.getScreenName() + 
				 Constants.NEW_IDENTIFIER + u.getName() +
				 Constants.NEW_IDENTIFIER + u.getFriendsCount() + 
				 Constants.NEW_IDENTIFIER + u.getFollowersCount() + 
				 Constants.NEW_IDENTIFIER + u.getStatusesCount() + 
				 Constants.NEW_IDENTIFIER + u.getLocation().replaceAll("[^\\w\\s]","") + 
				 Constants.NEW_IDENTIFIER + u.getTimeZone() + 
				 Constants.NEW_IDENTIFIER + u.getCreatedAt().getTime();
				 
				 // [^a-zA-Z0-9\\s] 
				 
				 userString = userString.replace(System.getProperty("line.separator"), "") + System.getProperty("line.separator");
				 
				 out.write(userString);
				 
			 	} catch(Exception e) {
			 		System.err.println("error for user "+u+": "+e.getCause());
			 	}
				 
			
		}
		 //ps.close();
		 //fout.close();
		 out.close();
	}
	
//	public static void saveToCSVFile(List<User> users, HashMap<User,HashSet<Link>> links, String networkOutputFile) throws IOException{
//		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(networkOutputFile),"UTF8")); 
//		
//		//FileOutputStream fout = new FileOutputStream(new File(networkOutputFile));
//		// PrintStream ps = new PrintStream(fout);
//		 
//		 for (HashSet<Link> linksPerUser : links.values()) {
//			 
//			 if (linksPerUser.size() > 0) {
//				 
//				 out.write(linksPerUser.iterator().next().from.getId() + ";");
//				 
//				 for (Link curLink : linksPerUser) {
//					 out.write(curLink.to.getId() + ";");
//
//				}
//				 out.write("\n");
//			 }
//			
//		}
//		 //ps.close();
//		 out.close();
//	}
	
	public static void saveToCSVFile(HashMap<Long,Set<Long>> network, String removedNetworkOutputFile) throws IOException{
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(removedNetworkOutputFile),"UTF8")); 
		
		for (Long userId : network.keySet()) {
		 
			out.write(userId+ ";");
			
			 for (Long friendId : network.get(userId)) {
					 out.write(friendId + ";");
			}
			out.write("\n");
		}
		 out.close();
	}
	
	public static void saveStatuses(HashMap<Long,List<Status>> statuses, String contentOutputFile) throws IOException{
		 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(contentOutputFile),"UTF8")); 
		
		 for (Long userId : statuses.keySet()) {
			 if (statuses.get(userId) != null && !statuses.get(userId).isEmpty()) {
				 
				 for (Status s : statuses.get(userId)) { 
					 String statusLine = Constants.NEW_IDENTIFIER +userId+
					 Constants.NEW_IDENTIFIER +s.getId()+ Constants.NEW_IDENTIFIER+ s.isRetweet()+ Constants.NEW_IDENTIFIER+ 
					 s.getCreatedAt().getTime()+ Constants.NEW_IDENTIFIER+
					 s.getText().replaceAll("[^\\w\\s]",""); //[^a-zA-Z0-9\\s]
					 
					 statusLine = statusLine.replace(System.getProperty("line.separator"), "") + System.getProperty("line.separator");
					 
					 out.write(statusLine); // very important: replace line breaks
				 }
				 
				 
			 }
		 }
		 
		 out.close();
	}
	
//	public static void saveStatuses(HashMap<User,List<Status>> statuses, String contentOutputFile) throws IOException{
//		 //FileOutputStream fout = new FileOutputStream(new File(contentOutputFile));
//		 //PrintStream ps = new PrintStream(fout);
//		 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(contentOutputFile),"UTF8")); 
//		
//		 for (List<Status> statusesPerUser : statuses.values()) {
//			 
//			 if (statusesPerUser.size() > 0) {
//				 
//				 User u = statusesPerUser.iterator().next().getUser();
//				 
//				 out.write(Constants.NEW_IDENTIFIER +u.getId() +
//						 Constants.NEW_IDENTIFIER +u.getId() +
//						 Constants.NEW_IDENTIFIER +statusesPerUser.size() + "\n"
//						 );
//				 
//				 for (Status curStatus : statusesPerUser) {
//					 
//					 String statusLine = Constants.NEW_IDENTIFIER +curStatus.getUser().getId()+
//					 Constants.NEW_IDENTIFIER +curStatus.getId()+ Constants.NEW_IDENTIFIER+ curStatus.isRetweet()+ Constants.NEW_IDENTIFIER+ 
//					 curStatus.getCreatedAt().getTime()+ Constants.NEW_IDENTIFIER+
//					 curStatus.getText().replaceAll("[^\\w\\s]",""); //[^a-zA-Z0-9\\s]
//					 
//					 statusLine = statusLine.replace(System.getProperty("line.separator"), "") + System.getProperty("line.separator");
//					 
//					 out.write(statusLine); // very important: replace line breaks
//
//				}
//			 }
//			
//		}
//		 //ps.close();
//		 out.close();
//	}
	
	
	
	

}
