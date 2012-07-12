/*
 * Author: Cassio Melo (melo.cassio at gmail.com)
 */
package crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

// TODO: preserve network distribution: normalize number of nodes
// TODO: discard re-tweet option
// TODO: multifocal approach
public class CrawlerNetwork {

	public static final String FOCAL_NODE = "cassiomelo";
	public static final int NUMBER_LINKS = 5;
	public static final int NUMBER_NODES = 40;
	public static final int NUMBER_STATUSES = 20;
	
	
	public String networkOutputFile = "/Users/cassiomelo/Dropbox/code/twittertest/data/twitter2.csv";
	public String contentOutputFile = "/Users/cassiomelo/Dropbox/code/twittertest/data/twitter2.txt";
	
	
	Twitter twitter;
	 
	 
	 public CrawlerNetwork(){
		 try {
			 
		 twitter = new TwitterFactory().getInstance();

	     AccessToken token = twitter.getOAuthAccessToken();
	     System.out.println("Access Token " +token );

	     
		 } catch (Exception e) {
			 e.printStackTrace();
			 System.exit(1);
		 }

		
	 }
	 
	
	public HashSet<User> getFriends(User u, int max) throws Exception {
    	HashSet<User> ret = new HashSet<User>();
    	long cursor = -1;
    	IDs ids;
    	int count = 0;
    	do {
    		
            ids = twitter.getFriendsIDs(u.getScreenName(), cursor);
    		
            
            for (int i = 0; i < ids.getIDs().length && i < max; i++) {
				long id = ids.getIDs()[i];
                ret.add(twitter.showUser(id));
            }
            
            count++;
            
    		
            
        } while ((cursor = ids.getNextCursor()) != 0 && count < max);
    	
    	return ret;
	}
	
	
	public void crawl() throws TwitterException, IOException{
		ArrayList<User> ret = new ArrayList<User>();
		HashMap<User,HashSet<Link>> links = new HashMap<User,HashSet<Link>>();
		HashMap<User,ResponseList<Status>> statuses = new HashMap<User,ResponseList<Status>>();
		
		String[] startnodes = { FOCAL_NODE };
		
        Twitter twitter = new TwitterFactory().getInstance();
        ResponseList<User> users = twitter.lookupUsers(startnodes);
        //ret.addAll(users);
        
        for (int i = 0; i < users.size() && ret.size() < NUMBER_NODES; i++) {

        	User user = (User) users.get(i);//it.next();
        	
        	
        	try {
        		
				// save links
	        	Iterator it2 =  this.getFriends(user, NUMBER_LINKS).iterator(); // this might throw an "unauthorized" exception that's why it should come first
	        	
	        	links.put(user, new HashSet<Link>());
	        	
	        	int countFriends = 0;
	        	while (it2.hasNext() && countFriends < NUMBER_LINKS) {
	        		User friend = (User) it2.next();
	        		
	        		Link l = new Link(user, friend);
	        		links.get(user).add(l);
	        		users.add(friend);
	        		countFriends++;
	        	}
	        	
        		// successfully fetched user info, save user
            	ret.add(user);
        		
            	// save statuses
            	Paging paging = new Paging(1, NUMBER_STATUSES);
            	ResponseList<Status> userStatuses = twitter.getUserTimeline(user.getId(),paging);
            	statuses.put(user, userStatuses);
	           
	            System.out.println("Done with: @" + user.getScreenName());
        	
        	
        	} catch (Exception e) {
        		//System.err.println(e.getMessage());
				System.out.println("Error - not authorized for: "+user.getScreenName() + " - skipping...");
				
				// remove links to people that had "unauthorized access" exception
				
				for (HashSet<Link> linksPerUser : links.values()) {
					ArrayList<Link> markedForRemoval = new ArrayList<Link>();
					for (Link curLink : linksPerUser) {
						if (curLink.from.getId() == user.getId() || curLink.to.getId() == user.getId()) {
							markedForRemoval.add(curLink);
						}
					}
					for (Link toRemoveLink : markedForRemoval) {
						linksPerUser.remove(toRemoveLink);
					}
				}
				
				
			}
        	
        	
        	
        }
        
        saveToCSVFile(ret, links);
	    saveContentToFile(statuses);
	}
	
	/*
	 * Saves the network in NET format (Pajek)
	 * http://gephi.org/users/supported-graph-formats/pajek-net-format/
	 */
	public void saveToNetFile(ArrayList<User> users, HashMap<User,HashSet<Link>> links, HashMap<User,ResponseList<Status>> statuses) throws IOException{
		 FileOutputStream fout = new FileOutputStream(new File(networkOutputFile));
		 PrintStream ps = new PrintStream(fout);
		 
		 String linksString = "*Edges\n";
		 
		 ps.println("*Vertices "+users.size());
		 
		 for (User user : users) {
			 ps.println(user.getId() + " \""+ user.getScreenName()+"\"");
			 
			 if (links.containsKey(user)) {
				 for(Link link : links.get(user)) {
					 linksString += link.toString() + "\n";
				 }
			 }
		 }
		 
		 ps.println(linksString);
		 
		 ps.close();
		 fout.close();
		
	}
	
	public void saveToCSVFile(ArrayList<User> users, HashMap<User,HashSet<Link>> links) throws IOException{
		 FileOutputStream fout = new FileOutputStream(new File(networkOutputFile));
		 PrintStream ps = new PrintStream(fout);
		 
		 for (HashSet<Link> linksPerUser : links.values()) {
			 
			 if (linksPerUser.size() > 0) {
				 
				 ps.print(linksPerUser.iterator().next().from.getScreenName() + ";");
				 
				 for (Link curLink : linksPerUser) {
					 ps.print(curLink.to.getScreenName() + ";");

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
				 
				 ps.println(statusesPerUser.iterator().next().getUser().getScreenName() + " " +statusesPerUser.size());
				 
				 for (Status curStatus : statusesPerUser) {
					 ps.println(curStatus.getText().replace("\n", "") + ""); // very important: replace line breaks

				}
			 }
			
		}
		 ps.close();
		 fout.close();
	}
	
	
	
	public static void main(String[] args) {
		
		try {
		CrawlerNetwork cn = new CrawlerNetwork();
		cn.crawl();
		
		} catch (TwitterException te) {
	        te.printStackTrace();
	        System.out.println("Failed to lookup users: " + te.getMessage());
	        System.exit(-1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	class Link{
		public User from;
		public User to;
		
		public Link(User f, User t) {
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
			Link other = (Link)obj;
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
