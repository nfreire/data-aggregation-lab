package eu.europeana.research.iiif.crawl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.profile.model.Manifest;
import eu.europeana.research.iiif.profile.model.SeeAlso;
import inescid.dataaggregation.crawl.http.HttpRequestService;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.LinkedDataUtil;

public class ManifestSeeAlsoHarvester {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(ManifestSeeAlsoHarvester.class);

	TimestampTracker manifestSource;
	IiifDataset dataset;
	Repository repository;
	String format;
	String profile;
	
	public ManifestSeeAlsoHarvester(Repository repository, IiifDataset dataset, String seeAlsoFormat, String seeAlsoProfile) {
		super();
		this.dataset = dataset;
		this.repository = repository;
		this.format = seeAlsoFormat;
		this.profile = seeAlsoProfile;
	}

	public void harvest() throws IOException, InterruptedException {
		String repositoryDatasetUri=dataset.getSeeAlsoDatasetUri();
		Set<String> allDatasetManifestSeeAlsos = repository.getAllDatasetResourceUris(repositoryDatasetUri);
		List<Entry<String,File>> allDatasetManifests = repository.getAllDatasetResourceFiles(dataset.getUri());
		
		int cnt=0;
		for(Entry<String,File> manifestJsonFile: allDatasetManifests) {
			cnt++;
			
			Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
			String fileData = new String(Files.readAllBytes(Paths.get(manifestJsonFile.getValue().getAbsolutePath())));
			Manifest m = gson.fromJson(fileData, Manifest.class);

			SeeAlso targetSeeAlso=null;
			for(SeeAlso seeAlso: m.seeAlso) {
				if(seeAlso.matches(format, profile)) {
					targetSeeAlso=seeAlso;
					break;
				}
			}
			if(targetSeeAlso==null)
				continue;
			
			try {
				List<org.apache.http.Header> headers = HttpUtil.getAndStoreWithHeaders(targetSeeAlso.id, repository.getFile(repositoryDatasetUri, targetSeeAlso.id));
				repository.saveMeta(repositoryDatasetUri, targetSeeAlso.id, HttpUtil.convertHeaderStruct(headers));
			} catch (AccessException e) {
				log.warn(targetSeeAlso.id, e);
				continue;
			} catch (Exception e) {
				log.error(targetSeeAlso.id, e);
				continue;
			}
			
			allDatasetManifestSeeAlsos.remove(targetSeeAlso.id);
			if(cnt%100 == 0)
				System.out.println(cnt+" 'seeAlsos' harvested");
		}

		Set<String> deletedManifestSeeAlsos=allDatasetManifestSeeAlsos;
		for(String mUrl: deletedManifestSeeAlsos) {
			repository.remove(repositoryDatasetUri, mUrl);
		}
	}




	public byte[] httpGet(URL url) throws IOException {
		byte[] reader = null;
	    try {
	    	return IOUtils.toByteArray((java.io.InputStream) url.getContent());
	    } finally {
	        if (reader != null) try {
	        	((java.io.InputStream) url.getContent()).close();
	        } catch (Throwable t) {
	        }
	    }
	}
}
