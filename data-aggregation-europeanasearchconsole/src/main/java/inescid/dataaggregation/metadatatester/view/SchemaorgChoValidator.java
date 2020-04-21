package inescid.dataaggregation.metadatatester.view;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.RegSchemaorg;

public class SchemaorgChoValidator {

	public static class ValidationReport{
		java.util.List<String> messages=new ArrayList<>();
		
		public boolean isClean() {
			return messages.isEmpty();
		}

		public void addError(String message) {
			messages.add(message);
		}

		public java.util.List<String> getErrors() {
			return messages;
		}
		
		
	}
	
	public static ValidationReport validate(Resource choRes){
		ValidationReport report=new ValidationReport();
		
		//is CreativeWork
		boolean hasAboutness=false;
		boolean hasName=false;
		boolean hasProvider=false;
		boolean hasLicense=false;
		boolean hasDigital=false;
		
		for(Statement st: choRes.listProperties().toList()) {
			if (st.getPredicate().equals(RegSchemaorg.spatial) 
					|| st.getPredicate().equals(RegSchemaorg.spatialCoverage)
					|| st.getPredicate().equals(RegSchemaorg.locationCreated)
					|| st.getPredicate().equals(RegSchemaorg.temporal)
					|| st.getPredicate().equals(RegSchemaorg.temporalCoverage)
					|| st.getPredicate().equals(RegSchemaorg.keywords)
					|| st.getPredicate().equals(RegSchemaorg.about)
					|| st.getPredicate().equals(RegSchemaorg.contentLocation)
					) 
				hasAboutness=true;

			if (st.getPredicate().equals(RegSchemaorg.name) 
					|| st.getPredicate().equals(RegSchemaorg.description)
					|| st.getPredicate().equals(RegSchemaorg.artMedium)
					|| st.getPredicate().equals(RegSchemaorg.pagination)
					) 
				hasName=true;

			if (st.getPredicate().equals(RegSchemaorg.provider) 
					) hasProvider=true;
			
			if (st.getPredicate().equals(RegSchemaorg.license) 
					) hasLicense=true;
					
			if (st.getPredicate().equals(RegSchemaorg.url) 
				|| st.getPredicate().equals(RegSchemaorg.associatedMedia)
				|| st.getPredicate().equals(RegSchemaorg.audio)
				|| st.getPredicate().equals(RegSchemaorg.image)
				) hasDigital=true;
		}
		
		if(!hasAboutness)
			report.addError("One of 'spatial, spatialCoverage, locationcreated, temporal, temporalCoverage, keywords, about, contentLocation' is required.");
		if(!hasProvider)
			report.addError("'provider' is required.");
		if(!hasName)
			report.addError("One of 'name, description, artMedium, pagination' is required.");
		if(!hasLicense)
			report.addError("'license' is required.");
		if(!hasDigital)
			report.addError("One of 'url, associatedMedia, audio, image' is required.");
		return report;
	}
}
