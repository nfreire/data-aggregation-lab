package eu.europeana.research.iiif.crawl;

import com.google.gson.annotations.SerializedName;

public class Collection {
	@SerializedName("@id")
	String id;
	Reference[] manifests;
	Reference[] collections;
}
