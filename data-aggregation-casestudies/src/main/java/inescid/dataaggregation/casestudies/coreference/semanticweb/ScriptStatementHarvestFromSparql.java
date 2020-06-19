package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.h2.mvstore.MVMap;

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.RdfUtil.Jena;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;
import inescid.util.ThreadedRunner;

public class ScriptStatementHarvestFromSparql {
	File repoFolder;
	
	public ScriptStatementHarvestFromSparql(String repoFolder) {
		this.repoFolder=new File(repoFolder);
	}

	public static void main(String[] args) throws Exception {
    	String repoFolder = "c://users/nfrei/desktop/data/coreference-semanticweb";
    	String datasetId;
    	datasetId =Consts.dbPedia_datasetId;
    	datasetId =Consts.dataBnfFr_datasetId;
    	datasetId =Consts.gnd_datasetId;
    	datasetId =Consts.getty_datasetId;
    	datasetId =Consts.wikidata_datasetId;
		if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
				if(args.length>=2) 
					datasetId = args[1];
			}
		}
//		Global.init_componentDataRepository(repoFolder);

		ScriptStatementHarvestFromSparql corefFinder=new ScriptStatementHarvestFromSparql(repoFolder);
		if (datasetId.equals(Consts.wikidata_datasetId)) {
			corefFinder.runIngestWikidata(datasetId);
		} else if (datasetId.equals(Consts.dbPedia_datasetId)) {
			corefFinder.runIngest(datasetId, Consts.dbPedia_sparql);
		} else if (datasetId.equals(Consts.gnd_datasetId)) {
			corefFinder.runIngest(datasetId, Consts.gnd_sparql);
		}else if (datasetId.equals(Consts.dataBnfFr_datasetId)) {
			corefFinder.runIngest(datasetId, Consts.dataBnfFr_sparql);
		}else if (datasetId.equals(Consts.getty_datasetId)) {
			corefFinder.runIngest(datasetId, Consts.getty_sparql);
		}else if (datasetId.equalsIgnoreCase("all")) {
			inescid.util.ThreadedRunner runner=new ThreadedRunner(4);
			runner.run(new Runnable() {
				public void run() {
					try {
						corefFinder.runIngestWikidata(Consts.wikidata_datasetId);
					} catch (Exception e) {
						System.err.println("Error in wikidata ingest:");
						e.printStackTrace();
					}
				}
			});
			runner.run(new Runnable() {
				public void run() {
					try {
						corefFinder.runIngest(Consts.dbPedia_datasetId, Consts.dbPedia_sparql);
					} catch (Exception e) {
						System.err.println("Error in dbpedia ingest:");
						e.printStackTrace();
					}
				}
			});
			runner.run(new Runnable() {
				public void run() {
					try {
						corefFinder.runIngest(Consts.dataBnfFr_datasetId, Consts.dataBnfFr_sparql);
					} catch (Exception e) {
						System.err.println("Error in bnf ingest:");
						e.printStackTrace();
					}
				}
			});
			runner.run(new Runnable() {
				public void run() {
					try {
						corefFinder.runIngest(Consts.gnd_datasetId, Consts.gnd_sparql);
					} catch (Exception e) {
						System.err.println("Error in gnd ingest:");
						e.printStackTrace();
					}
				}
			});
			runner.run(new Runnable() {
				public void run() {
					try {
						corefFinder.runIngest(Consts.getty_datasetId, Consts.getty_sparql);
					} catch (Exception e) {
						System.err.println("Error in getty ingest:");
						e.printStackTrace();
					}
				}
			});
			runner.awaitTermination(0);
		}
		System.out.println("FININSHED: "+datasetId);
	}

	private void runIngestWikidata(String datasetId) throws Exception {
		new WikidataIngest().runIngestWikidata(datasetId, repoFolder);
	}

	private void runIngest(String datasetId, String sparqlEndpoint) throws Exception {
		FileOutputStream fos=new FileOutputStream(new File(repoFolder, datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		StreamRDF writer = StreamRDFWriter.getWriterStream(fos, Consts.RDF_SERIALIZATION) ;
		ArrayList<Triple> toAddTripleAux=new ArrayList<Triple>(1) {{ add(null); }};

		try {
			SparqlClient endpoint=new SparqlClient(sparqlEndpoint, "");
			for(Property p: new Property[] { Owl.sameAs, Skos.exactMatch, Skos.closeMatch,
					Schemaorg.sameAs}) {
				endpoint.queryWithPaging("SELECT ?s ?o WHERE { {?s <" + p + "> ?o} .}",
						50000, null, new Handler() {
					int cnt=0;
					@Override
					public boolean handleSolution(QuerySolution solution) throws Exception {
						try {
							if(!solution.get("s").isURIResource() || !solution.get("o").isURIResource()) 
								return true;
							cnt++;
							Triple t=new Triple(NodeFactory.createURI(solution.get("s").asResource().getURI()), 
									NodeFactory.createURI(p.getURI()),
									NodeFactory.createURI(solution.get("o").asResource().getURI()));
							toAddTripleAux.set(0, t);
							StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);
							if(cnt % 10000 == 0) {
								System.out.println(cnt);
								fos.flush();
							}
							return true;
						} catch (Exception e) {
							e.printStackTrace();
							return false;
						}
					}
					
				});
			}
		}finally {
			fos.close();
		}
		System.out.println("Finished "+sparqlEndpoint);
	}
	
}
