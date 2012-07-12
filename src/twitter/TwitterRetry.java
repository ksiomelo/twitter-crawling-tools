package twitter;

import java.util.HashSet;

import twitter4j.IDs;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class TwitterRetry { // hack to extend Twitter4J and retry if rate limit exceed
	
	private Twitter twitter;
	private static TwitterRetry _instance = null;
	
	private TwitterRetry(){
		twitter = new TwitterFactory().getInstance();
	}
	
	public static TwitterRetry getInstance(){
		if (_instance == null) _instance = new TwitterRetry();
		return _instance;
	}
	
	private void checkRateLimitStatus()  {
		try {
		RateLimitStatus limit = twitter.getRateLimitStatus();
		System.out.print("- limit: "+limit.getRemainingHits() +"\n");
		if (limit.getRemainingHits() <= 2) {
			int remainingTime = limit.getSecondsUntilReset();
			System.out.println("Twitter request rate limit reached. Waiting "+remainingTime/60+" minutes to request again.");
			
			try {
				Thread.sleep(remainingTime*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		} catch (TwitterException te) {
			System.err.println(te.getMessage());
			if (te.getStatusCode()==503) {
				try {
					Thread.sleep(120*1000);// wait 2 minutes
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
			
		}
	}
	
	
	public AccessToken getOAuthAccessToken () throws TwitterException {
		return twitter.getOAuthAccessToken();
	}
	public IDs getFollowersIDs(Long userId, Long cursor) throws TwitterException {
		System.out.print("[request] getFollowersIDs:"+userId +" ");
		checkRateLimitStatus();
		return twitter.getFollowersIDs(userId, cursor);
	}
	
	public IDs getFriendsIDs(Long userId, Long cursor) throws TwitterException {
		System.out.print("[request] getFriendsIDs:"+userId +" ");
		checkRateLimitStatus();
		return twitter.getFriendsIDs(userId, cursor);
	}
	
	public IDs getFriendsIDs(String screename, Long cursor) throws TwitterException {
		System.out.print("[request] getFriendsIDs:"+screename +" ");
		checkRateLimitStatus();
		return twitter.getFriendsIDs(screename, cursor);
	}
	
	public User showUser(Long userId) throws TwitterException {
		System.out.print("[request] showUser:"+userId +" ");
		checkRateLimitStatus();
		return twitter.showUser(userId);
	}
	
	public User showUser(String userId) throws TwitterException {
		System.out.print("[request] showUser:"+userId +" ");
		checkRateLimitStatus();
		return twitter.showUser(userId);
	}
	
	public ResponseList<User> lookupUsers(long[] userIds) throws TwitterException {
		System.out.print("[request] lookupUsers ");
		checkRateLimitStatus();
		return twitter.lookupUsers(userIds);
	}
	public ResponseList<User> lookupUsers(String[] screenames) throws TwitterException {
		System.out.print("[request] lookupUsers ");
		checkRateLimitStatus();
		return twitter.lookupUsers(screenames);
	}
	public ResponseList<Status> getUserTimeline(long userId, Paging paging) throws TwitterException {
		System.out.print("[request] getUserTimeline:"+userId +" ");
		checkRateLimitStatus();
		return twitter.getUserTimeline(userId, paging);
	}
	
	
	
	
	
	/*
	 * Extended methods
	 */
	public HashSet<Long> getFriendsIDs(Long userId, int max) throws Exception {
		HashSet<Long> ret = new HashSet<Long>();
		long cursor = -1;
		IDs ids;
		int count = 0;
		do {
			if (max > 0 && ret.size() >= max) {
				break;
			}

			ids = this.getFriendsIDs(userId, cursor);

			for (int i = 0; i < ids.getIDs().length; i++) {

				if (max > 0 && i >= max) {
					break;
				}

				long id = ids.getIDs()[i];
				ret.add(id);
			}

		} while ((cursor = ids.getNextCursor()) != 0);

		return ret;
	}

	public HashSet<Long> getFollowersIDs(Long userId, int max) throws Exception {
		HashSet<Long> ret = new HashSet<Long>();
		long cursor = -1;
		IDs ids;
		int count = 0;
		do {
			if (max > 0 && ret.size() >= max) {
				break;
			}

			ids = this.getFollowersIDs(userId, cursor);

			for (int i = 0; i < ids.getIDs().length; i++) {

				if (max > 0 && i >= max) {
					break;
				}

				long id = ids.getIDs()[i];
				ret.add(id);
			}

		} while ((cursor = ids.getNextCursor()) != 0);

		return ret;
	}

	public HashSet<User> getFriends(User u, int max) throws Exception {
		HashSet<User> ret = new HashSet<User>();
		long cursor = -1;
		IDs ids;
		int count = 0;
		do {
			if (max > 0 && ret.size() >= max) {
				break;
			}

			ids = this.getFriendsIDs(u.getScreenName(), cursor);

			for (int i = 0; i < ids.getIDs().length; i++) {

				if (max > 0 && i >= max) {
					break;
				}

				long id = ids.getIDs()[i];
				ret.add(this.showUser(id));
			}

			count++;

		} while ((cursor = ids.getNextCursor()) != 0);

		return ret;
	}
	
	
	
}