package inescid.dataaggregation.dataset.view.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.WwwDataset;
import inescid.dataaggregation.dataset.WwwDataset.Microformat;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;
import inescid.dataaggregation.dataset.job.JobRunner;
import inescid.dataaggregation.dataset.view.GlobalFrontend;
public class WwwForm extends DatasetForm {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(WwwForm.class);
	
	public WwwForm() {
		super(new WwwDataset());
	}

	public WwwForm(HttpServletRequest req) {
		super(new WwwDataset());
		WwwDataset dataset=(WwwDataset) super.dataset;
		dataset.setUri(req.getParameter("uri"));
		try {
			dataset.setMicroformat(Microformat.valueOf( req.getParameter("microformat")));
		} catch (Exception e) {
			System.err.println("WARNING: " + e.getMessage());
			e.printStackTrace();
		}
		dataset.setOrganization(req.getParameter("organization"));
		dataset.setTitle(req.getParameter("title"));
	}

	public void setUri(String uri) {
		((IiifDataset)dataset).setUri(uri);
	}


	public String getUri() {
		return dataset.getUri();
	}
	
	public String getMicroformat() {
		return ((WwwDataset)dataset).getMicroformat().toString();
	}
	
	@Override
	public boolean register() {
		return dataset!=null && !StringUtils.isEmpty(dataset.getUri());
	}
	
	@Override
	public boolean validate() {
		ArrayList<String> errors=new ArrayList<>();
		try {
			if(StringUtils.isEmpty(((WwwDataset)dataset).getUri())) {
				errors.add("Provide a URI");
			}else if(!Global.urlPattern.matcher(((WwwDataset)dataset).getUri()).matches()) 
				errors.add("The URI is in an invalid format");
			else {
				if(null!=Global.getDatasetRegistryRepository().getDatasetByUri(dataset.getUri()))
					errors.add("The URI of this dataset is already registered");
			}
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
