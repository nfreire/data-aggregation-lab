package inescid.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import javax.xml.bind.JAXBElement.GlobalScope;

import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.crawl.ld.LdGlobals;
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
			FileUtils.write(logFile, String.format("[%1$tF %1$tR] %2$s [H] %3$s\n", new Date(), rdfResouceUri, message), LdGlobals.charset, true);
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
	}
	public void logValidationIssue(String rdfResouceUri, String message) {
		try {
			FileUtils.write(logFile, String.format("[%1$tF %1$tR] %2$s [V] %3$s\n", new Date(), rdfResouceUri, message), LdGlobals.charset, true);
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
