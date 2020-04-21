package inescid.util.datastruct;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.apache.any23.extractor.csv.CSVReaderBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

public class CsvDataPersistReader {
	Reader readFrom;

	public CsvDataPersistReader(Reader readFrom) {
		this.readFrom = readFrom;
	}
	
	public void read(Map map) throws IOException {
		read(map, false);
	}
	public void read(Map map, boolean ignoreHeader) throws IOException {
		CSVParser parser=new CSVParser(readFrom, CSVFormat.DEFAULT);
		parser.forEach(rec -> {
			map.put(rec.get(0), rec.get(1));
		});
		parser.close();
	}
	
}
