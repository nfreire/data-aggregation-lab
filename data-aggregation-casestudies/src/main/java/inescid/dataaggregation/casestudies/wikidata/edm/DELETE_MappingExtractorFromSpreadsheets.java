package inescid.dataaggregation.casestudies.wikidata.edm;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import inescid.dataaggregation.data.model.Edm;

public class DELETE_MappingExtractorFromSpreadsheets {

	
	public static void main(String[] args) throws Exception {
		BufferedReader reader = Files.newBufferedReader(new File("src/data/wikidata/wikidata_edm_mappings.csv").toPath(), StandardCharsets.UTF_8);
		CSVParser csvParser=new CSVParser(reader, CSVFormat.DEFAULT);
		for(CSVRecord r: csvParser) {
			
		}
		csvParser.close();
		reader.close();		
	}
}
