package inescid.dataaggregation.dataset.view.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateNotFoundException;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.view.Global;
import inescid.util.AccessException;
import inescid.util.LinkedDataUtil;
import inescid.util.RdfUtil;

public class LodForm extends DatasetForm {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(LodForm.class);

	String operation=null;
	boolean uriChecked=false;
	
	public LodForm() {
		super(new LodDataset());
	}

	public LodForm(HttpServletRequest req) {
		super(new LodDataset());
		((LodDataset)dataset).setUri(req.getParameter("uri"));
		dataset.setOrganization(req.getParameter("organization"));
		dataset.setTitle(req.getParameter("title"));
		operation=req.getParameter("registration");
		String paramUriChecked=req.getParameter("uriChecked");
		uriChecked=StringUtils.isEmpty(paramUriChecked) ? false : Boolean.parseBoolean(paramUriChecked);
	}

	public void setUri(String uri) {
		dataset.setUri(uri);
	}

	public String getUri() {
		return dataset.getUri();
	}
	
	
	@Override
	public boolean register() {
		if(dataset==null || operation==null)
			return false;
		if(operation.equals("Send registration of dataset"))
			return true;
		if(operation.equals("Check URI")) {
			try {
				message=validateUri();
				if(message==null) {
					Resource  dsResource = LinkedDataUtil.getResource(dataset.getUri());
					Statement stmTitle = dsResource.getProperty(RdfReg.SCHEMAORG_NAME);
					if (stmTitle!=null) {
						dataset.setTitle(stmTitle.getObject().asLiteral().getString());
					} else { 
						stmTitle = dsResource.getProperty(RdfReg.DCTERMS_TITLE);
						if (stmTitle!=null) 
							dataset.setTitle(stmTitle.getObject().asLiteral().getString());
					}
					Statement stmProvider = dsResource.getProperty(RdfReg.SCHEMAORG_PROVIDER);
					if (stmProvider!=null) {
						dataset.setOrganization(stmProvider.getObject().asLiteral().getString());
					} 			
					uriChecked=true;
				}
			} catch (InterruptedException e) {
				message=("The processing of your request was canceled. Please try again.");				
				uriChecked=false;
			} catch (IOException e) {
				message=("URI unreachable: "+e.getMessage());
				uriChecked=false;
			} catch (AccessException e) {
				message=("URI unreachable: "+e.getMessage());
				uriChecked=false;
			}
			return false;
		}
		return true;
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

	public boolean isUriChecked() {
		return uriChecked;
	}

	public void setUriChecked(boolean uriChecked) {
		this.uriChecked = uriChecked;
	}
	
}
