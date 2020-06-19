package inescid.dataaggregation.dataset.convert;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.RdfsClassHierarchy;
import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.dataset.convert.rdfconverter.RdfConverter;
import inescid.dataaggregation.dataset.convert.rdfconverter.SchemaOrgToEdmConversionSpecification;
import inescid.util.AccessException;
import inescid.util.RdfUtil;

/**
 * @author nfrei
 *
 *	This class is not thread-safe
 *
 */
public class SchemaOrgToEdmDataConverter {
	Map<Resource, Resource> blankNodesMapped=new HashMap<Resource, Resource>();
	
//	RdfConverter conv=new RdfConverter(SchemaOrgToEdmConversionSpecification.spec, new RdfsClassHierarchy(RdfUtil.readRdfFromUri("https://schema.org/docs/schemaorg.owl")));
	RdfConverter conv=null;
		
	protected String provider;
	protected String dataProvider;
	protected String datasetRights;
	
	Resource mainTargetResource=null;

	protected void addAdditionalStatements(Model additionalStatements) {
		if(additionalStatements!=null)
			mainTargetResource.getModel().add( additionalStatements.listStatements());
	}
	
	public SchemaOrgToEdmDataConverter() throws AccessException, InterruptedException, IOException {
		conv=new RdfConverter(SchemaOrgToEdmConversionSpecification.spec, new RdfsClassHierarchy(RdfUtil.readRdfFromUri("https://schema.org/docs/schemaorg.owl")));
	}

	public SchemaOrgToEdmDataConverter(String dataProvider) {
		super();
		this.dataProvider = dataProvider;
	}
	
	public String getDataProvider() {
		return dataProvider;
	}
	public void setDataProvider(String dataProvider) {
		this.dataProvider = dataProvider;
	}
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}

	public SchemaOrgToEdmDataConverter(String dataProvider, String provider) {
		super();
		this.dataProvider = dataProvider;
		this.provider = provider;
	}
	public Resource convert(Resource source, Model additionalStatements) {
		Resource mainTargetResource=conv.convert(source);
		if(mainTargetResource==null)
			return null;
		ResIterator aggregations = mainTargetResource.getModel().listResourcesWithProperty(Rdf.type, Ore.Aggregation);
		Resource ag = aggregations.next();
		mainTargetResource.getModel().add(ag, Edm.aggregatedCHO, mainTargetResource);
		
		if(dataProvider!=null) {
			StmtIterator provs = mainTargetResource.getModel().listStatements(ag, Edm.dataProvider, (String) null);
			if(!provs.hasNext())
				mainTargetResource.getModel().add(ag, Edm.dataProvider, dataProvider);
			if(provider==null) {
				provs = mainTargetResource.getModel().listStatements(ag, Edm.provider, (String) null);
				if(!provs.hasNext())
					mainTargetResource.getModel().add(ag, Edm.provider, dataProvider);
			}
		}
		
		if(provider!=null) {
			StmtIterator provs = mainTargetResource.getModel().listStatements(ag, Edm.provider, (String) null);
			if(!provs.hasNext())
				mainTargetResource.getModel().add(ag, Edm.provider, provider);
		}
		if(datasetRights!=null) {
			StmtIterator rights = mainTargetResource.getModel().listStatements(ag, Edm.rights, (String) null);
			if(!rights.hasNext())
				mainTargetResource.getModel().add(ag, Edm.rights, mainTargetResource.getModel().createResource(datasetRights));
		}
		
		// logic to calculate hasViews would be to complex to implement in the RdfConverter. So we do like this:
		HashSet<String> wrUris=new HashSet<>();
		StmtIterator provs = mainTargetResource.getModel().listStatements(null, Rdf.type, Edm.WebResource);
		while(provs.hasNext()) {
			Statement stm = provs.next();
			wrUris.add(stm.getSubject().getURI());
		}
		for(Resource schemaWebResourceType: new Resource[] {Schemaorg.ImageObject, Schemaorg.MediaObject, Schemaorg.AudioObject}) {
			StmtIterator mediaStms = source.getModel().listStatements(null, Rdf.type, schemaWebResourceType);
			while(mediaStms.hasNext()) {
				Statement medStm = mediaStms.next();				
				StmtIterator contStms = source.getModel().listStatements(medStm.getSubject(), Schemaorg.contentUrl, (RDFNode) null);
				while(contStms.hasNext()) {
					Statement stm = contStms.next();		
					if(!wrUris.contains(RdfUtil.getUriOrLiteralValue(stm.getObject().asResource())))
						mainTargetResource.getModel().add(ag, Edm.hasView, mainTargetResource.getModel().createResource(RdfUtil.getUriOrLiteralValue(stm.getObject().asResource())));
				}
			}
		}
		
		if(additionalStatements!=null && !additionalStatements.isEmpty())
			addAdditionalStatements(additionalStatements);
		return mainTargetResource;
	}

	public String getDatasetRights() {
		return datasetRights;
	}

	public void setDatasetRights(String datasetRights) {
		this.datasetRights = datasetRights;
	}


}
