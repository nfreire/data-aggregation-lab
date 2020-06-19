package inescid.dataaggregation.casestudies.wikidata.edm;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.data.model.Dcat;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;

class ScriptGetWikidataSubsetViaSparql {
	

	public interface Filter{
		public boolean accept(Triple t);
	}
	public static class EdmNamespacesFilter implements Filter {
		@Override
		public boolean accept(Triple t) {
			if(!t.getObject().isURI())
				return false;
			String uri=t.getObject().getURI();
//			System.out.println(uri);
			for(String ns: Edm.NS_EXTERNAL_PREFERRED_BY_PREFIXES.values()) {
				if(uri.startsWith(ns)) {
					System.out.println("accepting: "+uri);
					return true;
				}
			}
			if(uri.startsWith(Schemaorg.NS)) {
				System.out.println("accepting: "+uri);
				return true;			
			}
			if(uri.startsWith(Dcat.NS))
				return true;			
			if(uri.startsWith(inescid.dataaggregation.data.model.Void.NS))
				return true;			
			return false;			
		}
	}
	
	public static class Query {
		String query;
		Filter filter;
		public Query(String query, Filter filter) {
			super();
			this.query = query;
			this.filter = filter;
		}
		public Query(String query) {
			this(query, null);
		}
		public boolean accept(Triple t) {
			return filter==null || filter.accept(t);
		}
	}
	
	ArrayList<Query> sparqlQueries=new ArrayList<>();
	
	
	


	public static void main(String[] args) throws Exception {
		ScriptGetWikidataSubsetViaSparql extractor=new ScriptGetWikidataSubsetViaSparql();
		
//		extractor.addProperties(RdfRegWikidata.NsWdt+"P1921", RdfRegWikidata.NsWdt+"P279", RdfRegWikidata.NsWdt+"P1647");
//		extractor.addQueries("select ?s (<http://www.wikidata.org/prop/direct/P1709> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P1709> ?o. FILTER regex(str(?o), \"http://schema.org\")}",
//				"select ?s (<http://www.wikidata.org/prop/direct/P1268> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P1268> ?o. FILTER regex(str(?o), \"http://schema.org\")}",			
//				"select ?s (<http://www.wikidata.org/prop/direct/P2888> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P2888> ?o. FILTER regex(str(?o), \"http://schema.org\")}"			
//				);
		
//		for(String ns: RegEdm.NS_EXTERNAL_PREFERRED_BY_PREFIXES.values()) {
//			extractor.addQueries("select ?s (<http://www.wikidata.org/prop/direct/P1709> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P1709> ?o. FILTER regex(str(?o), \""+ns+"\")}",
//					"select ?s (<http://www.wikidata.org/prop/direct/P1268> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P1268> ?o. FILTER regex(str(?o), \""+ns+"\")}",			
//					"select ?s (<http://www.wikidata.org/prop/direct/P2888> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P2888> ?o. FILTER regex(str(?o), \""+ns+"\")}"			
//					);			
//		}
		
		extractor.addQueries(new EdmNamespacesFilter(), "select ?s (<http://www.wikidata.org/prop/direct/P1709> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P1709> ?o. }",
					"select ?s (<http://www.wikidata.org/prop/direct/P1628> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P1628> ?o. }"			
//					"select ?s (<http://www.wikidata.org/prop/direct/P2888> AS ?p) ?o WHERE { ?s <http://www.wikidata.org/prop/direct/P2888> ?o. }"			
					);			
				
		extractor.run(new File("C:\\Users\\nfrei\\Desktop\\data\\wikidata-to-edm\\wikidata2.rt"), Lang.RDFTHRIFT);
//		extractor.run(new File("C:\\Users\\nfrei\\Desktop\\data\\wikidata-to-edm\\wikidata.nt"), Lang.NTRIPLES);
	}
	
//	public void addProperties(String... propUris) {
//		for(String p: propUris) 
//			properties.add(Jena.createProperty(p));
//	}	
//	public void addQueries(String... queries) {
//		for(String q: queries) 
//			this.sparqlQueries.add(q);
//	}	
	public void addQueries(Filter filter,  String... queries) {
		for(String q: queries) 
			this.sparqlQueries.add(new Query(q, filter));
	}	


//	for(Property p: new Property[] { RegOwl.sameAs, RdfRegWikidata.EXACT_MATCH, RdfRegWikidata.EQUIVALENT_CLASS,
//			RegSkos.exactMatch, RegSkos.closeMatch, RegSchemaorg.sameAs}) {
	public ScriptGetWikidataSubsetViaSparql() {
	}
	
