package inescid.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class JsonUtil {
	
	public static List<String> readArrayOrValue(JsonReader jr) throws IOException {
		List<String> vals=new ArrayList<>(3);
		if(jr.peek()==JsonToken.BEGIN_ARRAY){
			jr.beginArray();
			while(jr.hasNext())
				if(jr.peek()==JsonToken.STRING)
					vals.add(jr.nextString());
			jr.endArray();
		}else if(jr.peek()==JsonToken.STRING)
			vals.add(jr.nextString());
		return vals;

	}

	public static List<String> readArrayOrValue(JsonElement label) {
		List<String> vals=new ArrayList<>(3);
		if(label.isJsonArray()) {
			JsonArray labelObj = label.getAsJsonArray();
			for(JsonElement je: labelObj) {
				vals.add(je.getAsString());					
			}
		}else if(label.isJsonPrimitive())
			vals.add(label.getAsString());
		return vals;
	}
}
