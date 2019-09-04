package eu.europeana.research.iiif.profile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.FileWriterWithEncoding;

import eu.europeana.research.iiif.profile.model.Manifest;
import eu.europeana.research.iiif.profile.model.Metadata;

public class ManifestLabelValuesProfile {

	public static class MetadataLabelProfile extends UsageCount{
		Map<String, Integer> langsUsed=new HashMap<>();
//TODO:		distinct values
	}
	
	HashMap<String, MetadataLabelProfile> labels;

	public ManifestLabelValuesProfile() {
		labels=new HashMap<>();
	}
	
	public ManifestLabelValuesProfile(HashMap<String, MetadataLabelProfile> labels) {
		super();
		this.labels = labels;
	}
	
	public void profileManifest(Manifest manifest) {
		if(manifest==null || manifest.metadata==null) return;
		HashSet<String> onceOrMoreUsage=new HashSet<>();
		for(Metadata md: manifest.metadata) {
			String labelString = md.getLabelString();
			MetadataLabelProfile lbProf = labels.get(labelString);
			if(lbProf==null) {
				lbProf=new MetadataLabelProfile();
				labels.put(labelString, lbProf);
			}
			lbProf.total++;
			onceOrMoreUsage.add(labelString);
		}
		
		for(String lb : onceOrMoreUsage) {
			labels.get(lb).onceOrMore++;
		}
	}

	public void save(File csvFile) throws IOException {
		if(!csvFile.getParentFile().exists())
			csvFile.getParentFile().mkdirs();
		FileWriterWithEncoding writer=new FileWriterWithEncoding(csvFile, "UTF-8");
		CSVPrinter printer=new CSVPrinter(writer, CSVFormat.DEFAULT);
		
		HashSet<String> langsUsedSet=new HashSet<>();
		for(MetadataLabelProfile v : labels.values()) 
			langsUsedSet.addAll(v.langsUsed.keySet());
		ArrayList<String> langsUsedList=new ArrayList<>(langsUsedSet);
		Collections.sort(langsUsedList);
		
		printer.print("Label");
		printer.print("Total uses");
		printer.print("Used once or more");
		for(String lang: langsUsedList)
			printer.print(lang);
		printer.println();
			
		for(Entry<String, MetadataLabelProfile> entry : labels.entrySet()) {
			printer.print(entry.getKey());
			printer.print(entry.getValue().total);
			printer.print(entry.getValue().onceOrMore);
			for(String lang: langsUsedList) {
				Integer count = entry.getValue().langsUsed.get(lang);
				printer.print(count!=null ? count : 0);
			}
			printer.println();
		}
		
		printer.close();
	}
}
