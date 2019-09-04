package eu.europeana.research.iiif.profile.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class Manifest {

	@SerializedName("@id")
	public String id;
	public Metadata[] metadata;
	public SeeAlso[] seeAlso;
	public JsonElement license;
	
	
	
}

//
//
//public String id;
//public List<Metadata> metadata;
//public List<SeeAlso> seeAlso;
//public List<String> license;
//
//
//public static Manifest ReadFromJson(String inString) {
//	JsonReader jr=new JsonReader(new StringReader(inString));
//	Manifest manif=new Manifest();
//	try {
//		try {
//			jr.beginObject();
//		} catch (IllegalStateException e) {
//			return null;
//		}
//		while(jr.peek()!=JsonToken.END_OBJECT){
//			String field = jr.nextName();
//			if(field.equals("metadata")) {
//				manif.metadata=new ArrayList<>(3);
//				if(jr.peek()==JsonToken.BEGIN_ARRAY){
//					jr.beginArray();
//					while(jr.hasNext())
//						manif.metadata.add(Metadata.readFromJson(jr));
//					jr.endArray();
//				}else if(jr.peek()==JsonToken.BEGIN_OBJECT)
//					manif.metadata.add(Metadata.readFromJson(jr));
//			}else if(field.equals("seeAlso")) {
//			}else if(field.equals("license")) {
//			}else if(field.equals("id")) {
//			}
//		}
//		jr.close();
//	} catch (IOException e) {
//		//ignore
//	}
//}	