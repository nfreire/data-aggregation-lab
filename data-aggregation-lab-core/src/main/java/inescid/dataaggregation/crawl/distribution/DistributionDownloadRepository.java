package inescid.dataaggregation.crawl.distribution;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.any23.extractor.rdf.RDFParserFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.SysRIOT;
import org.apache.jena.riot.lang.LangNTriples;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.util.Context;

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;

public class DistributionDownloadRepository {
	Repository repo;
	String datasetId;

	public DistributionDownloadRepository(Repository repo, String datasetId) {
		super();
		this.repo = repo;
		this.datasetId = datasetId;
	}

	protected void downloadAndSave(String url) throws InterruptedException, IOException {
		HttpRequest request = Global.getHttpRequestService().fetchStreamedWithoutCache(new HttpRequest(url));
		HttpResponse response = request.getResponse();
		repo.save(datasetId, url, response.getBodyStream(), response.getHeaders());
	}

	public void streamRdf(String url, StreamRDFBase handler) throws IOException, InterruptedException {
		if(!repo.contains(datasetId, url))
			downloadAndSave(url);
//	LangNTriples
//		IRIResolver.suppressExceptions();
		InputStream contentStream = repo.getContentStream(datasetId, url);
		//contentStream.available()
		String contentType = repo.getContentType(datasetId, url);
		if (contentType == null && url.toLowerCase().endsWith(".gz"))
			contentType = "application/x-gzip";
		if (contentType == null && url.toLowerCase().endsWith(".zip"))
			contentType = "application/zip";
		if (contentType == null && url.toLowerCase().endsWith(".bz2"))
			contentType = "application/x-bzip2";
		if (StringUtils.equals(contentType, "application/x-gzip")) {
			contentStream = new GZIPInputStream(contentStream);
			String uriWithoutFileExtension = null;
			if (url.toLowerCase().endsWith(".gz"))
				uriWithoutFileExtension = url.substring(0, url.length()-3);
			if (uriWithoutFileExtension == null)
				throw new IllegalArgumentException("undeterminable mime type: " + url);
			Lang filenameToLang = RDFLanguages.filenameToLang(uriWithoutFileExtension);

			RDFParserBuilder builder = RDFParser.create().checking(false).forceLang(filenameToLang);
			BufferedReader br=new BufferedReader(new InputStreamReader(contentStream, "UTF-8"));
			for(String line=br.readLine() ; line!=null; line=br.readLine()) {
				try {
					builder.source(new StringReader(line)).parse(handler);
				} catch (RiotException e) {
					System.err.println("... Ignoring triple due to RIOT exception");
					e.printStackTrace();
				}
			}
			br.close();
		} else if (StringUtils.equals(contentType, "application/x-bzip2")) {
		    BufferedReader br;
			BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(contentStream);
			br = new BufferedReader(new InputStreamReader(bzIn,  "UTF-8"));

				String uriWithoutFileExtension = null;
			if (url.toLowerCase().endsWith(".bz2"))
				uriWithoutFileExtension = url.substring(0, url.length()-4);
			if (uriWithoutFileExtension == null)
				throw new IllegalArgumentException("undeterminable mime type: " + url);
			Lang filenameToLang = RDFLanguages.filenameToLang(uriWithoutFileExtension);
			
			RDFParserBuilder builder = RDFParser.create().checking(false).forceLang(filenameToLang);
			for(String line=br.readLine() ; line!=null; line=br.readLine()) {
				try {
					builder.source(new StringReader(line)).parse(handler);
				} catch (RiotException e) {
					System.err.println("... Ignoring triple due to RIOT exception");
					e.printStackTrace();
				}
			}
			br.close();
		} else if (StringUtils.equals(contentType, "application/zip")) {
			ZipInputStream zip = new ZipInputStream(contentStream);
			ZipEntry entry = zip.getNextEntry();
			while (entry != null) {
				Lang filenameToLang = RDFLanguages.filenameToLang(entry.getName());
				RDFParserBuilder builder = RDFParser.create().checking(false).forceLang(filenameToLang);
				BufferedReader br=new BufferedReader(new InputStreamReader(zip, "UTF-8"));
				for(String line=br.readLine() ; line!=null; line=br.readLine()) {
					try {
						builder.source(new StringReader(line)).parse(handler);
					} catch (RiotException e) {
						System.err.println("... Ignoring triple due to RIOT exception");
						e.printStackTrace();
					}
				}
//				RDFDataMgr.parse(handler, zip, filenameToLang);
				zip.closeEntry();
				entry = zip.getNextEntry();
			}
		}
	}
}
