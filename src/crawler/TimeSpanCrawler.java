package crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import twitter.TwitterRetry;
import twitter.io.Input;
import twitter.io.Output;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import utils.SetUtils;



/*
 * Crawls a time span from previous crawl
 * TODO discard nodes that appeared with SKIP_NODES_WITH_MORE_THAN
 */

public class TimeSpanCrawler {
	// constants
	// input files
	private final String TWITTER_STATUSES_FILE = "C:\\data\\twitter7-statuses.txt";
	private final String TWITTER_NETWORK_FILE = "C:\\data\\twitter7-network.csv";
	private final String TWITTER_USER_FILE = "C:\\data\\twitter7-users.txt";
	//output files
	private final String TWITTER_ADDED_STATUSES_FILE = "C:\\data\\twitter7-statuses_2_added.txt";
	private final String TWITTER_REMOVED_STATUSES_FILE = "C:\\data\\twitter7-statuses_2_removed.txt";
	private final String TWITTER_ADDED_NETWORK_FILE = "C:\\data\\twitter7-network_2_added.csv";
	private final String TWITTER_ADDED_USER_FILE = "C:\\data\\twitter7-users_2.txt";
	private final String TWITTER_REMOVED_NETWORK_FILE = "C:\\data\\twitter7-network_2_removed.csv";
	
	private final String UPDATED_TWITTER_STATUSES_FILE = "C:\\data\\twitter7-statuses_updated.txt";
	private final String UPDATED_TWITTER_USER_FILE = "C:\\data\\twitter7-users_updated.txt";
	
	// previous data
	private HashMap<Long,HashSet<Long>> network;
	private HashMap<Long,List<Status>> statuses;
	private List<User> users;
	
	// new data
	private HashMap<Long,Set<Long>> addedNetwork;
	private HashMap<Long,List<Status>> addedStatuses;
	private HashMap<Long,List<Status>> removedStatuses;
	private List<User> addedUsers;
	//private List<User> removedUsers; // TODO
	private HashMap<Long,Set<Long>> removedNetwork; // TODO
	
	
	public static final int SKIP_NODES_WITH_MORE_THAN = 2000; //friends 
	
	
	private TwitterRetry twitter;
	
	public TimeSpanCrawler(){
		try {
		 
			 twitter = TwitterRetry.getInstance();
	
		     AccessToken token = twitter.getOAuthAccessToken();
		     System.out.println("Access Token " +token );

	     
		 } catch (Exception e) {
			 e.printStackTrace();
			 System.exit(1);
		 }
		
	}
	
	
	/*
	 * Read the crawled files to memory
	 */
	private void readFiles() throws IOException {
		//users = Input.readUsers(TWITTER_USER_FILE);
		
		
		users = Input.readOnlyUsersWithTheirNetwork(TWITTER_USER_FILE,TWITTER_NETWORK_FILE);
		network = Input.readNetwork(Input.readUsers(TWITTER_USER_FILE), TWITTER_NETWORK_FILE);
		statuses = Input.readStatuses(TWITTER_STATUSES_FILE);
		
		
		addedNetwork = new HashMap<Long, Set<Long>>();
		removedNetwork = new HashMap<Long, Set<Long>>();
		addedStatuses = new HashMap<Long, List<Status>>();
		removedStatuses = new HashMap<Long, List<Status>>();
		addedUsers = new ArrayList<User>();
	}
	
	
	
	private boolean isFetched(User u) {
		return users.contains(u) || addedUsers.contains(u);
	}
	
	private Long getLastStatusId(User u) { 
		List<Status> prevUserStatuses = statuses.get(u);
		if (prevUserStatuses != null && !prevUserStatuses.isEmpty()) {
			// TODO check if they are ordered when reading from file
			return prevUserStatuses.get(0).getId();
		}
		return null;
	}
	
	
	
