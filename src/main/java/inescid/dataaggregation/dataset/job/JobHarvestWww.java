package inescid.dataaggregation.dataset.job;

import java.util.Calendar;

import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.dataaggregation.crawl.ld.LdDatasetHarvest;
import inescid.dataaggregation.crawl.www.WwwDatasetHarvest;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.WwwDataset;
import inescid.dataaggregation.dataset.detection.DataProfileDetectorFromHttpHeaders;

public class JobHarvestWww extends JobWorker {
	Integer sampleSize;

	public JobHarvestWww() {
	}
	
	public JobHarvestWww(int sampleSize) {
		this.sampleSize = sampleSize;
	}
	
	@Override
	public void runJob() throws Exception {
			TimestampTracker timestampTracker=Global.getTimestampTracker();
			WwwDataset lodDataset=(WwwDataset)dataset;
			WwwDatasetHarvest harvest=new WwwDatasetHarvest(lodDataset, Global.getDataRepository(), true/*skip existing*/, this);
			harvest.setSampleSize(sampleSize);
			Calendar startOfCrawl=harvest.startProcess();
			//I'm not sure if detection should be done for WWW datasets
//			if(dataset.getDataFormat()==null) {
//				KindOfData detected = DataProfileDetectorFromHttpHeaders.detect(lodDataset.getUri(), Global.getDataRepository());
//				if(detected!=null)
//					dataset.setDataFormat(detected);
//				else
//					dataset.setDataFormat(KindOfData.ANY_TRIPLES);
//				Global.getDatasetRegistryRepository().updateDataset(dataset);
//			}
			
			if(harvest.getRunError()!=null)
				finishedWithFailure(harvest.getRunError());
			timestampTracker.setDatasetTimestamp(lodDataset.getUri(), startOfCrawl);
			timestampTracker.commit();
	}

}
