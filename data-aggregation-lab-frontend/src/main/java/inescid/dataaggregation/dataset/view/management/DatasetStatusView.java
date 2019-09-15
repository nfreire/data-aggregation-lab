package inescid.dataaggregation.dataset.view.management;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import freemarker.template.SimpleDate;
import freemarker.template.Template;
import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.job.JobLog;
import inescid.dataaggregation.dataset.view.GlobalFrontend;
import inescid.dataaggregation.dataset.view.registry.DatasetView;

public class DatasetStatusView extends DatasetView{
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(DatasetStatusView.class);

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
	public String getPublicationDiagnosisUrl() {
		return Global.getPublicationRepository().getIiifDiagnosisForEuropeanaUrl(dataset);
	}
	public boolean isDiagnosed() {
		return Global.getPublicationRepository().isDiagnosed(dataset);
	}
	public boolean isConverted() {
		return Global.getPublicationRepository().isConverted(dataset);
	}
	public boolean isConvertedAnalysis() {
		return Global.getPublicationRepository().isConvertedAnalysis(dataset);
	}
	public String getPublicationConvertedEdmUrl() {
		return Global.getPublicationRepository().getPublicationConvertedUrl(dataset);
	}
	public boolean isProfiled() {
		return Global.getPublicationRepository().isProfiled(dataset);
	}
	public boolean isValidatedEdm() {
		File profileFolder = Global.getPublicationRepository().getProfileFolder(dataset);
		return new File(profileFolder, "edm-validation.csv").exists();
	}
	public boolean isProfiledForIiif() {
		return dataset.getType()==DatasetType.IIIF && isProfiled();
	}
	public String getProfileUrl() {
		try {
			return Global.getPublicationRepository().getProfileUrl(dataset);
		} catch (IOException e) {
			return "";
		}
	}
	public boolean isHarvested() {
		return Global.getTimestampTracker().getDatasetTimestamp(dataset.getUri())!=null;
	}
	public boolean isProfilable() {
		return isHarvested(); 
//				&& dataset.getType()==DatasetType.IIIF;
	}
	public boolean isRunning() {
		try {
			return Global.getJobRunner().isDatasetWithJob(dataset);
		} catch (IOException e) {
			log.info(e.getMessage(), e);
			return false;
		}
	}

	public boolean isEdmAvailable() {
		return (dataset.getDataProfile()!=null && dataset.getDataProfile().equals(DatasetProfile.EDM.toString()))
				|| isConverted();
	}
	
	public boolean isConvertibleToEdm() {
		return dataset.getDataProfile()!=null && dataset.getDataProfile().equals(DatasetProfile.SCHEMA_ORG.toString());
	}
	
	public boolean isProfilableForRdf() {
		return dataset.getDataFormat()!=null && ContentTypes.isRdf(dataset.getDataFormat());
	}
	
	public boolean isProfilableForIiif() {
		return isHarvested() && getType()==DatasetType.IIIF;
	}
	
	public boolean isHarvestedForSeeAlso() {
		return Global.getTimestampTracker().getDatasetTimestamp(((IiifDataset)dataset).getSeeAlsoDatasetUri())!=null;
	}
	public String getLastHarvest() {
		Calendar date = Global.getTimestampTracker().getDatasetTimestamp(dataset.getUri());
		if(date==null) 
			return "never";
		return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(date.getTime());
	}
	public String getSeeAlsoLastHarvest() {
		Calendar date = Global.getTimestampTracker().getDatasetTimestamp(((IiifDataset)dataset).getSeeAlsoDatasetUri());
		if(date==null) 
			return "never";
		return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(date.getTime());
	}
	
	@Override
	public String output() throws Exception {
		StringWriter w=new StringWriter();
		Template temp = GlobalFrontend.FREE_MARKER.getTemplate("datasetmanagement/"+getClass().getSimpleName()+".html");
		temp.process(this, w);
		w.close();
		return w.toString();
	}
	
	@Override
	public boolean validate() {
		return true;
	}
}