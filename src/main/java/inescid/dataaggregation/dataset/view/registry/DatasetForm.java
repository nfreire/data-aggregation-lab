package inescid.dataaggregation.dataset.view.registry;

import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.Template;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.LodDataset;

public abstract class DatasetForm extends View {

	Dataset dataset=null;
	
	public DatasetForm(Dataset dataset) {
		super();
		this.dataset = dataset;
	}

	public String output() throws Exception {
		StringWriter w=new StringWriter();
		Template temp = Global.FREE_MARKER.getTemplate("datasetregistry/"+getClass().getSimpleName()+".html");
		temp.process(this, w);
		w.close();
		return w.toString();
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

	public Dataset toDataset() {
		return dataset;
	}


}