package inescid.dataaggregation.dataset.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Dcat;
import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.Schemaorg;
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
		StmtIterator voidRootResources = dsResource.listProperties(inescid.dataaggregation.data.model.Void.rootResource);
		voidRootResources.forEachRemaining(st -> uris.add(st.getObject().asResource().getURI()));
		return uris;
	}


	public String getSparqlEndpoint() {
		Resource  dsResource=model.createResource(datasetUri);
		if (dsResource==null ) return null;
		Resource dataService=RdfUtil.findResource(dsResource, Dcat.distribution, Dcat.accessService);
		if (dataService==null ) return null;
		
		boolean isSparqlConformant=false;
		StmtIterator conforms = dataService.listProperties(DcTerms.conformsTo);
		for(Statement s: conforms.toList()) {
			String standard = RdfUtil.getUriOrLiteralValue(s.getObject());
			if(standard!=null && (standard.equals("http://www.w3.org/2005/09/sparql-protocol-types/#") 
					|| standard.equals("http://www.w3.org/2005/sparql-results#") 
					|| standard.equals("http://www.w3.org/2005/08/sparql-protocol-query/#"))) {
				isSparqlConformant=true;
				break;
			}
		}
		if (!isSparqlConformant) return null;
		
		Statement endPoint = dataService.getProperty(Dcat.endpointURL);
		if (endPoint==null ) return null;
		return RdfUtil.getUriOrLiteralValue(endPoint.getObject());		
	}

	public String getSparqlEndpointQuery() {
		Resource  dsResource=model.createResource(datasetUri);
		if (dsResource==null ) return null;
		Resource searchAction=RdfUtil.findResource(dsResource, Dcat.distribution, RdfReg.PROV_WAS_GENERATED_BY);
		if (searchAction==null ) return null;
		Statement query = searchAction.getProperty(Schemaorg.query);
		if (query==null ) return null;
		return query.getObject().asLiteral().getString() ;		
	}
	
	public List<Distribution> getDistributions(){
		ArrayList<Distribution> uris= new ArrayList<>();
		Resource  dsResource=model.createResource(datasetUri);
		if (dsResource==null ) return uris;
		StmtIterator voidRootResources = dsResource.listProperties(inescid.dataaggregation.data.model.Void.dataDump);
		voidRootResources.forEachRemaining(st -> uris.add(new Distribution(st.getObject().asResource().getURI(), null)));

		StmtIterator distributions = dsResource.listProperties(Dcat.distribution);
		distributions.forEachRemaining(st -> {
			if (st.getObject().isResource()) {
				Statement downloadUrl = st.getResource().getProperty(Dcat.downloadURL);
				if(downloadUrl!=null) {
					Statement mediaType = st.getResource().getProperty(Dcat.mediaType);
					Distribution dist=new Distribution(RdfUtil.getUriOrLiteralValue(downloadUrl.getObject()), 
							mediaType==null ? null : RdfUtil.getUriOrLiteralValue(mediaType.getObject()));
					uris.add(dist);
				}
			}
			});

		distributions = dsResource.listProperties(Schemaorg.distribution);
		distributions.forEachRemaining(st -> {
			if (st.getObject().isResource()) {
				Statement downloadUrl = st.getResource().getProperty(Schemaorg.contentUrl);
				if(downloadUrl!=null) {
					Statement mediaType = st.getResource().getProperty(Schemaorg.encodingFormat);
					Distribution dist=new Distribution(RdfUtil.getUriOrLiteralValue(downloadUrl.getObject()), 
							mediaType==null ? null : RdfUtil.getUriOrLiteralValue(mediaType.getObject()));
					uris.add(dist);
				}
			}
		});
		return uris;
	}
}
