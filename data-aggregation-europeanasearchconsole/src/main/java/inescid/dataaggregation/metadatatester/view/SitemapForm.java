package inescid.dataaggregation.metadatatester.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.any23.Any23;
import org.apache.any23.extractor.ExtractionException;
import org.apache.any23.extractor.ExtractorGroup;
import org.apache.any23.extractor.html.EmbeddedJSONLDExtractorFactory;
import org.apache.any23.extractor.rdfa.RDFa11ExtractorFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.SplitIRI;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.UnknownFormatException;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.crawl.sitemap.SitemapUtil;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class SitemapForm extends UriForm {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(SitemapForm.class);
	public static Pattern domainNamePattern=Pattern.compile("^\\s*(https?://)?([-a-zA-Z0-9_\\.]+)\\s*");
	public static Pattern sitemapInRobotsTxtPattern=Pattern.compile("^Sitemap:\\s*([^\\s]*)");
	
	String operation=null;
	boolean uriChecked=false;

	boolean validatedDomain=false;
	boolean validatedSitemap=false;
	
	boolean robotsCheckHasRobotsTxt=false;
	List<String> robotsChecksitemaps;

	List<String> sitemapCheck;
	
	public SitemapForm() {
	} 

	public SitemapForm(HttpServletRequest req) {
		super(req.getParameter("sitemapURL"));
		operation=req.getParameter("operation");
	}

	public boolean isUriChecked() {
		return uriChecked;
	}

	public void check() {
		try {
			uriChecked=true;
			message=validateUri();
			if(message!=null)
				return;
			if(validatedSitemap) {
				sitemapCheck=new ArrayList<String>();
				sitemapCheck.add(validateSitemap(uri));
			} else { //domain
				Matcher m = domainNamePattern.matcher(uri);
				if(m.find()) {
					robotsChecksitemaps=new ArrayList<String>();
					String domainName = m.group(2);
					String robotsFile=HttpUtil.makeRequestForContent("http://"+domainName+"/robots.txt");
					robotsCheckHasRobotsTxt=robotsFile!=null;
					List<String> sitemaps=null;
					if(robotsCheckHasRobotsTxt) {
						sitemaps=listSitemaps(robotsFile);
						if(sitemaps.isEmpty()) {
							String sitemapFile=HttpUtil.makeRequestForContent("http://"+domainName+"/sitemap.xml");
							if(sitemapFile!=null)
								sitemaps.add("http://"+domainName+"/sitemap.xml");
						}
						for(String smUrl:sitemaps) {
							robotsChecksitemaps.add(smUrl+" - " + validateSitemap(smUrl));
						}
					}  
					if(sitemaps==null || sitemaps.isEmpty())
						robotsChecksitemaps.add("No sitemap.xml file was found. Locations searched: "+ "http://"+domainName+"/robots.txt"+" and "+"http://"+domainName+"/sitemap.xml"+".");
				}
			}
		} catch (AccessException | InterruptedException | IOException e) {
			message="An error occured during validation. Error message: "+e.getMessage();
		}
	}

	private String validateSitemap(String uri) throws AccessException, InterruptedException {
		HttpRequest req = HttpUtil.makeRequest(uri);
		if (req.getResponseStatusCode() != 200) {
			return "Error accessing sitemap.xml (HTTP code "+req.getResponseStatusCode();
		}
		try {
			AbstractSiteMap siteMap = SitemapUtil.parseSiteMap(req.getMimeType(), req.getResponseContent(), uri);
			return("sitemap.xml accessed correctly.");
		} catch (Exception e) {
			return("sitemap.xml file could not be parsed. Error message: "+e.getMessage());				
		}
	}

	@Override
	public String validateUri() {
		String uriVal = super.validateUri();
		if(uriVal!=null) {
			Matcher m = domainNamePattern.matcher(uri);
			if(m.find()) {
				validatedDomain=true;
				return null;
			}			
		}else {
			validatedSitemap=true;
			return null;
		}
		return "Invalid URL or domain name";
	}



	
	private List<String> listSitemaps(String robotsFile) {
		List<String> ret=new ArrayList<String>();
		try {
			BufferedReader r=new BufferedReader(new StringReader(robotsFile));
			for(String l=r.readLine() ; l!=null ; l=r.readLine()) {
				Matcher m = sitemapInRobotsTxtPattern.matcher(l);
				if(m.find()) {
					String sitemap = m.group(1);
					ret.add(sitemap);
				}
			}
			r.close();
			return ret;
		} catch (IOException e) {
			//never happens on a string reader
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public boolean isValidatedDomain() {
		return validatedDomain;
	}

	public boolean isValidatedSitemap() {
		return validatedSitemap;
	}

	public boolean isRobotsCheckHasRobotsTxt() {
		return robotsCheckHasRobotsTxt;
	}

	public List<String> getRobotsChecksitemaps() {
		return robotsChecksitemaps;
	}

	public List<String> getSitemapCheck() {
		return sitemapCheck;
	}

	
}
