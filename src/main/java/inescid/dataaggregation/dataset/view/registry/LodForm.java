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

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateNotFoundException;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.LodDataset;

public class LodForm extends DatasetForm {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(LodForm.class);
	
	public LodForm() {
		super(new LodDataset());
	}

	public LodForm(HttpServletRequest req) {
		super(new LodDataset());
		((LodDataset)dataset).setUri(req.getParameter("uri"));
		dataset.setOrganization(req.getParameter("organization"));
		dataset.setTitle(req.getParameter("title"));
	}

	public void setUri(String uri) {
		dataset.setUri(uri);
	}

	public String getUri() {
		return dataset.getUri();
	}
	
	@Override
	public boolean validate() {
		ArrayList<String> errors=new ArrayList<>();
		try {
			if(StringUtils.isEmpty(((LodDataset)dataset).getUri())) {
				errors.add("Provide a URI");
			}else if(!Global.urlPattern.matcher(dataset.getUri()).matches()) 
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
