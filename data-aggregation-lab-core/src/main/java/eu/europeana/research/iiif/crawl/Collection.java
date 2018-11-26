package eu.europeana.research.iiif.crawl;

import com.google.gson.annotations.SerializedName;

public class Collection {
	@SerializedName("@id")
	public String id;
	public String label;
	public String attribution;
	public String description;
	public Reference[] manifests;
	public Reference[] collections;
}
