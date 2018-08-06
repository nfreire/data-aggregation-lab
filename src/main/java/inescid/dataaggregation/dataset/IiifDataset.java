package inescid.dataaggregation.dataset;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import inescid.dataaggregation.dataset.Dataset.DatasetType;

public class IiifDataset extends Dataset {
	public enum IiifCrawlMethod {COLLECTION, SITEMAP, DISCOVERY};
	
	IiifCrawlMethod crawlMethod=IiifCrawlMethod.DISCOVERY;
	
	public IiifDataset(String uri) {
		super(DatasetType.IIIF);
		this.uri=uri;
	}

	public IiifDataset(CSVRecord csvRecord) {
		super(csvRecord.get(0), DatasetType.IIIF);
		organization=csvRecord.get(2);
		title=csvRecord.get(3);
		uri=csvRecord.get(4);
		crawlMethod=IiifCrawlMethod.valueOf(csvRecord.get(5));
	}

	public IiifDataset() {
		super(DatasetType.IIIF);
	}

	@Override
	public String toCsv() {
		try {
			StringBuilder sb=new StringBuilder();
			CSVPrinter rec=new CSVPrinter(sb, CSVFormat.DEFAULT);
			rec.printRecord(localId, type.toString(), organization, title, uri, crawlMethod);
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
}
