package inescid.dataaggregation.casestudies.ontologies.reasoning.old;

import java.io.File;
import java.io.InputStream;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.tdb2.TDB2Factory;

import inescid.dataaggregation.casestudies.ontologies.reasoning.Settings;
import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.casestudies.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.data.RegOwl;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.data.RegSkos;
import inescid.dataaggregation.data.reasoning.ReasonerStabilizer;
import inescid.dataaggregation.data.reasoning.ReasonerUtil;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;
import inescid.util.SparqlClient.Handler;

public class OutdatedScriptBuildWikidataMetamodelReasoner {

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
		final Model metaAlignMdl = dataset.getNamedModel(Settings.WD_REASONING_ALIGN_META_DS);
		
		final Model stableMdl = dataset.getNamedModel(Settings.WD_REASONING_DS);
		final Model stableMdlUnstableStatements = dataset.getNamedModel(Settings.WD_REASONING_STABLE_REMOVABLE_DS);
		if (Settings.RESET_MODELS) {
			modelMdl.removeAll();
			metaAlignMdl.removeAll();
			stableMdl.removeAll();
			stableMdlUnstableStatements.removeAll();
		}
		
		GenericRuleReasoner reasoner = ReasonerUtil
				.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-owl-ontologies-rules.txt");
//		.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-extend.txt");

//		dataset.begin(ReadWrite.WRITE);
//		System.out.println("stabelizing");
//		ReasonerStabilizer reasonerStbl = new ReasonerStabilizer(reasoner, metaModelMdl, stableMdl);
//		stableMdlUnstableStatements.add(reasonerStbl.getUnstableSatements());
//		dataset.begin(ReadWrite.READ);

//		System.out.println("Metamodel stms: "+metaModelMdl.size());
//		System.out.println("Stable Metamodel stms: "+stableMdl.size());
//	System.out.println( RdfUtil.statementsToString(model));
		
		
//		Model modelMdl = Jena.createModel();		
//		Model metaAlignMdl = Jena.createModel();		
////		Model metaAlignMdl = null;		
////		Jena.createStatementAddToModel(model, model.createResource(propUri), RdfRegRdf.type, RdfRegRdf.Property)
////		Jena.createStatementAddToModel(model, model.createResource(propUri), RdfRegRdfs.subClassOf, model.createResource("http://wikiba.se/ontology#Property"))
////		Jena.createStatementAddToModel(model, model.createResource(propUri), RdfRegRdf.type, RdfRegRdfs.Class)
//		+"\n"+Jena.createStatementAddToModel(model, model.createResource("http://wikiba.se/ontology#Property"), RegRdfs.subClassOf, RegRdf.Property)
////		+"\n"+Jena.createStatementAddToModel(model, model.createResource("http://wikiba.se/ontology#Property"), RdfRegRdf.type, RdfRegRdf.Property)
//		+"\n"+Jena.createStatementAddToModel(model, model.createResource("http://wikiba.se/ontology#Entity"), RegRdfs.subClassOf, RegRdfs.Class)
//		+"\n"+Jena.createStatementAddToModel(model, model.createResource("http://wikiba.se/ontology#Entity"), RegRdfs.subClassOf, RegOwl.Thing)
//		+"\n"+Jena.createStatementAddToModel(model, RdfRegWikidata.EQUIVALENT_CLASS, RdfReg.OWL_EQUIVALENT_PROPERTY, RegOwl.equivalentClass)
//		+"\n"+Jena.createStatementAddToModel(model, RdfRegWikidata.EQUIVALENT_PROPERTY, RdfReg.OWL_EQUIVALENT_PROPERTY, RegOwl.equivalentProperty)
////		+"\n"+Jena.createStatementAddToModel(model, model.createResource("http://wikiba.se/ontology#Entity"), RdfRegRdfs.subClassOf, RdfReg.OWL_Thing)
		
		
		InputStream systemResourceAsStream = ClassLoader
				.getSystemResourceAsStream("inescid/dataaggregation/data/reasoning/schemaorg.owl");
		Model schemaorgMdl = RdfUtil.readRdf(systemResourceAsStream);
		systemResourceAsStream.close();
		modelMdl.add(schemaorgMdl.listStatements(null, RegRdfs.subClassOf, (RDFNode) null));
		modelMdl.add(schemaorgMdl.listStatements(null, RegRdfs.subPropertyOf, (RDFNode) null));
		modelMdl.add(schemaorgMdl.listStatements(null, RegRdf.type, RegRdfs.Class));
		modelMdl.add(schemaorgMdl.listStatements(null, RegRdf.type, RegRdf.Property));

