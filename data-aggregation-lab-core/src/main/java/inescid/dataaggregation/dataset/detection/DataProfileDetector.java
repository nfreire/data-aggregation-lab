package inescid.dataaggregation.dataset.detection;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.store.Repository;
import inescid.util.XmlUtil;

public class DataProfileDetector { 
	static final Pattern xmlDecl=Pattern.compile("^([^\\s\\n\\r]{3}[\\s\\n\\r]*|[\\s\\n\\r]*)<\\?xml ");
	static final Pattern jsonPattern=Pattern.compile("^([^\\s\\n\\r]{3})?[\\s\\n\\r]*\\{.*\\}[\\s\\n\\r]*$", Pattern.DOTALL);

	
	public DataTypeResult detect(File source) throws IOException {
		if(!source.exists())
			return null;
		String inString = FileUtils.readFileToString(source, "UTF-8");
		return detect(inString);
	}
	
	public DataTypeResult detect(String inString) {
		DataTypeResult detected=new DataTypeResult();
		detected.format=null;
		if(xmlDecl.matcher(inString).find())
			detected.format=ContentTypes.fromMime("text/xml");
		else if(jsonPattern.matcher(inString).matches()) {
			Boolean checkJsonLd=checkJsonLd(inString);
			if(checkJsonLd!=null) {
				if(checkJsonLd)
					detected.format=ContentTypes.fromMime("application/ld+json");
				else
					detected.format=ContentTypes.fromMime("application/json");
			}
		} 
		if(detected.format!=null) {
			switch (detected.format) {
			case JSON_LD:
				detected.profile=detectProfileFromJsonLd(inString);
				break;
			case XML:
				detected.profile=detectProfileFromXml(inString);
				break;
			}
		}
		return detected;
	}
	
	private DatasetProfile detectProfileFromXml(String inString) {
		Document dom = XmlUtil.parseDom(new StringReader(inString));
		String ns = dom.getDocumentElement().getNamespaceURI();
		if(ns.equals(Rdf.NS)) {
			NodeList chos = dom.getDocumentElement().getElementsByTagNameNS(Edm.NS, Edm.ProvidedCHO.getLocalName());
			if(chos!=null && chos.getLength()>0)
				return DatasetProfile.EDM;
		}
		return null;
	}
	private DatasetProfile detectProfileFromJsonLd(String inString) {
		JsonReader jr=new JsonReader(new StringReader(inString));
		try {
			try {
				jr.beginObject();
			} catch (IllegalStateException e) {
				return DatasetProfile.ANY_TRIPLES;
			}
			while(jr.peek()!=JsonToken.END_OBJECT){
				String field = jr.nextName();
				if(field.equals("@context")) {
					if(jr.peek()==JsonToken.BEGIN_ARRAY){
						jr.beginArray();
						if(jr.peek()==JsonToken.STRING)
							 return DatasetProfile.fromNamespace(jr.nextString());
					}else if(jr.peek()==JsonToken.STRING)
						return DatasetProfile.fromNamespace(jr.nextString());
					return DatasetProfile.ANY_TRIPLES;
				}
			}
			jr.close();
		} catch (IOException e) {
			//ignore
		}
		return DatasetProfile.ANY_TRIPLES;
	}

	private Boolean checkJsonLd(String inString) {
		JsonReader jr=new JsonReader(new StringReader(inString));
		Boolean onlyUriNames=null;
		try {
			try {
				jr.beginObject();
			} catch (IllegalStateException e) {
				return false;
			}
			while(jr.peek()!=JsonToken.END_OBJECT){
				String field = jr.nextName();
				if(field.equals("@context")) {
					return true;
				} else if(!field.startsWith("http")) {
					onlyUriNames=false;
				} else if(onlyUriNames==null) 
					onlyUriNames=true;
			}
			jr.close();
			if(onlyUriNames!=null && onlyUriNames)
				return true;
		} catch (IOException e) {
			//ignore
		}
		return null;
	}

	public static void main(String[] args) {
		String XML_BOM_STRING = "ï»¿<?xml version=...";
		String XML_BOM_SPC_STRING = "ï»¿  <?xml version=...";
		String XML_SPC_STRING = "  \n <?xml vaersion=...";
		String XML_STRING = "<?xml version=...";

		String JSON_STRING = "{ ... }\n";
		
		DataProfileDetector dtc=new DataProfileDetector();
		DataTypeResult result = dtc.detect(XML_BOM_SPC_STRING);
		System.out.println( result.format);
		result = dtc.detect(XML_BOM_SPC_STRING);
		System.out.println( result.format);
		result = dtc.detect(XML_SPC_STRING);
		System.out.println( result.format);
		result = dtc.detect(XML_STRING);
		System.out.println( result.format);
		result = dtc.detect(JSON_STRING);
		System.out.println( result.format);
	}

	public static DataTypeResult detect(String datasetUri, Repository repo) throws IOException{
//		public static DataTypeResult detect(Dataset dataset, Repository repo) throws IOException{
//			String datasetUri=dataset.getUri();
		DataTypeResult detectedInHeaders=DataProfileDetectorFromHttpHeaders.detect(datasetUri, repo);
		if(detectedInHeaders!=null && detectedInHeaders.profile!=null) {
			if (detectedInHeaders.profile!=DatasetProfile.ANY_TRIPLES) 
				return detectedInHeaders;
		
			DataProfileDetector detector = new DataProfileDetector();
			int idx=0;
			for(String recUri: repo.getIterableOfResourceUris(datasetUri)) {
				DataTypeResult detected = detector.detect(repo.getFile(datasetUri, recUri));
				if (detected!=null && detected.profile!=null) 
					return detected;
				if(idx>50)
					break;
			}
		} else {
			
		}
		return detectedInHeaders;
	}
	
}
