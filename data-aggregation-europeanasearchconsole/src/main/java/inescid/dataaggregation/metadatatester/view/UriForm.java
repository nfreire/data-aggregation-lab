package inescid.dataaggregation.metadatatester.view;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import inescid.dataaggregation.dataset.Global;

public abstract class UriForm extends View {

	protected String uri=null;
	
	public UriForm() {
	}
	
	public UriForm(String uri) {
		super();
		this.uri = uri;
	}

	public void setUri(String uri) {
		this.uri=uri;
	}

	public String getUri() {
		return uri;
	}

	public String validateUri() {
		if(StringUtils.isEmpty(uri)) {
			return "Provide a URI";
		}else if(!Global.urlPattern.matcher(uri).matches()) 
			return("The URI is in an invalid format");
		else {
			try {
				new URL(uri);
			} catch (MalformedURLException e) {
				return "The URI is in an invalid format: "+e.getMessage();
			}
		}
		return null;
	}
}