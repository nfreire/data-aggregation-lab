package inescid.europeana.dataprocessing.googleanalytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import inescid.util.datastruct.MapOfInts;

public class GoogleAnalyticsRecordViewsCsv {

	public static void main(String[] args) throws Exception {
		combineCsvExports(new File("src/data/Analytics Filtered view (to use) Pages 20190101-20191231.csv"));
	}
	
	
	private static void combineCsvExports(File firstCsvFile) throws IOException {
		Pattern uriToIdPattern=Pattern.compile("/record/(.*/.*)\\.html");
		Pattern uriToIdPatternNoExtension=Pattern.compile("/record/([^?/]*/[^?/]*)");
		
		int fileNum=0;
		MapOfInts<String> combinedUriCounts=new MapOfInts<String>();
		
		String baseFilename=firstCsvFile.getName().substring(0, firstCsvFile.getName().lastIndexOf('.'));
		File nextCsv=firstCsvFile;
		while(nextCsv.exists()) {
			BufferedReader reader = Files.newBufferedReader(nextCsv.toPath(), StandardCharsets.UTF_8);
			CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);
			for(CSVRecord r: parser) {
				String url=r.get(0);
				Matcher matcher=uriToIdPattern.matcher(url);
				if(!matcher.find()) {
					matcher=uriToIdPatternNoExtension.matcher(url);
					if(!matcher.find()) {
						System.out.println("WARNING: record id not matching - "+ url);
						continue;
					}
				}
				String recId=matcher.group(1);
				int views=Integer.parseInt(r.get(2).replaceAll(",", ""));
				combinedUriCounts.addTo(recId, views);
			}
			parser.close();
			reader.close();
			fileNum++;
			nextCsv=new File(firstCsvFile.getParentFile(), baseFilename+" ("+fileNum+").csv");
		}
		
		
		BufferedWriter writer = Files.newBufferedWriter(new File(firstCsvFile.getParentFile(), baseFilename+"_combined.csv").toPath(), StandardCharsets.UTF_8);
		CSVPrinter out=new CSVPrinter(writer, CSVFormat.DEFAULT);
		for(String recId: combinedUriCounts.keySet()) {
			out.printRecord(recId, combinedUriCounts.get(recId));
		}
		out.close();
		writer.close();
	}
	
	
}
