package inescid.dataaggregation.dataset.detection;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;

import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.store.Repository;

public class DataProfileDetectorFromHttpHeaders {

	private DataProfileDetectorFromHttpHeaders() {
	}
	
	public static DataTypeResult detect(String datasetUri, Repository repo) throws IOException{
		int idx=0;
		for(String recUri: repo.getIterableOfResourceUris(datasetUri)) {
			List<Entry<String, String>> meta = repo.getMeta(datasetUri, recUri);
			DataTypeResult detect = detect(meta);
			if (detect!=null)
				return detect;
			if(idx>50)
				break;
		}
		return null;
	}
	public static DataTypeResult detect(List<Entry<String, String>> headers){
		String contentType=null;
		String link=null;
		for(Entry<String, String> h: headers) {
			if(h.getKey().equals("Content-Type")) {
				contentType=h.getValue();
				if(contentType!=null && link!=null)
					break;
			} else if(h.getKey().equals("Link")) {
				link=h.getValue();
				if(contentType!=null && link!=null)
					break;
			} 
		}
		
		if(contentType==null) 
			return null;
		
		DataTypeResult ret=null;
		ContentTypes mimeEnum = ContentTypes.fromMime(contentType);
		if(mimeEnum!=null) {
			ret=new DataTypeResult(mimeEnum);
			switch (mimeEnum) {
			case JSON:
				if(!StringUtils.isEmpty(link)) {
					HeaderElement hEls = BasicHeaderValueParser.parseHeaderElement(link, null);
					NameValuePair rel=hEls.getParameterByName("rel");
					if(rel!=null && rel.getValue().equals("http://www.w3.org/ns/json-ld#context")) {
						DatasetProfile d=DatasetProfile.fromNamespace(hEls.getValue());
						ret.profile=d==null ? DatasetProfile.ANY_TRIPLES : d;
					}
				}
				break;
			case JSON_LD:
				ret.profile= DatasetProfile.ANY_TRIPLES;
			case XML:
				break;
			}
		}
		return ret;
	}
	
}
