package eu.europeana.research.iiif.profile.model;

import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class SeeAlso {
	@SerializedName("@id")
	public String id;
	public String format;
	public String profile;
	public JsonElement label;


	public String getLabelString() {
		if(label.isJsonObject()) {
			JsonObject labelObj = label.getAsJsonObject();
			if(labelObj.get("@value")==null){
				for(Entry<String, JsonElement> lang: labelObj.entrySet())
					return lang.getValue().getAsString();
				return "";
			} else
				return labelObj.get("@value").getAsString();			
		} else 
			return label.getAsString();
	}


	public boolean matches(String format, String profile) {
		if(!format.equals(this.format))
			return false;
		if(StringUtils.isEmpty(profile)) {
			if(!StringUtils.isEmpty(this.profile))
				return false;
		} else {
			if(StringUtils.isEmpty(this.profile))
				return false;
			if(!profile.equals(this.profile))
				return false;
		}
		return true;
	}
}
