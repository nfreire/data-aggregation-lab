package inescid.dataaggregation.crawl.distribution;

import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.StreamRDFBase;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.HttpRequestService;
import inescid.dataaggregation.dataset.Global;

public class LargeFileDownloader {

	public static void main(String[] args) throws Exception {
		String repoFolder = "c://users/nfrei/desktop/data/HttpRepository";
		if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
			}
		}
		
		Global.init_componentDataRepository(repoFolder);
		Global.init_componentHttpRequestService();
		Global.init_enableComponentHttpRequestCache();

		HttpRequestService httpRequestService = Global.getHttpRequestService();
		
		String testUri="http://viaf.org/viaf/data/viaf-20200203-clusters-rdf.nt.gz";
		
		HttpRequest uriReqResp = new HttpRequest(testUri);
		httpRequestService.fetchStreamed(uriReqResp);
		InputStream bodyStream = uriReqResp.getResponse().getBodyStream();
		
		if(uriReqResp.getMimeType()!=null && uriReqResp.getMimeType().equals("application/x-gzip")) {
//			Pattern gzipUrlPattern=Pattern.compile("/?([^/]+).gz$",Pattern.CASE_INSENSITIVE);
			bodyStream=new GZIPInputStream(bodyStream);
//			Matcher matcher = gzipUrlPattern.matcher(testUri);
//			String fileExtension=matcher.find() ? matcher.group(1) : null;
			String uriWithoutFileExtension=null;
			if(testUri.toLowerCase().endsWith(".gz"))
				uriWithoutFileExtension=testUri.substring(0, testUri.length());
			if(uriWithoutFileExtension==null)
				throw new IllegalArgumentException("undeterminable mime type: "+testUri);
			Lang filenameToLang = RDFLanguages.filenameToLang(uriWithoutFileExtension);
			RDFDataMgr.parse(new StreamRDFBase() {
				public void triple(Triple triple) {
					System.out.println(triple);
	//				boolean added = sameAsSetsEuropeana.addIfOverlap(triple.getSubject().getURI(), triple.getObject().getURI());
	//				changed[0]=changed[0] || added;
				}
			}, bodyStream, filenameToLang);
//		}, bodyStream, RDFLanguages.fileExtToLang(fileExtension));
		}else if(uriReqResp.getMimeType()!=null && uriReqResp.getMimeType().equals("application/zip")) {
			
			
		} else {
			RDFDataMgr.parse(new StreamRDFBase() {
				public void triple(Triple triple) {
					System.out.println(triple);
	//				boolean added = sameAsSetsEuropeana.addIfOverlap(triple.getSubject().getURI(), triple.getObject().getURI());
	//				changed[0]=changed[0] || added;
				}
			}, bodyStream, RDFLanguages.contentTypeToLang(uriReqResp.getMimeType()));
		}
		
	}
	
}
