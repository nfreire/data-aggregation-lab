package inescid.dataaggregation.metadatatester.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.any23.rdf.RDFUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;

import inescid.dataaggregation.casestudies.wikidata.evaluation.Dqc10PointRatingCalculatorNoRights;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.dataset.profile.multilinguality.MultilingualSaturation;
import inescid.dataaggregation.dataset.profile.tiers.EpfTiersCalculator;
import inescid.dataaggregation.dataset.profile.tiers.TiersCalculation;
import inescid.dataaggregation.metadatatester.view.ResourceView.DataModel;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.util.AccessException;
import inescid.util.HttpUtil;
import inescid.util.RdfUtil;
import inescid.util.europeana.EdmRdfUtil;

public class EuropeanaForm extends UriForm {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(EuropeanaForm.class);

	String operation=null;
	boolean uriChecked=false;
	
	ResourceView cho;
	
	public EuropeanaForm() {
	} 

	public EuropeanaForm(HttpServletRequest req) {
		super(req.getParameter("europeanaID"));
		operation=req.getParameter("operation");
	}

	private String loadAndExtractDataFromUri() {
		String rdfXml = null;
		try {
			String canonicalUri = convertToCanonical(uri);
			rdfXml = HttpUtil.makeRequestForContent(canonicalUri, "Accept", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER);
			System.out.println(rdfXml);
			
			if(rdfXml!=null) {
				Model edm = RdfUtil.readRdf(rdfXml, Lang.RDFXML);
	//			String edmXml=HttpUtil.makeRequest(uri, "Accept", RdfUtil.CONTENT_TYPES_ACCEPT_HEADER).getResponseContentAsString();
	////			TiersCalculation calculate = EpfTiersCalculator.calculateOnInternalEdm(edm.createResource(uri)); 
	//			TiersCalculation calculate = EpfTiersCalculator.calculate(edmXml); 
				if(edm!=null) {
					Resource edmChoRes= edm.createResource(canonicalUri);
					if(edmChoRes!=null) {
						cho=new ResourceView(edmChoRes, DataModel.ALL);
					}else
						message="CHO not found: "+ uri;
				} else {
					message="Could not find Europeana record: "+ uri;
				}
			} else {
				message="Could not find Europeana record: "+ uri;
			}
		} catch (RiotException e) {
			message="Invalid response received from Europeana: "+ e.getMessage()+" : "+ uri ;
			e.printStackTrace();
		} catch (IOException e) {
			message="Problem accessing Europeana: "+ e.getMessage()+" : "+ uri ;
			e.printStackTrace();
		} catch (InterruptedException e) {
			message="Unkown problem accessing Europeana: "+ uri;
			e.printStackTrace();
		} catch (AccessException e) {
			message="Could not find Europeana record: "+ uri;
			e.printStackTrace();
		}
		return rdfXml;
	}

	public void checkUri() {
		uriChecked=true;
		message=validateUri();
		if(message!=null)
			return;
		String rdfXml=loadAndExtractDataFromUri();
		
		if(cho!=null) {
			try {
				cho.setTiers(EpfTiersCalculator.calculate(rdfXml));
				cho.setMultilingualSaturation(MultilingualSaturation.calculate(cho.getModel()));
				cho.setCompleteness(Dqc10PointRatingCalculatorNoRights.calculate(cho.getModel()));
			} catch (Exception e) {
				message=e.getMessage();
				e.printStackTrace();
			}
		}
	}

	public boolean isUriChecked() {
		return uriChecked;
	}
	
	@Override
	public String validateUri() {
		if(StringUtils.isEmpty(uri)) {
			return "Provide the Europeana ID or URI";
		}
		return null;
	}

	public static String convertToCanonical(String idOrUri) {
		if(!idOrUri.startsWith("http://")) 
			idOrUri="http://data.europeana.eu/item"+
					(idOrUri.startsWith("/") ? "" : "/") +
					idOrUri;
		return idOrUri;
	}

	public ResourceView getCho() {
		return cho;
	}
}
