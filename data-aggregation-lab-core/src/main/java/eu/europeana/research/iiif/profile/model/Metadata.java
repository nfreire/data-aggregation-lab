package eu.europeana.research.iiif.profile.model;

import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Metadata {
	public JsonElement label;
	public JsonElement value;
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
}
