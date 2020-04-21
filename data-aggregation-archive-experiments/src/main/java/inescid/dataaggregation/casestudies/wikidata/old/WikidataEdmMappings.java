package inescid.dataaggregation.casestudies.wikidata.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class WikidataEdmMappings {
	Map<String, String> wikidataToEdmMap=new HashMap<>();

	public WikidataEdmMappings(File csvFile) throws IOException {
		Reader csvReader=new InputStreamReader(new FileInputStream(csvFile), "UTF-8");
		BufferedReader csvBufferedReader = new BufferedReader(csvReader);
		csvBufferedReader.readLine(); //skip heading line
		while(csvBufferedReader.ready()) {
			String mappingLine = csvBufferedReader.readLine();
			String[] split = mappingLine.split(",");
			if (split.length>=2) {
				String wdProp=split[0].trim();
				String edmProp=split[1].trim();
				if (!StringUtils.isEmpty(wdProp) && !StringUtils.isEmpty(edmProp)) {
					wikidataToEdmMap.put(wdProp, edmProp);
					System.out.println(wdProp +" , "+edmProp);
				}
			}
		}
		csvBufferedReader.close();
		
		System.out.println(wikidataToEdmMap.size() + " wikidata edm mappings loaded.");
	}
	
	public static void main(String[] args) throws Exception {
		WikidataEdmMappings wdMappings=new WikidataEdmMappings(WikidataEuropeanaConstants.MAPPINGS_WD_EDM_CSV);
	}

	public String getFromWdId(String id) {
		String edmProp=wikidataToEdmMap.get("https://www.wikidata.org/wiki/Property:"+id);
		if(edmProp==null) //try http instead of https
			edmProp=wikidataToEdmMap.get("http://www.wikidata.org/wiki/Property:"+id);
		if(edmProp==null) //try with variants
			edmProp=wikidataToEdmMap.get("http://www.wikidata.org/entity/"+id);
		if(edmProp==null) //try with variants
			edmProp=wikidataToEdmMap.get("http://www.wikidata.org/prop/"+id);
		return edmProp;
	}
}
