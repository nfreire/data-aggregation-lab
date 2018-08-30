package eu.europeana.research.iiif.profile;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.jena.atlas.logging.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import eu.europeana.research.iiif.crawl.ManifestRepository;
import eu.europeana.research.iiif.profile.model.Manifest;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;
import inescid.util.googlesheets.GoogleSheetsCsvUploader;

public class ManifestMetadataProfiler {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
		.getLogger(ManifestMetadataProfiler.class); 
	
	String datasetUri;
	Repository repository;
	File outputFolder;
	
	private SeeAlsoProfile seeAlsoProfile=new SeeAlsoProfile();
	private ManifestLabelValuesProfile manifestMdProfile=new ManifestLabelValuesProfile();
	private LicenseProfile licenseProfile=new LicenseProfile();
	
	public ManifestMetadataProfiler(String datasetUri, Repository repository, File outputFolder) {
		super();
		this.datasetUri = datasetUri;
		this.repository = repository;
		this.outputFolder = outputFolder;
	}
	
	public void process() throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
		for(Entry<String, File> manifestJsonFile: repository.getAllDatasetResourceFiles(datasetUri)) {
			String fileData = new String(Files.readAllBytes(Paths.get(manifestJsonFile.getValue().getAbsolutePath())));
			
			try {
				Manifest m = gson.fromJson(fileData, Manifest.class);

				manifestMdProfile.profileManifest(m);
				seeAlsoProfile.profileManifest(m);
				licenseProfile.profileManifest(m);
			} catch (JsonSyntaxException e) {
				log.debug("Error parsing of "+manifestJsonFile.getKey()+"\n"+ fileData);
			}
		}
		File manifestMdCsvFile = new File(outputFolder, "manifest-metadata-profile.csv");
		manifestMdProfile.save(manifestMdCsvFile);
		File seeAlsoCsvFile = new File(outputFolder, "seeAlso-profile.csv");
		seeAlsoProfile.save(seeAlsoCsvFile);
		File licenseCsvFile = new File(outputFolder, "license-profile.csv");
		licenseProfile.save(licenseCsvFile);
		
		String manifestMdSheetTitle=manifestMdCsvFile.getName().substring(0, manifestMdCsvFile.getName().lastIndexOf('.'));
		String seeAlsoSheetTitle=seeAlsoCsvFile.getName().substring(0, seeAlsoCsvFile.getName().lastIndexOf('.'));
		String licenseSheetTitle=licenseCsvFile.getName().substring(0, licenseCsvFile.getName().lastIndexOf('.'));

		File sheetsIdFile = new File(outputFolder, "google-sheet-id.txt");
		String spreadsheetId=null;
		if(sheetsIdFile.exists()) {
			 spreadsheetId=FileUtils.readFileToString(sheetsIdFile, Global.UTF8);
		} else {
			spreadsheetId=GoogleSheetsCsvUploader.create(URLDecoder.decode(outputFolder.getName(), "UTF-8"), manifestMdSheetTitle);
		}
		GoogleSheetsCsvUploader.update(spreadsheetId, manifestMdSheetTitle, manifestMdCsvFile);			
		GoogleSheetsCsvUploader.update(spreadsheetId, seeAlsoSheetTitle, seeAlsoCsvFile);			
		GoogleSheetsCsvUploader.update(spreadsheetId, licenseSheetTitle, licenseCsvFile);			
		
//		manifestMdProfile.save(new File(outputFolder, files/"+URLEncoder.encode(datasetUri, "UTF-8")+"/manifest-metadata-profile.csv"));
//				seeAlsoProfile.save(new File("target/iiif-manifest-profiles/"+URLEncoder.encode(datasetUri, "UTF-8")+"/seeAlso-profile.csv"));
//		licenseProfile.save(new File("target/iiif-manifest-profiles/"+URLEncoder.encode(datasetUri, "UTF-8")+"/license-profile.csv"));
	}
	
}
