package inescid.dataaggregation.casestudies.ontologies.reasoning.old;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.riot.Lang;
import org.apache.jena.tdb2.TDB2Factory;

import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.casestudies.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.data.RegOwl;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.data.RegSchemaorg;
import inescid.dataaggregation.data.RegSkos;
import inescid.dataaggregation.data.reasoning.ReasonerStabilizer;
import inescid.dataaggregation.data.reasoning.ReasonerUtil;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;
import inescid.util.SparqlClient.Handler;

public class OldScriptAssembleKbForReasoningInLargeOntologiesExperiment {

	public static void main(String[] args) throws Exception {
		final String WD_REASONING_DS = "wikidata-reasoning";
		final String WD_REASONING_ALIGNS_DS = "wikidata-reasoning-aligns";
		final boolean RESET_MODELS = true;

		if(args==null || args.length<1) {
			System.out.println("Missing parameter: folder for storing data");
			System.exit(0);
		}
		File dataFolder=new File(args[0]);
		String httpCacheFolder = new File(dataFolder, "HttpRepository").getAbsolutePath();
		String tripleStoreFolder = new File(dataFolder, "TripleStore").getAbsolutePath();

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);

		// Make a TDB-backed dataset
		final Dataset dataset = TDB2Factory.connectDataset(tripleStoreFolder);
		final Model metaModelMdl = dataset.getNamedModel(WD_REASONING_DS);
		final Model alignmentMdl = dataset.getNamedModel(WD_REASONING_ALIGNS_DS);
		if (RESET_MODELS) {
			dataset.begin(ReadWrite.WRITE);
			metaModelMdl.removeAll();
			alignmentMdl.removeAll();
			dataset.commit();
		}