		systemResourceAsStream = ClassLoader
				.getSystemResourceAsStream("inescid/dataaggregation/data/reasoning/owl_def.owl");
		Model owlMdl = RdfUtil.readRdf(systemResourceAsStream);
		systemResourceAsStream.close();
		modelMdl.add(owlMdl.listStatements(null, RegRdfs.subClassOf, (RDFNode) null));
		modelMdl.add(owlMdl.listStatements(null, RegRdfs.subPropertyOf, (RDFNode) null));
		modelMdl.add(owlMdl.listStatements(null, RegRdf.type, RegRdfs.Class));
		modelMdl.add(owlMdl.listStatements(null, RegRdf.type, RegRdf.Property));
//		
//		Jena.createStatementAddToModel(metaAlignMdl, "http://wikiba.se/ontology#Property", RegRdfs.subClassOf, RegRdf.Property);
//		Jena.createStatementAddToModel(metaAlignMdl, "http://wikiba.se/ontology#Entity", RegRdfs.subClassOf, RegRdfs.Class);
//		Jena.createStatementAddToModel(metaAlignMdl, "http://wikiba.se/ontology#Entity", RegRdfs.subClassOf, RegOwl.Thing);

		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EQUIVALENT_CLASS, RegOwl.equivalentProperty,
				RegOwl.equivalentClass);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EQUIVALENT_PROPERTY, RegOwl.equivalentProperty,
				RegOwl.equivalentProperty);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.SUBCLASS_OF, RegOwl.equivalentProperty,
				RegRdfs.subClassOf);
		;
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.SUBPROPERTY_OF, RegOwl.equivalentProperty,
				RegRdfs.subPropertyOf);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.INSTANCE_OF, RegOwl.equivalentProperty,
				RegRdf.type);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RegOwl.equivalentProperty,
				RegSkos.narrowMatch);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.BROADER_CONCEPT, RegOwl.equivalentProperty,
				RegSkos.broadMatch);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.MAPPING_RELATION_TYPE, RegOwl.equivalentProperty,
				RegSkos.mappingRelation);
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EXTERNAL_SUBPROPERTY, RegOwl.equivalentProperty,
				RegSkos.broadMatch);// there is no superProperty in rdfs
		Jena.createStatementAddToModel(metaAlignMdl, RdfRegWikidata.EXTERNAL_SUPERPROPERTY, RegOwl.equivalentProperty,
				RegRdfs.subPropertyOf);
		
		SparqlClientWikidata.queryWithPaging("SELECT ?s ?o WHERE { ?s <"+RdfRegWikidata.SUBCLASS_OF.getURI()+"> ?o .}"
				, 5000, null,
				new Handler() {
			int cnt=0;
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				modelMdl.add(Jena.createStatement(solution.getResource("?s"), RdfRegWikidata.SUBCLASS_OF, solution.getResource("?o")));
				cnt++;
				if(cnt % 10000 == 0) {
					dataset.commit();
					dataset.begin(ReadWrite.WRITE) ;
					System.out.println("Commited "+cnt);
				}					
				return true;
			}
		});		
		
		SparqlClientWikidata.queryWithPaging(
				"SELECT ?s ?o WHERE { ?s <" + RdfRegWikidata.SUBPROPERTY_OF.getURI() + "> ?o .}", 5000, null,
				new Handler() {
					int cnt = 0;

					@Override
					public boolean handleSolution(QuerySolution solution) throws Exception {
						String subjUri=solution.getResource("?s").getNameSpace().equals(RdfRegWikidata.NsWd) ? RdfRegWikidata.NsWdt + solution.getResource("?s").getLocalName() : solution.getResource("?s").getURI(); 
						modelMdl.add(Jena.createStatement(modelMdl.createResource(subjUri), RdfRegWikidata.SUBPROPERTY_OF,
								solution.getResource("?o")));
						cnt++;
						if (cnt % 10000 == 0) {
							dataset.commit();
//					dataset.end();
							dataset.begin(ReadWrite.WRITE);
							System.out.println("Commited " + cnt);
						}
						return true;
					}
				});
		System.out.println("Meta model sizes: " + modelMdl.size());

		for (Property p : new Property[] { RdfRegWikidata.EQUIVALENT_CLASS, RdfRegWikidata.EQUIVALENT_PROPERTY,
				RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RdfRegWikidata.BROADER_CONCEPT,
				RdfRegWikidata.MAPPING_RELATION_TYPE, RdfRegWikidata.EXTERNAL_SUBPROPERTY,
				RdfRegWikidata.EXTERNAL_SUPERPROPERTY }) {
			SparqlClientWikidata.queryWithPaging("SELECT ?s ?o WHERE { ?s <" + p + "> ?o .}", 5000, null,
					new Handler() {
						int cnt = 0;
						@Override
						public boolean handleSolution(QuerySolution solution) throws Exception {
							String subjUri=solution.getResource("?s").getNameSpace().equals(RdfRegWikidata.NsWd) ? RdfRegWikidata.NsWdt + solution.getResource("?s").getLocalName() : solution.getResource("?s").getURI(); 
							modelMdl.add(
									Jena.createStatement(modelMdl.createResource(subjUri), p, solution.getResource("?o")));
							cnt++;
							if (cnt % 10000 == 0) {
								dataset.commit();
								dataset.begin(ReadWrite.WRITE);
								System.out.println("Commited " + cnt);
							}
							return true;
						}
					});
			System.out.println("align model sizes: " + modelMdl.size() +" - "+p.getURI());
		}
		
		modelMdl.add(metaAlignMdl);
		
		stableMdl.removeAll();
		stableMdlUnstableStatements.removeAll();
		dataset.commit();
		dataset.begin(ReadWrite.WRITE);
		stableMdl.add(modelMdl);
		ReasonerStabilizer reasonerStbl = new ReasonerStabilizer(reasoner, stableMdl);
		stableMdlUnstableStatements.add(reasonerStbl.getUnstableSatements());
		dataset.commit();
		dataset.end();
	}

}
