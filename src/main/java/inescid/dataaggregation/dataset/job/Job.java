package inescid.dataaggregation.dataset.job;

import java.io.IOException;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;

public class Job {
	public enum JobStatus {PENDING, RUNNING, COMPLETED, FAILED};

	public enum JobType {
		HARVEST, HARVEST_SAMPLE, HARVEST_SEEALSO, PUBLISH_DATA, PUBLISH_SEEALSO_DATA, PROFILE_MANIFESTS, PROFILE_RDF, CONVERT, VALIDATE_EDM;
		
		public JobWorker newWorker(DatasetType datasetType, String jobParams) {
			switch (this) {
			case HARVEST:
				if(datasetType==DatasetType.IIIF)
					return new JobHarvestIiif();
				else if(datasetType==DatasetType.LOD)
					return new JobHarvestLod();
			case HARVEST_SAMPLE:
				if(datasetType==DatasetType.IIIF)
					return new JobHarvestIiif(100);
				else if(datasetType==DatasetType.LOD)
					return new JobHarvestLod(100);
				else if(datasetType==DatasetType.WWW)
					return new JobHarvestWww(100);
			case PROFILE_MANIFESTS:
				if(datasetType==DatasetType.IIIF)
					return new JobProfileIiifManifests();
				break;
			case PROFILE_RDF:
				return new JobProfileSchemaOrg();
			case CONVERT:
				return new JobConvertSchemaOrgToEdm();
			case VALIDATE_EDM:
				return new JobValidateEdm();
			case PUBLISH_DATA:
				return new JobPublish();
			case PUBLISH_SEEALSO_DATA:
				return new JobPublishSeeAlso();
			case HARVEST_SEEALSO:
				return new JobHarvestIiifSeeAlso(jobParams);
			}
			throw new RuntimeException("Missing implementation: "+this+" "+datasetType);
		}
	};
	
	JobType type;
	JobWorker worker;
	JobStatus status;
	GregorianCalendar statusTime;
	public String parameters;
	

	
	public Job(JobType type, Dataset dataset) {
		this.type = type;
		status=JobStatus.PENDING;
		statusTime=new GregorianCalendar();
		worker=type.newWorker(dataset.getType(), null);
		worker.setDataset(dataset);
	}
	
//	public Job(JobType type, Dataset dataset, JobStatus status) {
//		this.type = type;
//		this.status=status;
//		statusTime=new GregorianCalendar();
//		worker=type.newInstance(dataset.getType());
//		worker.setDataset(dataset);
//	}

	public Job(JobLog jl, Dataset dataset) {
		type=jl.type;
		status=jl.status;
		statusTime=jl.statusTime;
		worker=jl.type.newWorker(dataset.getType(), jl.parameters);
		worker.setDataset(dataset);
	}



	public Job(JobType harvestSeealso, Dataset dataset, String parameters) {
		this(harvestSeealso, dataset);
		this.parameters = parameters;
	}

}