		dataset.begin(ReadWrite.WRITE) ;
		SparqlClientWikidata.queryWithPaging("SELECT ?s ?o WHERE { ?s <"+RdfRegWikidata.SUBCLASS_OF.getURI()+"> ?o .}"
				, 5000, null,
				new Handler() {
			int cnt=0;
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				metaModelMdl.add(Jena.createStatement(solution.getResource("?s"), RdfRegWikidata.SUBCLASS_OF, solution.getResource("?o")));
				cnt++;
				if(cnt % 10000 == 0) {
					dataset.commit();
					dataset.begin(ReadWrite.WRITE) ;
					System.out.println("Commited "+cnt);
				}					
				return true;
			}
		});		
		dataset.commit();
		
		dataset.begin(ReadWrite.WRITE);
		SparqlClientWikidata.queryWithPaging(
				"SELECT ?s ?o WHERE { ?s <" + RdfRegWikidata.SUBPROPERTY_OF.getURI() + "> ?o .}", 5000, null,
				new Handler() {
					int cnt = 0;

					@Override
					public boolean handleSolution(QuerySolution solution) throws Exception {
						metaModelMdl.add(Jena.createStatement(solution.getResource("?s"), RdfRegWikidata.SUBPROPERTY_OF,
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
		System.out.println("Meta model sizes: " + metaModelMdl.size());
		dataset.commit();

		for (Property p : new Property[] { RdfRegWikidata.EQUIVALENT_CLASS, RdfRegWikidata.EQUIVALENT_PROPERTY,
				RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RdfRegWikidata.BROADER_CONCEPT,
				RdfRegWikidata.MAPPING_RELATION_TYPE, RdfRegWikidata.EXTERNAL_SUBPROPERTY,
				RdfRegWikidata.EXTERNAL_SUPERPROPERTY }) {
			dataset.begin(ReadWrite.WRITE);
			SparqlClientWikidata.queryWithPaging("SELECT ?s ?o WHERE { ?s <" + p + "> ?o .}", 5000, null,
					new Handler() {
						int cnt = 0;
						@Override
						public boolean handleSolution(QuerySolution solution) throws Exception {
							alignmentMdl.add(
									Jena.createStatement(solution.getResource("?s"), p, solution.getResource("?o")));
							cnt++;
							if (cnt % 10000 == 0) {
								dataset.commit();
								dataset.begin(ReadWrite.WRITE);
								System.out.println("Commited " + cnt);
							}
							return true;
						}
					});
			System.out.println("align model sizes: " + alignmentMdl.size() +" - "+p.getURI());
			dataset.commit();
		}

		// load schema.org owl
		// get all statements of subclassof, and subpropertyof
		InputStream systemResourceAsStream = ClassLoader
				.getSystemResourceAsStream("inescid/dataaggregation/data/reasoning/schemaorg.owl");
		Model schemaorgMdl = RdfUtil.readRdf(systemResourceAsStream);
		systemResourceAsStream.close();

		systemResourceAsStream = ClassLoader
				.getSystemResourceAsStream("inescid/dataaggregation/data/reasoning/owl_def.owl");
		Model owlAndSchemaOrgMdl = RdfUtil.readRdf(systemResourceAsStream);
		systemResourceAsStream.close();

		owlAndSchemaOrgMdl.add(schemaorgMdl.listStatements(null, RegRdfs.subClassOf, (RDFNode) null));
		owlAndSchemaOrgMdl.add(schemaorgMdl.listStatements(null, RegRdfs.subPropertyOf, (RDFNode) null));
		owlAndSchemaOrgMdl.add(schemaorgMdl.listStatements(null, RegRdf.type, RegRdfs.Class));
		owlAndSchemaOrgMdl.add(schemaorgMdl.listStatements(null, RegRdf.type, RegRdf.Property));

		dataset.begin(ReadWrite.WRITE);
		alignmentMdl.add(owlAndSchemaOrgMdl);
		dataset.commit();
		dataset.end();

		schemaorgMdl.close();
		owlAndSchemaOrgMdl.close();
		schemaorgMdl = null;
		owlAndSchemaOrgMdl = null;

		System.exit(0);

		GenericRuleReasoner reasoner = ReasonerUtil
				.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-extend.txt");
//		GenericRuleReasoner reasoner = ReasonerUtil.instanciateRdfsBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rules.txt");

//		Jena.createStatementAddToModel(metaModelMdl, metaModelMdl.createResource("http://wikiba.se/ontology#Property"), RegRdfs.subClassOf, RegRdf.Property);
//		Jena.createStatementAddToModel(metaModelMdl, metaModelMdl.createResource("http://wikiba.se/ontology#Entity"), RegRdfs.subClassOf, RegRdfs.Class)
//		Jena.createStatementAddToModel(metaModelMdl, metaModelMdl.createResource("http://wikiba.se/ontology#Item"), RegRdfs.subClassOf, RegOwl.Thing);
		Jena.createStatementAddToModel(alignmentMdl, RdfRegWikidata.EQUIVALENT_CLASS, RegOwl.equivalentProperty,
				RegOwl.equivalentClass);
		Jena.createStatementAddToModel(alignmentMdl, RdfRegWikidata.EQUIVALENT_PROPERTY, RegOwl.equivalentProperty,
				RegOwl.equivalentProperty);
		Jena.createStatementAddToModel(alignmentMdl, RdfRegWikidata.SUBCLASS_OF, RegOwl.equivalentProperty,
				RegRdfs.subClassOf);
		;
		Jena.createStatementAddToModel(alignmentMdl, RdfRegWikidata.SUBPROPERTY_OF, RegOwl.equivalentProperty,
				RegRdfs.subPropertyOf);
		Jena.createStatementAddToModel(alignmentMdl, RdfRegWikidata.INSTANCE_OF, RegOwl.equivalentProperty,
				RegRdf.type);
		Jena.createStatementAddToModel(alignmentMdl, RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RegOwl.equivalentProperty,
				RegSkos.narrowMatch);
		Jena.createStatementAddToModel(alignmentMdl, RdfRegWikidata.BROADER_CONCEPT, RegOwl.equivalentProperty,
				RegSkos.broadMatch);
		Jena.createStatementAddToModel(alignmentMdl, RdfRegWikidata.MAPPING_RELATION_TYPE, RegOwl.equivalentProperty,
				RegSkos.mappingRelation);
		Jena.createStatementAddToModel(alignmentMdl, RdfRegWikidata.EXTERNAL_SUBPROPERTY, RegOwl.equivalentProperty,
				RegSkos.broadMatch);// there is no superProperty in rdfs
		Jena.createStatementAddToModel(alignmentMdl, RdfRegWikidata.EXTERNAL_SUPERPROPERTY, RegOwl.equivalentProperty,
				RegRdfs.subPropertyOf);

//		InfModel inferedPre = ReasonerUtil.infer(reasoner, metaModelMdl);
//		metaModelMdl.add(inferedPre.getDeductionsModel().listStatements());
//
//		System.out.println("Metamodel stms: "+metaModelMdl.size());
//		System.out.println("Deduction stms: "+inferedPre.getDeductionsModel().size());
//
//		inferedPre = ReasonerUtil.infer(reasoner, metaModelMdl);
//		metaModelMdl.add(inferedPre.getDeductionsModel().listStatements());
//		
//		System.out.println("Metamodel stms: "+metaModelMdl.size());
//		System.out.println("Deduction stms: "+inferedPre.getDeductionsModel().size());
//		
//		inferedPre = ReasonerUtil.infer(reasoner, metaModelMdl);
//		metaModelMdl.add(inferedPre.getDeductionsModel().listStatements());
//
//		System.out.println("Metamodel stms: "+metaModelMdl.size());
//		System.out.println("Deduction stms: "+inferedPre.getDeductionsModel().size());
//		
//		inferedPre = ReasonerUtil.infer(reasoner, metaModelMdl);
//		metaModelMdl.add(inferedPre.getDeductionsModel().listStatements());
//		
//		System.out.println("Metamodel stms: "+metaModelMdl.size());
//		System.out.println("Deduction stms: "+inferedPre.getDeductionsModel().size());
//		
//		inferedPre=null;

		Model testMdl = Jena.createModel();

		testMdl.add(Jena.createStatement("http://nuno.pt/pintura", RegRdf.type, RegSchemaorg.VisualArtwork));

		testMdl.add(Jena.createStatement("http://nuno.pt/pintura", RdfRegWikidata.INSTANCE_OF, RegSchemaorg.Person));

		dataset.begin(ReadWrite.READ);
		ReasonerStabilizer reasonerStbl = new ReasonerStabilizer(reasoner, metaModelMdl);

		Model infered = reasonerStbl.inferNewDeductions(testMdl);
		StringWriter w = new StringWriter();
		RdfUtil.writeRdf(infered, Lang.TURTLE, w);
		System.out.println(w.toString());

		dataset.end();

//		System.out.println("Metamodel stms: "+metaModelMdl.size());
		System.out.println("Deduction stms: " + infered.size());
	}

}
