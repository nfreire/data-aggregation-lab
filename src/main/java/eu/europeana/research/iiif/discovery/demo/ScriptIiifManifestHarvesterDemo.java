package eu.europeana.research.iiif.discovery.demo;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import eu.europeana.research.iiif.crawl.ManifestHarvester;
import eu.europeana.research.iiif.crawl.ManifestRepository;
import eu.europeana.research.iiif.discovery.CrawlingHandler;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.model.Activity;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.dataaggregation.dataset.store.Repository;

public class ScriptIiifManifestHarvesterDemo {

	
	public static void main(String[] args) throws Exception {
		String iiifManifestListCsv="target/syncdb";
		String iiifManifestsRepository="target/manifests-repository";
		String inputDiscoveryJson="https://scrc.lib.ncsu.edu/sal_staging/iiif-discovery.json";
		
		IiifManifestHarvesterDemo demo=new IiifManifestHarvesterDemo(inputDiscoveryJson, iiifManifestListCsv, iiifManifestsRepository);
		demo.executeManifestHarvesting();
	}

	static class IiifManifestHarvesterDemo {

		private InMemoryTimestampStore timestampTracker;
		private Repository repository;
		String dataset;
		
		public IiifManifestHarvesterDemo(String dataset, String iiifManifestTimestampFile, String iiifManifestsRepository) throws Exception {
			this.dataset = dataset;
			timestampTracker = new InMemoryTimestampStore(iiifManifestTimestampFile);
			repository=new Repository();
			repository.init(iiifManifestsRepository);
		}
		
		public void executeManifestHarvesting() throws Exception {
			timestampTracker.open();
			
			ManifestHarvester harvester=new ManifestHarvester(repository, timestampTracker, dataset);
			harvester.harvest();
			
			timestampTracker.close();
		}
		
	}
	
}
