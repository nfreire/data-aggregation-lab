package inescid.europeana.dataprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import eu.europeana.indexing.tiers.model.MetadataTier;
import inescid.util.datastruct.MapOfInts;

public class ScriptCalcTiersGlobalStats {

	public static void main(String[] args) throws Exception {
		File csvFile=new File("C:\\Users\\nfrei\\Desktop\\data\\processing\\tiers.csv");
		CSVParser parser=new CSVParser(Files.newBufferedReader(csvFile.toPath()), CSVFormat.DEFAULT);

		MapOfInts<MetadataTier> statsByTier=new MapOfInts<MetadataTier>();
		MapOfInts<MetadataTier> statsByTierLang=new MapOfInts<MetadataTier>();
		MapOfInts<MetadataTier> statsByTierContext=new MapOfInts<MetadataTier>();
		MapOfInts<MetadataTier> statsByTierEnabling=new MapOfInts<MetadataTier>();
		MapOfInts<MetadataTier>[] stats=new MapOfInts[] {
				statsByTier,
				statsByTierEnabling,
				statsByTierLang,
				statsByTierContext,
		};
		
		for(Iterator<CSVRecord> it=parser.iterator(); it.hasNext() ; ) {
			it.next();//collection
			it.next();//headers
			
			for(int i=0 ; i<4 ; i++) {
				CSVRecord l=it.next();
				stats[i].addTo(MetadataTier.T0, l.get(1).isEmpty() ? 0 : Integer.parseInt(l.get(1)));
				stats[i].addTo(MetadataTier.TA, l.get(2).isEmpty() ? 0 : Integer.parseInt(l.get(2)));
				stats[i].addTo(MetadataTier.TB, l.get(3).isEmpty() ? 0 : Integer.parseInt(l.get(3)));
				stats[i].addTo(MetadataTier.TC, l.get(4).isEmpty() ? 0 : Integer.parseInt(l.get(4)));
			}
			
		}
		parser.close();
		

		StringBuilder sb=new StringBuilder();
		CSVPrinter csv=new CSVPrinter(sb, CSVFormat.DEFAULT);
		
		MapOfInts<MetadataTier> colStats = statsByTier;
		csv.printRecord("Collection:", "Europeana");
		csv.print("Metadata Tier/Subtier");
		for(MetadataTier t: MetadataTier.values()) 
			csv.print(t.name());
		csv.println();
		
		csv.print("Metadata Tier");
		for(MetadataTier t: MetadataTier.values()) {
			Integer cnt = colStats.get(t);
			csv.print(cnt);
		}
		csv.println();
		colStats = statsByTierEnabling;
		csv.print(" - Enabling elements");
		for(MetadataTier t: MetadataTier.values()) {
			Integer cnt = colStats.get(t);
			csv.print(cnt);
		}
		csv.println();
		colStats = statsByTierLang;
		csv.print(" - Language");
		for(MetadataTier t: MetadataTier.values()) {
			Integer cnt = colStats.get(t);
			csv.print(cnt);
		}
		csv.println();
		colStats = statsByTierContext;
		csv.print(" - Context");
		for(MetadataTier t: MetadataTier.values()) {
			Integer cnt = colStats.get(t);
			csv.print(cnt);
		}
		csv.println();
		csv.close();
		
		System.out.println(sb.toString());
	}
}
