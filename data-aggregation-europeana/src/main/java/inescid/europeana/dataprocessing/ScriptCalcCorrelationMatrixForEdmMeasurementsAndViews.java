package inescid.europeana.dataprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.jena.rdf.model.Resource;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

public class ScriptCalcCorrelationMatrixForEdmMeasurementsAndViews {

	public static void main(String[] args) throws Exception {
		String storeCsvPath="C:\\Users\\nfrei\\Desktop\\data\\edm-measurements.mvstore.bin";
		ArrayList<String> pageViewsCsvPaths=new ArrayList<String>() {{
			add("src\\data\\europeana_record_uviews_2019.csv");
			add("src\\data\\europeana_record_uviews_2020.csv");
		}};
		String edmMeasurementsCsvPath="src\\data\\edm-measurements.csv";
		if (args != null) {
			if (args.length >= 1) {
				storeCsvPath = args[0];
				if (args.length >= 2) {
					edmMeasurementsCsvPath = args[1];
					if (args.length >= 3) {
						pageViewsCsvPaths.clear();
						for(int i=2; i<args.length; i++) {
							pageViewsCsvPaths.add(args[i]);
						}
					}
				}
			}
		}
		
		MVMap<String, String> idToCsv;
		File mapsFile = new File(storeCsvPath);
		if(!mapsFile.exists())
			idToCsv = createEdmMeasurementsStore(edmMeasurementsCsvPath, mapsFile);
		else {
			MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();
			idToCsv = mvStore.openMap(mapsFile.getName());
		}
		
		EdmMeasurementSet measurementSet = EdmMeasurementSet.getMeasurementSetA();
		measurementSet=new EdmMeasurementSet(measurementSet, new EdmMeasurementSet.EdmMeasurement() {
			public String[] getCsvResult(Resource edmCho, String edmRdfXml) throws Exception {
				throw new RuntimeException("Not used");
			}
			public String[] getHeaders() {
				return new String[] { "unique_views" };
			}
		});
		
		int nVars=measurementSet.getNumberOfMeasurements();
		SimpleRegression[][] regressions=new SimpleRegression[nVars][nVars];
		for(int x=0; x<nVars; x++) {
			for(int y=x+1; y<nVars; y++) 
				regressions[x][y]=new SimpleRegression();
		}

		int[] recordNumber=new int[] {0};
		int[] recordNumberWithViews=new int[] {0};
		for(String pageViewsCsvPath: pageViewsCsvPaths) {
			BufferedReader reader = Files.newBufferedReader(new File(pageViewsCsvPath).toPath(), StandardCharsets.UTF_8);
			CSVParser parserViews=new CSVParser(reader, CSVFormat.DEFAULT);
			for(CSVRecord viewsRec: parserViews) {
				recordNumberWithViews[0]++;
	//			System.out.println(viewsRec.get(0));
				String csv=idToCsv.get(viewsRec.get(0));
				
				if(csv==null) continue;
	        	try {
		        	CSVParser parser=new CSVParser(new StringReader(csv) , CSVFormat.DEFAULT);
		        	for(CSVRecord r: parser) {
		        		for(int x=0; x<nVars; x++) {
		        			for(int y=x+1; y<nVars; y++) {
		        				if(y==nVars-1)
		        					regressions[x][y].addData(Double.parseDouble(r.get(x)), Double.parseDouble(viewsRec.get(1)));
		        				else
		        					regressions[x][y].addData(Double.parseDouble(r.get(x)), Double.parseDouble(r.get(y)));
		        			}
		        		}
		        	}
					parser.close();
		        	recordNumber[0]++;
		        	if(recordNumber[0] % 10==0)
		        		System.out.println(recordNumber[0]);
	        	} catch (IOException e) {
	        		e.printStackTrace();
	        	}
	        }
			parserViews.close();
		}
		
		System.out.println(recordNumber[0] +" found out of "+recordNumberWithViews[0]+" in google analytics");
		
		StringBuilder csvSb = new StringBuilder();
		CSVPrinter csvOut=new CSVPrinter(csvSb, CSVFormat.DEFAULT);
		
		csvOut.printRecord(recordNumber[0], " found out of ", recordNumberWithViews[0], " in google analytics");
		csvOut.println();
		csvOut.println();
		
		csvOut.print("");
		List<String> headers = measurementSet.getHeaders();
		for(String head: headers) {
			csvOut.print(head);
		}
		csvOut.println();
		for(int x=0; x<nVars; x++) {
			csvOut.print(headers.get(x));
			for(int x2=0; x2<x; x2++) {
				System.out.println(x2+" "+x);
				csvOut.print(regressions[x2][x].getR());
			}
			csvOut.print("");
			for(int y=x+1; y<nVars; y++) { 
				System.out.println(x+" "+y);
				csvOut.print(regressions[x][y].getR());
			}
			csvOut.println();
		}
		csvOut.close();
		
		System.out.println(csvSb);
		FileUtils.write(new File(mapsFile.getParentFile(), new File(pageViewsCsvPaths.get(0)).getName()+".all-corr.csv"), csvSb.toString(), StandardCharsets.UTF_8);
	}

	private static MVMap<String, String> createEdmMeasurementsStore(String edmMeasurementsCsvPath, File mapsFile) throws IOException {
		if(!mapsFile.getParentFile().exists())
			mapsFile.getParentFile().mkdirs();
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();
		MVMap<String, String> idToCsv = mvStore.openMap(mapsFile.getName());

		BufferedReader reader = Files.newBufferedReader(new File(edmMeasurementsCsvPath).toPath(), StandardCharsets.UTF_8);
		for(String line=reader.readLine(); line!=null ; line=reader.readLine()) {
			String recId=line.substring(0, line.indexOf(','));
			String recFields=line.substring(line.indexOf(',')+1);
			idToCsv.put(recId, recFields);
		}
		return idToCsv;
	}
}
