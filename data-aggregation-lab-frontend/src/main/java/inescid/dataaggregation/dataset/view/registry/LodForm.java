package inescid.dataaggregation.dataset.view.registry;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.util.AccessException;
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
					Resource  dsResource = RdfUtil.readRdfResourceFromUri(dataset.getUri());
					Statement stmTitle = dsResource.getProperty(Schemaorg.name);
					if (stmTitle!=null) {
						dataset.setTitle(stmTitle.getObject().asLiteral().getString());
					} else { 
						stmTitle = dsResource.getProperty(DcTerms.title);
						if (stmTitle!=null) 
							dataset.setTitle(stmTitle.getObject().asLiteral().getString());
					}
					Statement stmProvider = dsResource.getProperty(Schemaorg.provider);
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
