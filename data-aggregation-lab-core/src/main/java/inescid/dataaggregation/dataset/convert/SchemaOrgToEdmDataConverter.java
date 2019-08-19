package inescid.dataaggregation.dataset.convert;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.w3c.dom.Document;

import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RdfRegEdm;
import inescid.dataaggregation.data.RdfRegRdf;
import inescid.dataaggregation.dataset.convert.rdfconverter.RdfConversionSpecification;
import inescid.dataaggregation.dataset.convert.rdfconverter.RdfConverter;
import inescid.dataaggregation.dataset.convert.rdfconverter.ResourceTypeConversionSpecification;
import inescid.dataaggregation.dataset.convert.rdfconverter.SchemaOrgToEdmConversionSpecification;
import inescid.util.RdfUtil;
import inescid.util.XmlUtil;

/**
 * @author nfrei
 *
 *	This class is not thread-safe
 *
 */
public class SchemaOrgToEdmDataConverter {
	
	private static final Charset UTF8=Charset.forName("UTF8");
	
	Map<Resource, Resource> blankNodesMapped=new HashMap<Resource, Resource>();
	
	RdfConverter conv=new RdfConverter(SchemaOrgToEdmConversionSpecification.spec);
	
	protected String provider;
	protected String dataProvider;
	protected String datasetRights;
	
	Resource mainTargetResource=null;

	protected void addAdditionalStatements(Model additionalStatements) {
		if(additionalStatements!=null)
			mainTargetResource.getModel().add( additionalStatements.listStatements());
	}
	
	public SchemaOrgToEdmDataConverter() {
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
		ResIterator aggregations = mainTargetResource.getModel().listResourcesWithProperty(RdfRegRdf.type, RdfReg.ORE_AGGREGATION);
		Resource ag = aggregations.next();
		mainTargetResource.getModel().add(ag, RdfRegEdm.aggregatedCHO, mainTargetResource);
		
		if(dataProvider!=null) {
			StmtIterator provs = mainTargetResource.getModel().listStatements(ag, RdfRegEdm.dataProvider, (String) null);
			if(!provs.hasNext())
				mainTargetResource.getModel().add(ag, RdfRegEdm.dataProvider, dataProvider);
			if(provider==null) {
				provs = mainTargetResource.getModel().listStatements(ag, RdfRegEdm.provider, (String) null);
				if(!provs.hasNext())
					mainTargetResource.getModel().add(ag, RdfRegEdm.provider, dataProvider);
			}
		}
		
		if(provider!=null) {
			StmtIterator provs = mainTargetResource.getModel().listStatements(ag, RdfRegEdm.provider, (String) null);
			if(!provs.hasNext())
				mainTargetResource.getModel().add(ag, RdfRegEdm.provider, provider);
		}
		if(datasetRights!=null) {
			StmtIterator rights = mainTargetResource.getModel().listStatements(ag, RdfRegEdm.rights, (String) null);
			if(!rights.hasNext())
				mainTargetResource.getModel().add(ag, RdfRegEdm.rights, mainTargetResource.getModel().createResource(datasetRights));
		}
		
		// logic to calculate hasViews would be to complex to implement in the RdfConverter. So we do like this:
		HashSet<String> wrUris=new HashSet<>();
		StmtIterator provs = mainTargetResource.getModel().listStatements(null, RdfRegRdf.type, RdfRegEdm.WebResource);
		while(provs.hasNext()) {
			Statement stm = provs.next();
			wrUris.add(stm.getSubject().getURI());
		}
		for(Resource schemaWebResourceType: new Resource[] {RdfReg.SCHEMAORG_IMAGE_OBJECT, RdfReg.SCHEMAORG_MEDIA_OBJECT, RdfReg.SCHEMAORG_AUDIO_OBJECT}) {
			StmtIterator mediaStms = source.getModel().listStatements(null, RdfRegRdf.type, schemaWebResourceType);
			while(mediaStms.hasNext()) {
				Statement medStm = mediaStms.next();				
				StmtIterator contStms = source.getModel().listStatements(medStm.getSubject(), RdfReg.SCHEMAORG_CONTENT_URL, (RDFNode) null);
				while(contStms.hasNext()) {
					Statement stm = contStms.next();		
					if(!wrUris.contains(RdfUtil.getUriOrLiteralValue(stm.getObject().asResource())))
						mainTargetResource.getModel().add(ag, RdfRegEdm.hasView, mainTargetResource.getModel().createResource(RdfUtil.getUriOrLiteralValue(stm.getObject().asResource())));
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
