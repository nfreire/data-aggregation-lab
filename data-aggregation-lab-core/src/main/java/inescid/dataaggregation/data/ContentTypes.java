package inescid.dataaggregation.data;

public enum ContentTypes {
	JSON("application/json","json"), JSON_LD("application/ld+json","jsonld"), XML("application/xml","xml"), 
	RDF_XML("application/rdf+xml","rdf"), TURTLE("text/turtle","ttl");
	
	String mime;
	String filenameExtension;
	private ContentTypes(String mime, String filenameExtension) {
		this.mime = mime;
		this.filenameExtension = filenameExtension;
	}

	public String getFilenameExtension() {
		return filenameExtension;
	}
	
	public static ContentTypes fromMime(String mime) {
		if(mime.contains(";"))
			mime=mime.substring(0, mime.indexOf(';'));
		for(ContentTypes t: values()) {
			if(t.mime.equals(mime))
				return t;
		}
		if(mime.equals("application/x-turtle"))
			return TURTLE;
		return null;
	}

	public static boolean isRdf(String mime) {
		ContentTypes f;
		try {
			 f = ContentTypes.valueOf(mime);
		}catch(Exception e) {//ignore the exception
			if(mime.contains(";"))
				mime=mime.substring(0, mime.indexOf(';'));
			f = ContentTypes.fromMime(mime);
		}
		return f !=null && (f==RDF_XML || f==TURTLE || f==ContentTypes.JSON_LD);
	}
	
	public String getMimetype() {
		return mime;
	}
}