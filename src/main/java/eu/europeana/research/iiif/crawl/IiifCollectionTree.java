package eu.europeana.research.iiif.crawl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.util.HttpRequestException;
import inescid.util.HttpUtil;

public class IiifCollectionTree {
	String collectionUri;
	String label;
	List<IiifCollectionTree> subcollections=new ArrayList<>();
	
	
	public IiifCollectionTree(String collectionUri) {
		this.collectionUri = collectionUri;
	}
	
	public IiifCollectionTree() {
	}

	public void fetchFromPresentationApi() throws IOException, InterruptedException {
		if(StringUtils.isEmpty(collectionUri))
			return;
		Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
		try {
			String collectionJson = HttpUtil.makeRequestForContent(collectionUri);
			Collection m = gson.fromJson(collectionJson, Collection.class);
			if(m.collections!=null)
				for(Reference r: m.collections) {
					IiifCollectionTree sc = new IiifCollectionTree(r.id);
					sc.setLabel(r.label);
					sc.fetchFromPresentationApi();
					subcollections.add(sc);
				}
		} catch (JsonSyntaxException | HttpRequestException e) {
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
}
