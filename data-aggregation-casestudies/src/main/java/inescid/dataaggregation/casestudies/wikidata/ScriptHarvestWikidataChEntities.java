package inescid.dataaggregation.casestudies.wikidata;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;

import inescid.dataaggregation.casestudies.wikidata.ScriptExportSamples.DataDumps;
import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.SparqlClient.Handler;

public class ScriptHarvestWikidataChEntities {
	public static void main(String[] args) throws Exception {
		String httpCacheFolder = "c://users/nfrei/desktop/data/HttpRepository";
		final int SAMPLE_RECORDS;

		if (args.length > 0)
			httpCacheFolder = args[0];
		if (args.length > 1)
			SAMPLE_RECORDS = Integer.parseInt(args[1]);
		else
			SAMPLE_RECORDS = -1;

		final HashSet<String> europeanaIdsBroken = new HashSet<String>();
		try {
			File brokenLinksReportFile = new File("src/data/wikidata/wikidata_broken_links_to_europeana.csv");
			if (brokenLinksReportFile.exists()) {
				List<String> lines = FileUtils.readLines(brokenLinksReportFile, "UTF-8");
				lines.remove(0);
				for (String l : lines) {
					String[] split = l.split(",");
					if (split[2].equals("404"))
						europeanaIdsBroken.add(split[1]);
				}
			}
		} catch (IOException e1) {
			System.err.println();
			e1.printStackTrace();
		}

		System.out.printf(
				"Settings:\n-Cache:%s\n-Records:%d\n-Broken EuropenaIDs:%d\n------------------------\n",
				httpCacheFolder, SAMPLE_RECORDS, europeanaIdsBroken.size());
		Global.init_componentHttpRequestService();
		Global.init_componentDataRepository(httpCacheFolder);

		Repository dataRepository = Global.getDataRepository();

		CachedHttpRequestService rdfCache = new CachedHttpRequestService();
		rdfCache.setRequestRetryAttempts(1);

		final HashMap<String, String> wikidataEuropeanaIdsMap = new HashMap<>();

		SparqlClientWikidata.query("SELECT ?item ?europeana WHERE {" +
//                "  ?item wdt:"+RdfRegWikidata.IIIF_MANIFEST+" ?x ." + 
				"  ?item wdt:" + RdfRegWikidata.EUROPEANAID.getLocalName() + " ?europeana . }", new Handler() {

					int stop = SAMPLE_RECORDS;

					public boolean handleSolution(QuerySolution solution)
							throws AccessException, InterruptedException, IOException {
						String europeanaId = solution.getLiteral("europeana").getString();
						if (europeanaIdsBroken.contains(europeanaId)) {
//							System.out.println("Skipping broken "+europeanaId);
							return true;
						}
						Resource resourceResult = solution.getResource("item");
						String uri = resourceResult.getURI();
						wikidataEuropeanaIdsMap.put(uri, europeanaId);

						stop--;
						return stop != 0;
//						return true;
					}

				});
		System.out.println("Got " + wikidataEuropeanaIdsMap.size() + " wikidata europeana id pairs");

		// Profile properties
		final AtomicInteger cnt = new AtomicInteger(0);
		wikidataEuropeanaIdsMap.keySet().forEach((uri) -> {
			try {
				Resource resource = RdfUtil.readRdfResourceFromUri(uri);

				removeOtherResources(resource.getModel(), uri);
				removeNonTruthyStatements(resource.getModel());
				addRdfTypesFromP31(resource.getModel());

				dataRepository.save(DataDumps.WIKIDATA_ONTOLOGY.name(), uri, RdfUtil.writeRdf(resource.getModel(), Lang.TURTLE), "Content-Type",
						Lang.TURTLE.getContentType().getContentType());
			} catch (AccessException | InterruptedException | IOException e) {
				System.err.println("Exception in " + uri);
				e.printStackTrace();
				wikidataEuropeanaIdsMap.remove(uri);
			}
			if (cnt.incrementAndGet() % 500 == 0)
				System.out.println("progress " + cnt);
		});


	}

	private static void removeOtherResources(Model rdfWikidata, String keepUri) {
		Resource keep = rdfWikidata.createResource(keepUri);
		for (StmtIterator stmts = rdfWikidata.listStatements(); stmts.hasNext();) {
			Statement stm = stmts.next();
			if (!(stm.getSubject().equals(keep) || stm.getObject().equals(keep)))
				stmts.remove();
		}
	}

	private static void removeNonTruthyStatements(Model rdfWikidata) {
		for (StmtIterator stmts = rdfWikidata.listStatements(); stmts.hasNext();) {
			Statement stm = stmts.next();
			if (stm.getPredicate().getNameSpace().startsWith("http://www.wikidata.org/")
					&& !(stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWd)
							|| stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWdt)
							|| stm.getPredicate().getNameSpace().startsWith(RdfRegWikidata.NsWdtn)))
				stmts.remove();
		}
	}

	private static void addRdfTypesFromP31(Model rdfWikidata) {
		for (StmtIterator stmts = rdfWikidata.listStatements(null, RdfRegWikidata.INSTANCE_OF, (RDFNode) null); stmts
				.hasNext();) {
			Statement stm = stmts.next();
			rdfWikidata.add(rdfWikidata.createStatement(stm.getSubject(), Rdf.type, stm.getObject()));
		}
	}

}