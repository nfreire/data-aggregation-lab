package inescid.dataaggregation.crawl.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.params.HttpParams;

import inescid.util.AccessException;

public class HttpResponse {
	private byte[] body; 
	private InputStream bodyStream; 
	CloseableHttpResponse closeableHttpResponse;
	private List<Entry<String, String>> headers;
	private int status=-1;
	public HttpResponse(byte[] body, List<Entry<String, String>> headers, int status) {
		super();
		this.body = body;
		this.headers = headers;
		this.status = status;
	}
	public HttpResponse(InputStream body, List<Entry<String, String>> headers, int status) {
		super();
		this.bodyStream = body;
		this.headers = headers;
		this.status = status;
	}
	public HttpResponse(CloseableHttpResponse closeableHttpResponse, boolean stream) throws IOException {
		headers=new ArrayList<>(5);
		for(String headerName: new String[] { "Content-Type", "Content-Encoding", "Content-Disposition"/*, "Content-MD5"*/}) {
			for(Header h : closeableHttpResponse.getHeaders(headerName)) 
				headers.add(new SimpleEntry<String, String>(h.getName(), h.getValue()));
		}
		if(!stream) {
			if(closeableHttpResponse.getEntity()!=null)
				body = IOUtils.toByteArray(closeableHttpResponse.getEntity().getContent());
			closeableHttpResponse.close();
			bodyStream=null;
		}else {
			this.closeableHttpResponse=closeableHttpResponse;
			bodyStream=closeableHttpResponse.getEntity().getContent();
		}
		status=closeableHttpResponse.getStatusLine().getStatusCode();
	}
	public String getHeader(String headerToGet) {
		for (Entry<String, String> h : headers) {
			if(h.getKey().equalsIgnoreCase(headerToGet))
				return h.getValue();
		}
		return null;
	}
	public boolean isSuccess() {
		return status==200 && hasBody();
	}
	public boolean hasBody() {
		return body!=null && body.length>0;
	}
	public String getContentType() {
		return getHeader("Content-Type");
	}
	public ContentType getContentTypeParsed() {
		String header = getHeader("Content-Type");
		if(header==null)
			return null;
		return ContentType.parse(header);
	}
	public AccessException throwException(String resourceUrl) {
		String msg=null;
		if (hasBody()) {
			try {
				msg=new String(body, "UTF8");
				if(msg.length()>300)
					msg=msg.substring(0, 300);
			} catch (UnsupportedEncodingException e) { msg=null; }
		}
		return new AccessException(resourceUrl, status, msg);
	}
	public byte[] getBody() {
		return body;
	}
	public void setBody(byte[] body) {
		this.body = body;
	}
	
	public List<Entry<String, String>> getHeaders() {
		return headers;
	}
	public void setHeaders(List<Entry<String, String>> headers) {
		this.headers = headers;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public void close() throws IOException {
		if(closeableHttpResponse!=null)
			closeableHttpResponse.close();
	}
	public InputStream getBodyStream() {
		return bodyStream == null ? (body==null ? null : new ByteArrayInputStream(body)) : bodyStream;
	}
	public void setBodyStream(InputStream bodyStream) {
		this.bodyStream = bodyStream;
	}
}