	void run(File outputFile, Lang rdfLang) throws Exception {
		FileOutputStream fos=new FileOutputStream(outputFile);
		StreamRDF writer = StreamRDFWriter.getWriterStream(fos, rdfLang) ;
		ArrayList<Triple> toAddTripleAux=new ArrayList<Triple>(1) {{ add(null); }};
		try {
			SparqlClient endpoint=new SparqlClient(SparqlClientWikidata.ENDPOINT_URL, "");
			
			if(sparqlQueries!=null) {
				for(Query query: sparqlQueries) {
					String q=query.query;
					endpoint.queryWithPaging(q,
							10000, null, new Handler() {
						int cnt=0;
						@Override
						public boolean handleSolution(QuerySolution solution) throws Exception {
							try {
								cnt++;
								Triple t=new Triple(solution.get("s").asNode(), 
										solution.get("p").asNode(), 
										solution.get("o").asNode());
								if(query.accept(t)) {
									toAddTripleAux.set(0, t);
									StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);
								}
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
			}
			
//			if(properties!=null) {
//				for(Property p: properties) {
//					endpoint.queryWithPaging("SELECT ?s ?o WHERE { {?s <" + p + "> ?o} .}",
//							50000, null, new Handler() {
//						int cnt=0;
//						@Override
//						public boolean handleSolution(QuerySolution solution) throws Exception {
//							try {
//								cnt++;
//								Triple t=new Triple(solution.get("s").asNode(), 
//										NodeFactory.createURI(p.getURI()), 
//										solution.get("o").asNode());
//								toAddTripleAux.set(0, t);
//								StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);
//								if(cnt % 10000 == 0) {
//									System.out.println(cnt);
//									fos.flush();
//								}
//								return true;
//							} catch (Exception e) {
//								e.printStackTrace();
//								return false;
//							}
//						}
//					});
//				}
//			}
			
			fos.flush();
			
			
//			for(String wdProp: idsToUris.keySet()) {
//				Property p=ResourceFactory.createProperty(RdfRegWikidata.NsWdt+wdProp);
//				String pattern=idsToUris.get(wdProp);
//				endpoint.queryWithPaging("SELECT ?s ?o WHERE { {?s <" + p + "> ?o} .}",
//						50000, null, new Handler() {
//					int cnt=0;
//					@Override
//					public boolean handleSolution(QuerySolution solution) throws Exception {
//						try {
//							if(!solution.get("s").isURIResource() || !solution.get("o").isLiteral()) 
//								return true;
//							cnt++;
//							String objUri=pattern.replaceAll("\\$1", solution.get("o").asLiteral().getString());
//							Triple t=new Triple(NodeFactory.createURI(solution.get("s").asResource().getURI()), 
//									NodeFactory.createURI(p.getURI()), NodeFactory.createURI(objUri));
//							toAddTripleAux.set(0, t);
//							StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);
//							if(cnt % 10000 == 0) {
//								System.out.println(cnt);
//								fos.flush();
//							}
//							return true;
//						} catch (Exception e) {
//							e.printStackTrace();
//							return false;
//						}
//					}
//				});
//			}
		}finally {
			writer.finish();
			fos.close();
		}
		System.out.println("Finished Wikidata.");
	}

}