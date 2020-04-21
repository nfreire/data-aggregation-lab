package inescid.dataaggregation.casestudies.wikidata.old;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import inescid.util.datastruct.MapOfInts;

public class ScriptAnalyzePageJsonForEuropeanaLinksWebClient {
	Pattern EUROPEANA_URL_PATTERN=Pattern.compile("\"?https?://(www.)?europeana.eu/", Pattern.CASE_INSENSITIVE);
	
	HashMap<String, HashSet<String>> europeanaReferences=new HashMap<>();
	MapOfInts<String> propertiesEuropeanaReferences=new MapOfInts<>();
	MapOfInts<String> propertiesOthersReference=new MapOfInts<>();
	int europeanaIdCount=0;
	
	public static void main(String[] args) throws Exception {
		new ScriptAnalyzePageJsonForEuropeanaLinksWebClient().run();
		
	}

	public void run() throws IOException, MediaWikiApiErrorException {
		WikidataEntityCache wikidataClient=new WikidataEntityCache(WikidataEuropeanaConstants.CACHE_WIKIDATA_PROPERTIES);

		FileInputStream fileInStream=new FileInputStream(WikidataEuropeanaConstants.REFERENCES_JSON_DUMP);
		GZIPInputStream gzipInStream=new GZIPInputStream(fileInStream);
		InputStreamReader inStreamReader=new InputStreamReader(gzipInStream);
		BufferedReader bufReader=new BufferedReader(inStreamReader);
		
		int cnt=0;
		while(cnt<50000 && bufReader.ready()) {
			cnt++;
			if(cnt % 200 == 0)
				System.out.println(cnt+" items analyzed");
			
			String jsonRec=bufReader.readLine();
//			System.out.println(jsonRec);
			JsonParser p=new JsonFactory(new ObjectMapper()).createParser(jsonRec);
			TreeNode jsonTree = p.readValueAsTree();
			String id = ((JsonNode) jsonTree.get("id")).asText();
			EntityDocument entityDoc = wikidataClient.getEntityDocument(id);
			if (entityDoc instanceof ItemDocument) {
				ItemDocument itemDoc = (ItemDocument) entityDoc;
				System.out.println("entity "+id);
				
				for(StatementGroup stGrp: itemDoc.getStatementGroups()) {
					for(Statement st: stGrp.getStatements()) {
						for(Reference ref: st.getReferences()) {
							for(Iterator<Snak> snaks = ref.getAllSnaks() ; snaks.hasNext() ;) {
								Snak snak=snaks.next();
								if(snak.getPropertyId().getId().equals("P854")) {
									String v = snak.getValue().toString();
//									v = v.substring(1, v.length()-1);
									if( EUROPEANA_URL_PATTERN.matcher(v).find() ) {
										logEuropeanaReference(v, id, stGrp.getProperty().getId());
//										System.out.println(	"grp "+ stGrp.getProperty().getId() +" "+/*stGrp.getProperty().getEntityType()+*/" snack " +snak.getPropertyId().getId() + " - "+ v );
									} else {
										logOthersReference(v, id, stGrp.getProperty().getId());										
									}
								}
							}
						}
					}
				}
			}

		}
		finalizeOutput();
		
		bufReader.close();
		inStreamReader.close();
		gzipInStream.close();
		fileInStream.close();
		
	}
	
	private void finalizeOutput() throws IOException {
		FileWriterWithEncoding logWriter=new FileWriterWithEncoding(WikidataEuropeanaConstants.REFERENCES_CSV, "UTF-8");
		logWriter.write("Europeana Referenced Properties\n");
		ArrayList<Entry<String, Integer>> entries=new ArrayList<>(propertiesEuropeanaReferences.entrySet()); 
		Collections.sort(entries, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue()-o1.getValue();
			}
			
		});
		for(Entry<String, Integer> p: entries) 
			logWriter.write(p.getKey()+","+p.getValue()+"\n");
		logWriter.write("\nOther Providers Referenced Properties\n");
		entries=new ArrayList<>(propertiesOthersReference.entrySet()); 
		Collections.sort(entries, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue()-o1.getValue();
			}
		});
		for(Entry<String, Integer> p: entries) 
			logWriter.write(p.getKey()+","+p.getValue()+"\n");
		logWriter.write("\nEuropeana Item References\n");
		ArrayList<Entry<String, HashSet<String>>> europeanaEntries=new ArrayList<Entry<String, HashSet<String>>>(europeanaReferences.entrySet()); 
		Collections.sort(europeanaEntries, new Comparator<Entry<String, HashSet<String>>>() {
			@Override
			public int compare(Entry<String, HashSet<String>> o1, Entry<String, HashSet<String>> o2) {
				return o1.getValue().iterator().next().compareTo(o2.getValue().iterator().next());
			}
			
		});
		for(Entry<String, HashSet<String>> ref: europeanaEntries) {
			logWriter.write(ref.getKey());
			for(String wdId: ref.getValue())
				logWriter.write(","+wdId);
			logWriter.write("\n");
		}
		logWriter.close();
	}

	
	private void logEuropeanaReference(String europeanaUrl, String wikidataId, String propertyId) {
		if(propertyId.equals("P727"))
			europeanaIdCount++;
//		else {
			HashSet<String> wikidataIds = europeanaReferences.get(europeanaUrl);
			if(wikidataIds==null) {
				wikidataIds=new HashSet<>();
				europeanaReferences.put(europeanaUrl, wikidataIds);
			}
			wikidataIds.add(wikidataId);
			
			propertiesEuropeanaReferences.incrementTo(propertyId);

//		}
	}

	private void logOthersReference(String europeanaUrl, String wikidataId, String propertyId) {
		propertiesOthersReference.incrementTo(propertyId);
	}
	
}
