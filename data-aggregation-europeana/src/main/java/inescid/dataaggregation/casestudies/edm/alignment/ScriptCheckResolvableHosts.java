package inescid.dataaggregation.casestudies.edm.alignment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.dataset.Global;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;
import inescid.util.ThreadedRunner;
import inescid.util.datastruct.MapOfInts;

public class ScriptCheckResolvableHosts {
	private static final int MAX_FAILS=100;

	static Set<String> withData=new HashSet<String>();
	static Set<String> withoutData=new HashSet<String>();
	static MapOfInts<String> checks=new MapOfInts<String>();
	static MapOfInts<String> errors=new MapOfInts<String>();

	static BufferedWriter resolvableHostsOutput;
	static BufferedWriter nonResolvableHostsOutput;
	
	public static void main(String[] args) throws Exception {
		String outputFolder = "c://users/nfrei/desktop/data/";
		if (args != null) {
			if (args.length >= 1) {
				outputFolder = args[0];
			}
		}
		
		File resolvableHostsFile=new File(outputFolder, "resolvable-context-hosts.txt");
		if(resolvableHostsFile.exists()) {
			withData.addAll(FileUtils.readLines(resolvableHostsFile, StandardCharsets.UTF_8));
			resolvableHostsOutput = Files.newBufferedWriter(resolvableHostsFile.toPath(), StandardOpenOption.APPEND);
		} else
			resolvableHostsOutput = Files.newBufferedWriter(resolvableHostsFile.toPath());
		File nonResolvableHostsFile=new File(outputFolder, "non-resolvable-context-hosts.txt");
		if(nonResolvableHostsFile.exists()) {
			withoutData.addAll(FileUtils.readLines(nonResolvableHostsFile, StandardCharsets.UTF_8));
			nonResolvableHostsOutput = Files.newBufferedWriter(nonResolvableHostsFile.toPath(), StandardOpenOption.APPEND);
		}else
			nonResolvableHostsOutput = Files.newBufferedWriter(nonResolvableHostsFile.toPath());
		
		Global.init_componentHttpRequestService();
		
	//	Global.init_componentDataRepository(repoFolder);
	//	Global.init_enableComponentHttpRequestCache();
	//	Repository repository = Global.getDataRepository();
	
		File mapsFile = new File(outputFolder, "context_uris.mvstore.bin");
		if (!mapsFile.getParentFile().exists())
			mapsFile.getParentFile().mkdirs();
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();

		MVMap<String, String> urisConcept = mvStore.openMap("Concept");
		MVMap<String, String> urisPlace = mvStore.openMap("Place");
		MVMap<String, String> urisAgent = mvStore.openMap("Agent");
		MVMap<String, String> urisTimespan = mvStore.openMap("Timespan");

		runTest(urisConcept);
		runTest(urisPlace);
		runTest(urisTimespan);
		runTest(urisAgent);
		
		resolvableHostsOutput.close();
		nonResolvableHostsOutput.close();
		mvStore.close();
				
		System.out.println("FININSHED TEST OF URIS");
	}


	private static void runTest(MVMap<String, String> uris) throws Exception {
		final Pattern hostPattern=Pattern.compile("^https?://([^/]+)/");
		
		ThreadedRunner runner=new ThreadedRunner(10, 15);  
		
		int cnt=0;
		for(Iterator<String> it = uris.keyIterator(null) ; it.hasNext() ; ) {
			String uri=it.next();

			try {
				new URI(uri);
			} catch (Exception e) {
				continue;
			}
			if(cnt % 100 == 0) {
				System.out.println("Processed: "+cnt);
				System.out.println("\nWITH DATA:"+ withData.size());
				System.out.println("\nWITHOUT DATA:"+withoutData.size());
			}
				
			Matcher matcher = hostPattern.matcher(uri);
			if(matcher.find()) {
				String host=matcher.group(1);
				if(withData.contains(host) || withoutData.contains(host) || host.matches("^\\d+.*"))
					continue;
				cnt++;
				
				runner.run(new Runnable() {
					@Override
					public void run() {
						System.out.println(uri);
						try {
							if(RdfUtil.isUriResolvable(uri)) {
								checks.remove(host);
								if(! withData.contains(host)) {
									withData.add(host);
									resolvableHostsOutput.write(host+"\n");
								}
							} else {
								checks.incrementTo(host);
								if(checks.get(host)>=MAX_FAILS) {
									checks.remove(host);
									if(! withoutData.contains(host)) {
										withoutData.add(host);
										nonResolvableHostsOutput.write(host+"\n");
									}
								}
							}
						} catch (AccessException | InterruptedException | IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
		runner.awaitTermination(10);	
		resolvableHostsOutput.flush();
		nonResolvableHostsOutput.flush();
		
//		System.out.println("\nFINAL RESULTS:");
//
//		for(String host: new ArrayList<String>(withData)) {
//			System.out.println(host);
//		}
//		System.out.println("\nWITHOUT DATA:");
//		for(String host: new ArrayList<String>(withoutData)) {
//			System.out.println(host);
//		}
//		for(String host: checks.keySet()) {
//			System.out.println(host);
//		}
	}
	

	
}
