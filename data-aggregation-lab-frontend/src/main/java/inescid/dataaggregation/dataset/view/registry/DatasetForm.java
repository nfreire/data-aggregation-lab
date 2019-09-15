package inescid.dataaggregation.dataset.view.registry;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.Template;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.view.GlobalFrontend;

public abstract class DatasetForm extends View {

	protected Dataset dataset=null;
	
	public DatasetForm(Dataset dataset) {
		super();
		this.dataset = dataset;
	}

	public abstract boolean validate() ;

	public String getOrganization() {
		return dataset.getOrganization();
	}

	public void setOrganization(String organization) {
		dataset.setOrganization(organization);
	}

	public String getTitle() {
		return dataset.getTitle();
	}

	public void setTitle(String title) {
		dataset.setTitle(title);
	}

	public void setMetadataUri(String uri) {
		dataset.setMetadataUri(uri);
	}
	
	public Dataset toDataset() {
		return dataset;
	}

	public String getMetadataUri() {
		return dataset.getMetadataUri();
	}
	
	public abstract boolean register();
	
	

	public String validateUri() throws IOException {
		if(StringUtils.isEmpty(dataset.getUri())) {
			return "Provide a URI";
		}else if(!Global.urlPattern.matcher(dataset.getUri()).matches()) 
			return("The URI is in an invalid format");
		else {
//			if(null!=GlobalCore.getDatasetRegistryRepository().getDatasetByUri(dataset.getUri()))
//				return("The URI of this dataset is already registered");
		}
		return null;
	}
}