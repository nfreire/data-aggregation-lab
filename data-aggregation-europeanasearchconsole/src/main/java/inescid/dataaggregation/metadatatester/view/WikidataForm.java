package inescid.dataaggregation.metadatatester.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.casestudies.wikidata.evaluation.Dqc10PointRatingCalculatorNoRights;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.dataset.profile.multilinguality.MultilingualSaturation;
import inescid.dataaggregation.dataset.profile.tiers.EpfTiersCalculator;
import inescid.dataaggregation.metadatatester.view.ResourceView.DataModel;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.util.AccessException;
import inescid.util.europeana.EdmRdfUtil;

public class WikidataForm extends UriForm {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(WikidataForm.class);

	String operation=null;
	boolean uriChecked=false;
	boolean uriValidated=false;
	
	ResourceView cho;
	List<ResourceView> otherResources;
	
	public WikidataForm() {
	} 

	public WikidataForm(HttpServletRequest req) {
		super(req.getParameter("wikidataID"));
		operation=req.getParameter("operation");
	}

	public boolean isUriChecked() {
		return uriChecked;
	}

	public void setUriChecked(boolean uriChecked) {
		this.uriChecked = uriChecked;
	}

	public void checkUri() {
		uriChecked=true;
		message=validateUri();
		if(message!=null)
			return;
		loadAndExtractDataFromUri();
	}

	private void loadAndExtractDataFromUri() {
		otherResources=new ArrayList<ResourceView>();		
		try {
			String canonicalUri = WikidataUtil.convertWdUriToCanonical(uri);
			Resource wdChoRes= WikidataUtil.fetchResource(canonicalUri);
			if(wdChoRes!=null) {
				Resource edmChoRes = GlobalMetadataTester.wikidataEdmConverter.convert(wdChoRes);
				if(edmChoRes!=null) {
					for(Resource res: edmChoRes.getModel().listSubjects().toList()) {
						ResourceView resView=new ResourceView(res, DataModel.ALL);
						if(!resView.hasProperties()) continue;
						if(resView.getResource().equals(edmChoRes))
							cho=resView;
						else
							otherResources.add(resView);
					}
				}else
					message="Wikidata entity does not have a type valid for conversion to EDM: "+ uri;
			} else {
				message="Could not find Wikidata entity: "+ uri;
			}
		} catch (IOException e) {
			message="Problem accessing Wikidata entity: "+ e.getMessage()+" : "+ uri ;
			e.printStackTrace();
		} catch (InterruptedException e) {
			message="Unkown problem accessing Wikidata entity: "+ uri;
			e.printStackTrace();
		} catch (AccessException e) {
			message="Could not find Wikidata entity: "+ uri;
			e.printStackTrace();
		}
	}

	public List<ResourceView> getOtherResources() {
		return otherResources;
	}

	public void validateEdmUri() {
		uriChecked=true;
		uriValidated=true;
		message=validateUri();
		if(message!=null)
			return;
		loadAndExtractDataFromUri();
		
		if(cho!=null) {
			Resource aggRes = EdmRdfUtil.getAggregationResource(cho.getResource().getModel());
			aggRes.addLiteral(Edm.provider, "Wikidata");
			Statement dataProviderSt = aggRes.getProperty(Edm.dataProvider);
			if(dataProviderSt==null)
				aggRes.getModel().createStatement(aggRes, Edm.dataProvider, "Unknown");
			aggRes.getModel().removeAll(aggRes,  Edm.dataProvider, null);
			aggRes.getModel().add(dataProviderSt);
			
			aggRes.addProperty(Edm.rights, aggRes.getModel().createResource("http://creativecommons.org/publicdomain/mark/1.0/"));
			cho.setValidation(EdmValidator.validate(cho.getResource()));
			
			try {
				cho.setTiers(EpfTiersCalculator.calculateOnExternalEdm(cho.getResource()));
				cho.setMultilingualSaturation(MultilingualSaturation.calculate(cho.getModel()));
				cho.setCompleteness(Dqc10PointRatingCalculatorNoRights.calculate(cho.getModel()));
			} catch (Exception e) {
				message=e.getMessage();
				e.printStackTrace();
			}
		}
	}

	public boolean isUriValidated() {
		return uriValidated;
	}
	
	@Override
	public String validateUri() {
		if(StringUtils.isEmpty(uri)) {
			return "Provide the entity ID from Wikidata";
		}
		return null;
	}

	public ResourceView getCho() {
		return cho;
	}
	
	
}
