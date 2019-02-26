package inescid.dataaggregation.dataset.job;

import java.util.GregorianCalendar;

import eu.europeana.research.iiif.crawl.CollectionCrawler;
import eu.europeana.research.iiif.crawl.ManifestHarvester;
import eu.europeana.research.iiif.crawl.ManifestSeeAlsoHarvester;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.demo.TimestampCrawlingHandler;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.profile.SeeAlsoProfile;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.detection.ContentTypes;
import inescid.dataaggregation.dataset.detection.DataProfileDetector;
import inescid.dataaggregation.dataset.detection.DataProfileDetectorFromHttpHeaders;
import inescid.dataaggregation.dataset.detection.DataTypeResult;
import inescid.util.LinkedDataUtil;

public class JobHarvestIiifSeeAlso extends JobWorker implements Runnable {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
		.getLogger(JobHarvestIiifSeeAlso.class);
	
	Integer sampleSize;
	String seeAlsoType;

	public JobHarvestIiifSeeAlso(Job job, Dataset dataset, String seeAlsoType) {
		super(job, dataset);
		this.seeAlsoType = seeAlsoType;
	}
	
	public JobHarvestIiifSeeAlso(Job job, Dataset dataset, String seeAlsoType, int sampleSize) {
		super(job, dataset);
		this.sampleSize = sampleSize;
		this.seeAlsoType = seeAlsoType;
	}

	@Override
	public void runJob() throws Exception{
			TimestampTracker timestampTracker=GlobalCore.getTimestampTracker();
			GregorianCalendar startOfCrawl=new GregorianCalendar();
			IiifDataset iiifDataset=(IiifDataset)dataset;
			try {
				String[] ps=SeeAlsoProfile.parseFormatProfile(seeAlsoType);
				String format=ps[0];
				String profile=ps[1];
				iiifDataset.setDataFormat(format);
				ManifestSeeAlsoHarvester harvester=new ManifestSeeAlsoHarvester(GlobalCore.getDataRepository(), timestampTracker, iiifDataset, format, profile);
				harvester.harvest();
				DatasetProfile datasetProfileEnum=DatasetProfile.fromString(profile);
				if(datasetProfileEnum==null || datasetProfileEnum==DatasetProfile.ANY_TRIPLES) {
					DataTypeResult detected = DataProfileDetector.detect(iiifDataset.getSeeAlsoDatasetUri(), GlobalCore.getDataRepository());
					if(detected!=null) {
						if(detected.format!=null && dataset.getDataFormat()==null) {
							dataset.setDataFormat(detected.format.toString());
						}
						if(detected.profile!=null && dataset.getDataProfile()==null) {
							dataset.setDataProfile(detected.profile.toString());
						}
					} else if (datasetProfileEnum!=null){
						dataset.setDataProfile(datasetProfileEnum.toString());
					}
				} else {
					dataset.setDataProfile(datasetProfileEnum.toString());				
				}
				GlobalCore.getDatasetRegistryRepository().updateDataset(dataset);
				timestampTracker.setDatasetTimestamp(iiifDataset.getSeeAlsoDatasetUri(), startOfCrawl);
				timestampTracker.commit();
				finishedSuccsessfuly();
			} catch (Exception e) {
				log.info("Harvest failed: ",e);
				timestampTracker.setDatasetLastError(iiifDataset.getSeeAlsoDatasetUri(), startOfCrawl);
				timestampTracker.commit();
				finishedWithFailure(e);
			}
	}
}
