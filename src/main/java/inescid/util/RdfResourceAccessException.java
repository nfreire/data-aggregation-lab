package inescid.util;

public class RdfResourceAccessException extends Exception {
	private static final long serialVersionUID = 1L;
	
	int httpStatus;
	String resourceUri;
	
	public RdfResourceAccessException(String resourceUri, Throwable cause) {
		super(resourceUri+" - "+cause.getMessage(), cause);
		httpStatus=-1;
		this.resourceUri=resourceUri;
	}
	
	public RdfResourceAccessException(String resourceUri, int httpResponseStatus) {
		super(resourceUri+" responded with http status: "+httpResponseStatus);
		httpStatus=httpResponseStatus;
		this.resourceUri=resourceUri;
	}

	public RdfResourceAccessException(String resourceUri, String message) {
		super(resourceUri+" - "+message);
		this.resourceUri=resourceUri;
		httpStatus=-1;
	}

	@Override
	public String toString() {
		return "Response to RDF resource "+ resourceUri+" returned status "+httpStatus;	
	}
}
