package eu.europeana.research.iiif.crawl;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.HttpUtil;

public class ManifestHarvester {
private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
		.getLogger(ManifestHarvester.class);
	TimestampTracker manifestSource;
	String datasetUri;
	Repository repository;
	
	public ManifestHarvester(Repository repository, TimestampTracker manifestSource, String datasetUri) {
		super();
		this.manifestSource = manifestSource;
		this.datasetUri = datasetUri;
		this.repository = repository;
	}

	public void harvest() throws IOException, AccessException, InterruptedException {
		Set<String> allDatasetManifests = repository.getAllDatasetResourceUris(datasetUri);
		
		int cnt=0;
		for(String mUrl: manifestSource.getIterableOfObjects(datasetUri, TimestampTracker.Deleted.EXCLUDE)) {
			cnt++;
			try {
				List<Entry<String, String>> headers = HttpUtil.getAndStoreWithHeaders(
						new UrlRequest(mUrl, "accept", "application/json"), repository.getFile(datasetUri, mUrl));
				repository.saveMeta(datasetUri, mUrl, headers);

				manifestSource.setObjectTimestamp(datasetUri, mUrl, new GregorianCalendar());
			} catch (IOException | AccessException e) {
				log.error(e.getMessage(), e);
				continue;
			}
			allDatasetManifests.remove(mUrl);
			if(cnt%100 == 0)
				System.out.println(cnt+" manifests harvested");
		}

		Set<String> deletedManifests=allDatasetManifests;
		for(String mUrl: deletedManifests) {
			repository.remove(datasetUri, mUrl);
		}
	}




}
