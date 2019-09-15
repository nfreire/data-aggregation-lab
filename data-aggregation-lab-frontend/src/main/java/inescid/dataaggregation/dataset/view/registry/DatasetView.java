package inescid.dataaggregation.dataset.view.registry;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.processing.SupportedAnnotationTypes;

import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.StringUtils;

import freemarker.template.Template;
import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.WwwDataset;
import inescid.dataaggregation.dataset.view.GlobalFrontend;
import inescid.dataaggregation.dataset.Dataset.DatasetType;

public abstract class DatasetView extends View {

	protected Dataset dataset=null;
	
	public DatasetView(Dataset dataset) {
		super();
		this.dataset = dataset;
	}

	public String output() throws Exception {
		StringWriter w=new StringWriter();
		Template temp = GlobalFrontend.FREE_MARKER.getTemplate("datasetregistry/"+getClass().getSimpleName()+".html");
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
	public String getUri() {
		return dataset.getUri();
	}

	public void setTitle(String title) {
		dataset.setTitle(title);
	}

	public String getLocalId() {
		return dataset.getLocalId();
	}
	public DatasetType getType() {
		return dataset.getType();
	}
	
	public Dataset toDataset() {
		return dataset;
	}

	public String getDataFormat() {
		return dataset.getDataFormat();
	}

	public String getDataProfile() {
		return dataset.getDataProfile();
	}
	
	public String getDataProfileDisplay() {
		DatasetProfile dataProfile = DatasetProfile.fromString(dataset.getDataProfile());
		return dataProfile!=null ? dataProfile.getDisplay() : dataset.getDataProfile();
	}

	public void setDataProfile(String dataProfile) {
		this.dataset.setDataProfile(dataProfile);
	}
	
	
	public boolean isConvertibleToEdm() {
		return dataset.getDataProfile()!=null && dataset.getDataProfile().equals(DatasetProfile.EDM.toString());
	}
	
	public boolean isProfilableForRdf() {
		return dataset.getDataFormat()!=null && ContentTypes.isRdf(dataset.getDataFormat());
	}
	
	public List<Entry<String, String>> getTypeInformation(){
		List<Entry<String, String>> info=new ArrayList<>(3);
		if(dataset instanceof IiifDataset) {
			final IiifDataset ds=(IiifDataset)dataset;
			info.add(new DefaultMapEntry<String, String>("Harvesting method", ds.getCrawlMethod().toString()));
		}
		if(dataset instanceof WwwDataset) {
			final WwwDataset ds=(WwwDataset)dataset;
			info.add(new DefaultMapEntry<String, String>("Harvesting method", ds.getMicroformat().toString()));
		}
		return info;
	}

}