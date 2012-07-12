package utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CollectionUtils {
	
	public static boolean containsIgnoreCase(List <String> l, String s){
		 Iterator <String> it = l.iterator();
		 while(it.hasNext()){
		  if(it.next().equalsIgnoreCase(s))
		  return true;
		 }
		 return false;
		}
	
	
	public static int addIfNotThere(Collection<String> c, String a) {
		
		Iterator it = c.iterator();
		int index = 0;
		while(it.hasNext()){
			String b = (String) it.next();
			
			if (b.equalsIgnoreCase(a)) return index;
			
			index++;
		}
		
		c.add(a);
		
		return c.size()-1; // return the index of last added element
		
		
	}

}
