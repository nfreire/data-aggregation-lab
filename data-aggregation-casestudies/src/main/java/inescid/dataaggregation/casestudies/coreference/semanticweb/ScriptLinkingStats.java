package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.casestudies.coreference.semanticweb.AgentVocabsIntelinkingStudy.AgentVocabulary;
import inescid.util.datastruct.MapOfInts;

public class ScriptLinkingStats {
	
	public static void main(String[] args) throws Exception {
		final int matchingIterations=4;
		String repoFolder = "c://users/nfrei/desktop/data/coreference-semanticweb";
    	
		if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
			}
		}
		
		MapOfInts<String>[] statsByStepNoCloseMatch = new MapOfInts[] { new MapOfInts<String>(), new MapOfInts<String>(), new MapOfInts<String>(), new MapOfInts<String>() , new MapOfInts<String>() }; 
		MapOfInts<String>[] statsByStepAllMatches = new MapOfInts[] { new MapOfInts<String>(), new MapOfInts<String>(), new MapOfInts<String>(), new MapOfInts<String>(), new MapOfInts<String>() }; 
		
		//join all results from batches
		Pattern resultFilenamePattern=Pattern.compile("linking-(no_close_match|all_matches)-batch_\\d+-step_(\\d)\\.csv");
		for(File f: new File(repoFolder).listFiles()) {
			Matcher matcher = resultFilenamePattern.matcher(f.getName());
			if(matcher.matches()) {
				String experiment=matcher.group(1);
				int step=Integer.parseInt(matcher.group(2));
				
				MapOfInts<String>[] xpStats= (experiment.equals("no_close_match")) ? statsByStepNoCloseMatch : statsByStepAllMatches;
				MapOfInts<String> stepStats=xpStats[step];
				
				System.out.println("Loading "+f.getName());
				SameAsSetsByUri urisSets=new SameAsSetsByUri(f);
				MapOfInts<String> targetHostsStats = urisSets.getTargetHostsStats();
				stepStats.addToAll(targetHostsStats);
				
				loadStatsInto(f, targetHostsStats);
			}
		}
		
		for(int i=0; i<=matchingIterations; i++) {
			File saveTo=new File(repoFolder, "linking-no_close_match-joined-step_"+i+".csv");
			MapOfInts.writeCsv(statsByStepNoCloseMatch[i], 
					Files.newBufferedWriter(saveTo.toPath()	, StandardCharsets.UTF_8));
		}
		for(int i=0; i<=matchingIterations; i++) {
			File saveTo=new File(repoFolder, "linking-all_matches-joined-step_"+i+".csv");
			MapOfInts.writeCsv(statsByStepAllMatches[i], 
					Files.newBufferedWriter(saveTo.toPath()	, StandardCharsets.UTF_8));
		}

		Map<String, AgentVocabulary> studiedVocabs = AgentVocabsIntelinkingStudy.getStudiedVocabs(new File(repoFolder));
		ArrayList<String> sortedVocabs=new ArrayList<String>(studiedVocabs.keySet());
		Collections.sort(sortedVocabs);

//		MultiKeyMap<String, Integer> integratedResultsNoCloseMatch=new MultiKeyMap<String, Integer>();
//		for(int i=0; i<=matchingIterations; i++) {
//			for(String vocabHost: studiedVocabs.keySet()) {
//				integratedResultsNoCloseMatch.put(vocabHost, String.valueOf(i), statsByStepNoCloseMatch[i].get(vocabHost));
//			}
//		}		
		List<String> resolvableUris = FileUtils.readLines(new File(repoFolder, "providers-resolvable-uris.txt"), StandardCharsets.UTF_8);
		resolvableUris.addAll(FileUtils.readLines(new File(repoFolder, "providers-indirectly-resolvable-uris.txt"), StandardCharsets.UTF_8));
		
		MapOfInts<String> hostLinksAtEuropeana=new MapOfInts<String>();
		for(String uri: resolvableUris) 
			hostLinksAtEuropeana.incrementTo(Util.getHost(uri));
		
		{
			File saveTo=new File(repoFolder, "linking-no_close_match-vocabs_crawl.csv");
			BufferedWriter csvWriter = Files.newBufferedWriter(saveTo.toPath()	, StandardCharsets.UTF_8);
			CSVPrinter p=new CSVPrinter(csvWriter, CSVFormat.DEFAULT);
			p.printRecord("Vocabulary", "Directly referenced in Europeana","1st Crawl","2nd Crawl","3rd Crawl","4th Crawl","5th Crawl");
			for(String vocabHost: sortedVocabs) {
				p.print(vocabHost);
				p.print(hostLinksAtEuropeana.get(vocabHost)==null ? 0 : hostLinksAtEuropeana.get(vocabHost));
				for(int i=0; i<=matchingIterations; i++) {
					Integer countOfHost = statsByStepNoCloseMatch[i].get(vocabHost);
					p.print(countOfHost == null ? 0 : countOfHost);
				}
				p.println();
			}
			p.close();
			csvWriter.close();
		}
		{
			File saveTo=new File(repoFolder, "linking-all_matches-vocabs_crawl.csv");
			BufferedWriter csvWriter = Files.newBufferedWriter(saveTo.toPath()	, StandardCharsets.UTF_8);
			CSVPrinter p=new CSVPrinter(csvWriter, CSVFormat.DEFAULT);
			p.printRecord("Vocabulary", "Directly referenced in Europeana","1st Crawl","1st Crawl","2nd Crawl","3rd Crawl","4th Crawl","5th Crawl");
			for(String vocabHost: sortedVocabs) {
				p.print(vocabHost);
				p.print(hostLinksAtEuropeana.get(vocabHost)==null ? 0 : hostLinksAtEuropeana.get(vocabHost));
				for(int i=0; i<=matchingIterations; i++) {
					Integer countOfHost = statsByStepAllMatches[i].get(vocabHost);
					p.print(countOfHost == null ? 0 : countOfHost);
				}
				p.println();
			}
			p.close();
			csvWriter.close();
		}
		
	}

	private static void loadStatsInto(File csvFile, MapOfInts<String> linkedToVocabCounts) throws IOException {
		BufferedReader reader = Files.newBufferedReader(csvFile.toPath(), StandardCharsets.UTF_8);		
		CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);
		for(CSVRecord r: parser) {
			String uri=r.get(0);
			String hostOfProviderUri=Util.getHost(uri);
			HashSet<String> hostsSet=new HashSet<String>();
			for(int i=1; i<r.size(); i++) {
				String host = Util.getHost(r.get(i));
				if(host!=null && !host.equals(hostOfProviderUri)) {
					hostsSet.add(host);
				}
			}			
			linkedToVocabCounts.incrementToAll(hostsSet);
		}
		parser.close();
	}
	
	
	
	
}
