package eu.europeana.research.iiif.profile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import eu.europeana.research.iiif.profile.model.Manifest;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;
import inescid.util.googlesheets.GoogleSheetsCsvUploader;

public class ManifestMetadataProfiler {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
		.getLogger(ManifestMetadataProfiler.class); 
	
	Dataset dataset;
	Repository repository;
	File outputFolder;
	
	private SeeAlsoProfile seeAlsoProfile=new SeeAlsoProfile();
	private ManifestLabelValuesProfile manifestMdProfile=new ManifestLabelValuesProfile();
	private LicenseProfile licenseProfile=new LicenseProfile();
	
	public ManifestMetadataProfiler(Dataset datasetUri, Repository repository, File outputFolder) {
		super();
		this.dataset = datasetUri;
		this.repository = repository;
		this.outputFolder = outputFolder;
	}
	
	public void process(boolean saveResults) throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
		for(Entry<String, File> manifestJsonFile: repository.getAllDatasetResourceFiles(dataset.getUri())) {
			String fileData = new String(Files.readAllBytes(Paths.get(manifestJsonFile.getValue().getAbsolutePath())));
			
			try {
				Manifest m = gson.fromJson(fileData, Manifest.class);
				if(m==null)
					continue;
				manifestMdProfile.profileManifest(m);
				seeAlsoProfile.profileManifest(m);
				licenseProfile.profileManifest(m);
			} catch (JsonSyntaxException e) {
				log.info("Error parsing of "+manifestJsonFile.getKey()+"\n"+ fileData, e);
			}
		}
		if(saveResults) {
			File manifestMdCsvFile = new File(outputFolder, "manifest-metadata-profile.csv");
			manifestMdProfile.save(manifestMdCsvFile);
			File seeAlsoCsvFile = new File(outputFolder, "seeAlso-profile.csv");
			seeAlsoProfile.save(seeAlsoCsvFile);
			File rightsCsvFile = new File(outputFolder, "rights-profile.csv");
			licenseProfile.save(rightsCsvFile);
			
			String manifestMdSheetTitle=manifestMdCsvFile.getName().substring(0, manifestMdCsvFile.getName().lastIndexOf('.'));
			String seeAlsoSheetTitle=seeAlsoCsvFile.getName().substring(0, seeAlsoCsvFile.getName().lastIndexOf('.'));
			String rightsSheetTitle=rightsCsvFile.getName().substring(0, rightsCsvFile.getName().lastIndexOf('.'));
	
			File sheetsIdFile = new File(outputFolder, "google-sheet-id.txt");
			String spreadsheetId=null;
			if(sheetsIdFile.exists()) {
				 spreadsheetId=FileUtils.readFileToString(sheetsIdFile, Global.UTF8);
			} else {
				String spreadsheetTitle="Data Profiling - "+ dataset.getTitle();
				spreadsheetId=GoogleSheetsCsvUploader.create(spreadsheetTitle, manifestMdSheetTitle);
				FileUtils.write(sheetsIdFile, spreadsheetId, Global.UTF8);
			}
			GoogleSheetsCsvUploader.update(spreadsheetId, manifestMdSheetTitle, manifestMdCsvFile);			
			GoogleSheetsCsvUploader.update(spreadsheetId, seeAlsoSheetTitle, seeAlsoCsvFile);			
			GoogleSheetsCsvUploader.update(spreadsheetId, rightsSheetTitle, rightsCsvFile);	
		}
	}

	public SeeAlsoProfile getSeeAlsoProfile() {
		return seeAlsoProfile;
	}

	public ManifestLabelValuesProfile getManifestMdProfile() {
		return manifestMdProfile;
	}

	public LicenseProfile getLicenseProfile() {
		return licenseProfile;
	}
	
}
