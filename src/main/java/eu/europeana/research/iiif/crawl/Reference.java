package eu.europeana.research.iiif.crawl;

import com.google.gson.annotations.SerializedName;

public class Reference {
	@SerializedName("@id")
	public String id;
	@SerializedName("@type")
	public String type;
	public String label;
}
