package inescid.dataaggregation.casestudies.coreference;

import java.util.HashSet;
import java.util.Set;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

public class SameAsSets {
	MVMap<String, Set<String>> uriIndex=null;
	
	public SameAsSets(MVStore mvStore, String datasetId) {
		uriIndex = mvStore.openMap(datasetId);
	}
	
	public synchronized boolean addSameAsIfOverlap(String subject, String object) {
	    MVStore.TxCounter txCounter = uriIndex.getStore().registerVersionUsage();
		try {
			boolean containsSubject = uriIndex.containsKey(subject);
			boolean containsObject = uriIndex.containsKey(object);
			if(containsSubject || containsObject) {
				Set<String> sameAsSet = null;
				if(containsSubject && containsObject) {
					sameAsSet = uriIndex.get(subject);
					Set<String> sameAsSetObj = uriIndex.get(object);
					if(sameAsSet.size()==sameAsSetObj.size() && sameAsSet.containsAll(sameAsSetObj))
						return false;
					CoreferenceDebugger.INSTANCE.debugMerge(subject, object, sameAsSet, sameAsSetObj);
					sameAsSet.addAll(sameAsSetObj);
					for(String u: sameAsSet)
						uriIndex.put(u, sameAsSet);
					return true;
				} else if(containsSubject) {
					sameAsSet = uriIndex.get(subject);
						CoreferenceDebugger.INSTANCE.debugMerge(subject, object, sameAsSet, null);
					sameAsSet.add(object);
					uriIndex.put(object, sameAsSet);
					return true;
				} else {
					sameAsSet = uriIndex.get(object);
					sameAsSet.add(subject);
						CoreferenceDebugger.INSTANCE.debugMerge(subject, object, null, sameAsSet);
					uriIndex.put(subject, sameAsSet);
					return true;
				}
			} 
			return false;
		} finally {
			uriIndex.getStore().deregisterVersionUsage(txCounter);
		}
	}
	
	public synchronized void addSameAs(String subject, String object) {
		boolean containsSubject = uriIndex.containsKey(subject);
		boolean containsObject = uriIndex.containsKey(object);
		if(containsSubject || containsObject) {
			Set<String> sameAsSet = null;
			if(containsSubject && containsObject) {
				sameAsSet = uriIndex.get(subject);
				Set<String> sameAsSetObj = uriIndex.get(object);
				if(sameAsSet != sameAsSetObj) 
					sameAsSet.addAll(sameAsSetObj);
			} else if(containsSubject) {
				sameAsSet = uriIndex.get(subject);
				sameAsSet.add(object);
				uriIndex.put(object, sameAsSet);
			} else {
				sameAsSet = uriIndex.get(object);
				sameAsSet.add(subject);
			}
			uriIndex.put(subject, sameAsSet);
			uriIndex.put(object, sameAsSet);
		} else {
			Set<String> sameAsSetSub = java.util.Collections.synchronizedSet(new HashSet<String>(5));
			sameAsSetSub.add(subject);
			sameAsSetSub.add(object);
			uriIndex.put(subject, sameAsSetSub);
			uriIndex.put(object, sameAsSetSub);
		} 
	}

	public int size() {
		return uriIndex.size();
	}

	public MVMap<String, Set<String>> getUriIndex() {
		return uriIndex;
	}

	public synchronized Set<String> putSameAsSet(Set<String> set) {
		Set<String> toAdd=java.util.Collections.synchronizedSet(new HashSet<String>(set));
		for(String uri : set) {
			uriIndex.put(uri, toAdd);
		}
		return toAdd;
	}
	
	public  synchronized void commit() {
		uriIndex.getStore().commit();
	}

	public  synchronized void closeStore() {
		uriIndex.getStore().close();
	}

	public synchronized Set<String> addSet(Set<String> uriSet) {
		Set<String> toAdd=null;
		for(String uri:uriSet) {
			toAdd = uriIndex.get(uri);
			if(toAdd!=null)
				break;
		}
		if(toAdd==null)
			toAdd=java.util.Collections.synchronizedSet(new HashSet(uriSet));
		return putSameAsSet(uriSet);
	}

	public synchronized void clear() {
		getUriIndex().clear();
	}

	public synchronized Set<String> getSameAsSet(String uri) {
		return getUriIndex().get(uri);
	}
	
	
}



