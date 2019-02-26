package inescid.dataaggregation.dataset.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class PropertyProfiler  implements ProfileOfInterface {
	
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
	}

	public void toCsv(CSVPrinter csv) throws IOException {
		csv.print(profileOfUniqueness.uniqueness);		
		csv.print(profileOfExternalLinks.literalRatio);		
		csv.print(profileOfExternalLinks.linkageToInternalRatio);		
		csv.print(profileOfExternalLinks.linkageToExternalRatio);		
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


}
