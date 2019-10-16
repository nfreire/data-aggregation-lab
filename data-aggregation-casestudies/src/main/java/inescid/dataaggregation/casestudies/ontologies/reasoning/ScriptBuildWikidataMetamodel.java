package inescid.dataaggregation.casestudies.ontologies.reasoning;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.tdb2.TDB2Factory;

import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.casestudies.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.data.RegOwl;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.data.RegSkos;
import inescid.dataaggregation.data.reasoning.ReasonerUtil;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;
import inescid.util.SparqlClient.Handler;

public class ScriptBuildWikidataMetamodel {

	public static void main(String[] args) throws Exception {
		if(args==null || args.length<1) {
			System.out.println("Missing parameter: folder for storing data");
			System.exit(0);
		}
		File dataFolder=new File(args[0]);
		String httpCacheFolder = new File(dataFolder, Settings.HTTP_CHACHE_FOLDER).getAbsolutePath();
		String tripleStoreFolder = new File(dataFolder, Settings.TRIPLE_STORE_FOLDER).getAbsolutePath();

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);

		// Make a TDB-backed dataset
		final Dataset dataset = TDB2Factory.connectDataset(tripleStoreFolder);
		dataset.begin(ReadWrite.WRITE);
		final Model modelMdl = dataset.getNamedModel(Settings.WD_REASONING_MODEL_DS);
		final Model modelPropertiesMdl = dataset.getNamedModel(Settings.WD_REASONING_MODEL_PROPERTIES_DS);
		final Model modelClassesMdl = dataset.getNamedModel(Settings.WD_REASONING_MODEL_CLASSES_DS);
		final Model metaAlignMdl = dataset.getNamedModel(Settings.WD_REASONING_ALIGN_META_DS);
		final Model schemaorgMdl = dataset.getNamedModel(Settings.WD_REASONING_SCHEMAORG_DS);
		final Model owlMdl = dataset.getNamedModel(Settings.WD_REASONING_OWL_DS);
		
		if (Settings.RESET_MODELS) {
			modelMdl.removeAll();
			modelPropertiesMdl.removeAll();
			modelClassesMdl.removeAll();
			metaAlignMdl.removeAll();
			schemaorgMdl.removeAll();
			owlMdl.removeAll();
		}
		System.out.println("Cleaning: done");
		
		InputStream systemResourceAsStream = ClassLoader
				.getSystemResourceAsStream("inescid/dataaggregation/data/reasoning/schemaorg.owl");
		Model schemaorgDefMdl = RdfUtil.readRdf(systemResourceAsStream);
		systemResourceAsStream.close();
		schemaorgMdl.add(schemaorgDefMdl.listStatements(null, RegRdfs.subClassOf, (RDFNode) null));
		schemaorgMdl.add(schemaorgDefMdl.listStatements(null, RegRdfs.subPropertyOf, (RDFNode) null));
		schemaorgMdl.add(schemaorgDefMdl.listStatements(null, RegRdf.type, RegRdfs.Class));
		schemaorgMdl.add(schemaorgDefMdl.listStatements(null, RegRdf.type, RegRdf.Property));
		
//		 String sparql = "SELECT ?s ?p ?o WHERE { " +
//                 "?p rdfs:subClassOff <rd" + THING + "> . " +
//                 "?thing <" + HAS_STRING + "> ?str . " +
//                 "FILTER (?str = \"" + s + "\") . }";
//
//Query qry = QueryFactory.create(sparql);
//QueryExecution qe = QueryExecutionFactory.create(qry, getModel());
//ResultSet rs = qe.execSelect();
//
//while(rs.hasNext())
//{
// QuerySolution sol = rs.nextSolution();
// RDFNode str = sol.get("str"); 
// RDFNode thing = sol.get("thing"); 
//
// ...
//}
//
//qe.close(); 
		
		
		

		systemResourceAsStream = ClassLoader
				.getSystemResourceAsStream("inescid/dataaggregation/data/reasoning/owl_def.owl");
		Model owlDefMdl = RdfUtil.readRdf(systemResourceAsStream);
		
		System.out.println("OWL size: "+owlDefMdl.size());
		systemResourceAsStream.close();
		owlMdl.add(owlDefMdl.listStatements(null, RegRdfs.subClassOf, (RDFNode) null));
		owlMdl.add(owlDefMdl.listStatements(null, RegRdfs.subPropertyOf, (RDFNode) null));
		owlMdl.add(owlDefMdl.listStatements(null, RegRdf.type, RegRdfs.Class));
		owlMdl.add(owlDefMdl.listStatements(null, RegRdf.type, RegRdf.Property));
		System.out.println("Schema.org and OWL: done");

		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EQUIVALENT_CLASS, RegOwl.equivalentProperty,
				RegOwl.equivalentClass);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EQUIVALENT_PROPERTY, RegOwl.equivalentProperty,
				RegOwl.equivalentProperty);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.SUBCLASS_OF, RegOwl.equivalentProperty,
				RegRdfs.subClassOf);
		
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.SUBPROPERTY_OF, RegOwl.equivalentProperty,
				RegRdfs.subPropertyOf);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.INSTANCE_OF, RegOwl.equivalentProperty,
				RegRdf.type);