	/* ******************************************************************
	 * Run the crawler
	 */
	public void run() throws IOException{
		
		// Read files
		this.readFiles();
		
		// for each user, get followers ids and compute the difference. 
		
		for (User u : users ) {
			try {
			
				// Why not check if the friends count changed? 
				// -Answer: because user could have replaced a friend for another and the counter would remain the same
				HashSet<Long> newFollowers = twitter.getFollowersIDs(u.getId(), -1);
				//(B - A) = links added | (A- B) links removed
				Set<Long> linksAdded = SetUtils.difference(newFollowers, network.get(u));
				Set<Long> linksRemoved = SetUtils.difference(network.get(u),newFollowers);
				
				if (linksRemoved.size() > 0)
					removedNetwork.put(u.getId(), linksRemoved); // save removed links if any
				
				// For each link added
				for(Long uid : linksAdded) {
					
					User newUser = twitter.showUser(uid);
					
					if (SKIP_NODES_WITH_MORE_THAN > 0 &&
							newUser.getFriendsCount() > SKIP_NODES_WITH_MORE_THAN){
						System.out.println("User: @"+newUser.getScreenName() + " detected as new link but has more than 2000 friends, skipping");
						continue;
					}
					
					System.out.println("new link added from user: @"+u.getScreenName()+" to user: @"+newUser.getScreenName());
					
					// show new user
					if (!isFetched(newUser)) { // If user was not previously fetched, save it
						System.out.println(newUser.getScreenName() + " was not previously fetched, fetching now...");
						
						// save user
						addedUsers.add(newUser);
						
						// save link connection
						if (!addedNetwork.containsKey(u)) 
							addedNetwork.put(u.getId(), new HashSet<Long>());
						
						addedNetwork.get(u.getId()).add(newUser.getId());
						
						// get new user friends
//						HashSet<User> newUserFriends = twitter.getFriends(newUser, -1);
//						
//						for(User newUserFriend : newUserFriends) {
//							if (!isFetched(newUserFriend)) {
//								
//							}
//						}
						
						// get new user statuses
						Paging paging = new Paging(1, 50);
		            	ResponseList<Status> userStatuses = twitter.getUserTimeline(newUser.getId(),paging);
		            	statuses.put(newUser.getId(), userStatuses); // OBS: here we use statuses instead of 
			           
			            System.out.println("Done with (new): @" + newUser.getScreenName());
					}
					
				} // end for each link added
				
//				// For each link removed
				for(Long uid : linksRemoved) {
					User remUser = twitter.showUser(uid);
					
					if (SKIP_NODES_WITH_MORE_THAN > 0 &&
							remUser.getFriendsCount() > SKIP_NODES_WITH_MORE_THAN){
						System.out.println("User: @"+remUser.getScreenName() + " detected as removed link but has more than 2000 friends, skipping");
						continue;
					}
					
					if (!removedNetwork.containsKey(u.getId())) {
						removedNetwork.put(u.getId(), new HashSet<Long>());
					}
					removedNetwork.get(u.getId()).add(remUser.getId());
					
					// get rem user statuses
					Paging paging = new Paging(1, 50);
	            	ResponseList<Status> userStatuses = twitter.getUserTimeline(remUser.getId(),paging);
	            	removedStatuses.put(remUser.getId(), userStatuses); // OBS: here we use statuses instead of 
					
					System.out.println("Done with (removed): @" + remUser.getScreenName());
					
				}
				
				
				// get statuses updates 
				// get last status id
				Long lastStatusId = getLastStatusId(u);
				
				Paging paging;
				if (lastStatusId != null)
					paging = new Paging(lastStatusId); // gets statuses since then
				else 
					paging = new Paging(1, 50); // no status were previously fetched, fetch now
				
            	ResponseList<Status> userNewStatuses = twitter.getUserTimeline(u.getId(),paging);
            	addedStatuses.put(u.getId(), userNewStatuses); // OBS: here we use statuses instead of 
	           
	            System.out.println("Done with: @" + u.getScreenName());
				
			
			} catch(Exception e) {
				System.err.println(e.getMessage());
				System.out.println("Not possible for: "+u.getScreenName()+" skipping...");
			}
		}// end for each previously fetched user
		
		Output.saveUsers(addedUsers, TWITTER_ADDED_USER_FILE);
		Output.saveToCSVFile(addedNetwork, TWITTER_ADDED_NETWORK_FILE);
		Output.saveToCSVFile(removedNetwork, TWITTER_REMOVED_NETWORK_FILE);
		
		Output.saveStatuses(addedStatuses, TWITTER_ADDED_STATUSES_FILE);
		Output.saveStatuses(removedStatuses, TWITTER_ADDED_STATUSES_FILE);
		Output.saveStatuses(statuses, UPDATED_TWITTER_STATUSES_FILE);
		
		
		
		
	}
	
	
	public static void main(String[] args) {
		TimeSpanCrawler crawler = new TimeSpanCrawler();
		try {
			crawler.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
