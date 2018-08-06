package inescid.dataaggregation.dataset.view.management;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import freemarker.template.SimpleDate;
import freemarker.template.Template;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.job.JobLog;
import inescid.dataaggregation.dataset.view.registry.DatasetView;

public class DatasetStatusView extends DatasetView{
	List<JobLog> jobHistory;
	public DatasetStatusView(Dataset dataset, List<JobLog> jobHistory) {
		super(dataset);
		this.jobHistory = jobHistory;
	}

	public DatasetStatusView(Dataset dataset) throws IOException {
		super(dataset);
		jobHistory=Global.getJobRunner().listJobHistoric(dataset.getLocalId());
	}

	public boolean isPublished() {
		return Global.getPublicationRepository().isPublished(dataset);
	}
	public String getPublicationUrl() {
		return Global.getPublicationRepository().getPublicationUrl(dataset);
	}
	public boolean isPublishedForSeeAlso() {
		return Global.getPublicationRepository().isPublishedForSeeAlso(dataset);
	}
	public String getPublicationSeeAlsoUrl() {
		return Global.getPublicationRepository().getPublicationSeeAlsoUrl(dataset);
	}
	public boolean isConverted() {
		return Global.getPublicationRepository().isConverted(dataset);
	}
	public String getPublicationConvertedEdmUrl() {
		return Global.getPublicationRepository().getPublicationConvertedUrl(dataset);
	}
	public boolean isProfiled() {
		return Global.getPublicationRepository().isProfiled(dataset);
	}
	public boolean isProfiledForIiif() {
		return dataset.getType()==DatasetType.IIIF && isProfiled();
	}
	public String getProfileUrl() {
		return Global.getPublicationRepository().getProfileUrl(dataset);
	}
	public boolean isHarvested() {
		return Global.getTimestampTracker().getDatasetStatus(dataset.getUri())!=null;
	}
	public boolean isProfilable() {
		return isHarvested(); 
//				&& dataset.getType()==DatasetType.IIIF;
	}
	public boolean isHarvestedForSeeAlso() {
		return Global.getTimestampTracker().getDatasetStatus(Global.SEE_ALSO_DATASET_PREFIX+dataset.getUri())!=null;
	}
	public String getLastHarvest() {
		Calendar date = Global.getTimestampTracker().getDatasetStatus(dataset.getUri());
		if(date==null) 
			return "never";
		return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(date.getTime());
	}
	public String getSeeAlsoLastHarvest() {
		Calendar date = Global.getTimestampTracker().getDatasetStatus(Global.SEE_ALSO_DATASET_PREFIX+dataset.getUri());
		if(date==null) 
			return "never";
		return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(date.getTime());
	}
	
	@Override
	public String output() throws Exception {
		StringWriter w=new StringWriter();
		Template temp = Global.FREE_MARKER.getTemplate("datasetmanagement/"+getClass().getSimpleName()+".html");
		temp.process(this, w);
		w.close();
		return w.toString();
	}
	
	@Override
	public boolean validate() {
		return true;
	}
}