package inescid.dataaggregation.dataset;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class WwwDataset extends Dataset {
	public enum CrawlMethod {SITEMAP, HTML_LINK_CRAWL};
	public enum Microformat {SCHEMAORG, DC, HTML5_META,  META_ALL};
	
	Microformat microformat=Microformat.SCHEMAORG;
	
	public WwwDataset(String uri) {
		super(DatasetType.WWW);
		this.uri=uri;
	}

	public WwwDataset() {
		super(DatasetType.WWW);
	}

	public WwwDataset(CSVRecord csvRecord) {
		super(csvRecord, DatasetType.WWW);
		microformat=Microformat.valueOf(csvRecord.get(8));
//		readMetaFromCsv(csvRecord, 7);
	}

	@Override
	public String toCsv() {
		try {
			StringBuilder sb=new StringBuilder();
			CSVPrinter rec=new CSVPrinter(sb, CSVFormat.DEFAULT);
			super.toCsvPrint(rec);
			rec.print(microformat);
//			super.toCsvPrintMeta(rec);
			rec.println();
			rec.close();
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Microformat getMicroformat() {
		return microformat;
	}

	public void setMicroformat(Microformat microdata) {
		this.microformat = microdata;
	}
}
