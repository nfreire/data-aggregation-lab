package inescid.dataaggregation.casestudies.coreference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.dataset.Global;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;

public class ScriptCoreferenceReporter {
	static final Pattern WIKIDATA_ENTITY_IN_URI=Pattern.compile(".`http://.*wikidata\\..+/(Q\\d+)$");
	
	RepositoryOfSameAs repoSameAs;
	EuropeanaSameAsSets sameAsSetsEuropeana;
	String datasetIdEuropeana;
	String reportFolderOut;
	HashMap<String, Integer> urisChosCounts=new HashMap<String, Integer>();
	
	public ScriptCoreferenceReporter(String repoFolder, String urisCsvPath, String datasetIdEuropeana, String reportFolderOut) throws IOException {
		repoSameAs = new RepositoryOfSameAs(new File(repoFolder));
		this.datasetIdEuropeana = datasetIdEuropeana;
		this.reportFolderOut = reportFolderOut;
		sameAsSetsEuropeana=new EuropeanaSameAsSets(urisCsvPath, repoSameAs.getSameAsSet(datasetIdEuropeana));
		
		File urisChoLinks=new File(new File(urisCsvPath).getParentFile(), "agents.chos.csv");
		BufferedReader fr=new BufferedReader(new FileReader(urisChoLinks));
		CSVParser csvReader=new CSVParser(fr, CSVFormat.DEFAULT);
		for(CSVRecord rec : csvReader) {
			String uri= rec.get(0);
			int chos = Integer.parseInt(rec.get(1));
			Matcher matcher = Consts.HOST_PATTERN.matcher(uri);
			if(!matcher.find()) continue;
//			String host = matcher.group(1);
//			if(!Consts.RDF_HOSTS.contains(host))
//				continue;
			urisChosCounts.put(uri, chos);
		}
		csvReader.close();
		fr.close();
	}

	public static void main(String[] args) throws Exception {
    	String csvUrisFile = "c://users/nfrei/desktop/data/coreference/agents.coref.csv";
    	String repoFolder = "c://users/nfrei/desktop/data/coreference";
    	String reportFolderOut = "c://users/nfrei/desktop/data/coreference";
    	String datasetIdEuropeanaConsolidated=Consts.europeanaProviders_datasetId;

		if(args!=null) {
			if(args.length>=1) {
				csvUrisFile = args[0];
				if(args.length>=2) {
					repoFolder = args[1];
					if(args.length>=3) {
						reportFolderOut = args[2];
					}
				}
			}
		}
		
		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(new File(repoFolder, "http-cache").getPath());
		Global.init_enableComponentHttpRequestCache();
		
		ScriptCoreferenceReporter corefFinder=new ScriptCoreferenceReporter(repoFolder, csvUrisFile, datasetIdEuropeanaConsolidated, reportFolderOut);
		corefFinder.runReportCoreferences();
		corefFinder.close();
				
		System.out.println("FININSHED: "+datasetIdEuropeanaConsolidated +" - " + reportFolderOut);
	}

	private void close() {
		repoSameAs.close();		
	}

