package inescid.dataaggregation.casestudies.coreference.old;

import java.util.HashMap;
import java.util.HashSet;

public class SameAsSets {
	HashMap<String, HashSet<String>> uriIndex=new HashMap<String, HashSet<String>>(1000);
	
	public void addSameAs(String subject, String object) {
		boolean containsSubject = uriIndex.containsKey(subject);
		boolean containsObject = uriIndex.containsKey(object);
		if(containsSubject || containsObject) {
			if(containsSubject && containsObject) {
				HashSet<String> sameAsSetSub = uriIndex.get(subject);
				HashSet<String> sameAsSetObj = uriIndex.get(object);
				if(sameAsSetSub != sameAsSetObj) {
					sameAsSetSub.addAll(sameAsSetObj);
					uriIndex.put(object, sameAsSetSub);
				}
			} else if(containsSubject) {
				HashSet<String> sameAsSetSub = uriIndex.get(subject);
				sameAsSetSub.add(object);
				uriIndex.put(object, sameAsSetSub);
			} else {
				HashSet<String> sameAsSetObj = uriIndex.get(object);
				sameAsSetObj.add(subject);
				uriIndex.put(subject, sameAsSetObj);
			}
		} else {
			HashSet<String> sameAsSetSub = new HashSet<String>(5);
			sameAsSetSub.add(subject);
			sameAsSetSub.add(object);
			uriIndex.put(subject, sameAsSetSub);
			uriIndex.put(object, sameAsSetSub);
		} 
	}

	public int size() {
		return uriIndex.size();
	}
	
}
