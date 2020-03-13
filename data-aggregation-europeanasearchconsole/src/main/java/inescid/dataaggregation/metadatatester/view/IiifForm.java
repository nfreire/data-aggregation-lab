package inescid.dataaggregation.metadatatester.view;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class IiifForm extends UriForm {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(IiifForm.class);

	String operation=null;
	boolean uriChecked=false;

	List<IiifSeeAlsoView> seeAlsos;
	
	public IiifForm() {
	} 

	public IiifForm(HttpServletRequest req) {
		super(req.getParameter("manifestURI"));
		operation=req.getParameter("operation");
	}

	public boolean isUriChecked() {
		return uriChecked;
	}

//	public void setUriChecked(boolean uriChecked) {
//		this.uriChecked = uriChecked;
//	}

	public void checkUri() {
		uriChecked=true;
		message=validateUri();
		if(message!=null)
			return;
		try {
			seeAlsos=IiifManifestTester.testManifest(uri);
		} catch (InterruptedException | IOException e) {
			message="Could not access IIIF Manifest: "+e.getMessage();
		}
	}

	public List<IiifSeeAlsoView> getSeeAlsos() {
		return seeAlsos;
	}
	
}
