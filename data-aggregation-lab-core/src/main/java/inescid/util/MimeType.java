package inescid.util;

public enum MimeType {
	HTML("text/xml"), XHTML("application/xhtml+xml"), XML("application/xml"), RDF_XML("application/rdf+xml"), TURTLE("text/turtle"), JSON("application/json"), JSONLD("application/ld+json");
	
	String id;
	
	private MimeType(String id) {
		this.id=id;
	}
	
	public static boolean isRdf(String mimeTypeId) {
		return mimeTypeId.equals(RDF_XML.id) || mimeTypeId.equals(TURTLE.id) || mimeTypeId.equals(JSONLD.id);
	}

	public static boolean isData(String mimeTypeId) {
		return isRdf(mimeTypeId) || mimeTypeId.equals(XML.id) || mimeTypeId.equals(JSON.id);
	}
	
	public String id() {
		return id;
	}
	
	public static MimeType fromId(String id) {
		if(id==null) return null;
		for(MimeType mt: values()) {
			if(mt.id==id)
				return mt;
		}
		return null;
	}
}
