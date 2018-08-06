package inescid.dataaggregation.dataset.convert;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
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

import inescid.dataaggregation.dataset.convert.rdfconverter.RdfConversionSpecification;
import inescid.dataaggregation.dataset.convert.rdfconverter.RdfConverter;
import inescid.dataaggregation.dataset.convert.rdfconverter.ResourceTypeConversionSpecification;
import inescid.dataaggregation.dataset.convert.rdfconverter.SchemaOrgToEdmConversionSpecification;
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
		ResIterator aggregations = mainTargetResource.getModel().listResourcesWithProperty(RdfReg.RDF_TYPE, RdfReg.ORE_AGGREGATION);
		Resource ag = aggregations.next();
		mainTargetResource.getModel().add(ag, RdfReg.EDM_AGGREGATED_CHO, mainTargetResource);
		
		if(dataProvider!=null) {
			StmtIterator provs = mainTargetResource.getModel().listStatements(ag, RdfReg.EDM_DATA_PROVIDER, (String) null);
			if(!provs.hasNext())
				mainTargetResource.getModel().add(ag, RdfReg.EDM_DATA_PROVIDER, dataProvider);
			if(provider==null) {
				provs = mainTargetResource.getModel().listStatements(ag, RdfReg.EDM_PROVIDER, (String) null);
				if(!provs.hasNext())
					mainTargetResource.getModel().add(ag, RdfReg.EDM_PROVIDER, dataProvider);
			}
		}
		
		if(provider!=null) {
			StmtIterator provs = mainTargetResource.getModel().listStatements(ag, RdfReg.EDM_PROVIDER, (String) null);
			if(!provs.hasNext())
				mainTargetResource.getModel().add(ag, RdfReg.EDM_PROVIDER, provider);
		}
		if(datasetRights!=null) {
			StmtIterator rights = mainTargetResource.getModel().listStatements(ag, RdfReg.EDM_RIGHTS, (String) null);
			if(!rights.hasNext())
				mainTargetResource.getModel().add(ag, RdfReg.EDM_RIGHTS, mainTargetResource.getModel().createResource(datasetRights));
		}
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
