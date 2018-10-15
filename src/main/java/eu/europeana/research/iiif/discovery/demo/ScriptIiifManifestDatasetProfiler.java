package eu.europeana.research.iiif.discovery.demo;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import eu.europeana.research.iiif.crawl.ManifestRepository;
import eu.europeana.research.iiif.discovery.demo.ScriptIiifManifestHarvesterDemo.IiifManifestHarvesterDemo;
import eu.europeana.research.iiif.profile.ManifestMetadataProfiler;
import inescid.dataaggregation.store.Repository;

public class ScriptIiifManifestDatasetProfiler {

	public static void main(String[] args) throws IOException {
		String iiifManifestsRepository="target/manifests-repository";
		String inputDiscoveryJson="https://scrc.lib.ncsu.edu/sal_staging/iiif-discovery.json";
//
//		Repository repository=new Repository();
//		repository.init(iiifManifestsRepository);
//		ManifestMetadataProfiler demo=new ManifestMetadataProfiler(inputDiscoveryJson, repository, new File(iiifManifestsRepository, URLEncoder.encode(inputDiscoveryJson, "UTF8")));
//		demo.process();
	}
	
	
}
