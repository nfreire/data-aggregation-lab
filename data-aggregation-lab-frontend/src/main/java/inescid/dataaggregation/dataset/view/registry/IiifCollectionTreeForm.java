package inescid.dataaggregation.dataset.view.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import eu.europeana.research.iiif.crawl.IiifCollectionTree;
import freemarker.template.SimpleDate;
import freemarker.template.Template;
import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.job.JobLog;
import inescid.dataaggregation.dataset.view.GlobalFrontend;

public class IiifCollectionTreeForm extends DatasetForm{
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(IiifCollectionTreeForm.class);

	IiifCollectionTree topCollection;
	public IiifCollectionTreeForm(HttpServletRequest req, Dataset dataset) {
		super(dataset);
		String paramUri = req.getParameter("topCollectionUri");
		if(!StringUtils.isEmpty(paramUri)) {
			setTopCollectionUri(paramUri);			
		} else if(dataset!=null) {
			setTopCollectionUri(dataset.getUri());
		} else {
			topCollection=new IiifCollectionTree();
		}
		
		try {
			if(!StringUtils.isEmpty(topCollection.getCollectionUri())){
				topCollection.fetchFromPresentationApi(true);
			}
		} catch (IOException e) {
			message="Request to IIIF service failed. Reason: " +e.getMessage();
			log.info(e.getMessage(), e);
			topCollection= new IiifCollectionTree();
		} catch (InterruptedException e) {
			message="Request to IIIF service failed. Please retry.";
		}
	}

	public IiifCollectionTreeForm(Dataset dataset) throws IOException {
		super(dataset);
	}
	
	public void setTopCollectionUri(String uri) {
		topCollection=new IiifCollectionTree(uri);
	}
	
	public String getTopCollectionUri() {
		return topCollection==null ? null : topCollection.getCollectionUri();
	}

	
	public String getCollectionTree() {
		StringBuilder sb=new StringBuilder();
		getCollectionTree(sb, topCollection, 0);
		return sb.toString();
	}
	public void getCollectionTree(StringBuilder sb, IiifCollectionTree collection, int sublevel) {
		try {
			for(int i=0; i<=sublevel; i++) 
				sb.append("&nbsp;&nbsp;&nbsp;");
			sb.append(" - ");
			sb.append(collection.getLabel()).append(" (<a href=\"iiif-dataset-register?registration=import&"
					+ "uri="+URLEncoder.encode(collection.getCollectionUri(), "UTF-8")+
					"&title="+(collection.getLabel()==null? "" : URLEncoder.encode(collection.getLabel(), "UTF-8"))+
					"&organization="+(collection.getAttribution()==null? "" : URLEncoder.encode(collection.getAttribution(), "UTF-8"))+
					"&type=iiif&crawlMethod=COLLECTION\">register this collection</a>)</br>\n");
			for(IiifCollectionTree sc: collection.getSubcollections()) {
				getCollectionTree(sb, sc, sublevel+1);
			}
			if(collection.getSubcollections().isEmpty() && collection.getHasSubCollectionsButNotRetrieved()!=null && collection.getHasSubCollectionsButNotRetrieved()) {
				for(int i=0; i<=sublevel+1; i++) 
					sb.append("&nbsp;&nbsp;&nbsp;");
				sb.append(" ... (<a href=\"browse-iiif-service-collections?topCollectionUri="+URLEncoder.encode(collection.getCollectionUri(), "UTF-8")+"\">browse</a>)</br>\n");
			}
		} catch (UnsupportedEncodingException e) {
			//UTF8 is always supported
		}	
	}
	@Override
	public String output() throws Exception {
		StringWriter w=new StringWriter();
		Template temp = GlobalFrontend.FREE_MARKER.getTemplate("datasetregistry/"+getClass().getSimpleName()+".html");
		temp.process(this, w);
		w.close();
		return w.toString();
	}
	
	public List<IiifCollectionTree> getSubCollections() {
		return topCollection.getSubcollections();
	}
	
	public String getTopCollectionLabel() {
		return topCollection.getLabel();
	}
	
	@Override
	public boolean validate() {
		if(topCollection==null || StringUtils.isEmpty(topCollection.getCollectionUri()))
			message="Please enter the URL of a IIIF Collection";
		return true;
	}

	@Override
	public boolean register() {
		return false;
	}
}