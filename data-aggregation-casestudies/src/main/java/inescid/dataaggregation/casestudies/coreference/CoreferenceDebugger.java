package inescid.dataaggregation.casestudies.coreference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import inescid.util.MapOfInts;

public class CoreferenceDebugger {
	public static CoreferenceDebugger INSTANCE;
	public static void init(File testUrisCsv) throws Exception {
		INSTANCE=new CoreferenceDebugger(testUrisCsv);
	}
	
	File reportFolder;
	HashSet<String> testUris;
	MapOfInts<String> urisLinkedCount=new MapOfInts<String>();
	
	
	public CoreferenceDebugger(File testUrisCsv) throws Exception {
		super();
		this.reportFolder = testUrisCsv.getParentFile();
		testUris=new HashSet<String>();
		CSVParser urisParser=new CSVParser(new FileReader(testUrisCsv), CSVFormat.DEFAULT);
		for(CSVRecord rec:urisParser) {
			testUris.add(rec.get(0));
		}
		urisParser.close();
	}
	
	public void debug(EuropeanaSameAsSets sets, String reportName) throws IOException {
		if(! Consts.DEBUG) return;
		CSVPrinter vocabUrisPrinter=new CSVPrinter(new FileWriter(new File(reportFolder, reportName+".csv")), CSVFormat.DEFAULT);
		for(String uri: testUris) {
			Set<String> uriSet = sets.getUriSet(uri);
			if(uriSet==null)
				vocabUrisPrinter.printRecord(uri, "");
			else
				vocabUrisPrinter.printRecord(uri, uriSet.toString());
		}
		vocabUrisPrinter.close();
	}

	public void debugMerge(String subject, String object, Set<String> sbjSet, Set<String> objSet) {
		if(! Consts.DEBUG) return;
		CoreferenceDebugger.INSTANCE.debugUri(subject);
		CoreferenceDebugger.INSTANCE.debugUri(object);
		
		if(testUris.contains(subject) || testUris.contains(object)) 
		System.out.println("D: "+"Merge "+subject+" with "+object +"\n"
				+ "   sbj set: "+(sbjSet==null ? " - " : sbjSet) +"\n"
				+ "   obj set: "+(objSet==null ? " - " : objSet) );
	}

	public void debugUri(String uri) {
		urisLinkedCount.incrementTo(uri);
		if(urisLinkedCount.get(uri)>15)
			System.out.println("D: frequente URI: "+uri+" "+urisLinkedCount.get(uri));
	}
}
