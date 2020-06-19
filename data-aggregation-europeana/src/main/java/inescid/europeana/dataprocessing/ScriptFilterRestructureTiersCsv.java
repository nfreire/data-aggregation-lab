package inescid.europeana.dataprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class ScriptFilterRestructureTiersCsv {

	public static void main(String[] args) throws Exception {
		File csvFile=new File("C:\\Users\\nfrei\\Desktop\\data\\tiers.csv");
		File csvFileOut=new File("C:\\Users\\nfrei\\Desktop\\data\\tiers_out.csv");
		BufferedWriter outWrt = Files.newBufferedWriter(csvFileOut.toPath());
		CSVPrinter printer=new CSVPrinter(outWrt, CSVFormat.DEFAULT);
		CSVParser parser=new CSVParser(Files.newBufferedReader(csvFile.toPath()), CSVFormat.DEFAULT);
		for(Iterator<CSVRecord> it=parser.iterator(); it.hasNext() ; ) {
			printer.print(it.next().get(1));
			it.next();
			for(int i=0 ; i<4 ; i++) {
				CSVRecord l=it.next();
				printer.print(l.get(0));
				printer.print(l.get(1));
				printer.print(l.get(2));
				printer.print(l.get(3));
				printer.print(l.get(4));
			}
			printer.println();
		}
		printer.close();
		outWrt.close();
		
	}
}
