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

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.profile.model.Manifest;
import eu.europeana.research.iiif.profile.model.SeeAlso;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.store.Repository;

public class ManifestSeeAlsoHarvester {

	TimestampTracker manifestSource;
	String datasetUri;
	Repository repository;
	String format;
	String profile;
	
	public ManifestSeeAlsoHarvester(Repository repository, String datasetUri, String seeAlsoFormat, String seeAlsoProfile) {
		super();
		this.datasetUri = datasetUri;
		this.repository = repository;
		this.format = seeAlsoFormat;
		this.profile = seeAlsoProfile;
	}

	public void harvest() throws IOException {
		String repositoryDatasetUri=Global.SEE_ALSO_DATASET_PREFIX+datasetUri;
		Set<String> allDatasetManifestSeeAlsos = repository.getAllDatasetResourceUris(repositoryDatasetUri);
		List<Entry<String,File>> allDatasetManifests = repository.getAllDatasetResourceFiles(datasetUri);
		
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
			int tries=1;
			byte[] seeAlsoContent=null;
			while (seeAlsoContent==null) try {
				seeAlsoContent = httpGet(new URL(targetSeeAlso.id));
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

			repository.save(repositoryDatasetUri, targetSeeAlso.id, seeAlsoContent);
			
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