	private void runReportCoreferences() throws Exception {
		int cntProcessed=0;
		int cntCoref=0;
		
		File htmlOutFile=new File(reportFolderOut, "europeana-providers-coreferences.html");
		File csvOutFile=new File(reportFolderOut, "europeana-providers-coreferences.csv");
//		File csvUriListOutFile=new File(reportFolderOut, "europeana-providers-coreferences-uri-list.csv");
//		BufferedWriter wHtml=new BufferedWriter(new FileWriter(htmlOutFile));
		BufferedWriter wCsv=new BufferedWriter(new FileWriter(csvOutFile));
		
		MapOfInts<String> statsCountOneByHost=new MapOfInts<String>();
		MapOfInts<String> statsCountByHost1stLevelProviderWikidataSameAsWith=new MapOfInts<String>();
		MapOfInts<String> statsCountByHost1stLevelProviderWikidataSameAsWithout=new MapOfInts<String>();
		MapOfInts<String> statsCountByHost1stLevelProviderWikidataSameAsWithChos=new MapOfInts<String>();

		Map<String, MapOfLists<String, String>> linksByHost1stLevelProviderWikidataSameAs=new HashMap<>();
		
//		MVMap<String, Set<String>> uriIndexEuropeana = sameAsSetsEuropeana.getUriIndex();

	    	 int size = sameAsSetsEuropeana.keySetOfVocabUris().size();
			for(String uri: sameAsSetsEuropeana.keySetOfVocabUris()) {
	 			Matcher matcher = Consts.HOST_PATTERN.matcher(uri);
	 			if(!matcher.find() || uri.contains("#")) continue;
	 			String host = matcher.group(1);
//	 			if(!Consts.RDF_HOSTS.contains(host))
//	 				continue;

	 			Set<String> ySet = sameAsSetsEuropeana.getUriSet(uri);
	    		if(ySet==null || ySet.size()==1) {
	    			statsCountByHost1stLevelProviderWikidataSameAsWithout.incrementTo(host);
	    			continue;
	    		}
	    		 
	    		boolean wikidataSameAs=false;
	    		boolean viafSameAs=false;
	    		boolean locSameAs=false;
	    		HashSet<String> hostsInSet=new HashSet<String>();
	    		for(String uriInSet: ySet) {
	     			Matcher matcherUriInSet = Consts.HOST_PATTERN.matcher(uriInSet);
		 			if(!matcherUriInSet.find()) continue;
		 			String hostUriInSet = matcherUriInSet.group(1);
		 			hostsInSet.add(hostUriInSet);
		 			if(hostUriInSet.equals("www.wikidata.org")) {
//		 				if(hostUriInSet.equals("www.wikidata.org")) {
		 				wikidataSameAs=true;
		 				MapOfLists<String, String> hostLinks = linksByHost1stLevelProviderWikidataSameAs.get(host);
		 				if(hostLinks==null) {
		 					hostLinks=new MapOfLists<String, String>();
		 					linksByHost1stLevelProviderWikidataSameAs.put(host, hostLinks);
		 				}
		 				hostLinks.put(uri, uriInSet);
		 			}else if(hostUriInSet.equals("id.loc.org")) {
		 				locSameAs=true;
		 			}else if(hostUriInSet.equals("viaf.org")) {
		 				viafSameAs=true;
		 			}
	    		}
//	    		if(!wikidataSameAs) {
//		    		for(String uriInSet: ySet) {
//			 			Matcher matcherWdEntity = WIKIDATA_ENTITY_IN_URI.matcher(uriInSet);
//			 			if(matcherWdEntity.matches()) {
//			 				uriInSet="http://www.wikidata.org/entity/"+matcherWdEntity.group(1);
//			 				
//			 				MapOfLists<String, String> hostLinks = linksByHost1stLevelProviderWikidataSameAs.get(host);
//			 				if(hostLinks==null) {
//			 					hostLinks=new MapOfLists<String, String>();
//			 					linksByHost1stLevelProviderWikidataSameAs.put(host, hostLinks);
//			 				}
//			 				hostLinks.put(uri, uriInSet);
//			 				wikidataSameAs=true;
//			 			}
//		    		}
//	    		}
	    		if(wikidataSameAs) {
	    			statsCountByHost1stLevelProviderWikidataSameAsWith.incrementTo(host);
	    			Integer chosCount = urisChosCounts.get(uri);
					statsCountByHost1stLevelProviderWikidataSameAsWithChos.addTo(host, chosCount==null ? 1 : chosCount);	    			
	    			cntCoref++;
	    		} else {
	    			statsCountByHost1stLevelProviderWikidataSameAsWithout.incrementTo(host);
	    		}
	    		for(String hostInSet: hostsInSet)
	    			statsCountOneByHost.incrementTo(hostInSet);

    			 cntProcessed++;
    			 if(cntProcessed % 1000 == 0) { 
    				 System.out.println(cntProcessed +" checked; - "+cntCoref+" corefs");
    			 }
	    	 }
	    	 
	    	 CSVPrinter csvOut=new CSVPrinter(wCsv, CSVFormat.DEFAULT);
//	    	 
//	 		
//	    	 wHtml.write("<table>\n");
//	    	 wHtml.write("<tr><td><b>Host</b></td><td><b>URIs linked</b></td></tr>\n");
//	    	 for(String host: statsCountOneByHost.getSortedKeysByInts()) {
//	    		 Integer count = statsCountOneByHost.get(host);
//	    		 wHtml.write("<tr><td>"+host+"</td><td>"+count+"</td></tr>\n");
//	    	 }
//	    	 wHtml.write("</table></ br>");
//	    	 
	    	 csvOut.printRecord("Host", "URIs linked");
	    	 for(String host: statsCountOneByHost.getSortedKeysByInts()) {
	    		 Integer count = statsCountOneByHost.get(host);
		    	 csvOut.printRecord(host, count);
	    	 }
	    	 csvOut.println();

	    	 HashSet<String> allKeys = new HashSet<String>(statsCountByHost1stLevelProviderWikidataSameAsWith.keySet());
	    	 allKeys.addAll(statsCountByHost1stLevelProviderWikidataSameAsWithout.keySet());

	    	 //	    	 wHtml.write("<table>");
//	    	 wHtml.write("<tr><td><b>Host</b></td><td><b>URIs linked to Wikidata</b></td></tr>");
////	    	 wHtml.write("<tr><td><b>Host</b></td><td><b>URIs linked to Wikidata</b></td><td><b>URIs without link to Wikidata</b></td><td><b>Total</b></td></tr>");
//			for(String host: allKeys) {
//	    		 Integer count = statsCountByHost1stLevelProviderWikidataSameAsWith.get(host);
//	    		 if(count==null) continue;
////	    		 if(count==null) count=0;
//	    		 Integer countWithout = statsCountByHost1stLevelProviderWikidataSameAsWithout.get(host);
//	    		 if(countWithout==null) countWithout=0;
//	    		 wHtml.write("<tr><td>"+host+"</td><td>"+count+"</td></tr>");
////	    		 wHtml.write("<tr><td>"+host+"</td><td>"+count+"</td><td>"+countWithout+"</td><td>"+(countWithout+count)+"</td></tr>");
//	    	 }
//	    	 wHtml.write("</table></ br>");
	    	 

	    	 csvOut.printRecord("Host", "URIs linked to Wikidata", "URIs not linked to Wikidata", "Total",  "CHOs linked");
	    	 int othersCount=0;
	    	 for(String host: allKeys) {
	    		 Integer count = statsCountByHost1stLevelProviderWikidataSameAsWith.get(host);
	    		 Integer countWithout = statsCountByHost1stLevelProviderWikidataSameAsWithout.get(host);
//	    		 if(count==null) continue;
	    		 if(countWithout==null) countWithout=0;
	    		 if(count==null || count==0) { count=0; othersCount+=countWithout; continue;}
//		    	 csvOut.printRecord(host, count);
	    		 Integer chosCount = statsCountByHost1stLevelProviderWikidataSameAsWithChos.get(host);
		    	 csvOut.printRecord(host, count, countWithout, count+countWithout, chosCount);
	    	 }
	    	 csvOut.printRecord("other hosts", 0, othersCount, othersCount);
	    	 csvOut.println();
	    	 
	    	 for(String host: linksByHost1stLevelProviderWikidataSameAs.keySet()) {
	    		 csvOut.printRecord("Links to Wikidata for host "+host);
	    		 csvOut.printRecord("URI used by provider", "Wikidata URI(s)");
	    		 MapOfLists<String, String> linksOfHost = linksByHost1stLevelProviderWikidataSameAs.get(host);
	    		 for(String uriAtEuropeana: linksOfHost.keySet()) {
	    			 Collection<String> wdUris = linksOfHost.get(uriAtEuropeana);
	    			 csvOut.print(uriAtEuropeana);
	    			 if(wdUris.size()>1)
	    				 wdUris=deduplicate(wdUris);
	    			 for(String uriAtWd: wdUris) 
	    				 csvOut.print(uriAtWd);
	    			 csvOut.println();
	    		 }
	    		 csvOut.println();
	    	 }
	    	 
	    	 
	    	 
	    	 for(String uri: sameAsSetsEuropeana.keySetOfVocabUris()) {
	 			Matcher matcher = Consts.HOST_PATTERN.matcher(uri);
	 			if(!matcher.find() || uri.contains("#")) continue;
	 			Set<String> ySet = sameAsSetsEuropeana.getUriSet(uri);
	    		if(ySet==null || ySet.size()==1) continue;
	    		 
	    		csvOut.print(uri);
	    		for(String uriInSet: ySet) 
	    			csvOut.print(uriInSet);
	    		csvOut.println();
	    	 }
	    	 csvOut.println();
	    	 
	    	 csvOut.close();
	    	 wCsv.close();
//	    	 wHtml.close();
	    	 System.out.println("FINAL: "+cntProcessed +" checked; - "+cntCoref+" corefs");
	     
	}

	private Set<String> deduplicate(Collection<String> wdUris) {
		HashSet<String> redirects=new HashSet<String>();
		for(String wdUri: wdUris) {
			List<Statement> setSameAs = checkAndGetResourceSameAsStatements(wdUri);
			if(setSameAs.size()==1 && wdUris.contains(setSameAs.get(0).getObject().asResource().getURI())) {
				redirects.add(setSameAs.get(0).getObject().asResource().getURI());
				System.out.println(wdUri+" -> "+setSameAs.get(0).getObject().asResource().getURI());
			}else {
				redirects.add(wdUri);				
			}
		}
		return redirects;
	}
	

	public static List<Statement> checkAndGetResourceSameAsStatements(String uri) {
		Resource ent=null;
		try {
			ent = RdfUtil.readRdfResourceFromUri(uri);
		} catch (AccessException e) {
		} catch (Exception e) {
			System.err.println("Error in uri: "+uri);
			e.printStackTrace();
		}
		if(ent==null) 
			return Collections.EMPTY_LIST;
		return RdfUtil.listProperties(ent, Owl.sameAs);
	}
	
}
