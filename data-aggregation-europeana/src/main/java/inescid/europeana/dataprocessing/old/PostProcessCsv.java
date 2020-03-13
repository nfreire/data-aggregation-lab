package inescid.europeana.dataprocessing.old;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.google.common.io.Files;

public class PostProcessCsv {

	public static void main(String[] args) throws Exception {
		String csvPath="C:\\Users\\nfrei\\Desktop\\data\\edm-measurements.csv";
		String outFileBasename="edm-measurements-all";

		File csvInFile = new File(csvPath);
		CsvPrinterWithSizeLimit printer=new CsvPrinterWithSizeLimit(csvInFile.getParentFile(), outFileBasename, 99*1000*1024);
		
		BufferedReader csvReader = java.nio.file.Files.newBufferedReader(csvInFile.toPath(), StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(csvReader, CSVFormat.DEFAULT);
		for(CSVRecord r:parser) {
			for(int i=1; i<r.size(); i++)
				printer.print(r.get(i));
			printer.println();			
		}
		parser.close();
		
	}

	
	public static class CsvPrinterWithSizeLimit {
		String baseFilename;
		File currentCsvFile;
		CSVPrinter currentPrinter;
		BufferedWriter csvWriter;
		int fileCnt=1;
		long maxFileSize;
		
		public CsvPrinterWithSizeLimit(File outFolder, String baseFilename, long maxFileSize) throws IOException {
			super();
			this.baseFilename = baseFilename;
			this.maxFileSize = maxFileSize;
			
			currentCsvFile=new File(outFolder, baseFilename+"_"+fileCnt+".csv");
			csvWriter = Files.newWriter(currentCsvFile, StandardCharsets.UTF_8);
			currentPrinter=new CSVPrinter(csvWriter, CSVFormat.DEFAULT);
		}

		public void println() throws IOException {
			currentPrinter.println();
			currentPrinter.flush();
			csvWriter.flush();
			if(currentCsvFile.length() >= maxFileSize) {
				currentPrinter.close();
				csvWriter.close();
				fileCnt++;
				currentCsvFile=new File(currentCsvFile.getParent(), baseFilename+"_"+fileCnt+".csv");
				csvWriter = Files.newWriter(currentCsvFile, StandardCharsets.UTF_8);
				currentPrinter=new CSVPrinter(csvWriter, CSVFormat.DEFAULT);
			}
		}
		public void close() throws IOException {
			currentPrinter.close();
			csvWriter.close();
			if(currentCsvFile.length() == 0) 
				currentCsvFile.delete();
		}

		public void printRecord(CSVRecord r) throws IOException {
			for(int i=0; i<r.size(); i++)
				print(r.get(i));
			println();
		}

		public void print(String string) throws IOException {
			currentPrinter.print(string);			
		}
		
		
		
		
	}
	
}
