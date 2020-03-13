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
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import com.google.common.io.Files;

public class PostProcessCsvToH2 {

	public static void main(String[] args) throws Exception {
		String csvPath="C:\\Users\\nfrei\\Desktop\\data\\edm-measurements.csv";
		if(args!=null) {
			if(args.length>=1) {
				csvPath = args[0];
			}
		}		
		
		String outFileBasename="edm-measurements.mvstore.bin";

		File csvInFile = new File(csvPath);

		File mapsFile = new File(csvInFile.getParentFile(), outFileBasename);
		if(!mapsFile.getParentFile().exists())
			mapsFile.getParentFile().mkdirs();
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();
		
		MVMap<String, String> idToCsv = mvStore.openMap(mapsFile.getName());
		
		BufferedReader csvReader = java.nio.file.Files.newBufferedReader(csvInFile.toPath(), StandardCharsets.UTF_8);
		CSVParser parser=new CSVParser(csvReader, CSVFormat.DEFAULT);
		StringBuilder sb=new StringBuilder();
		boolean first=true;
		for(CSVRecord r:parser) {
			if(first) { first=false; continue; }
			CSVPrinter csv=new CSVPrinter(sb, CSVFormat.DEFAULT);
			for(int i=1; i<r.size(); i++) 
				csv.print(r.get(i));
			csv.println();
			csv.close();
			String recId=r.get(0);
			if(recId.startsWith("/")) 
				recId=r.get(0).substring(1);
			idToCsv.put(recId, sb.toString());
			sb.setLength(0);
		}
		parser.close();
		
		mvStore.close();
	}

	
}