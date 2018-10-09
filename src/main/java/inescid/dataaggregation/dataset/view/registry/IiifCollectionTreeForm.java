package inescid.dataaggregation.dataset.view.registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import eu.europeana.research.iiif.crawl.IiifCollectionTree;
import freemarker.template.SimpleDate;
import freemarker.template.Template;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.detection.ContentTypes;
import inescid.dataaggregation.dataset.job.JobLog;

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
			topCollection.fetchFromPresentationApi();
		} catch (IOException e) {
			message="Request to IIIF service failed. Reason: " +e.getMessage();
			log.info(e.getMessage(), e);
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
		return getCollectionTree(sb, topCollection);
	}
	public String getCollectionTree(StringBuilder sb, IiifCollectionTree collection) {
		sb.append(" - ");
		sb.append(collection.getLabel()).append(" <a href=\"TODO\">select</a></br>");
		for(IiifCollectionTree sc: collection.getSubcollections()) {
			sb.append(getCollectionTree(sb, sc));
		}	
		return sb.toString();
	}
	@Override
	public String output() throws Exception {
		StringWriter w=new StringWriter();
		Template temp = Global.FREE_MARKER.getTemplate("datasetregistry/"+getClass().getSimpleName()+".html");
		temp.process(this, w);
		w.close();
		return w.toString();
	}
	
	@Override
	public boolean validate() {
		if(topCollection==null || StringUtils.isEmpty(topCollection.getCollectionUri()))
			message="Please enter the URL of a IIIF Collection";
		return true;
	}
}