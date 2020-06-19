package inescid.dataaggregation.metadatatester.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.metadatatester.view.ResourceView.DataModel;

public class IiifSeeAlsoView {
	String uri;
	String format;
	String profile;
	
	List<ResourceView> creativeWorks=new ArrayList<ResourceView>();
	List<ResourceView> otherResources=new ArrayList<ResourceView>();		
	
	public void readData(Model model) {
		for(Resource res: model.listSubjects().toList()) {
			ResourceView resView=new ResourceView(res, DataModel.SchemaorgPlusEdm);
			if(resView.isCreativeWork())
				creativeWorks.add(resView);
			else
				otherResources.add(resView);
		}
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public List<ResourceView> getCreativeWorks() {
		return creativeWorks;
	}

	public List<ResourceView> getOtherResources() {
		return otherResources;
	}
	
	
	
}