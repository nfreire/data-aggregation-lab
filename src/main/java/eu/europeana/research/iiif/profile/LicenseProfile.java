package eu.europeana.research.iiif.profile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.FileWriterWithEncoding;

import eu.europeana.research.iiif.profile.ManifestLabelValuesProfile.MetadataLabelProfile;
import eu.europeana.research.iiif.profile.model.Manifest;
import eu.europeana.research.iiif.profile.model.Metadata;
import eu.europeana.research.iiif.profile.model.SeeAlso;

public class LicenseProfile {

	HashMap<String, Integer> values;
	
	public LicenseProfile() {
		values=new HashMap<>();
	}
	
	public void profileManifest(Manifest manifest) {
		if(manifest.license==null) return;
		for(String md: manifest.license) {
			if(md!=null){
				Integer lbProf = values.get(md);
				if(lbProf==null) 
					values.put(md, 1);
				else
					values.put(md, lbProf+1);
			}
		}
	}

	public void save(File csvFile) throws IOException {
		if(!csvFile.getParentFile().exists())
			csvFile.getParentFile().mkdirs();
		FileWriterWithEncoding writer=new FileWriterWithEncoding(csvFile, "UTF-8");
		CSVPrinter printer=new CSVPrinter(writer, CSVFormat.DEFAULT);
		
		printer.print("Value");
		printer.print("Total uses");
		printer.println();
			
		for(Entry<String, Integer> entry : values.entrySet()) {
			printer.print(entry.getKey());
			printer.print(entry.getValue());
			printer.println();
		}
		
		printer.close();
	}
}
