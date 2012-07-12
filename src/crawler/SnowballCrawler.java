/*
 * Author: Cassio Melo (melo.cassio at gmail.com)
 */
package crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import twitter.TwitterRetry;
import twitter.io.Output;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;

// TODO: preserve network distribution: normalize number of nodes
// TODO: check "rateLimitStatus" at each twitter request
// TODO: discard re-tweet option
// TODO add remaining links



// TODO caching the user = showUser
public class SnowballCrawler {

	public static final String[] FOCAL_NODES = {"keaneofficial"};
	public static final int START_NUMBER_LINKS = 100; //100 // paper: 10 k
	public static final int NUMBER_NODES = 50000; // 50000 paper ~ 13 mi
	public static final int NUMBER_STATUSES = 50;
	
	public static final int SKIP_NODES_WITH_MORE_THAN = 2000; //friends 
	
	public String usersOutputFile = "C:\\data\\twitter7-users.txt";
	public String networkOutputFile = "C:\\data\\twitter7-network.csv"; ///Users/cassiomelo/Dropbox/code/twittertest/data/twitter2.csv";
	public String contentOutputFile = "C:\\data\\twitter7-statuses.txt";//"/Users/cassiomelo/Dropbox/code/twittertest/data/twitter2.txt";
	
	
	TwitterRetry twitter;
	 
	 
	 public SnowballCrawler(){
		 try {
			 
		 twitter = TwitterRetry.getInstance();

	     AccessToken token = twitter.getOAuthAccessToken();
	     System.out.println("Access Token " +token );

	     
		 } catch (Exception e) {
			 e.printStackTrace();
			 System.exit(1);
		 }

		
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
	
	/* ***************************************
	 * CRAWL
	 */
	ArrayList<Object[]> userRank;
	public void crawl() throws Exception{
		String startnode = "keaneofficial";
		
		ArrayList<Long> usersIds = new ArrayList<Long>();
		HashMap<Long,HashSet<Long>> network = new HashMap<Long,HashSet<Long>>();
		
		
		HashMap<Long,Set<Long>> links = new HashMap<Long,Set<Long>>();
		HashMap<Long,List<Status>> statuses = new HashMap<Long,List<Status>>();
		
		
        User user1 = twitter.showUser(startnode);
        
        HashSet<Long> initialUsers = twitter.getFollowersIDs(user1.getId(), START_NUMBER_LINKS);
        
        ResponseList<User> users = twitter.lookupUsers(this.convertLongs(new ArrayList(initialUsers)));
        
        
        for (int i = 0; i < users.size() && users.size() < NUMBER_NODES; i++) {

        	User user = (User) users.get(i);//it.next();
        	
        	try {
        		
				// save links
	        	Iterator it2 =  twitter.getFriends(user,-1).iterator(); // this might throw an "unauthorized" exception that's why it should come first
	        	
	        	links.put(user.getId(), new HashSet<Long>());
	        	
	        	int countFriends = 0;
	        	while (it2.hasNext()) {
	        		User friend = (User) it2.next();
	        		
	        		if (SKIP_NODES_WITH_MORE_THAN > 0 &&
	        				friend.getFriendsCount() > SKIP_NODES_WITH_MORE_THAN) continue;
	        		
	        		
	        		
	        		links.get(user.getId()).add(friend.getId());
	        		
	        		if (!users.contains(friend))
	        			users.add(friend);
	        		countFriends++;
	        	}
	        	
        		// successfully fetched user info, save user
            	//ret.add(user);
        		
            	// save statuses
            	Paging paging = new Paging(1, NUMBER_STATUSES);
            	ResponseList<Status> userStatuses = twitter.getUserTimeline(user.getId(),paging);
            	statuses.put(user.getId(), userStatuses);
	           
	            System.out.println("Done with: @" + user.getScreenName());
        	
        	
        	} catch (Exception e) {
        		//System.err.println(e.getMessage());
				System.out.println("Error - not authorized for: "+user.getScreenName() + " - skipping...");
				
				// remove links to people that had "unauthorized access" exception
				
				for (Set<Long> linksPerUser : links.values()) {
					ArrayList<Long> markedForRemoval = new ArrayList<Long>();
					for (Long curLink : linksPerUser) {
						if (curLink == user.getId()) {
							markedForRemoval.add(curLink);
						}
					}
					for (Long toRemoveLink : markedForRemoval) {
						linksPerUser.remove(toRemoveLink);
					}
				}
				
				
			}
        	
        	
        	
        }
        Output.saveUsers(users, usersOutputFile);
        Output.saveToCSVFile(links, networkOutputFile);
	    Output.saveStatuses(statuses, contentOutputFile);
	}
	
	
	
	
	
	
	
	public static void main(String[] args) {
		
		try {
			
		SnowballCrawler cn = new SnowballCrawler();
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

	
	
	
	
	
	
	
}
