package eu.europeana.research.iiif.crawl;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import java.util.Set;

import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.dataaggregation.dataset.store.Repository;

public class ManifestHarvester {

	TimestampTracker manifestSource;
	String datasetUri;
	Repository repository;
	
	public ManifestHarvester(Repository repository, TimestampTracker manifestSource, String datasetUri) {
		super();
		this.manifestSource = manifestSource;
		this.datasetUri = datasetUri;
		this.repository = repository;
	}

	public void harvest() throws IOException {
		Set<String> allDatasetManifests = repository.getAllDatasetResourceUris(datasetUri);
		
		int cnt=0;
		for(String mUrl: manifestSource.getIterableOfObjects(datasetUri, TimestampTracker.Deleted.EXCLUDE)) {
			cnt++;
			String manifestJson=null;
			int tries=1;
			while (manifestJson==null) try {
				manifestJson = httpGet(mUrl);
			} catch (IOException e) {
				tries++;
				if(tries>3) {
					//TODO: change to: 
					//log error
					//change status to update failure?
					//keep in dataset
					//move to next
					throw e;
				}
				try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {
					throw e;
				}
			}
			repository.save(datasetUri, mUrl, manifestJson);
			allDatasetManifests.remove(mUrl);
			if(cnt%100 == 0)
				System.out.println(cnt+" manifests harvested");
		}

		Set<String> deletedManifests=allDatasetManifests;
		for(String mUrl: deletedManifests) {
			repository.remove(datasetUri, mUrl);
		}
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
