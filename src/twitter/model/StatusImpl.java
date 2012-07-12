package twitter.model;

import java.util.Date;

import crawler.Constants;


import twitter4j.Annotations;
import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class StatusImpl implements Status {

    private Date createdAt;
    private long id;
    private String text;
    private String source;
    private boolean isTruncated;
    private long inReplyToStatusId;
    private long inReplyToUserId;
    private boolean isFavorited;
    private String inReplyToScreenName;
    private GeoLocation geoLocation = null;
    private Place place = null;
    private long retweetCount;
    private boolean wasRetweetedByMe;
    
    private boolean isRetweet = false;

    private long[] contributors = null;
    private long[] contributorsIDs;
    private Annotations annotations = null;

    private Status retweetedStatus;
    private UserMentionEntity[] userMentionEntities;
    private URLEntity[] urlEntities;
    private HashtagEntity[] hashtagEntities;
    private MediaEntity[] mediaEntities;
    private Status myRetweetedStatus;
    
    public StatusImpl(){
    	
    }
    
    public StatusImpl(String line){
    	String [] lineArray = line.split(Constants.NEW_IDENTIFIER); 
    	
    	 // #*#204874575937404928#*#false#*#1337680879000#*#BIGBANG � SPECIAL EDITION [STILL ALIVE] http://t.co/k5GboLOL
    	this.id = Long.parseLong(lineArray[2]);
    	this.isRetweet = Boolean.parseBoolean(lineArray[3]);
    	this.createdAt = new Date(Long.parseLong(lineArray[4]));
    	this.text = lineArray[5];
    	
    }

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public boolean isTruncated() {
		return isTruncated;
	}

	public void setTruncated(boolean isTruncated) {
		this.isTruncated = isTruncated;
	}

	public long getInReplyToStatusId() {
		return inReplyToStatusId;
	}

	public void setInReplyToStatusId(long inReplyToStatusId) {
		this.inReplyToStatusId = inReplyToStatusId;
	}

	public long getInReplyToUserId() {
		return inReplyToUserId;
	}

	public void setInReplyToUserId(long inReplyToUserId) {
		this.inReplyToUserId = inReplyToUserId;
	}

	public boolean isFavorited() {
		return isFavorited;
	}

	public void setFavorited(boolean isFavorited) {
		this.isFavorited = isFavorited;
	}

	public String getInReplyToScreenName() {
		return inReplyToScreenName;
	}

	public void setInReplyToScreenName(String inReplyToScreenName) {
		this.inReplyToScreenName = inReplyToScreenName;
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}

	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	public long getRetweetCount() {
		return retweetCount;
	}

	public void setRetweetCount(long retweetCount) {
		this.retweetCount = retweetCount;
	}

	public boolean isWasRetweetedByMe() {
		return wasRetweetedByMe;
	}

	public void setWasRetweetedByMe(boolean wasRetweetedByMe) {
		this.wasRetweetedByMe = wasRetweetedByMe;
	}

	public long[] getContributors() {
		return contributors;
	}

	public void setContributors(long[] contributors) {
		this.contributors = contributors;
	}

	public long[] getContributorsIDs() {
		return contributorsIDs;
	}

	public void setContributorsIDs(long[] contributorsIDs) {
		this.contributorsIDs = contributorsIDs;
	}

	public Annotations getAnnotations() {
		return annotations;
	}

	public void setAnnotations(Annotations annotations) {
		this.annotations = annotations;
	}

	public Status getRetweetedStatus() {
		return retweetedStatus;
	}

	public void setRetweetedStatus(Status retweetedStatus) {
		this.retweetedStatus = retweetedStatus;
	}

	public UserMentionEntity[] getUserMentionEntities() {
		return userMentionEntities;
	}

	public void setUserMentionEntities(UserMentionEntity[] userMentionEntities) {
		this.userMentionEntities = userMentionEntities;
	}

	public URLEntity[] getUrlEntities() {
		return urlEntities;
	}

	public void setUrlEntities(URLEntity[] urlEntities) {
		this.urlEntities = urlEntities;
	}

	public HashtagEntity[] getHashtagEntities() {
		return hashtagEntities;
	}

	public void setHashtagEntities(HashtagEntity[] hashtagEntities) {
		this.hashtagEntities = hashtagEntities;
	}

	public MediaEntity[] getMediaEntities() {
		return mediaEntities;
	}

	public void setMediaEntities(MediaEntity[] mediaEntities) {
		this.mediaEntities = mediaEntities;
	}

	public Status getMyRetweetedStatus() {
		return myRetweetedStatus;
	}

	public void setMyRetweetedStatus(Status myRetweetedStatus) {
		this.myRetweetedStatus = myRetweetedStatus;
	}

	@Override
	public int compareTo(Status o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RateLimitStatus getRateLimitStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAccessLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public URLEntity[] getURLEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRetweet() {
		// TODO Auto-generated method stub
		return isRetweet;
	}

	@Override
	public boolean isRetweetedByMe() {
		// TODO Auto-generated method stub
		return false;
	}

}
