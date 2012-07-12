package crawler;

public interface Constants {
	
	
	//ENTER YOUR KEYS HERE
	public static final String YAHOO_API_KEY = "";
	public static final String ALCHEMY_API_KEY = "";
	
	
	
	public static final String[] STOP_WORDS = {"day", "rt", "new","youtube","happy", "time", "best", "birthday", "good", "love", "yeah"};
	
	
	
	public static final String TERM_EXTRACTOR_INPUT_FILE = "/Users/cassiomelo/Dropbox/code/twittertest/data/T_nico-rosberg_content_ouput.txt";
	public static final String SEARCH_TWEETS_BY_LOCATION_USERS_OUTPUT_FILE = "/Users/cassiomelo/Dropbox/code/twittertest/data/T_nico-rosberg_users_ouput.txt";
	
	public static final String TERM_EXTRACTOR_OUTPUT_FILE = "/Users/cassiomelo/Dropbox/code/twittertest/data/TE-nico-rosberg_output.txt";
	
	// network crawl
	public static final String NETWORK_CONTENT_FILE = "/Users/cassiomelo/Dropbox/code/twittertest/data/twitter.txt";
	public static final String NETWORK_TERM_FILE = "/Users/cassiomelo/Dropbox/code/twittertest/data/twitter_term.txt";
	

	
	
	
	public static final String FORMAL_CONTEXT_FILE = "/Users/cassiomelo/Dropbox/code/twittertest/data/twitter.cxt";
	
	
	// similarity
	public static final String SIMILARITY_NETWORK_FILE = "/Users/cassiomelo/Dropbox/code/twittertest/data/twitter_sim.csv";
	
	
	public static final String IDENTIFIER = "#\\*#"; //"#\\*#"
	public static final String NEW_IDENTIFIER = "#~#";
	

}
