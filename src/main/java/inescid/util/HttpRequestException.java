package inescid.util;

public class HttpRequestException extends Exception {
	private static final long serialVersionUID = 1L;
	
	int httpStatus;
	String resourceUri;
	
	public HttpRequestException(String resourceUri, Throwable cause) {
		super(resourceUri+" - "+cause.getMessage(), cause);
		httpStatus=-1;
		this.resourceUri=resourceUri;
	}
	
	public HttpRequestException(String resourceUri, int httpResponseStatus) {
		super(resourceUri+" responded with http status: "+httpResponseStatus);
		httpStatus=httpResponseStatus;
		this.resourceUri=resourceUri;
	}

	public HttpRequestException(String resourceUri, String message) {
		super(resourceUri+" - "+message);
		this.resourceUri=resourceUri;
		httpStatus=-1;
	}

	@Override
	public String toString() {
		return "Response to RDF resource "+ resourceUri+" returned status "+httpStatus;	
	}
}
