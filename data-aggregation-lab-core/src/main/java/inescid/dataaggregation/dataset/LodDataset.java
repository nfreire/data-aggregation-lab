package inescid.dataaggregation.dataset;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class LodDataset extends Dataset {
	
	public LodDataset(String uri) {
		super(DatasetType.LOD);
		this.uri=uri;
	}

	public LodDataset() {
		super(DatasetType.LOD);
	}

	public LodDataset(CSVRecord csvRecord) {
		super(csvRecord, DatasetType.LOD);
//		readMetaFromCsv(csvRecord, 5);
	}

	@Override
	public String toCsv() {
		try {
			StringBuilder sb=new StringBuilder();
			CSVPrinter rec=new CSVPrinter(sb, CSVFormat.DEFAULT);
			super.toCsvPrint(rec);
//			super.toCsvPrintMeta(rec);
			rec.println();
			rec.close();
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}


}
