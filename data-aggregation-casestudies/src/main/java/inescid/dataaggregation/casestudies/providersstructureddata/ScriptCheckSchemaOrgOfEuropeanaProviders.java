package inescid.dataaggregation.casestudies.providersstructureddata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.any23.Any23;
import org.apache.any23.extractor.ExtractionException;
import org.apache.any23.extractor.ExtractorGroup;
import org.apache.any23.extractor.html.EmbeddedJSONLDExtractorFactory;
import org.apache.any23.extractor.html.HTMLMetaExtractorFactory;
import org.apache.any23.extractor.microdata.MicrodataExtractorFactory;
import org.apache.any23.extractor.rdfa.RDFa11ExtractorFactory;
import org.apache.any23.writer.TurtleWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.SplitIRI;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.dataaggregation.casestudies.providersstructureddata.ReportSchemaOrgOfEuropeanaProviders.ReportOfProvider;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.HttpRequestService;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.dataset.Global;
import inescid.europeanaapi.clients.webresourcedata.ReportOfSchemaOrgInHtml;
import inescid.util.RdfUtil.Jena;

public class ScriptCheckSchemaOrgOfEuropeanaProviders {

	
	
	public static void main(String[] args) throws Exception {
		final int MAX_PAGES_PER_PROVIDER=3;
		String outputFolder = "c://users/nfrei/desktop/data/";

		if (args != null) {
			if (args.length >= 1) {
				outputFolder = args[0];
			}
		}
		
		Global.init_componentHttpRequestService();
		HttpRequestService httpClient = Global.getHttpRequestService();
		
		File mapsFile = new File(outputFolder, "shownAt.mvstore.bin");
		if (!mapsFile.exists()) 
			System.out.println(mapsFile.getAbsolutePath()+" not found.");
			
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();

		MVMap<String, String> idToCsv = mvStore.openMap(mapsFile.getName());
		
		final ReportSchemaOrgOfEuropeanaProviders report=new ReportSchemaOrgOfEuropeanaProviders();
		

		final Any23 any23=new Any23(new ExtractorGroup(new ArrayList() {{ 
			add(new EmbeddedJSONLDExtractorFactory());
			add(new RDFa11ExtractorFactory());
//			add(new MicrodataExtractorFactory());
//			add(new HTMLMetaExtractorFactory());
		}}));
		
		idToCsv.forEach(new BiConsumer<String, String>() {
			public void accept(String europeanaId, String viewsCsv) {
				try {
					String providerId=europeanaId.substring(0, europeanaId.indexOf('/'));
					ReportOfProvider providerReport = report.getProvider(providerId);
					if(providerReport.testedPages >= MAX_PAGES_PER_PROVIDER ||
							(providerReport.testedPages==0 && providerReport.errors >= MAX_PAGES_PER_PROVIDER)) return;
					CSVParser parser=new CSVParser(new StringReader(viewsCsv), CSVFormat.DEFAULT);
					CSVRecord views=parser.iterator().next();
					String isShownAtUrl=views.get(0);
					if(StringUtils.isEmpty(isShownAtUrl)) return;
					
					HttpRequest request = new HttpRequest(isShownAtUrl);
					request.fetch();
					
					if(request.getResponseStatusCode()==200) {
						String charset = "UTF8";
						if (request.getCharset()!=null)
							charset = request.getCharset().name();
						Model model=Jena.createModel();
						any23.extract(request.getResponseContentAsString(), request.getUrl(), request.getMimeType(), charset, 
								new JenaTripleHandler(model));
						
						for(Statement st : model.listStatements().toList()) {
							String namespace = SplitIRI.namespace(st.getPredicate().getURI());
							providerReport.structuredDataPredicates.incrementTo(namespace);
						}
						
						providerReport.testedPages++;
					} else {
						providerReport.errors++;
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExtractionException e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
}
