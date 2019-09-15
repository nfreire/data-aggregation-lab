package inescid.dataaggregation.dataset;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class IiifDataset extends Dataset {
	public enum IiifCrawlMethod {COLLECTION, SITEMAP, DISCOVERY};
	
	IiifCrawlMethod crawlMethod=null;
	
	public IiifDataset(String uri) {
		super(DatasetType.IIIF);
		this.uri=uri;
	}

	public IiifDataset(CSVRecord csvRecord) {
		super(csvRecord, DatasetType.IIIF);
		crawlMethod=IiifCrawlMethod.valueOf(csvRecord.get(8));
//		readMetaFromCsv(csvRecord, 7);
	}


	public IiifDataset() {
		super(DatasetType.IIIF);
	}

	@Override
	public String toCsv() {
		try {
			StringBuilder sb=new StringBuilder();
			CSVPrinter rec=new CSVPrinter(sb, CSVFormat.DEFAULT);
			super.toCsvPrint(rec);
			rec.print(crawlMethod);
//			super.toCsvPrintMeta(rec);
			rec.println();
			rec.close();
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public IiifCrawlMethod getCrawlMethod() {
		return crawlMethod;
	}

	public void setCrawlMethod(IiifCrawlMethod crawlMethod) {
		this.crawlMethod = crawlMethod;
	}

	public String getSeeAlsoDatasetUri() {
		return Global.SEE_ALSO_DATASET_PREFIX+localId;
	}
}
