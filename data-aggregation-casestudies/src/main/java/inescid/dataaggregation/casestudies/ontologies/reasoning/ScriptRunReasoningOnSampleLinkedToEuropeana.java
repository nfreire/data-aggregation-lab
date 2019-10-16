package inescid.dataaggregation.casestudies.ontologies.reasoning;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.riot.Lang;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.poi.hslf.record.Sound;

import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.casestudies.wikidata.ScriptExportSamples.DataDumps;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.data.RegSchemaorg;
import inescid.dataaggregation.data.reasoning.AlignmentReasoner;
import inescid.dataaggregation.data.reasoning.ReasonerUtil;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.profile.ClassUsageStats;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.dataaggregation.store.Repository;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class ScriptRunReasoningOnSampleLinkedToEuropeana {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if(args==null || args.length<1) {
			System.out.println("Missing parameter: folder for storing data");
			System.exit(0);
		}
		File dataFolder=new File(args[0]);
		String httpCacheFolder = new File(dataFolder, Settings.HTTP_CHACHE_FOLDER).getAbsolutePath();
		String tripleStoreJoinedFolder = new File(dataFolder, Settings.TRIPLE_STORE_JOINED_FOLDER).getAbsolutePath();

		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);

		final Dataset dataset = TDB2Factory.connectDataset(tripleStoreJoinedFolder);
		dataset.begin(ReadWrite.READ);
//		dataset.begin(ReadWrite.WRITE);
		
//		final Model modelMdl = dataset.getNamedModel(Settings.WD_REASONING_MODEL_DS);
		final Model reasoningMdl = dataset.getNamedModel(Settings.WD_REASONING_MODEL_ALIGN_META_DS);
		System.out.println("Reasoning model size: "+ reasoningMdl.size());
		InputStream systemResourceAsStream = ClassLoader
				.getSystemResourceAsStream("inescid/dataaggregation/data/reasoning/schemaorg.owl");
//		Model schemaorgMdl = RdfUtil.readRdf(systemResourceAsStream);
//		systemResourceAsStream.close();
//		reasoningMdl.add(schemaorgMdl.listStatements(null, RegRdfs.subClassOf, (RDFNode) null));
//		reasoningMdl.add(schemaorgMdl.listStatements(null, RegRdfs.subPropertyOf, (RDFNode) null));
////		metaAlignMdl.add(schemaorgMdl.listStatements(null, RegRdf.type, RegRdfs.Class));
////		metaAlignMdl.add(schemaorgMdl.listStatements(null, RegRdf.type, RegRdf.Property));
		
//		Jena.createStatementAddToModel(reasoningMdl, reasoningMdl.createResource("http://www.wikidata.org/entity/Q15401930") , 
//				RdfRegWikidata.EQUIVALENT_CLASS, RegSchemaorg.Organization);

		GenericRuleReasoner reasoner = ReasonerUtil
				.instanciateRuleBased("inescid/dataaggregation/casestudies/ontologies/reasoning/test-rdfs-owl-ontologies-rules-small.txt", GenericRuleReasoner.FORWARD_RETE);
		AlignmentReasoner alignReasoner=new AlignmentReasoner(reasoningMdl, reasoner);
		
		Repository dataRepository = Global.getDataRepository();

		ReasoningStats stats=new ReasoningStats();
		
		int cnt=0;
		List<Entry<String, File>> allDatasetFiles = dataRepository.getAllDatasetResourceFiles(DataDumps.WIKIDATA_ONTOLOGY.name());
		for(Entry<String, File> recordEntry: allDatasetFiles) {
			FileInputStream fis = new FileInputStream(recordEntry.getValue());
			Model wdEntMdl;
			try {
				wdEntMdl = RdfUtil.readRdf(fis, Lang.TURTLE);
			} catch (Exception e) {
				System.err.println("Error reasoning on "+recordEntry.getKey());
				e.printStackTrace();
				continue;
			}
			fis.close();

			Resource wdResource = wdEntMdl.createResource(recordEntry.getKey());
			stats.addWikidataResource(wdResource);
			
			stats.startReasoningTotalChronometer();
			stats.startReasoningSubsetSelectChronometer();
			InfModel infered = alignReasoner.infer(wdResource);
			stats.endReasoningSubsetSelectChronometer();
			stats.startReasoningChronometer();
			Model deductionsModel = infered.getDeductionsModel();
			stats.endReasoningChronometer();
			stats.endReasoningTotalChronometer();
//			System.out.println("WD ent stms: " + wdEntMdl.size());
//			System.out.println("Deduction stms: " + deductionsModel.size());
//			System.out.println("################## WD ent model #########################");		
//			RdfUtil.printOutRdf(testSubject.getModel().listStatements(testSubject, null, (RDFNode) null));
//			System.out.println("################## deductions model #########################");		
//			RdfUtil.printOutRdf(deductionsModel.listStatements(testSubject, null, (RDFNode) null));
			stats.addSubSchema(alignReasoner.getSubSchemaOfLastInference());
			stats.addDeductions(deductionsModel.createResource(wdResource.getURI()));
			
			if(cnt==100 || cnt%1000==0)
				System.out.println("Processed "+cnt);
//			if(cnt>10)
//				break;
			cnt++;
		}
		
		if(dataset.isInTransaction())
			dataset.abort();
		dataset.close();
		
		String statsCsv = stats.toCsv();
		System.out.println(statsCsv);
		FileUtils.write(new File(dataFolder, "reasoningwikidata-evaluation.csv"), statsCsv, "UTF8");
		FileUtils.write(new File("c:\\\\user\\nfrei\\desktop", "reasoningwikidata-evaluation.csv"), statsCsv,"UTF8");
	}

}
