package inescid.dataaggregation.casestudies.coreference.old;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import inescid.util.datastruct.MapOfSets;

public class SameAsSetsJava {
	MapOfSets<String, String> uriIndex=null;
	String datasetId;
	File storeFile;
	
	public SameAsSetsJava(File storeFolder, String datasetId) {
		this.datasetId = datasetId;
		storeFile=new File(storeFolder, datasetId+".bin");
	}
	
	public synchronized boolean addSameAsIfOverlap(String subject, String object) {
			boolean containsSubject = uriIndex.containsKey(subject);
			boolean containsObject = uriIndex.containsKey(object);
			if(containsSubject || containsObject) {
				Set<String> sameAsSet = null;
				if(containsSubject && containsObject) {
					sameAsSet = uriIndex.get(subject);
					Set<String> sameAsSetObj = uriIndex.get(object);
					if(sameAsSet.size()==sameAsSetObj.size() && sameAsSet.containsAll(sameAsSetObj))
						return false;
					sameAsSet.addAll(sameAsSetObj);
					for(String u: sameAsSet)
						uriIndex.set(u, sameAsSet);
					return true;
				} else if(containsSubject) {
					sameAsSet = uriIndex.get(subject);
					sameAsSet.add(object);
					uriIndex.set(object, sameAsSet);
					return true;
				} else {
					sameAsSet = uriIndex.get(object);
					sameAsSet.add(subject);
					uriIndex.set(subject, sameAsSet);
					return true;
				}
			} 
			return false;
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
				uriIndex.set(object, sameAsSet);
			} else {
				sameAsSet = uriIndex.get(object);
				sameAsSet.add(subject);
			}
			uriIndex.set(subject, sameAsSet);
			uriIndex.set(object, sameAsSet);
		} else {
			Set<String> sameAsSetSub = java.util.Collections.synchronizedSet(new HashSet<String>(5));
			sameAsSetSub.add(subject);
			sameAsSetSub.add(object);
			uriIndex.set(subject, sameAsSetSub);
			uriIndex.set(object, sameAsSetSub);
		} 
	}

	public int size() {
		return uriIndex.size();
	}

	public MapOfSets<String, String> getUriIndex() {
		return uriIndex;
	}

	public synchronized Set<String> putSameAsSet(Set<String> set) {
		Set<String> toAdd=new HashSet<String>(set);
		for(String uri : set) {
			uriIndex.set(uri, toAdd);
		}
		return toAdd;
	}
	
	public  synchronized void commit() {
	}

	public  synchronized void closeStore() throws IOException {
		ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(storeFile));
		out.writeObject(uriIndex);
		out.close();
	}
	

	public synchronized Set<String> addSet(Set<String> uriSet) {
		Set<String> toAdd=null;
		for(String uri:uriSet) {
			toAdd = uriIndex.get(uri);
			if(toAdd!=null)
				break;
		}
		if(toAdd==null)
			toAdd=new HashSet(uriSet);
		return putSameAsSet(uriSet);
	}

	public synchronized void clear() {
		getUriIndex().clear();
	}

	public synchronized Set<String> getSameAsSet(String uri) {
		return getUriIndex().get(uri);
	}
}
