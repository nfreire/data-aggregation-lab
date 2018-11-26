package eu.europeana.research.iiif.profile.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Manifest {

	@SerializedName("@id")
	public String id;
	public Metadata[] metadata;
	public SeeAlso[] seeAlso;
	public String[] license;
	
	
	
}
