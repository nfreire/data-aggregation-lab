package inescid.dataaggregation.dataset.view.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HEAD;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.StringUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import eu.europeana.research.iiif.crawl.Collection;
import eu.europeana.research.iiif.discovery.model.IiifSeeAlsoReference;
import eu.europeana.research.iiif.discovery.model.OrderedCollection;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.crawl.http.UrlRequest;
import inescid.dataaggregation.crawl.http.UrlRequest.HttpMethod;
import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.WwwDataset.CrawlMethod;
import inescid.dataaggregation.dataset.detection.DataProfileDetector;
import inescid.dataaggregation.dataset.detection.DataTypeResult;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;
import inescid.dataaggregation.dataset.job.JobRunner;
import inescid.dataaggregation.dataset.view.GlobalFrontend;
public class IiifForm extends DatasetForm {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(IiifForm.class);
	
	String operation=null;
	
	public IiifForm() {
		super(new IiifDataset());
	}

	public IiifForm(HttpServletRequest req) {
		super(new IiifDataset());
		operation=req.getParameter("registration");
		IiifDataset dataset=(IiifDataset) super.dataset;
		dataset.setUri(req.getParameter("uri"));
		try {
			if(!StringUtils.isEmpty(req.getParameter("crawlMethod")))
					dataset.setCrawlMethod(IiifCrawlMethod.valueOf( req.getParameter("crawlMethod")));
		} catch (Exception e) {
			System.err.println("WARNING: " + e.getMessage());
			e.printStackTrace();
		}
		dataset.setOrganization(req.getParameter("organization"));
		dataset.setTitle(req.getParameter("title"));
		dataset.setMetadataUri(req.getParameter("metadataUri"));
	}

	public void setUri(String uri) {
		((IiifDataset)dataset).setUri(uri);
	}

	@Override
	public boolean register() {
		if(dataset==null || operation==null)
			return false;
		if(operation.equals("Send registration of dataset"))
			return true;
		if(operation.equals("Auto detect harvesting method")) {
			try {
				message=validateUri();
				if(message==null) {
					HttpRequest req = new HttpRequest(new UrlRequest(getUri(), "accept", "application/json, application/ld+json, application/xml"));
					req.fetch();
					String contentType = req.getResponseHeader("content-type");
					ContentTypes detectedType = null;
					DataProfileDetector dpd=new DataProfileDetector();
					if(StringUtils.isEmpty(contentType) || contentType.startsWith("text/plain")) {
						 DataTypeResult detect = dpd.detect(req.getResponseContentAsString());
						if(detect!=null)
							detectedType=detect.format;
					} else 
						detectedType=ContentTypes.fromMime(contentType);
					if(detectedType==null)
						message=("The URI content could not be detected. Please set the harvesting method manually");
					else if(detectedType==ContentTypes.XML) {
						message=("Harvesting method detected: Sitemaps");
						((IiifDataset)dataset).setCrawlMethod(IiifCrawlMethod.SITEMAP);
					} else if(detectedType==ContentTypes.JSON_LD) {
						try {
							String collectionJson = req.getResponseContentAsString();
							JsonReader jr=new JsonReader(new StringReader(collectionJson));
							try {
								jr.beginObject();
							} catch (IllegalStateException e) {
								message=("The URI content could not be detected. Please set the harvesting method manually");
							}
							while(message==null && jr.peek()!=JsonToken.END_OBJECT){
								String field = jr.nextName();
								if(field.equals("@context")) {
									String ctx = null;
									if(jr.peek()==JsonToken.BEGIN_ARRAY){
										jr.beginArray();
										while(ctx==null && jr.peek()!=JsonToken.END_ARRAY){
											ctx = jr.nextString();																		
											if(!ctx.equals("http://iiif.io/api/discovery/0/context.json") && !ctx.startsWith("http://iiif.io/api/presentation")) 
												ctx=null;
										}
									} else if(jr.peek()==JsonToken.STRING) {
										ctx = jr.nextString();									
									}
											
									if(StringUtils.isEmpty(ctx)) 
										message=("The URI content could not be detected. Please set the harvesting method manually");
									else if(ctx.equals("http://iiif.io/api/discovery/0/context.json")) {
										message=("Harvesting method detected: Change Discovery API");
										((IiifDataset)dataset).setCrawlMethod(IiifCrawlMethod.DISCOVERY);
										List<IiifSeeAlsoReference> context = OrderedCollection.getSeeAlso(collectionJson);
										if(!context.isEmpty())
											dataset.setMetadataUri(context.get(0).getId());
									} else if(ctx.startsWith("http://iiif.io/api/presentation")) {
										message=("Harvesting method detected: IIIF Collection");
										((IiifDataset)dataset).setCrawlMethod(IiifCrawlMethod.COLLECTION);		
										try {
											Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
											Collection m = gson.fromJson(collectionJson, Collection.class);
											dataset.setTitle(m.label);
											dataset.setOrganization(m.attribution);
										} catch (JsonSyntaxException e) {
											log.warn("Could not process collection json",e );
										}
									}
								} else 
									jr.skipValue();
							}
							jr.close();
						} catch (Exception e) {
							message=("The URI content could not be detected. Please set the harvesting method manually");
						}
					}
				}
			} catch (InterruptedException e) {
				message=("The processing of your request was canceled. Please try again.");				
			} catch (IOException e) {
				message=("URI unreachable: "+e.getMessage());
			}
			return false;
		}
		return false;
	}
	

	public String getUri() {
		return dataset.getUri();
	}
	
	
	public String getCrawlMethod() {
		IiifCrawlMethod crawlMethod = ((IiifDataset)dataset).getCrawlMethod();
		return crawlMethod==null ? null : crawlMethod.toString();
	}
	
	
	
	
	@Override
	public boolean validate() {
		ArrayList<String> errors=new ArrayList<>();
		try {
			String uriError=validateUri();
			if(uriError!=null)
				errors.add(uriError);
			if(StringUtils.isEmpty(dataset.getOrganization())) 
				errors.add("Provide the name or the organization");
			if(StringUtils.isEmpty(dataset.getTitle())) 
				errors.add("Provide a title for the dataset");
		} catch (IOException e) {
			errors.add("An internal error error occoured.");
			log.error(e.getMessage(), e);
		}
		if(errors.isEmpty())
			return true;
		StringBuilder sb=new StringBuilder();
		sb.append("The form contains errors. It was not possible to register the dataset. Please check the following:</br><ul>");
		for(String e: errors) {
			sb.append("\n<li>").append(e).append("</li>");
		}
		sb.append("</ul>\n");
		message=sb.toString();
		return false;
	}
}
