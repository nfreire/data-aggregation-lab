package inescid.dataaggregation.wikidata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.model.Rdfs;
import inescid.util.AccessException;
import inescid.util.datastruct.CsvDataPersistReader;
import inescid.util.datastruct.CsvDataPersistWriter;

public class WikidataLabels {
	HashMap<String, String> labels;
	
	public WikidataLabels() {
		labels=new HashMap<String, String>();
	}

	public WikidataLabels(File csvFile) throws IOException {
		this();
		BufferedReader reader = java.nio.file.Files.newBufferedReader(csvFile.toPath(), StandardCharsets.UTF_8);
		new CsvDataPersistReader(reader).read(labels);
		reader.close();
	}

	public void saveToCsv(File csvFile) throws IOException {
		BufferedWriter bufWrt = java.nio.file.Files.newBufferedWriter(csvFile.toPath(), StandardCharsets.UTF_8);
		CsvDataPersistWriter w=new CsvDataPersistWriter(bufWrt);
		w.write(labels);
	}
	
	public String get(String wdUri) {
		try {
			if(labels.containsKey(wdUri))
				return labels.get(wdUri);
			String wdLabel = getWdLabel(WikidataUtil.fetchResource(wdUri));
			labels.put(wdUri, wdLabel);
			return wdLabel;
		} catch (AccessException | InterruptedException | IOException e) {
			labels.put(wdUri, null);
			return null;
		}
	}
	
	public String get(Resource wdResource) {
		if(labels.containsKey(wdResource.getURI()))
			return labels.get(wdResource.getURI());
		String wdLabel = getWdLabel(wdResource);
		labels.put(wdResource.getURI(), wdLabel);
		return wdLabel;
	}
	
	private static String getWdLabel(Resource wdResource) {
		StmtIterator labelProps = wdResource.listProperties(Rdfs.label);
		String label=null;
		for (Statement st : labelProps.toList()) {
			String lang = st.getObject().asLiteral().getLanguage();
			if(lang.equals("en"))
				return st.getObject().asLiteral().getString();
			if(label==null) 
				label=st.getObject().asLiteral().getString();
		}
		return label;
	}

	public Map<String, String> getMap() {
		return labels;
	}		
}
