package eu.europeana.research.iiif.crawl;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.Set;

import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker.Deleted;
import eu.europeana.research.iiif.profile.model.Manifest;

public class CollectionCrawler {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(CollectionCrawler.class);
	
	TimestampTracker timestampTracker;
	String datasetUri;
	Integer sampleSize;
	int harvestedCount=0;
	
	Gson gson;
	Set<String> allExistingDatasetManifests;

	public CollectionCrawler(TimestampTracker timestampTracker, String datasetUri) {
		super();
		this.timestampTracker = timestampTracker;
		this.datasetUri = datasetUri;
		gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
	}

	public void setSampleSize(Integer sampleSize) {
		this.sampleSize = sampleSize;
	}
	
	public Calendar crawl(boolean updateCollectionHarvestDate) throws Exception {
		Calendar startOfCrawl=new GregorianCalendar();
		allExistingDatasetManifests = new HashSet<>();
		for(String mUrl: timestampTracker.getIterableOfObjects(datasetUri, TimestampTracker.Deleted.EXCLUDE))
			allExistingDatasetManifests.add(mUrl);
		crawlCollection(datasetUri);
		Set<String> deletedManifests=allExistingDatasetManifests;
		for(String mUrl: deletedManifests) 
			timestampTracker.setObjectTimestamp(datasetUri, mUrl, new GregorianCalendar(), true);
		if(updateCollectionHarvestDate)
			timestampTracker.setDatasetTimestamp(datasetUri, startOfCrawl);
		timestampTracker.commit();
		return startOfCrawl;
	}
	public void crawlCollection(String collectionId) throws IOException {
		String collectionJson = httpGet(collectionId);
		try {
			Collection m = gson.fromJson(collectionJson, Collection.class);
			log(collectionId+":\n"+(m.collections==null ? 0 : m.collections.length)+" collections, "+(m.manifests==null ? 0 : m.manifests.length)+" manifests");
			
			if(m.collections!=null)
				for(Reference r: m.collections) {
					crawlCollection(r.id);
				}
			if(m.manifests!=null)	
				for(Reference r: m.manifests) {
					timestampTracker.setObjectTimestamp(datasetUri, r.id, new GregorianCalendar());
					allExistingDatasetManifests.remove(r.id);
					harvestedCount++;
					if(sampleSize!=null && harvestedCount>=sampleSize)
						break;
				}
		} catch (JsonSyntaxException e) {
			log.error("Error getting collection "+collectionId, e);
			throw e;
		}
	}

	public void log(String message) {
		System.out.println(message);
	}

	public void log(String message, Exception ex) {
		System.out.println(message);
		ex.printStackTrace(System.out);
	}

	public String httpGet(String url) throws IOException {
		int tries = 0;
		while (true) {
			tries++;
			try {
				HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
				if(conn.getResponseCode()>=300 && conn.getResponseCode()<400) {
					String location = conn.getHeaderField("Location");
					if(location!=null)
						return httpGet(location);
				}
				return IOUtils.toString(conn.getInputStream(), "UTF-8");
			} catch (IOException ex) {
				if (tries >= 3)
					throw ex;
			}
		}
	}
}