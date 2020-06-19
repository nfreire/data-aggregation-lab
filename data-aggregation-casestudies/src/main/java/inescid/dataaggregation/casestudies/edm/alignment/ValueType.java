package inescid.dataaggregation.casestudies.edm.alignment;

import java.util.Set;

public enum ValueType {
	Resource (true, false, false), Uri(false, true, false), Literal (false, false, true),
	ResourceOrUri (true, true, false), UriOrLiteral(false, true, true), ResourceOrLiteral(true, true, true);
	
	boolean allowsResource;
	boolean allowsUri;
	boolean allowsLiteral;

	private ValueType(boolean allowsResource, boolean allowsUri, boolean allowsLiteral) {
		this.allowsResource = allowsResource;
		this.allowsUri = allowsUri;
		this.allowsLiteral = allowsLiteral;
	}
	public boolean allowsResource(){
		return allowsResource;
	}
	public boolean allowsUri() {
		return allowsUri;
		
	}
	public boolean allowsLiteral() {
		return allowsLiteral;		
	}
}
