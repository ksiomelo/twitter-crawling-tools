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
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import utils.SetUtils;



/*
 * Crawls a time span from previous crawl
 * TODO discard nodes that appeared with SKIP_NODES_WITH_MORE_THAN
 */

public class CommunityCrawler {
	// constants
	// input files
	private final String TWITTER_NETWORK_FILE = "C:\\data\\twitter6-cluster-2.csv";

	//output files
//	private final String TWITTER_ADDED_STATUSES_FILE = "C:\\data\\twitter4-statuses_2.txt";
//	private final String TWITTER_ADDED_NETWORK_FILE = "C:\\data\\twitter4-network_2_added.csv";
//	private final String TWITTER_ADDED_USER_FILE = "C:\\data\\twitter4-3-3-clusters_users.txt";
//	private final String TWITTER_REMOVED_NETWORK_FILE = "C:\\data\\twitter4-network_2_removed.csv";
	
	private final String UPDATED_TWITTER_STATUSES_FILE = "C:\\data\\twitter6-cluster-2-statuses.txt";
//	private final String UPDATED_TWITTER_USER_FILE = "C:\\data\\twitter4-users_updated.txt";
	
	// previous data
	private HashMap<User,HashSet<Long>> network;
	private HashMap<Long,List<Status>> statuses;
	private List<User> users;
	
	// new data
	private HashMap<Long,HashSet<Long>> addedNetwork;
	private HashMap<Long,List<Status>> addedStatuses;
	private List<User> addedUsers;
	//private List<User> removedUsers; // TODO
	private HashMap<Long,Set<Long>> removedNetwork; // TODO
	
	
	private TwitterRetry twitter;
	
	public CommunityCrawler(){
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
	private void readFiles() throws IOException, TwitterException {
		
		//network =  Input.readNetworkCSVWeighted(twitter, TWITTER_NETWORK_FILE);
		
		users = Input.readUsersFromCSVFile("C:\\data\\twitter6-cluster-10.csv");//new ArrayList(network.keySet());
		
		statuses = new HashMap<Long, List<Status>>();
		
		addedNetwork = new HashMap<Long, HashSet<Long>>();
		removedNetwork = new HashMap<Long, Set<Long>>();
		addedStatuses = new HashMap<Long, List<Status>>();
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
	public void run() throws IOException, TwitterException{
		
		// Read files
		this.readFiles();
		//Output.saveUsers(users, network, TWITTER_ADDED_USER_FILE);
		
		
		for (User u : users ) {
			try {
			
						
				// get user statuses
				Paging paging = new Paging(1, 200);
            	ResponseList<Status> userStatuses = twitter.getUserTimeline(u.getId(),paging);
            	statuses.put(u.getId(), userStatuses); // OBS: here we use statuses instead of 
	           
	            System.out.println("Done with (new): @" + u.getId());
			
			
			} catch(Exception e) {
				System.err.println(e.getMessage());
				System.out.println("Not possible for: "+u.getId()+" skipping...");
			}
		}// end for each previously fetched user
		
		//Output.saveUsers(users, network, TWITTER_ADDED_USER_FILE);
		//Output.saveToCSVFile(addedUsers,addedNetwork, TWITTER_ADDED_NETWORK_FILE);
		Output.saveStatuses(statuses, UPDATED_TWITTER_STATUSES_FILE);
		//Output.saveToCSVFile(removedNetwork, TWITTER_REMOVED_NETWORK_FILE);
		
	}
	
	
	public static void main(String[] args) {
		CommunityCrawler crawler = new CommunityCrawler();
		try {
			crawler.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

}
