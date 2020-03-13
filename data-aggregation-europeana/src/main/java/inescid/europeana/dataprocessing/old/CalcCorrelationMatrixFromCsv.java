package inescid.europeana.dataprocessing.old;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import inescid.europeana.dataprocessing.EdmMeasurementSet;

public class CalcCorrelationMatrixFromCsv {

	public static void main(String[] args) throws Exception {
		String csvPath="C:\\Users\\nfrei\\Desktop\\data\\edm-measurements.csv";
		
		EdmMeasurementSet measurementSet = EdmMeasurementSet.getMeasurementSetA();
		
		int nVars=measurementSet.getNumberOfMeasurements();
		SimpleRegression[][] regressions=new SimpleRegression[nVars][nVars];
		for(int x=0; x<nVars; x++) {
			for(int y=x+1; y<nVars; y++) 
				regressions[x][y]=new SimpleRegression();
		}
		
		File csvInFile = new File(csvPath);
		
		BufferedReader csvReader = java.nio.file.Files.newBufferedReader(csvInFile.toPath(), StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(csvReader, CSVFormat.DEFAULT);
		
		for(CSVRecord r:parser) {
			if(parser.getRecordNumber()==1)
				continue;
			for(int x=0; x<nVars; x++) {
				for(int y=x+1; y<nVars; y++) 
					regressions[x][y].addData(Double.parseDouble(r.get(x+1)), Double.parseDouble(r.get(y+1)));
			}
			if(parser.getRecordNumber()%500000==0)
				System.out.println(parser.getRecordNumber());
		}
		parser.close();
		
		StringBuilder csvSb = new StringBuilder();
		CSVPrinter csvOut=new CSVPrinter(csvSb, CSVFormat.DEFAULT);
		csvOut.print("");
		List<String> headers = measurementSet.getHeaders();
		for(String head: headers) {
			csvOut.print(head);
		}
		csvOut.println();
		for(int x=0; x<nVars; x++) {
			csvOut.print(headers.get(x));
			for(int x2=0; x2<=x; x2++) 
				csvOut.print("");
			for(int y=x+1; y<nVars; y++) { 
				csvOut.print(regressions[x][y].getR());
			}
			csvOut.println();
		}
		csvOut.close();
		
		System.out.println(csvSb);
		
		FileUtils.write(new File(csvInFile.getParentFile(), csvInFile.getName()+".corr.csv"), csvSb.toString(), StandardCharsets.UTF_8);
	}
}
