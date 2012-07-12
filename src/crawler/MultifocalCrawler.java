/*
 * Author: Cassio Melo (melo.cassio at gmail.com)
 */
package crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import twitter.TwitterRetry;
import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;

// TODO: preserve network distribution: normalize number of nodes
// TODO: check "rateLimitStatus" at each twitter request
// TODO: discard re-tweet option


public class MultifocalCrawler {

	public static final String[] FOCAL_NODES = {"manas", "sbisker", "jvaleski", "nicolecasanova"};
	public static final int NUMBER_Link2S = 30;
	public static final int NUMBER_NODES = 600;
	public static final int NUMBER_STATUSES = 20;
	
	
	public String networkOutputFile = "C:\\data\\twitter3.csv"; ///Users/cassiomelo/Dropbox/code/twittertest/data/twitter2.csv";
	public String contentOutputFile = "C:\\data\\twitter3.txt";//"/Users/cassiomelo/Dropbox/code/twittertest/data/twitter2.txt";
	
	
	TwitterRetry twitter;
	 
	 
	 public MultifocalCrawler(){
		 try {
			 
		 twitter = TwitterRetry.getInstance();

	     AccessToken token = twitter.getOAuthAccessToken();
	     System.out.println("Access Token " +token );

	     
		 } catch (Exception e) {
			 e.printStackTrace();
			 System.exit(1);
		 }

		
	 }
	
	 
	 public HashSet<Long> getFriendsIDs(Long userId, int max) throws Exception {
	    	HashSet<Long> ret = new HashSet<Long>();
	    	long cursor = -1;
	    	IDs ids;
	    	int count = 0;
	    	do {
	    		
	            ids = twitter.getFriendsIDs(userId, cursor);
	    		
	            for (int i = 0; i < ids.getIDs().length && i < max; i++) {
					long id = ids.getIDs()[i];
	                ret.add(id);
	            }
	            
	            count++;
	            
	        } while ((cursor = ids.getNextCursor()) != 0 && count < max);
	    	
	    	return ret;
		}
	
	public HashSet<User> getFriends(User u) throws Exception {
    	HashSet<User> ret = new HashSet<User>();
    	long cursor = -1;
    	IDs ids;
    	int count = 0;
    	do {
    		
            ids = twitter.getFriendsIDs(u.getScreenName(), cursor);
    		
            
            for (int i = 0; i < ids.getIDs().length; i++) {
				long id = ids.getIDs()[i];
                ret.add(twitter.showUser(id));
            }
            
            count++;
            
        } while ((cursor = ids.getNextCursor()) != 0);
    	
    	return ret;
	}
	
	
	public ArrayList<Long> getMostConnected(ArrayList<Long> users, int max) {
		// Sort by userRank
        Collections.sort(users, new Comparator<Long>() {
			@Override
			public int compare(Long o1, Long o2) {
				return (getRankForUser(o1, userRank)).compareTo(getRankForUser(o2, userRank));
			}
			
        });
        
        if (max <= users.size())
        	return (ArrayList<Long>) users.subList(0, max-1);
        else return users;
	}
	
