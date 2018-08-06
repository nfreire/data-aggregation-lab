package inescid.util;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class DataProfileDetector {
	static final Pattern xmlDecl=Pattern.compile("^([^\\s\\n\\r]{3}[\\s\\n\\r]*|[\\s\\n\\r]*)<\\?xml ");
	static final Pattern jsonPattern=Pattern.compile("^([^\\s\\n\\r]{3})?[\\s\\n\\r]*\\{.*\\}[\\s\\n\\r]*$");

	String contentType;
	String profile;
	
	public boolean detect(File source) throws IOException {
		String inString = FileUtils.readFileToString(source, "UTF-8");
		return detect(inString);
	}
	
	public boolean detect(String inString) {
		if(xmlDecl.matcher(inString).find())
			contentType="text/xml";
		else if(jsonPattern.matcher(inString).matches())
			contentType="application/json";
		else
			contentType=null;
		return contentType!=null;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getProfile() {
		return profile;
	}
	
	
	public static void main(String[] args) {
		String XML_BOM_STRING = "ï»¿<?xml version=...";
		String XML_BOM_SPC_STRING = "ï»¿  <?xml version=...";
		String XML_SPC_STRING = "  \n <?xml vaersion=...";
		String XML_STRING = "<?xml version=...";

		String JSON_STRING = "{ ... }\n";
		
		DataProfileDetector dtc=new DataProfileDetector();
		dtc.detect(XML_BOM_SPC_STRING);
		System.out.println( dtc.getContentType());
		dtc.detect(XML_BOM_SPC_STRING);
		System.out.println( dtc.getContentType());
		dtc.detect(XML_SPC_STRING);
		System.out.println( dtc.getContentType());
		dtc.detect(XML_STRING);
		System.out.println( dtc.getContentType());
		dtc.detect(JSON_STRING);
		System.out.println( dtc.getContentType());
		
		
		
	}

	public String getFilenameExtension() {
		if (contentType.equals("text/xml"))
			return "xml";
		if (contentType.equals("application/json"))
			return "json";
		return "";
	}
}
