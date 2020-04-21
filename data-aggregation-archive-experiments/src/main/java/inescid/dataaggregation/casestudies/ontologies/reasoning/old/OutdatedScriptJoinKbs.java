package inescid.dataaggregation.casestudies.ontologies.reasoning.old;

import java.io.File;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb2.TDB2Factory;

import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.data.RegOwl;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.data.RegSkos;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil.Jena;

public class OutdatedScriptJoinKbs {

	public static void main(String[] args) throws Exception {
		final String WD_REASONING_DS = "wikidata-reasoning";
		final String WD_REASONING_ALIGNS_DS = "wikidata-reasoning-aligns";

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

		dataset.begin(ReadWrite.WRITE) ;

		System.out.println("Meta model sizes before: " + metaModelMdl.size());
		metaModelMdl.add(alignmentMdl);
		
		{
			//		final Model alignmentMdl = Jena.createModel();
					Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.EQUIVALENT_CLASS, RegOwl.equivalentProperty,
							RegOwl.equivalentClass);
					Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.EQUIVALENT_PROPERTY, RegOwl.equivalentProperty,
							RegOwl.equivalentProperty);
					Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.SUBCLASS_OF, RegOwl.equivalentProperty,
							RegRdfs.subClassOf);
					;
					Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.SUBPROPERTY_OF, RegOwl.equivalentProperty,
							RegRdfs.subPropertyOf);
					Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.INSTANCE_OF, RegOwl.equivalentProperty,
							RegRdf.type);
					Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RegOwl.equivalentProperty,
							RegSkos.narrowMatch);
					Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.BROADER_CONCEPT, RegOwl.equivalentProperty,
							RegSkos.broadMatch);
					Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.MAPPING_RELATION_TYPE, RegOwl.equivalentProperty,
							RegSkos.mappingRelation);
					Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.EXTERNAL_SUBPROPERTY, RegOwl.equivalentProperty,
							RegSkos.broadMatch);// there is no superProperty in rdfs
					Jena.createStatementAddToModel(metaModelMdl, RdfRegWikidata.EXTERNAL_SUPERPROPERTY, RegOwl.equivalentProperty,
							RegRdfs.subPropertyOf);
					
				}
		

		System.out.println("Meta model sizes: " + metaModelMdl.size());
		dataset.commit();
		dataset.end();

		System.exit(0);
	}

}