//		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RegOwl.equivalentProperty,
//				RegSkos.narrowMatch);
//		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.BROADER_CONCEPT, RegOwl.equivalentProperty,
//				RegSkos.broadMatch);
//		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.MAPPING_RELATION_TYPE, RegOwl.equivalentProperty,
//				RegSkos.mappingRelation);
//		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EXTERNAL_SUBPROPERTY, RegOwl.equivalentProperty,
//				RegSkos.broadMatch);// there is no superProperty in rdfs
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EXTERNAL_SUPERPROPERTY, RegOwl.equivalentProperty,
				RegRdfs.subPropertyOf);
		
		dataset.commit();
		System.out.println("Equivalences: done");
		dataset.begin(ReadWrite.WRITE);
		
		for (Property p : new Property[] { RdfRegWikidata.SUBCLASS_OF, RdfRegWikidata.EQUIVALENT_CLASS, 
				RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RdfRegWikidata.BROADER_CONCEPT }) {
			System.out.println("Harvesting: "+p.getURI());
				SparqlClientWikidata.queryWithPaging("SELECT ?s ?o WHERE { ?s <" + p + "> ?o .}", 5000, null,
					new Handler() {
						int cnt = 0;
						@Override
						public boolean handleSolution(QuerySolution solution) throws Exception {
							String subjUri=solution.getResource("?s").getURI(); 
							Resource subj = modelClassesMdl.createResource(subjUri);
							modelClassesMdl.add(
									Jena.createStatement(subj, p, solution.getResource("?o")));
							
							//					takes too long to execute, and we always get 0 results 
//							SparqlClientWikidata.query("SELECT ?o WHERE {  VALUES ?o { rdf:Class rdf:Property }\n <" + subjUri + "> rdf:type ?o .}", new Handler() {
//								int cnt = 0;
//								@Override
//								public boolean handleSolution(QuerySolution solution) throws Exception {
//									String objUri=solution.getResource("?o").getURI(); 
//									modelClassesMdl.add(Jena.createStatement(subj, RegRdf.type, modelClassesMdl.createResource(objUri)));
//									return true;
//								}
//							});
							
							cnt++;
							if (cnt % 10000 == 0) {
								dataset.commit();
								dataset.begin(ReadWrite.WRITE);
								System.out.println("Commited " + cnt);
							}
							return true;
						}
					});
				System.out.println(p+": done");
			System.out.println("align model sizes: " + modelClassesMdl.size() +" - "+p.getURI());
		}
		System.out.println("classes: done");

		dataset.commit();
		dataset.begin(ReadWrite.WRITE);
		
		for (Property p : new Property[] { RdfRegWikidata.SUBPROPERTY_OF, RdfRegWikidata.EQUIVALENT_PROPERTY,
				RdfRegWikidata.MAPPING_RELATION_TYPE, RdfRegWikidata.EXTERNAL_SUBPROPERTY,
				RdfRegWikidata.EXTERNAL_SUPERPROPERTY }) {
			System.out.println("Harvesting: "+p.getURI());
			SparqlClientWikidata.queryWithPaging("SELECT ?s ?o WHERE { ?s <" + p + "> ?o .}", 5000, null,
					new Handler() {
				int cnt = 0;
				@Override
				public boolean handleSolution(QuerySolution solution) throws Exception {
					String subjUri=solution.getResource("?s").getNameSpace().equals(RdfRegWikidata.NsWd) ? RdfRegWikidata.NsWdt + solution.getResource("?s").getLocalName() : solution.getResource("?s").getURI(); 
					Resource subj = modelPropertiesMdl.createResource(subjUri);
					modelPropertiesMdl.add(
							Jena.createStatement(subj, p, solution.getResource("?o")));

					//					takes too long to execute, and we always get 0 results 
//					SparqlClientWikidata.query("SELECT ?o WHERE {  VALUES ?o { rdf:Class rdf:Property }\n <" + subjUri + "> rdf:type ?o .}", new Handler() {
//						@Override
//						public boolean handleSolution(QuerySolution solution) throws Exception {
//							String objUri=solution.getResource("?o").getURI(); 
//							modelPropertiesMdl.add(Jena.createStatement(subj, RegRdf.type, modelPropertiesMdl.createResource(objUri)));
//							return true;
//						}
//					});
					
					cnt++;
					if (cnt % 10000 == 0) {
						dataset.commit();
						dataset.begin(ReadWrite.WRITE);
						System.out.println("Commited " + cnt);
					}
					return true;
				}
			});
			System.out.println("align model sizes: " + modelPropertiesMdl.size() +" - "+p.getURI());
		}

		dataset.commit();
		dataset.begin(ReadWrite.READ);

		String tripleStoreJoinedFolder = new File(dataFolder, Settings.TRIPLE_STORE_JOINED_FOLDER).getAbsolutePath();
		final Dataset datasetJoined = TDB2Factory.connectDataset(tripleStoreJoinedFolder);
		datasetJoined.begin(ReadWrite.WRITE);
		final Model joinedMdl = datasetJoined.getNamedModel(Settings.WD_REASONING_MODEL_ALIGN_META_DS);
		
		if (Settings.RESET_MODELS) {
			joinedMdl.removeAll();
		}
		System.out.println("Cleaning: done");
		
		joinedMdl.add(modelPropertiesMdl);
		System.out.println("Added properties: "+modelPropertiesMdl.size());
		joinedMdl.add(modelClassesMdl);
		System.out.println("Added classes: "+modelClassesMdl.size());
		joinedMdl.add(schemaorgMdl);
		System.out.println("Added schema.org: "+schemaorgMdl.size());
		joinedMdl.add(metaAlignMdl);
		System.out.println("Added align: "+metaAlignMdl.size());
		joinedMdl.add(owlMdl);
		System.out.println("Added owl: "+metaAlignMdl.size());
		System.out.println("Final Joined: "+joinedMdl.size());
		
		datasetJoined.commit();
		System.out.println("Committed");
		
		dataset.end();
		datasetJoined.end();

	}

}
