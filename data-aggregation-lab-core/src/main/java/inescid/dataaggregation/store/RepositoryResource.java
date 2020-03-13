package inescid.dataaggregation.store;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class RepositoryResource {
	String uri;
	File contentFile;
	
	public RepositoryResource(String uri, File content) {
		super();
		this.uri = uri;
		this.contentFile = content;
	}
	
	public String getUri() {
		return uri;
	}
	
	public byte[] getContent() throws IOException {
		return FileUtils.readFileToByteArray(contentFile);
	}
	
	
	
}
