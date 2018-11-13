package inescid.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.dataset.Global;

public class DatasetLog {
	private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatasetLog.class);
	
	File logFile;

	private int harvestSuccessCount;
	private int harvestFailCount;
	
	public DatasetLog(String datasetUri) {
		logFile=Global.getDataRepository().getDatasetLogFile(datasetUri);
	}
	
	public void logHarvestIssue(String rdfResouceUri, String message) {
		harvestFailCount++;
		if((harvestSuccessCount + harvestFailCount) % 100 == 0)
			log.info(String.format("Harvest progress: %d resources, %d failures", harvestSuccessCount, harvestFailCount));
		try {
			FileUtils.write(logFile, String.format("[%1$tF %1$tR] %2$s [H] %3$s\n", new Date(), rdfResouceUri, message), Global.UTF8, true);
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
	}
	
	public void logHarvestIssue(String rdfResouceUri, Exception e) {
		logHarvestIssue(rdfResouceUri, e.getClass().getSimpleName()+" - "+e.getMessage());
		log.info(rdfResouceUri, e);
	}
	public void logValidationIssue(String rdfResouceUri, String message) {
		try {
			FileUtils.write(logFile, String.format("[%1$tF %1$tR] %2$s [V] %3$s\n", new Date(), rdfResouceUri, message), Global.UTF8, true);
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
	
	}

	public void logHarvestSuccess() {
		harvestSuccessCount++;
		if((harvestSuccessCount + harvestFailCount) % 100 == 0)
			log.info(String.format("Harvest progress: %d resources, %d failures", harvestSuccessCount, harvestFailCount));
	}

	public void logSkippedRdfResource() {
		logHarvestSuccess();
	}

	public void logFinish() {
		log.info(String.format("Harvest finished: %d resources, %d failures", harvestSuccessCount, harvestFailCount));		
	}
}
