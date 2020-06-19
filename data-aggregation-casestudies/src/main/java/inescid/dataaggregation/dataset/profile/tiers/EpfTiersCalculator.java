package inescid.dataaggregation.dataset.profile.tiers;

import java.io.InputStream;
import java.util.HashMap;

import org.apache.jena.rdf.model.Resource;
import org.w3c.dom.Document;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.indexing.fullbean.StringToFullBeanConverter;
import eu.europeana.indexing.tiers.ClassifierFactory;
import eu.europeana.indexing.tiers.metadata.ContextualClassClassifier;
import eu.europeana.indexing.tiers.metadata.EnablingElementsClassifier;
import eu.europeana.indexing.tiers.metadata.LanguageClassifier;
import eu.europeana.indexing.utils.RdfWrapper;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.util.XmlUtil;
import inescid.util.europeana.EdmRdfToXmlSerializer;

public class EpfTiersCalculator {
	
	public static TiersCalculation calculateOnExternalEdm(Resource choRes) throws Exception {
		EdmRdfToXmlSerializer edmXmlSerializer=new EdmRdfToXmlSerializer(choRes);
		Document edmXmlExternalDom = edmXmlSerializer.getXmlDom();
		InputStream xsltStream = EpfTiersCalculator.class.getClassLoader().getResourceAsStream("EDM_external2internal_METIS.xsl");
		Document xsltDom = XmlUtil.parseDom(xsltStream);
		xsltStream.close();
		
		HashMap<String, Object> xslParams=new HashMap<String, Object>();
		xslParams.put("datasetName", "experimental");
		xslParams.put("edmCountry", "Europe");
		xslParams.put("edmLanguage", "en");
		String choId="/experimental/experimental";
		xslParams.put("providedCHOAboutId", choId);
		xslParams.put("aggregationAboutId", "/aggregation/provider"+choId);
		xslParams.put("europeanaAggregationAboutId", "/aggregation/europeana"+choId);
		xslParams.put("proxyAboutId", "/proxy/provider"+choId);
		xslParams.put("europeanaProxyAboutId", "/proxy/europeana"+choId);
		xslParams.put("dcIdentifier", choRes.getURI());
		Document edmInternalDom = XmlUtil.transform(edmXmlExternalDom, xsltDom, xslParams);
		String edmXmlInternal=XmlUtil.writeDomToString(edmInternalDom);
		return calculate(edmXmlInternal);
	}

	public static TiersCalculation calculateOnInternalEdm(Resource choRes) throws Exception {
		EdmRdfToXmlSerializer edmXmlSerializer=new EdmRdfToXmlSerializer(choRes);
		Document edmInternalDom = edmXmlSerializer.getXmlDom();
		String edmXmlInternal=XmlUtil.writeDomToString(edmInternalDom);
		return calculate(edmXmlInternal);
	}

	
	public static TiersCalculation calculate(String edmRdf) throws Exception {
		RDF rdf=new StringToFullBeanConverter().convertStringToRdf(edmRdf); 
	    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
	    TiersCalculation result=new TiersCalculation();
		result.setContent(ClassifierFactory.getMediaClassifier().classify(rdfWrapper));
		result.setMetadata(ClassifierFactory.getMetadataClassifier().classify(rdfWrapper));
		result.setLanguage(new LanguageClassifier().classify(rdfWrapper)); 
		result.setEnablingElements(new EnablingElementsClassifier().classify(rdfWrapper));
		result.setContextualClass(new ContextualClassClassifier().classify(rdfWrapper));
		return result;
	}
}
