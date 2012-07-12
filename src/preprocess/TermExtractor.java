package preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import crawler.Constants;

import twitter.io.Input;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONObject;




public class TermExtractor {
	
	public static final int TERM_EXTRACTOR = 1;
	public static final int CONCEPT_TAGGING = 2;
	public static final int TEXT_CATEGORIZATION = 3;
	
	
	HttpURLConnection connection;
	 
	 URL url;
	
	public TermExtractor() {
		String request = "http://search.yahooapis.com/ContentAnalysisService/V1/termExtraction";
		
		try {
			url = new URL(request);
			 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* 
	 * Extract terms using alchemy API service
	 */
	private JSONArray alchemyTermExtractor(String tweet, int extractionType, boolean onlyText) throws Exception {
		HttpURLConnection connection;
		
		String keywordUrl = "http://access.alchemyapi.com/calls/text/TextGetRankedKeywords";
		String conceptUrl = "http://access.alchemyapi.com/calls/text/TextGetRankedConcepts";
		
		URL url;
		if (extractionType == CONCEPT_TAGGING) {
		url = new URL(conceptUrl);
		} else {// else do term extraction
			url = new URL(keywordUrl);
		}
		
		
		connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setAllowUserInteraction(true);
		
		 String data = URLEncoder.encode("apikey", "UTF-8") + "=" + URLEncoder.encode(Constants.ALCHEMY_API_KEY, "UTF-8");
		    data += "&" + URLEncoder.encode("text", "UTF-8") + "=" + URLEncoder.encode(tweet, "UTF-8");
		    data += "&" + URLEncoder.encode("outputMode", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8");
		    
		    if (extractionType == CONCEPT_TAGGING) {
		    	data += "&" + URLEncoder.encode("maxRetrieve", "UTF-8") + "=" + URLEncoder.encode("12", "UTF-8");
		    }
		    
		    
		    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
		    wr.write(data);
		    wr.flush();
		    
		    
		    // Get the response
		    BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    String line;
		    String jsonReponse = "";
		    while ((line = rd.readLine()) != null) {
		        // Process line...
		    	//ps.print(line);
		    	jsonReponse += line;
		    	
		    }
		    
		    JSONObject obj = null;
		    JSONArray terms = null;
		    try{
		    obj = new JSONObject(jsonReponse);
		    
		    if (extractionType == CONCEPT_TAGGING)
		    	terms = (JSONArray) obj.get("concepts");
		    else
		    	terms = (JSONArray) obj.get("keywords");
		    } catch(Exception e) {
		    	System.err.println(e.getMessage());
		    	System.out.println("skipping...");
		    	wr.close();
				rd.close();
		    	return new JSONArray();
		    }
		    
		    
		    // for skipping relevance
		    if (onlyText) {
			    JSONArray ret = new JSONArray();
			    for (int i = 0; i < terms.length(); i++) {
					ret.put(((JSONObject)terms.get(i)).get("text"));
				}
			    terms = ret;
		    }
		    
		    wr.close();
			rd.close();
		    
		    return terms;//terms.join(",");
		
	}
	
	/*
	 * Extract terms using Yahoo term extractor service
	 */
	private JSONArray yahooTermExtractor(String tweet) throws Exception {
		connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setAllowUserInteraction(true);
		
		 String data = URLEncoder.encode("appid", "UTF-8") + "=" + URLEncoder.encode(Constants.YAHOO_API_KEY, "UTF-8");
		    data += "&" + URLEncoder.encode("context", "UTF-8") + "=" + URLEncoder.encode(tweet, "UTF-8");
		    data += "&" + URLEncoder.encode("output", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8");
		    
		    
		    OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
		    wr.write(data);
		    wr.flush();
		    
		    
		    // Get the response
		    int tryouts = 0;
		    BufferedReader rd = null;
		    while (rd == null) {
			    try {
			    	 rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			    }catch (IOException e) {
			    	
			    	System.err.println(e.getMessage());
			    	
			    	if (tryouts > 5) {
			    		System.out.println("impossible to fetch data, skipping...");
			    		return new JSONArray();
			    	}
			    	
			    	System.out.println("waiting 60 secondes before next try...");
			    	Thread.sleep(60*1000);
			    	tryouts++;
			    }
		    }
		    
		    String line;
		    String jsonReponse = "";
		    while ((line = rd.readLine()) != null) {
		        // Process line...
		    	//ps.print(line);
		    	jsonReponse += line;
		    	
		    }
		    
		    JSONObject obj = new JSONObject(jsonReponse);
		    JSONArray terms = (JSONArray) ((JSONObject) obj.get("ResultSet")).get("Result");
		    
		    wr.close();
			rd.close();
		    
		    return terms;//terms.join(",");
		
	}
	
	
	
	/**
	 * Given a input file format: 
	 * USERNAME N
	 * STATUS 1
	 * ..
	 * STATUS N
	 * 
	 * it extracts terms and saves in this format (% IS THE IDENTIFIER):
	 * 
	 * USERNAME%USERNAME%TERMS
	 * 
	 * @param inputfile
	 * @param outputfile
	 */
	public void extractTermsFromNetworkFile(String inputfile, String outputfile) throws Exception {
		
		FileOutputStream fout = new FileOutputStream(new File(outputfile));
		PrintStream ps = new PrintStream(fout);//.print(connection.getInputStream());
		 
		 
		// read input file line by line
		BufferedReader reader = new BufferedReader(new FileReader(
				Constants.NETWORK_CONTENT_FILE));
		String line = null;

		line = reader.readLine();


		while (line != null) {
			
			if (line.equals("")) { // skip blank lines
				line = reader.readLine();
				continue;
			}

			String [] lineArray = line.split(" ");
			
			if (lineArray.length < 2) {
				line = reader.readLine();
				continue;
			}
			
			
			String username = lineArray[0];
			int tweetCount = 0;
			try {
			 tweetCount = Integer.parseInt(lineArray[1]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String tweetsString = "";
			
			for (int i = 0; i < tweetCount; i++) {
				tweetsString += reader.readLine();
			}
			
			// extract terms from current tweet
			JSONArray extractedTerms = this.yahooTermExtractor(tweetsString);
			
			ps.println(username+ Constants.IDENTIFIER + username+ Constants.IDENTIFIER + extractedTerms);
			
			line = reader.readLine();
		}
		ps.close();
		connection.disconnect();

	}
	
	/**
	 * Given a input file format:
	 * %USER_ID%USERNAME%COUNT
	 * %USER_ID%STATUS_ID%RETWEET...
	 * 
	 * Each status is one REQUEST
	 * @param inputfile
	 * @param outputfile
	 * @throws Exception
	 */
	public void extractTermsFromContentFile(String inputfile, String outputfile) throws Exception {
		
		FileOutputStream fout = new FileOutputStream(new File(outputfile));
		PrintStream ps = new PrintStream(fout);//.print(connection.getInputStream());
		 
		 
		/*
		 * #\*#76298119#\*#aMrazing#\*#200
			#\*#76298119#\*#212231274398613504#\*#false#\*#1339434853000#\*#Search for keywords: "wasit anjing" and "wasit ngentot". HILARIOUS! =))))
			#\*#76298119#\*#212230643231358977#\*#false#\*#1339434703000#\*##NgedenDiTimeline RT @masova: hnnngggghhhh….
		 */
		
		 
		// read input file line by line
		BufferedReader reader = new BufferedReader(new FileReader(
				Constants.TERM_EXTRACTOR_INPUT_FILE));
		String line = null;

		line = reader.readLine();

		int count = 0;

		while (line != null) {

			String [] lineArray = line.split("#\\*#");
			
			if (lineArray.length < 2) {
				line = reader.readLine();
				continue;
			}
			
			String tweet = lineArray[5];
			
			// extract terms from current tweet
			String extractedTerms = this.yahooTermExtractor(tweet).join(",");
			
			ps.println(lineArray[0]+ Constants.IDENTIFIER + lineArray[1]+ Constants.IDENTIFIER + extractedTerms);
			
			count++;
			line = reader.readLine();
		}
		
		
		ps.close();
		connection.disconnect();
		 

	}
	

	/*******************************************
	 * Given
	 * @param users
	 * @param statusesFile
	 * @param termsOutputfile
	 * @param fromUserId
	 * @throws Exception
	 */
	public void extractTermsInBatchFromUsers(List<User> users, String statusesFile, String termsOutputfile, String fromUserId) throws Exception { 
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(termsOutputfile),"UTF8")); 
		
		HashMap<Long, List<Status>> userStatuses = Input.readStatuses(statusesFile);
		
		boolean fromHere =false;
		for(User u : users) {
			
			if (fromUserId != null && u.getId() == Long.parseLong(fromUserId)) fromHere = true;
			if (fromUserId != null && !fromHere)  // not the user id yet, go to next 
				continue;
			
			
			if (!userStatuses.containsKey(u.getId())) {
				System.out.println("no status detected for user: "+u.getId());
				continue;
			}
			
			String tweets = "";
			for (Status s : userStatuses.get(u.getId())){
				tweets += s.getText() + "\n";
			}
			
			
			String extractedTerms = this.alchemyTermExtractor(tweets, CONCEPT_TAGGING,true).join(",");
			out.write(u.getId() + Constants.NEW_IDENTIFIER + extractedTerms + System.getProperty("line.separator"));
			System.out.println("done for: "+u.getId());
		
		}
		
		
	}
	
	/*********************************************
	 * for a status input file format:
	 * %USER_ID%USERNAME%COUNT
	 * %USER_ID%STATUS_ID%RETWEET...
	 * 
	 * grabs all status from user and sends a request
	 * @param statusesFile
	 * @param termsOutputfile
	 * @param fromUserId 	to begin from user id
	 */
	public void extractTermsInBatch(String statusesFile, String termsOutputfile, String fromUserId) throws Exception { 
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(termsOutputfile),"UTF8")); 
		
		HashMap<Long, List<Status>> userStatuses = Input.readStatuses(statusesFile);
		
		int errorCount = 0;
		boolean fromHere =false;
		for(Long userId : userStatuses.keySet()) {
			
			if (fromUserId != null && userId == Long.parseLong(fromUserId)) fromHere = true;
			if (fromUserId != null && !fromHere)  // not the user id yet, go to next 
				continue;
			
			
			if (!userStatuses.containsKey(userId)) {
				System.out.println("no status detected for user: "+userId);
				continue;
			}
			
			String tweets = "";
			for (Status s : userStatuses.get(userId)){
				tweets += s.getText() + "\n";
			}
			
			try { 
			//String extractedTerms = this.alchemyTermExtractor(tweets, CONCEPT_TAGGING, true).join(",");
			String extractedTerms = this.yahooTermExtractor(tweets).join(",");
			
			out.write(userId + Constants.NEW_IDENTIFIER + extractedTerms + System.getProperty("line.separator"));
			System.out.println("done for: "+userId);
			} catch (Exception e) {
				errorCount++;
				System.err.println("ERROR: "+e.getMessage());
				System.out.println("not possible for user "+userId + " skipping...");
				
				if (errorCount == 10) {
					out.close();
					System.exit(1);
				}
			}
		
		}
		out.close();
		
	}
	
	
	public void extractTermsInBatchFromContentFile(String inputfile, String outputfile, boolean useRelevance, String fromUserId) throws Exception {
		
		FileOutputStream fout = new FileOutputStream(new File(outputfile));
		PrintStream ps = new PrintStream(fout);//.print(connection.getInputStream());
		 
		 
		/*
		 * #\*#76298119#\*#aMrazing#\*#200
			#\*#76298119#\*#212231274398613504#\*#false#\*#1339434853000#\*#Search for keywords: "wasit anjing" and "wasit ngentot". HILARIOUS! =))))
			#\*#76298119#\*#212230643231358977#\*#false#\*#1339434703000#\*##NgedenDiTimeline RT @masova: hnnngggghhhh….
		 */
		
		 
		// read input file line by line
		BufferedReader reader = new BufferedReader(new FileReader(inputfile));
		String line = null;

		line = reader.readLine();

		boolean fromHere = false;
		int count = 0;
		int missed = 0;
		while (line != null) {
			
			String [] lineArray = line.replace("\\","").split("#\\*#");
			
			if (4 != lineArray.length) {
				System.out.println("skipping... line: "+line);
				missed++;
				line = reader.readLine();
				continue;
			}
			
			String userId = lineArray[1];
			
			// continue fetching from a given user id (null to fetch it all)
			if (fromUserId != null && userId.equals(fromUserId)) fromHere = true;
			if (fromUserId != null && !fromHere) { // not the user id yet, go to next 
				line = reader.readLine();
				lineArray = line.replace("\\","").split("#\\*#");
				
				while (4 != lineArray.length) {
					line = reader.readLine(); // skip user statuses
					lineArray = line.replace("\\","").split("#\\*#");
				}
				continue;
			} //else System.out.println("continue from "+fromUserId);
			
			
			
			
			int size = 0;
			try {
			size = Integer.parseInt(lineArray[3]);
			} catch (Exception e) {
				System.err.println("error");
			}
			
			
			
			String tweets = "";
			for (int i = 0; i < size; i++) {
				line = reader.readLine();
				lineArray = line.replace("\\","").split("#\\*#");
				
				if (5 < lineArray.length)
					tweets += lineArray[5] + "\n";
				else size++;
			}
			
						
			// extract terms from current tweet
			String extractedTerms = "";
			if (useRelevance) {
				JSONArray res = this.alchemyTermExtractor(tweets,CONCEPT_TAGGING, false);
				
				    for (int i = 0; i < res.length(); i++) {
				    	extractedTerms += (((JSONObject)res.get(i)).get("text")) + "%" + (((JSONObject)res.get(i)).get("relevance")) + "%";
				    	if (i < res.length()-1) extractedTerms += ",";
				    }
			} else {
				extractedTerms = this.alchemyTermExtractor(tweets, CONCEPT_TAGGING,true).join(",");
			}
			
			ps.println(userId + Constants.NEW_IDENTIFIER + extractedTerms.replace(System.getProperty("line.separator"), ""));
			System.out.println("done for: "+userId);
			
			count++;
			line = reader.readLine();
		}
		
		System.out.println("Finished. Missed lines: "+missed);
		ps.close();
		//connection.disconnect();
		 

	}
	
	
	
	
	
	 public static void main(String args[]) {
		 
		 try {
			 
			 System.out.println("Starting Term Extraction process...");
			 TermExtractor te = new TermExtractor();
			 
			// get status from cluster
			// 
			String statusesFile = "/Users/cassiomelo/code/datasets/Twitter/twitter6-cluster-2-statuses.txt"; //"C:\\data\\twitter6-cluster-3-statuses.txt";
			String termsOutputfile = "/Users/cassiomelo/code/datasets/Twitter/twitter6-cluster-2-terms.txt";//"C:\\data\\twitter6-cluster-3-terms.txt";
			
			te.extractTermsInBatch(statusesFile, termsOutputfile, null);

			 
		 } catch (Exception e) {
			 e.printStackTrace();
			 
		 }
	 
	 }
	
	

}
