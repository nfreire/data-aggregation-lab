package inescid.dataaggregation.casestudies.wikidata.reasoning;

public class ScriptWikidataReasoningExperiment_1stTry {
// 	
//	public static void main(String[] args) throws Exception {
//		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository";
//
//		GlobalCore.init_componentHttpRequestService();
//		GlobalCore.init_componentDataRepository(httpCacheFolder);
//
//		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
//		rdfCache.setRequestRetryAttempts(1);
//				
//		ScriptMetadataAnalyzerOfCulturalHeritage.Files.init(new File("c://users/nfrei/desktop/data/wikidata-results-latest"));
//		File wdChoProfileCsv = ScriptMetadataAnalyzerOfCulturalHeritage.Files.wdChoProfile;
//		File wdMetamodelProfileCsv = ScriptMetadataAnalyzerOfCulturalHeritage.Files.schemaOrgEquivalences;
//		
//		//iterate all model resources and reason to get the schema.org alignments
//		HashSet<String> modelUris=new HashSet<String>();
//		CSVParser parser=new CSVParser(new FileReader(wdMetamodelProfileCsv), CSVFormat.DEFAULT);
//		parser.forEach(rec -> {
//			if(rec.size()>0) {
//				String fld=rec.get(0);
//				if(fld.startsWith("http://www.wikidata")) 
//					{ modelUris.add(fld); 
////					System.out.println(fld);
//					}
//				
//			}});
//		parser.close();
//		
//		HashSet<String> choUris=new HashSet<String>();
//		parser=new CSVParser(new FileReader(wdChoProfileCsv), CSVFormat.DEFAULT);
//		parser.forEach(rec -> {
//			if(rec.size()>0) {
//				String fld=rec.get(0);
//				if(fld.startsWith("http://www.wikidata")) choUris.add(fld);
//			}});
//		parser.close();
//		
//		Model schemasModel = null;
//		{//setup the metamodel reasoner
//			ReasonerByRulesAndSchemas reasoner=new ReasonerByRulesAndSchemas();
//			reasoner.loadSchemas("inescid/dataaggregation/data/reasoning/wikibase.owl", "inescid/dataaggregation/data/reasoning/owl_def.owl");
//	//		reasoner.loadSchemas("inescid/dataaggregation/data/reasoning/wikibase.owl", "inescid/dataaggregation/data/reasoning/owl_def.owl", "inescid/dataaggregation/data/reasoning/schemaorg.owl", "inescid/dataaggregation/data/reasoning/edm.owl");
//			
//			Model wikidataSchemaAlignments=Jena.createModel();
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, wikidataSchemaAlignments.createResource("http://wikiba.se/ontology#Property"), RdfRegRdfs.subClassOf, RdfRegRdf.Property);
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, wikidataSchemaAlignments.createResource("http://wikiba.se/ontology#Entity"), RdfRegRdfs.subClassOf, RdfRegRdfs.Class);
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, wikidataSchemaAlignments.createResource("http://wikiba.se/ontology#Entity"), RdfRegRdfs.subClassOf, RdfRegOwl.Thing);
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.EQUIVALENT_CLASS, RdfRegOwl.equivalentProperty, RdfRegOwl.equivalentClass);
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.EQUIVALENT_PROPERTY, RdfRegOwl.equivalentProperty, RdfRegOwl.equivalentProperty);
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.SUBCLASS_OF, RdfRegOwl.equivalentProperty, RdfRegRdfs.subClassOf);;
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.SUBPROPERTY_OF, RdfRegOwl.equivalentProperty, RdfRegRdfs.subPropertyOf);
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.INSTANCE_OF, RdfRegOwl.equivalentProperty, RdfRegRdf.type);
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RdfRegOwl.equivalentProperty, RdfRegSkos.narrowMatch);
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.BROADER_CONCEPT, RdfRegOwl.equivalentProperty, RdfRegSkos.broadMatch);
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.MAPPING_RELATION_TYPE, RdfRegOwl.equivalentProperty, RdfRegSkos.mappingRelation);
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.EXTERNAL_SUBPROPERTY, RdfRegOwl.equivalentProperty, RdfRegSkos.broadMatch);//there is no superProperty in rdfs
//			Jena.createStatementAddToModel(wikidataSchemaAlignments, RdfRegWikidata.EXTERNAL_SUPERPROPERTY, RdfRegOwl.equivalentProperty, RdfRegRdfs.subPropertyOf);
//			reasoner.loadSchemas(wikidataSchemaAlignments);
//			
//			modelUris.forEach(uri -> {
//				try {
//					HttpResponse recResponse = rdfCache.fetchRdf(uri);
//					if(recResponse.isSuccess()) {
//						Model readRdf = RdfUtil.readRdf(recResponse);
//						if(readRdf!=null)
//							 reasoner.loadSchemas(readRdf);
//					}
//				} catch (AccessException e) {
//					e.printStackTrace();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			});
//			
//			reasoner.initReasoner();
//			schemasModel = reasoner.getSchemasModel();
//		}		
//		ReasonerByRulesAndSchemas reasonerChos=new ReasonerByRulesAndSchemas();
//		reasonerChos.setRulesClasspath("inescid/dataaggregation/casestudies/wikidata/reasoning/WikidataModelReasoner-rules.txt");
//		reasonerChos.loadSchemas(schemasModel);
//		reasonerChos.initReasoner();
//		
//		choUris.forEach(uri -> {
//			try {
//				HttpResponse recResponse = rdfCache.fetchRdf(uri);
//				if(recResponse.isSuccess()) {
//					Model readRdf = RdfUtil.readRdf(recResponse);
//					if(readRdf==null) return;
//					InfModel inferedModel = reasonerChos.infer(readRdf);
//					System.out.println("### INFERED ### "+uri);
////					System.out.println( RdfUtil.statementsToString(inferedModel.getDeductionsModel()));
//					StringBuilder sb=new StringBuilder();
//					for(Statement st : inferedModel.getDeductionsModel().listStatements().toList()) {
////						System.err.println(st);
//						if(st.getSubject().getURI()!=null && st.getSubject().getURI().startsWith(uri)
//								&& !(st.getPredicate().equals(RdfRegRdf.type) && st.getObject().isAnon())) 
////							if(st.getSubject().getURI()!=null && 
////							!st.getSubject().getURI().startsWith("http://www.w3.org/2002/07/owl") &&
////							!st.getSubject().getURI().startsWith("http://www.w3.org/2000/01/rdf-schema") &&
////							!st.getSubject().getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns"))
//						sb.append(st.toString()).append('\n');
//					}
//					System.out.println(sb.toString());	
//				}
//			} catch (AccessException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}			
//		});
//		parser.close();		
//	}
//	
//	
}
