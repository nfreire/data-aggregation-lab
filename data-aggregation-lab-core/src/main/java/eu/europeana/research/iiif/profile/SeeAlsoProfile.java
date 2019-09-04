package eu.europeana.research.iiif.profile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;

import eu.europeana.research.iiif.profile.model.Manifest;
import eu.europeana.research.iiif.profile.model.SeeAlso;
import inescid.util.datastruct.MapOfLists;

public class SeeAlsoProfile {
	private static final String formatProfileSeparator = " | ";

	HashMap<String, UsageCount> formatAndProfile;
	MapOfLists<String, String> formatAndProfileExamples;
	HashMap<String, UsageCount> label;
	int profiledManifestsCount=0;
	int withSeeAlsoCount=0;
	
	public SeeAlsoProfile() {
		formatAndProfile=new HashMap<>();
		formatAndProfileExamples=new MapOfLists<>();
		label=new HashMap<>();
	}
	
	public void profileManifest(Manifest manifest) {
		profiledManifestsCount++;
		boolean hasSeeAlso=false;
		if(manifest.seeAlso==null) return;
		HashSet<String> onceOrMoreUsageFormat=new HashSet<>();
		HashSet<String> onceOrMoreUsageLabel=new HashSet<>();
		for(SeeAlso md: manifest.seeAlso) {
			if(md.format!=null){
				String formatProf=md.format;
				if(md.profile!=null) {
					formatProf+=formatProfileSeparator+md.profile;
				}
				if(StringUtils.isEmpty(formatProf))	continue;
				UsageCount lbProf = formatAndProfile.get(formatProf);
				if(lbProf==null) {
					lbProf=new UsageCount();
					formatAndProfile.put(formatProf, lbProf);
				}
				lbProf.total++;
				if (lbProf.total<=3)
					formatAndProfileExamples.put(formatProf, md.id);
				onceOrMoreUsageFormat.add(formatProf);
				hasSeeAlso=true;
			}
			if(md.label!=null){
				String labelString = md.getLabelString();
				UsageCount lbProf = label.get(labelString);
				if(lbProf==null) {
					lbProf=new UsageCount();
					label.put(labelString, lbProf);
				}
				lbProf.total++;
				onceOrMoreUsageLabel.add(labelString);
			}
		}
		for(String lb : onceOrMoreUsageFormat) 
			formatAndProfile.get(lb).onceOrMore++;
		for(String lb : onceOrMoreUsageLabel) 
			label.get(lb).onceOrMore++;
		if(hasSeeAlso)
			withSeeAlsoCount++;
		
	}

	public void save(File csvFile) throws IOException {
		if(!csvFile.getParentFile().exists())
			csvFile.getParentFile().mkdirs();
		FileWriterWithEncoding writer=new FileWriterWithEncoding(csvFile, "UTF-8");
		CSVPrinter printer=new CSVPrinter(writer, CSVFormat.DEFAULT);
		
		printer.print("Field");
		printer.print("Value");
		printer.print("Used once or more");
		printer.print("Total uses");
		printer.println();
			
		for(Entry<String, UsageCount> entry : formatAndProfile.entrySet()) {
			printer.print("formatAndProfile");
			printer.print(entry.getKey());
			printer.print(entry.getValue().total);
			printer.print(entry.getValue().onceOrMore);
			printer.println();
		}
		for(Entry<String, UsageCount> entry : label.entrySet()) {
			printer.print("label");
			printer.print(entry.getKey());
			printer.print(entry.getValue().total);
			printer.print(entry.getValue().onceOrMore);
			printer.println();
		}
		
		printer.close();
	}
	public static SeeAlsoProfile load(File csvFile) throws IOException {
		SeeAlsoProfile ret=new SeeAlsoProfile();
		CSVParser csvParser=new CSVParser(new FileReader(csvFile), CSVFormat.DEFAULT);
		for(Iterator<CSVRecord> it = csvParser.iterator() ; it.hasNext() ; ) {
			CSVRecord rec = it.next();
			if(rec.get(0).equals("formatAndProfile"))
				ret.formatAndProfile.put(rec.get(1), new UsageCount(Integer.parseInt(rec.get(2)), Integer.parseInt(rec.get(3))));
			else if(rec.get(0).equals("label"))
				ret.label.put(rec.get(1), new UsageCount(Integer.parseInt(rec.get(2)), Integer.parseInt(rec.get(3))));
		}		
		csvParser.close();
		return ret;
	}

	public Set<String> formatAndProfileValues() {
		return formatAndProfile.keySet();
	}
	
	public static String[] parseFormatProfile(String seeAlsoType) {
		int idx = seeAlsoType.indexOf(formatProfileSeparator);
		if(idx<0)
			return new String[] { seeAlsoType, null };
		return new String[] { seeAlsoType.substring(0, idx), seeAlsoType.substring(idx + formatProfileSeparator.length()) };
	}

	public float getSeeAlsoCoverage() {
		return (float)withSeeAlsoCount / (float)profiledManifestsCount;
	}

	public HashMap<String, UsageCount> getFormatAndProfile() {
		return formatAndProfile;
	}

	public HashMap<String, UsageCount> getLabel() {
		return label;
	}

	public MapOfLists<String, String> getFormatAndProfileExamples() {
		return formatAndProfileExamples;
	}
	
	

}