	public ArrayList<Long> getMostConnected(ArrayList<Long> users) {
		// Sort by userRank
        Collections.sort(users, new Comparator<Long>() {
			@Override
			public int compare(Long o1, Long o2) {
				return (getRankForUser(o1, userRank)).compareTo(getRankForUser(o2, userRank));
			}
			
        });
        
        int count = 0;
        for (int i =0; i < users.size(); i++) {
        	Long userId = users.get(i);
        	
        	if (getRankForUser(userId, userRank) <= 1) {
        		count++;
        	}
        	if (count >= 3) {// got 3 nodes with 1 connection, stop // this avoids a node connected with too many peripherical nodes
        		return (ArrayList<Long>) users.subList(0, i-1);
        	}
        }
        return users;
	}
	
	
	public Integer getRankForUser(Long userId, ArrayList<Object[]> userRank){
		for (Object [] userCount : userRank) {
			if ( (Long)userCount[0] == userId) return (Integer)userCount[1];
		}
		return 0;
	}
	
	
	ArrayList<Object[]> userRank;
	public void crawl() throws TwitterException, IOException{
		String[] startnodes = FOCAL_NODES;
		
		ArrayList<Long> usersIds = new ArrayList<Long>();
		HashMap<Long,HashSet<Long>> network = new HashMap<Long,HashSet<Long>>();
		
		
		ArrayList<User> ret = new ArrayList<User>();
		HashMap<User,HashSet<Link2>> Link2s = new HashMap<User,HashSet<Link2>>();
		HashMap<User,ResponseList<Status>> statuses = new HashMap<User,ResponseList<Status>>();
		
		
        ResponseList<User> users = twitter.lookupUsers(startnodes);
        
        for (User u : users) {
        	usersIds.add(u.getId());
        }
        
        /*
         * CRAWL NETWORK
         */
        int maxFriends = 0;
        
        int userIdx = 0;
        while (usersIds.size() < NUMBER_NODES) {

        	Long curUserId = (Long) usersIds.get(userIdx);
        	
        	try {
        		
				// save Link2s
	        	HashSet<Long> friendsIds =  this.getFriendsIDs(curUserId, NUMBER_Link2S); // this might throw an "unauthorized" exception that's why it should come first
	        	
	        	network.put(curUserId, friendsIds);
	        	usersIds.addAll(friendsIds);
	        	
	        	
        	} catch (Exception e) {
        		System.err.println(e.getMessage());
				System.out.println("Error - not authorized for: "+curUserId + " - skipping...");
			}
        	
        	userIdx++;
        	
        }
        
        // gather friends
        for (int i = userIdx; i < usersIds.size(); i++) { 
        	
        	Long curUserId = (Long) usersIds.get(i);
        	
        	try {
        		
				// save Link2s
	        	HashSet<Long> friendsIds =  this.getFriendsIDs(curUserId, NUMBER_Link2S); // this might throw an "unauthorized" exception that's why it should come first
	        	
	        	network.put(curUserId, friendsIds);
	        	
	        	
        	} catch (Exception e) {
        		System.err.println(e.getMessage());
				System.out.println("Error - not authorized for: "+curUserId + " - skipping...");
			}
        }
        
        /*
         * RANK USERS WITH THEIR NUMBER OF CONNECTIONS
         */
	    userRank = new ArrayList<Object[]>();
	    
	    
        for(Long userId : network.keySet() ) {
        	int count = 0;
        	
        	for(Long otherUserId : network.keySet() ) {
        		if (network.get(otherUserId).contains(userId))  // for each friendship with existing user, count++
        			count++;
        	} 
        	
        	Object[] usr = {userId, count};
        	userRank.add(usr);
        }
        
        // Sort userRank
        Collections.sort(userRank, new Comparator<Object[]>() {
           
			@Override
			public int compare(Object[] arg0, Object[] arg1) {
				return ((Integer) arg0[1]).compareTo((Integer)arg1[1]);
			}
        });
        
        /*
         * FILTER USERS
         */
	        	
	    for (Object[] userCount : userRank) {
	    	Long curUserId = (Long) userCount[0];
	    	
	    	User curUser = null; 
	    	try { 
	    		curUser = twitter.showUser(curUserId);
	    		Link2s.put(curUser, new HashSet<Link2>());
	    		
	    	} catch (Exception e) { // TODO remover user
	    		System.err.println("ERROR: CARAI "+e.getMessage());
	    		continue;
	    	}
	    	
	    	
	    	
	    	
	    	try {
	    	
	    	
				ArrayList<Long> userFriendsIds = new ArrayList<Long>(
						network.get(curUserId));
				ResponseList<User> friends = twitter.lookupUsers(convertLongs(getMostConnected(userFriendsIds, 7)));
				Iterator<User> it2 = friends.iterator();

				while (it2.hasNext()) {
					User friend = (User) it2.next();

					Link2 l = new Link2(curUser, friend);
					Link2s.get(curUser).add(l);
					users.add(friend);
				}

				// successfully fetched user info, save user
				ret.add(curUser);

				// save statuses
				Paging paging = new Paging(1, NUMBER_STATUSES);
				ResponseList<Status> userStatuses = twitter.getUserTimeline(
						curUser.getId(), paging);
				statuses.put(curUser, userStatuses);

				System.out.println("Done with: @" + curUser.getScreenName());
        	
        	} catch (Exception e) {
        		//System.err.println(e.getMessage());
				System.out.println("Error - not authorized for: "+curUser.getScreenName() + " - skipping...");
				
				// remove Link2s to people that had "unauthorized access" exception
				
				for (HashSet<Link2> Link2sPerUser : Link2s.values()) {
					ArrayList<Link2> markedForRemoval = new ArrayList<Link2>();
					for (Link2 curLink2 : Link2sPerUser) {
						if (curLink2.from.getId() == curUser.getId() || curLink2.to.getId() == curUser.getId()) {
							markedForRemoval.add(curLink2);
						}
					}
					for (Link2 toRemoveLink2 : markedForRemoval) {
						Link2sPerUser.remove(toRemoveLink2);
					}
				}
				
				
			}
        	
        	
        	
        }
        
        saveToCSVFile(ret, Link2s);
	    saveContentToFile(statuses);
	}
	
