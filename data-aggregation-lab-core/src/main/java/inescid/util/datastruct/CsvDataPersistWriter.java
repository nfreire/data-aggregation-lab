package inescid.util.datastruct;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CsvDataPersistWriter {
	Appendable writeTo;
	
	public CsvDataPersistWriter() {
		writeTo=new StringBuilder();
	}

	public CsvDataPersistWriter(Appendable writeTo) {
		this.writeTo = writeTo;
	}
	
	public void write(Map map) throws IOException {
		write(map, null, null);
	}
	public void write(Map map, String headerKey, String headerValue) throws IOException {
		CSVPrinter printer=new CSVPrinter(writeTo, CSVFormat.DEFAULT);
		if(headerKey!=null || headerValue!=null)
			printer.printRecord(headerKey, headerValue);
		map.entrySet();
		for(Object k:map.keySet()) {
			printer.printRecord(k, map.get(k));
		}
		printer.close();
	}
	
	public Appendable getOutput() {
		return writeTo;
	}
}
