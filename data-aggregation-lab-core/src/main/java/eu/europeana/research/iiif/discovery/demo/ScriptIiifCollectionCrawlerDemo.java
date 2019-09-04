package eu.europeana.research.iiif.discovery.demo;

import eu.europeana.research.iiif.crawl.CollectionCrawler;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;

public class ScriptIiifCollectionCrawlerDemo {

	
	public static void main(String[] args) throws Exception {
		String iiifManifestListCsv="target/syncdb-collection";
		String iiifCollectionId="http://biblissima.fr/iiif/collection/top";
		
		IiifCollectionCrawlerDemo demo=new IiifCollectionCrawlerDemo(iiifCollectionId, iiifManifestListCsv);
		demo.executeCollectionCrawling();
	}

	static class IiifCollectionCrawlerDemo {

		private InMemoryTimestampStore timestampTracker;
		String dataset;
		
		public IiifCollectionCrawlerDemo(String dataset, String iiifManifestTimestampFile) throws Exception {
			this.dataset = dataset;
			timestampTracker = new InMemoryTimestampStore(iiifManifestTimestampFile);
		}
		
		public void executeCollectionCrawling() throws Exception {
			timestampTracker.open();
			
			CollectionCrawler harvester=new CollectionCrawler(timestampTracker, dataset);
			harvester.crawl(true);
			
			timestampTracker.close();
		}
		
	}
	
}