	/*
	 * Saves the network in NET format (Pajek)
	 * http://gephi.org/users/supported-graph-formats/pajek-net-format/
	 */
	public void saveToNetFile(ArrayList<User> users, HashMap<User,HashSet<Link2>> Link2s, HashMap<User,ResponseList<Status>> statuses) throws IOException{
		 FileOutputStream fout = new FileOutputStream(new File(networkOutputFile));
		 PrintStream ps = new PrintStream(fout);
		 
		 String Link2sString = "*Edges\n";
		 
		 ps.println("*Vertices "+users.size());
		 
		 for (User user : users) {
			 ps.println(user.getId() + " \""+ user.getScreenName()+"\"");
			 
			 if (Link2s.containsKey(user)) {
				 for(Link2 Link2 : Link2s.get(user)) {
					 Link2sString += Link2.toString() + "\n";
				 }
			 }
		 }
		 
		 ps.println(Link2sString);
		 
		 ps.close();
		 fout.close();
		
	}
	
	public void saveToCSVFile(ArrayList<User> users, HashMap<User,HashSet<Link2>> Link2s) throws IOException{
		 FileOutputStream fout = new FileOutputStream(new File(networkOutputFile));
		 PrintStream ps = new PrintStream(fout);
		 
		 for (HashSet<Link2> Link2sPerUser : Link2s.values()) {
			 
			 if (Link2sPerUser.size() > 0) {
				 
				 ps.print(Link2sPerUser.iterator().next().from.getScreenName() + ";");
				 
				 for (Link2 curLink2 : Link2sPerUser) {
					 ps.print(curLink2.to.getScreenName() + ";");

				}
				ps.print("\n");
			 }
			
		}
		 ps.close();
		 fout.close();
	}
	
	public void saveContentToFile(HashMap<User,ResponseList<Status>> statuses) throws IOException{
		 FileOutputStream fout = new FileOutputStream(new File(contentOutputFile));
		 PrintStream ps = new PrintStream(fout);
		 
		 for (ResponseList<Status> statusesPerUser : statuses.values()) {
			 
			 if (statusesPerUser.size() > 0) {
				 
				 ps.println(Constants.IDENTIFIER +statusesPerUser.iterator().next().getUser().getScreenName() + Constants.IDENTIFIER +statusesPerUser.size());
				 
				 for (Status curStatus : statusesPerUser) {
					 ps.println(Constants.IDENTIFIER +curStatus.getId()+ Constants.IDENTIFIER+ curStatus.isRetweet()+ Constants.IDENTIFIER+ 
							 curStatus.getCreatedAt().getTime()+ Constants.IDENTIFIER+
							 curStatus.getText().replace("\n", "") + ""); // very important: replace line breaks

				}
			 }
			
		}
		 ps.close();
		 fout.close();
	}
	
	
	
	
	public static void main(String[] args) {
		
		try {
		MultifocalCrawler cn = new MultifocalCrawler();
		cn.crawl();
		
		} catch (TwitterException te) {
	        te.printStackTrace();
	        System.out.println("Failed to lookup users: " + te.getMessage());
	        System.exit(-1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private long[] convertLongs(List<Long> integers)
	{
	    long[] ret = new long[integers.size()];
	    for (int i=0; i < ret.length; i++)
	    {
	        ret[i] = integers.get(i).longValue();
	    }
	    return ret;
	}

	
	
	class Link2{
		public User from;
		public User to;
		
		public Link2(User f, User t) {
			from = f;
			to = t;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj == null || !(this.getClass() != obj.getClass())) {
				return false;
			}
			Link2 other = (Link2)obj;
			return
			this.from.getId() == other.from.getId() &&
			this.to.getId() == other.to.getId();
			}
		
		@Override
		public int hashCode() {
			return String.valueOf(this.from.getId()).hashCode() + String.valueOf(this.to.getId()).hashCode();
		}
		
		@Override
		public String toString() {
			return String.valueOf(this.from.getId()) + " " + String.valueOf(this.to.getId());

		}
		
		
		}
	
	
	
	
}
