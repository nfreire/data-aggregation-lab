package inescid.dataaggregation.dataset;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;

public class LodDataset extends Dataset {
	
	public LodDataset(String uri) {
		super(DatasetType.LOD);
		this.uri=uri;
	}


	public LodDataset() {
		super(DatasetType.LOD);
	}


	public LodDataset(CSVRecord csvRecord) {
		super(csvRecord.get(0), DatasetType.LOD);
		organization=csvRecord.get(2);
		title=csvRecord.get(3);
		uri=csvRecord.get(4);
	}


	@Override
	public String toCsv() {
		try {
			StringBuilder sb=new StringBuilder();
			CSVPrinter rec=new CSVPrinter(sb, CSVFormat.DEFAULT);
			rec.printRecord(localId, type.toString(), organization, title, uri);
			rec.close();
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


}
