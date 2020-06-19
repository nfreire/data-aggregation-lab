package inescid.europeana.dataprocessing;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

public class ScriptCalcStatsOfLangTags {

	public static void main(String[] args) throws Exception {
		long cntAll=0;
		int cntCountry=0;
		int cntVariant=0;
		int cntScript=0;
		int cntExtension=0;
		int cntSubtags=0;
		
		File csvFile=new File("C:\\Users\\nfrei\\Desktop\\processing\\langtags-provider.csv");
		CSVParser parser=new CSVParser(Files.newBufferedReader(csvFile.toPath()), CSVFormat.DEFAULT);
		for(Iterator<CSVRecord> it=parser.iterator(); it.hasNext() ; ) {
			CSVRecord rec = it.next();
			int count=Integer.parseInt(rec.get(1));
			cntAll+=count;
//			System.out.println(rec.get(0));
//			System.out.println(count);
			Locale loc=Locale.forLanguageTag(rec.get(0));
//			loc.getLanguage();
			if(!StringUtils.isEmpty(loc.getCountry()) || !StringUtils.isEmpty(loc.getVariant()) ||
					!StringUtils.isEmpty(loc.getScript()) || !loc.getExtensionKeys().isEmpty()) {
				cntSubtags+=count;
				if(!StringUtils.isEmpty(loc.getCountry()))
					cntCountry+=count;
				if(!StringUtils.isEmpty(loc.getVariant()))
					cntVariant+=count;
				if(!StringUtils.isEmpty(loc.getScript()))
					cntScript+=count;
				if(!loc.getExtensionKeys().isEmpty())
					cntExtension+=count;
			}
		}
		parser.close();
		System.out.printf("tags %,d\n", cntAll);
		System.out.printf("with subtags %,d (%2.2f%%)\n\n", cntSubtags, (double)cntSubtags/cntAll*100);
		System.out.printf("region %,d (%2.2f%%)\n", cntCountry, (double)cntCountry/cntAll*100);
		System.out.printf("variant %,d (%2.2f%%)\n", cntVariant, (double)cntVariant/cntAll*100);
		System.out.printf("script %,d (%2.2f%%)\n", cntScript, (double)cntScript/cntAll*100);
		System.out.printf("extension %,d (%2.2f%%)\n", cntExtension, (double)cntExtension/cntAll*100);
	}
}
