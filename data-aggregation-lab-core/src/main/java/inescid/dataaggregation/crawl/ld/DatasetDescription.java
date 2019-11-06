package inescid.dataaggregation.crawl.ld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.RdfReg;
import inescid.util.AccessException;
import inescid.util.RdfUtil;

public class DatasetDescription {
	String datasetUri;
	Model model;
	
	public DatasetDescription(String datasetUri) throws AccessException, InterruptedException, IOException {
		this.datasetUri = datasetUri;
		Model model = RdfUtil.readRdfFromUri(datasetUri);
	}

	public List<String> listRootResources() throws AccessException, InterruptedException, IOException{
		ArrayList<String> uris= new ArrayList<String>();
		Resource  dsResource=model.createResource(datasetUri);
		if (dsResource==null ) return uris;
		StmtIterator voidRootResources = dsResource.listProperties(RdfReg.VOID_ROOT_RESOURCE);
		voidRootResources.forEachRemaining(st -> uris.add(st.getObject().asResource().getURI()));
		return uris;
	}
	
}
