package inescid.dataaggregation.dataset.profile.completeness;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil;

public class ScriptTestCalculator {
	private static HttpRequestService httpRequestService=new HttpRequestService();

	public static void main(String[] args)  {
		try {
			File urisFolder=new File("src/data/europeana-dataset-uris");
			File chartsFolder=new File("c://users/nfrei/desktop/CompletenessCharts");
			String httpCacheFolder="c://users/nfrei/desktop/HttpRepository";
			int SAMPLE_RECORDS=5;
			int SAMPLE_COLLECTIONS=3;

			if(args.length>0)
				urisFolder=new File(args[0]);
			if(args.length>1)
				chartsFolder=new File(args[1]);
			if(args.length>2)
				httpCacheFolder=args[2];
			if(args.length>3)
				SAMPLE_RECORDS=Integer.parseInt(args[3]);
			if(args.length>4)
				SAMPLE_COLLECTIONS=Integer.parseInt(args[4]);
			
			System.out.printf("Settings:\n-URIs: %s\n-Charts:%s\n-Cache:%s\n-Records:%d\n-Collections:%d\n-------------------------\n",
					urisFolder.getPath(), chartsFolder.getPath(), httpCacheFolder, SAMPLE_RECORDS, SAMPLE_COLLECTIONS);
			
			Global.init_componentHttpRequestService();
			Global.init_componentDataRepository(httpCacheFolder);
			
			if(!chartsFolder.exists())
				chartsFolder.mkdirs();
			
			List<Double> scores=null;
			int processedCollections=0;
			for(File colUris : urisFolder.listFiles()) {
				String collection = colUris.getName().substring(0, colUris.getName().indexOf('.'));
				System.out.println("Processing collection "+collection);
				Pair<List<Double>, List<Integer>> allScores = testUrisIn(colUris, SAMPLE_RECORDS, 5);
				scores=allScores.getKey();
				if(!scores.isEmpty()) {
					AreaChartGenerator.generateChart(scores, collection, chartsFolder);
					AreaChartGenerator.generateChartOldCompleteness(allScores.getValue(), collection, chartsFolder);
				}
//			System.out.println(scores);

				//		scores=testUrisIn(new File(urisFolder, "00501.txt"), 10, 5);
//		System.out.println(scores);
//		scores=testUrisIn(new File(urisFolder, "00718.txt"), 10, 5);
//		System.out.println(scores);
//		scores=testUrisIn(new File(urisFolder, "0940403.txt"), 10, 5);
//		System.out.println(scores);
				processedCollections++;
				if(SAMPLE_COLLECTIONS>0 && processedCollections>=SAMPLE_COLLECTIONS)
					break;
			}	
			HtmlDisplayGenerator.generateChartsDisplay(chartsFolder, urisFolder.listFiles(), processedCollections, SAMPLE_RECORDS);

			//		scores=testUrisIn(new File(urisFolder, "10501.txt"), 10, 5);
//			scores=testUrisIn(new File(urisFolder, "0940403.txt"), 10, 5);
//			System.out.println(scores);
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.exit(0);
	}


	private static Pair<List<Double>, List<Integer>> testUrisIn(File urisFile, int maxTests, int maxErrors) throws IOException, InterruptedException {
		List<Double> ret=new ArrayList<>();
		List<Integer> retOldCompleteness=new ArrayList<>();
		TiersDqcCompletenessCalculator calculator=new TiersDqcCompletenessCalculator();
		int errorsCount=0;

		CachedHttpRequestService httpRequestService=new CachedHttpRequestService();
		
		BufferedReader reader=new BufferedReader(new FileReader(urisFile));
		while(reader.ready() && (maxTests<=0 || maxTests > ret.size()) && (maxErrors<=0 || maxErrors > errorsCount)) {
			String uri=reader.readLine();
			
			try {
				HttpResponse fetched = httpRequestService.fetchRdf(uri); 				
				Model rdf = RdfUtil.readRdf(fetched.getBody(), RdfUtil.fromMimeType(fetched.getHeader("Content-Type")));
				System.out.println(uri);
//				System.out.println("*** "+uri);
//				System.out.println(RdfUtil.printStatements(rdf));
				StmtIterator completenessStms = rdf.listStatements(null, Edm.completeness, (RDFNode)null);
				if(completenessStms.hasNext()) {
					int completeness = completenessStms.next().getObject().asLiteral().getInt();
					retOldCompleteness.add(completeness);
				}
				ret.add(calculator.calculate(rdf));
			} catch (Exception e) {
				errorsCount++;
				System.err.println("Error on: "+uri);
				e.printStackTrace();
			}
		}
		return new ImmutablePair<List<Double>, List<Integer>>(ret, retOldCompleteness);
	}


}
