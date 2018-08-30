package inescid.dataaggregation.dataset.job;

import java.util.Calendar;

import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.dataaggregation.crawl.ld.LdDatasetHarvest;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.detection.DataProfileDetector;
import inescid.dataaggregation.dataset.detection.DataProfileDetectorFromHttpHeaders;
import inescid.dataaggregation.dataset.detection.DataTypeResult;
import inescid.util.LinkedDataUtil;

public class JobHarvestLod extends JobWorker implements Runnable {
	Integer sampleSize;

	public JobHarvestLod() {
	}
	
	
	public JobHarvestLod(int sampleSize) {
		this.sampleSize = sampleSize;
	}
	
	@Override
	public void run() {
		running=true;
		try {
			TimestampTracker timestampTracker=Global.getTimestampTracker();
			LodDataset lodDataset=(LodDataset)dataset;
			LdDatasetHarvest harvest=new LdDatasetHarvest(lodDataset, Global.getDataRepository(), true/*skip existing*/);
			harvest.setSampleSize(sampleSize);
			Calendar startOfCrawl=harvest.startProcess();
			DatasetProfile datasetProfileEnum=DatasetProfile.fromString(dataset.getDataFormat());
			if(datasetProfileEnum==null || datasetProfileEnum==DatasetProfile.ANY_TRIPLES) {
				DataTypeResult detected = DataProfileDetector.detect(dataset.getUri(), Global.getDataRepository());
				boolean dsUpdated=false;
				if(detected!=null) {
					if(detected.format!=null && dataset.getDataFormat()==null) {
						dataset.setDataFormat(detected.format.toString());
						dsUpdated=true;
					}
					if(detected.profile!=null && dataset.getDataProfile()==null) {
						dataset.setDataProfile(detected.profile.toString());
						dsUpdated=true;
					}
				}
				if(dataset.getDataProfile()==null) {
					dataset.setDataProfile(DatasetProfile.ANY_TRIPLES.toString());
					dsUpdated=true;
				}
				if(dsUpdated)
					Global.getDatasetRegistryRepository().updateDataset(dataset);
			}
			timestampTracker.setDatasetTimestamp(lodDataset.getUri(), startOfCrawl);
			timestampTracker.commit();
			successful=true;
		} catch (Exception e) {
			failureCause=e;
		}
		running=false;
	}

}