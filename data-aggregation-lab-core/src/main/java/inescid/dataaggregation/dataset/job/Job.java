package inescid.dataaggregation.dataset.job;

import java.util.GregorianCalendar;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;

public class Job {
	public enum JobStatus {PENDING, RUNNING, COMPLETED, FAILED};

	public enum JobType {
		HARVEST, HARVEST_SAMPLE, HARVEST_SEEALSO, PUBLISH_DATA, PUBLISH_SEEALSO_DATA, PROFILE_MANIFESTS, DIAGNOSE_DATASET, PROFILE_RDF, CONVERT, VALIDATE_EDM;

		public JobWorker newWorker(Job job, Dataset dataset, String jobParams) {
			DatasetType datasetType=dataset.getType();
			switch (this) {
			case HARVEST:
				if(datasetType==DatasetType.IIIF)
					return new JobHarvestIiif(job, dataset);
				else if(datasetType==DatasetType.LOD)
					return new JobHarvestLod(job, dataset);
				else if(datasetType==DatasetType.WWW)
					return new JobHarvestWww(job, dataset);
			case HARVEST_SAMPLE:
				if(datasetType==DatasetType.IIIF)
					return new JobHarvestIiif(job, dataset,100);
				else if(datasetType==DatasetType.LOD)
					return new JobHarvestLod(job, dataset,100);
				else if(datasetType==DatasetType.WWW)
					return new JobHarvestWww(job, dataset,100);
			case PROFILE_MANIFESTS:
				if(datasetType==DatasetType.IIIF)
					return new JobProfileIiifManifests(job, dataset);
				break;
			case DIAGNOSE_DATASET:
				if(datasetType==DatasetType.IIIF)
					return new JobDiagnoseIiifSourceForEuropeana(job, dataset);
				break;
			case PROFILE_RDF:
				return new JobProfileSchemaOrg(job, dataset);
			case CONVERT:
				return new JobConvertSchemaOrgToEdm(job, dataset);
			case VALIDATE_EDM:
				return new JobValidateEdm(job, dataset);
			case PUBLISH_DATA:
				return new JobPublish(job, dataset);
			case PUBLISH_SEEALSO_DATA:
				return new JobPublishSeeAlso(job, dataset);
			case HARVEST_SEEALSO:
				return new JobHarvestIiifSeeAlso(job, dataset, jobParams);
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
		worker=type.newWorker(this, dataset, null);
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
		worker=jl.type.newWorker(this, dataset, jl.parameters);
	}



	public Job(JobType harvestSeealso, Dataset dataset, String parameters) {
		this(harvestSeealso, dataset);
		this.parameters = parameters;
	}

}
