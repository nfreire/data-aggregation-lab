package inescid.europeana.dataprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

public class CalcCorrelationMatrixForEdmMeasurementsNEEDSREVISION {

	public static void main(String[] args) throws Exception {
		String csvPath="C:\\Users\\nfrei\\Desktop\\data\\edm-measurements.mvstore.bin";

		File mapsFile = new File(csvPath);
		if(!mapsFile.getParentFile().exists())
			mapsFile.getParentFile().mkdirs();
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();
		MVMap<String, String> idToCsv = mvStore.openMap(mapsFile.getName());
		
		EdmMeasurementSet measurementSet = EdmMeasurementSet.getMeasurementSetA();
		
		int nVars=measurementSet.getNumberOfMeasurements();
		SimpleRegression[][] regressions=new SimpleRegression[nVars][nVars];
		for(int x=0; x<nVars; x++) {
			for(int y=x+1; y<nVars; y++) 
				regressions[x][y]=new SimpleRegression();
		}

		int[] recordNumber=new int[] {0};
		idToCsv.forEach( (uri, csv) -> { 
        	try {
	        	CSVParser parser=new CSVParser(new StringReader(csv) , CSVFormat.DEFAULT);
	        	for(CSVRecord r: parser) {
	        		for(int x=0; x<nVars; x++) {
	        			for(int y=x+1; y<nVars; y++) 
	        				regressions[x][y].addData(Double.parseDouble(r.get(x)), Double.parseDouble(r.get(y)));
	        		}
	        	}
				parser.close();
	        	recordNumber[0]++;
	        	if(recordNumber[0] % 500000==0)
	        		System.out.println(recordNumber[0]);
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        });
		
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
		
		FileUtils.write(new File(mapsFile.getParentFile(), mapsFile.getName()+".corr.csv"), csvSb.toString(), StandardCharsets.UTF_8);
	}
}
