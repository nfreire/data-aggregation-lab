package inescid.dataaggregation.metadatatester.view;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Schemaorg;

public class SchemaorgChoValidator {

	public static ValidationReport validate(Resource choRes){
		ValidationReport report=new ValidationReport();
		
		//is CreativeWork
		boolean hasAboutness=false;
		boolean hasName=false;
		boolean hasProvider=false;
		boolean hasLicense=false;
		boolean hasDigital=false;
		
		for(Statement st: choRes.listProperties().toList()) {
			if (st.getPredicate().equals(Schemaorg.spatial) 
					|| st.getPredicate().equals(Schemaorg.spatialCoverage)
					|| st.getPredicate().equals(Schemaorg.locationCreated)
					|| st.getPredicate().equals(Schemaorg.temporal)
					|| st.getPredicate().equals(Schemaorg.temporalCoverage)
					|| st.getPredicate().equals(Schemaorg.keywords)
					|| st.getPredicate().equals(Schemaorg.about)
					|| st.getPredicate().equals(Schemaorg.contentLocation)
					) 
				hasAboutness=true;

			if (st.getPredicate().equals(Schemaorg.name) 
					|| st.getPredicate().equals(Schemaorg.description)
					|| st.getPredicate().equals(Schemaorg.artMedium)
					|| st.getPredicate().equals(Schemaorg.pagination)
					) 
				hasName=true;

			if (st.getPredicate().equals(Schemaorg.provider) 
					) hasProvider=true;
			
			if (st.getPredicate().equals(Schemaorg.license) 
					) hasLicense=true;
					
			if (st.getPredicate().equals(Schemaorg.url) 
				|| st.getPredicate().equals(Schemaorg.associatedMedia)
				|| st.getPredicate().equals(Schemaorg.audio)
				|| st.getPredicate().equals(Schemaorg.image)
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
