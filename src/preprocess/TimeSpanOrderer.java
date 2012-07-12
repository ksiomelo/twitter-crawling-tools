package preprocess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import twitter4j.Status;
import twitter4j.User;

public class TimeSpanOrderer {
	
	public static HashMap<Date,List<Status>> splitStatuses(HashMap<User,List<Status>> statuses, int timeSpans){
		
		List<Status> statusCollection = new ArrayList();
		
		for (List<Status> sList : statuses.values()) {
//			for (Status s : sList) {
//				
//			}
			statusCollection.addAll(sList);
		}
		
		// sort list
		Collections.sort(statusCollection, new Comparator<Status>(){

			@Override
			public int compare(Status o1, Status o2) {

				return o1.getCreatedAt().compareTo(o2.getCreatedAt());
			}
			
		});
		
		// Split
		long tStart = statusCollection.get(0).getCreatedAt().getTime();
		long tEnd = statusCollection.get(statusCollection.size()-1).getCreatedAt().getTime();
		
		long tInterval = (tEnd-tStart)/timeSpans;
		
		for (Status s : statusCollection) {
			if (s.getCreatedAt().getTime() < (tStart+tInterval)){
				
			}
		}
		
		
		return null;
	}

}
