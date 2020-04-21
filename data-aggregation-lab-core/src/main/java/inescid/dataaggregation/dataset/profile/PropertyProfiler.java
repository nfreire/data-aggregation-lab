package inescid.dataaggregation.dataset.profile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.dataset.profile.ProfileOfValueDistribution.ValueDistribution;

public class PropertyProfiler  implements ProfileOfInterface, Serializable {
	private static final long serialVersionUID = 1L;

	ProfileOfUniqueness profileOfUniqueness;
	ProfileOfExternalLinks profileOfExternalLinks;
	ProfileOfValueDistribution profileOfValueDistribution;
	
	private List<ProfileOfInterface> profilesListAux;
	
	public PropertyProfiler() {
		profilesListAux=new ArrayList<>();
		profileOfUniqueness = new ProfileOfUniqueness();
		profilesListAux.add(profileOfUniqueness);
		profileOfExternalLinks = new ProfileOfExternalLinks();
		profilesListAux.add(profileOfExternalLinks);
		profileOfValueDistribution = new ProfileOfValueDistribution();
		profilesListAux.add(profileOfValueDistribution);
	}

	@Override
	public void eventInstanceStart(Resource resource) {
		for (ProfileOfInterface p: profilesListAux)
			p.eventInstanceStart(resource);
	}

	@Override
	public void eventInstanceEnd(Resource resource) {
		for (ProfileOfInterface p: profilesListAux)
			p.eventInstanceEnd(resource);
	}

	@Override
	public void eventProperty(Statement statement) {
		for (ProfileOfInterface p: profilesListAux)
			p.eventProperty(statement);
	}

	@Override
	public void finish() {
		for (ProfileOfInterface p: profilesListAux)
			p.finish();
	}

	public void printCsvHeaders(CSVPrinter csv) throws IOException {
		csv.print("Uniqueness(%)");
		csv.print("Literal(%)");
		csv.print("Dataset resource(%)");
		csv.print("External resource(%)");
		csv.print("Linked domains (domain [property, count...])...");
	}

	public void toCsv(CSVPrinter csv) throws IOException {
		csv.print(profileOfUniqueness.uniqueness);		
		csv.print(profileOfExternalLinks.literalRatio*100);	
		profileOfExternalLinks.toCsv(csv);
	}

	public ProfileOfUniqueness getProfileOfUniqueness() {
		return profileOfUniqueness;
	}

	public ProfileOfExternalLinks getProfileOfExternalLinks() {
		return profileOfExternalLinks;
	}

	public ProfileOfValueDistribution getProfileOfValueDistribution() {
		return profileOfValueDistribution;
	}

	public void toCsvOfValueDistribution(String classUri, String propUri, File outputFolder) throws IOException {
		try {
			if(profileOfValueDistribution.getDistribution().isEmpty())
				return;
			File csvFile=new File(outputFolder, URLEncoder.encode(classUri+"-----"+propUri, "UTF-8")+".csv");
			FileWriterWithEncoding w=new FileWriterWithEncoding(csvFile, "UTF-8");
			CSVPrinter csv=new CSVPrinter(w, CSVFormat.DEFAULT);
			csv.printRecord("Values distribution for: ", propUri, "(in instances of class "+classUri+")");
			csv.printRecord("Value", "Distribution");
			double totalPct=0;
			for(ValueDistribution d : profileOfValueDistribution.getDistribution()) {
				d.toCsv(csv);
				totalPct+=d.distribution;
			}
			if(totalPct < 0.99)
				csv.printRecord("[other values]", (1f-totalPct)*100);
			csv.close();
			w.close();
		} catch (UnsupportedEncodingException e) { /*does not happen */	}
	}


}
