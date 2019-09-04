package eu.europeana.research.iiif.crawl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import inescid.util.AccessException;
import inescid.util.HttpUtil;

public class IiifCollectionTree {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(IiifCollectionTree.class);
	
	int maxSubcollections=50;
	String collectionUri;
	String label;
	String attribution;
	List<IiifCollectionTree> subcollections=new ArrayList<>();
	Boolean hasSubCollectionsButNotRetrieved=null;
	
	
	public IiifCollectionTree(String collectionUri) {
		this.collectionUri = collectionUri;
	}
	
	public IiifCollectionTree() {
	}

	public void checkForSubcollectionsFromPresentationApi() throws IOException, InterruptedException {
		if(StringUtils.isEmpty(collectionUri))
			return;
		Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
		try {
			String collectionJson = HttpUtil.makeRequestForContent(collectionUri, "accept", "application/json");
			Collection m = gson.fromJson(collectionJson, Collection.class);
			hasSubCollectionsButNotRetrieved=m.collections==null || m.collections.length>0;
		} catch (JsonSyntaxException | AccessException e) {
			throw new IOException("On collection "+collectionUri, e);
		}
		
	}
	public void fetchFromPresentationApi(boolean with2ndSublevel) throws IOException, InterruptedException {
		if(StringUtils.isEmpty(collectionUri))
			return;
		Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
		try {
			String collectionJson = HttpUtil.makeRequestForContent(collectionUri, "accept", "application/json");
			Collection m = gson.fromJson(collectionJson, Collection.class);
			label=m.label;
			attribution=m.attribution;
			if(m.collections!=null)
				for(Reference r: m.collections) {
					try {
						IiifCollectionTree sc = new IiifCollectionTree(r.id);
						sc.setLabel(r.label);
						if(with2ndSublevel)
							sc.fetchFromPresentationApi(false);
						else
							sc.checkForSubcollectionsFromPresentationApi();
						subcollections.add(sc);
						if(subcollections.size()>=maxSubcollections || (!with2ndSublevel && subcollections.size()>=maxSubcollections/2)) {
							log.info("Too many sucollections. skipping: "+ r.id);
							break;
						}
					} catch (Exception e) {
						log.info("Sub collection failed: "+ r.id, e);
					}
				}
		} catch (JsonSyntaxException | AccessException e) {
			throw new IOException("On collection "+collectionUri, e);
		}
	}

	public void setLabel(String label) {
		this.label=label;
	}

	public String getCollectionUri() {
		return collectionUri;
	}

	public List<IiifCollectionTree> getSubcollections() {
		return subcollections;
	}

	public String getLabel() {
		return label;
	}

	public Boolean getHasSubCollectionsButNotRetrieved() {
		return hasSubCollectionsButNotRetrieved;
	}

	public void setHasSubCollectionsButNotRetrieved(Boolean hasSubCollectionsButNotRetrieved) {
		this.hasSubCollectionsButNotRetrieved = hasSubCollectionsButNotRetrieved;
	}

	public String getAttribution() {
		return attribution;
	}
}
