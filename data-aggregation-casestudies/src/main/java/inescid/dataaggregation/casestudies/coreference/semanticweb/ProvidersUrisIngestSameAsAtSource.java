package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.springframework.web.client.ResourceAccessException;

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.dataaggregation.dataset.Global;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;
import inescid.util.ThreadedRunner;
import inescid.util.datastruct.MapOfInts;
import inescid.util.datastruct.MapOfLists;

public class ProvidersUrisIngestSameAsAtSource {
	File repoFolder;
	
	FileOutputStream fos;
	final StreamRDF writer;		
	final ArrayList<Triple> toAddTripleAux;
	
	
	public ProvidersUrisIngestSameAsAtSource(String repoFolder) throws IOException {
		this.repoFolder = new File(repoFolder);
		fos=new FileOutputStream(new File(repoFolder,"providers."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		writer = StreamRDFWriter.getWriterStream(fos, Consts.RDF_SERIALIZATION) ;		
		 toAddTripleAux=new ArrayList<Triple>(1) {{ add(null); }};
	}

	public static void main(String[] args) throws Exception {
    	String repoFolder = "c://users/nfrei/desktop/data/coreference-semanticweb";
    	
		if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
			}
		}
		Global.init_componentDataRepository(repoFolder+"/http-cache");
		Global.init_componentHttpRequestService();
		Global.init_enableComponentHttpRequestCache();

		ProvidersUrisIngestSameAsAtSource corefFinder=new ProvidersUrisIngestSameAsAtSource(repoFolder);
		corefFinder.runIngest();
				
		System.out.println("FININSHED TEST OF URIS");
	}


	private void runIngest() throws Exception {
		MapOfInts<String> errorsCntByHost=new MapOfInts<String>();
		List<String> resolvableUris = FileUtils.readLines(new File(repoFolder, "providers-resolvable-uris.csv"), "UTF-8");
		
		ThreadedRunner runner=new ThreadedRunner(10);
		final int[] cnt=new int[] {0};
		for(String uri: resolvableUris) {
			runner.run(new Runnable() {
				public void run() {
					cnt[0]++;
					if(cnt[0] % 100 == 0)
						System.out.println(cnt[0]);
					try {
						checkUri(uri);
					} catch (Exception e) {
						errorsCntByHost.incrementTo(Util.getHost(uri));
						System.err.println("Failed for "+uri);
						e.printStackTrace();
					}
				}
			});
		}
		System.out.println("waiting termination");
		runner.awaitTermination(0);
		System.out.println("closing");
		writer.finish();
		fos.close();
		
		for(String host: errorsCntByHost.keySet()) {
			System.out.println(host+" - "+errorsCntByHost.get(host));
		}
	}
	
	protected void checkUri(String uri) {
		boolean isWikidata = Util.getHost(uri).equals("www.wikidata.org");
		List<Statement> sameAsStms=checkAndGetResourceSameAs(uri);
		if(!sameAsStms.isEmpty()) {
			for(Statement s: sameAsStms) {
				if(s.getObject().isURIResource()) {
					if(isWikidata) {
						if(!(s.getPredicate().equals(Skos.exactMatch) || s.getPredicate().equals(Skos.exactMatch) || s.getPredicate().equals(Owl.sameAs) || s.getPredicate().equals(Schemaorg.sameAs))) {
							String prop = s.getPredicate().getURI();
							prop=prop.substring(prop.lastIndexOf('/')+1);
							String pattern=WikidataIngest.idsToUris.get(prop);
							String objUri=pattern.replaceAll("\\$1", s.getObject().asLiteral().getString());
							Triple t=new Triple(NodeFactory.createURI(s.getSubject().getURI()), 
									NodeFactory.createURI(s.getPredicate().getURI()), NodeFactory.createURI(objUri));
							synchronized (writer) {
								toAddTripleAux.set(0, t);
								StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);						
							}
System.out.println(t);
							continue;
						}
					}
					Triple t=s.asTriple();
					synchronized (writer) {
						toAddTripleAux.set(0, t);
						StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);						
					}
				} else
					System.out.println(s);
			}
		}
	}

	
	public static List<Statement> checkAndGetResourceSameAs(String uri) {
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
 
		boolean isWikidata = Util.getHost(uri).equals("www.wikidata.org");
		
		Set<String> uriSet=new HashSet<String>();
		List<Statement> sameAsStms;
		if(isWikidata) 
			sameAsStms = RdfUtil.listProperties(ent, WikidataIngest.idsAndAlignsProperties);
		else
			sameAsStms = RdfUtil.listProperties(ent, Owl.sameAs, Skos.exactMatch, Skos.closeMatch, Schemaorg.sameAs);
		return sameAsStms;
	}

}
