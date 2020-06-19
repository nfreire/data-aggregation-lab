package inescid.dataaggregation.metadatatester.view;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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

import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.metadatatester.view.ResourceView.DataModel;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class SchemaorgForm extends UriForm {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(SchemaorgForm.class);

	String operation=null;
	boolean uriChecked=false;
	boolean uriValidated=false;
	
	List<ResourceView> creativeWorks;
	List<ResourceView> otherResources;
	
	public SchemaorgForm() {
	} 

	public SchemaorgForm(HttpServletRequest req) {
		super(req.getParameter("webpageURL"));
		operation=req.getParameter("operation");
	}

	public boolean isUriChecked() {
		return uriChecked;
	}

	public void setUriChecked(boolean uriChecked) {
		this.uriChecked = uriChecked;
	}

	public void checkUri() {
		uriChecked=true;
		message=validateUri();
		if(message!=null)
			return;
		loadAndExtractDataFromUri();
	}

	private void loadAndExtractDataFromUri() {
		creativeWorks=new ArrayList<ResourceView>();
		otherResources=new ArrayList<ResourceView>();		
		
		final Any23 any23=new Any23(new ExtractorGroup(new ArrayList() {{ 
			add(new EmbeddedJSONLDExtractorFactory());
			add(new RDFa11ExtractorFactory());
//			add(new MicrodataExtractorFactory());
//			add(new HTMLMetaExtractorFactory());
		}}));
		try {				
			HttpRequest request = new HttpRequest(uri);
			request.fetch();
			if(request.getResponseStatusCode()==200) {
				Model model=Jena.createModel();
				any23.extract(request.getResponseContentAsString(), request.getUrl(), request.getMimeType(), 
						request.getCharset()!=null ? request.getCharset().name() : StandardCharsets.UTF_8.name(),
								new JenaTripleHandler(model));
				
				checkForLinkedResources(model);
				
				for(Resource res: model.listSubjects().toList()) {
					ResourceView resView=new ResourceView(res, DataModel.Schemaorg);
					if(!resView.hasProperties()) continue;
					if(resView.isCreativeWork())
						creativeWorks.add(resView);
					else
						otherResources.add(resView);
				}
			} else {
				message="Could not access URL (HTTP statos code: "+request.getResponseStatusCode()+")";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExtractionException e) {
			e.printStackTrace();
		}
	}


	private void checkForLinkedResources(Model model) {
//		for (Resource res : model.listResourcesWithProperty(RegRdf.type, RegSchemaorg.WebPage).toList()) {
			for (Statement stAbout : model.listStatements(null, Schemaorg.about, (RDFNode)null).toList()) {
//				for (Statement stAbout : res.listProperties(RegSchemaorg.about).toList()) {
				if (stAbout.getObject().isURIResource()) {
//					if(!stAbout.getObject().asResource().listProperties().hasNext()) {
						try {
							Resource readRdfResourceFromUri = RdfUtil.readRdfResourceFromUri(stAbout.getObject().asResource().getURI());
							model.add(readRdfResourceFromUri.listProperties());
						} catch (AccessException | InterruptedException | IOException e) {
							//ignore and proceed
						}
//					}
				}
			}
			for (Statement stAbout : model.listStatements(null, Schemaorg.sameAs, (RDFNode)null).toList()) {
//			for (Statement stAbout : res.listProperties(RegSchemaorg.sameAs).toList()) {
				if (stAbout.getObject().isURIResource()) {
//					if(!stAbout.getObject().asResource().listProperties().hasNext()) {
						try {
							Resource readRdfResourceFromUri = RdfUtil.readRdfResourceFromUri(stAbout.getObject().asResource().getURI());
							model.add(readRdfResourceFromUri.listProperties());
						} catch (AccessException | InterruptedException | IOException e) {
							//ignore and proceed
						}
//					}
				}
			}
//		}
	}

	public List<ResourceView> getCreativeWorks() {
		return creativeWorks;
	}

	public List<ResourceView> getOtherResources() {
		return otherResources;
	}

	public void validateSchemaorgUri() {
		uriChecked=true;
		uriValidated=true;
		message=validateUri();
		if(message!=null)
			return;
		loadAndExtractDataFromUri();
		for(ResourceView res: creativeWorks) {
			res.setValidation(SchemaorgChoValidator.validate(res.getResource()));
		}
	}

	public boolean isUriValidated() {
		return uriValidated;
	}
}
