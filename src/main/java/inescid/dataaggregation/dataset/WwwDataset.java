package inescid.dataaggregation.dataset;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;

public class WwwDataset extends Dataset {
	public enum CrawlMethod {SITEMAP, HTML_LINK_CRAWL};
	public enum Microformat {SCHEMAORG, DC, HTML5_META,  META_ALL};
	
	Microformat microdata=Microformat.SCHEMAORG;
	
	public WwwDataset(String uri) {
		super(DatasetType.WWW);
		this.uri=uri;
	}

	public WwwDataset() {
		super(DatasetType.WWW);
	}

	public WwwDataset(CSVRecord csvRecord) {
		super(csvRecord, DatasetType.WWW);
		microdata=Microformat.valueOf(csvRecord.get(7));
//		readMetaFromCsv(csvRecord, 7);
	}

	@Override
	public String toCsv() {
		try {
			StringBuilder sb=new StringBuilder();
			CSVPrinter rec=new CSVPrinter(sb, CSVFormat.DEFAULT);
			super.toCsvPrint(rec);
			rec.print(microdata);
//			super.toCsvPrintMeta(rec);
			rec.println();
			rec.close();
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Microformat getMicroformat() {
		return microdata;
	}

	public void setMicroformat(Microformat microdata) {
		this.microdata = microdata;
	}
}
