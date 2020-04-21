package inescid.dataaggregation.metadatatester.view;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.RegEdm;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegSchemaorg;
import inescid.dataaggregation.metadatatester.view.SchemaorgChoValidator.ValidationReport;
import inescid.util.RdfUtil;

public class ResourceView{
	Resource res;
	boolean includeEdm;
	ValidationReport validation;
	
	public ResourceView(Resource st, boolean includeEdm) {
		super();
		this.res = st;
		this.includeEdm = includeEdm;
	}

	public String getUri() {
		return RdfUtil.getUriOrId(res);
	}
	
	
	public List<StatementView> getTypes() {
		List<StatementView> sts=new ArrayList<StatementView>();
		for(Statement st: res.listProperties(RegRdf.type).toList()) {
			if(includeEdm) {
				if(!st.getObject().isURIResource())
					continue;
				if(st.getObject().asResource().getURI().startsWith(RegSchemaorg.NS) || st.getObject().asResource().getURI().startsWith(RegRdf.NS)) {
					sts.add(new StatementView(st));
					continue;
				}					
				for(String ns: RegEdm.NS_EXTERNAL_PREFERRED_BY_NS.keySet())
					if(st.getObject().asResource().getURI().startsWith(ns) ) {
						sts.add(new StatementView(st));
						continue;
					}					
			} else {
				if(!st.getObject().isURIResource()
						|| (!st.getObject().asResource().getURI().startsWith(RegSchemaorg.NS) && !st.getObject().asResource().getURI().startsWith(RegRdf.NS))) 
					continue;
				sts.add(new StatementView(st));
			}
		}
		return sts;
	}

	public List<StatementView> getStatements() {
		List<StatementView> sts=new ArrayList<StatementView>();
		for(Statement st: res.listProperties().toList()) {
			if(st.getPredicate().equals(RegRdf.type)) continue;
			if(!st.getPredicate().getURI().startsWith(RegSchemaorg.NS) &&
					!st.getPredicate().getURI().startsWith(RegRdf.NS)) continue;
			sts.add(new StatementView(st));
		}
		return sts;			
	}

	public boolean isCreativeWork() {
		for(Statement st: res.listProperties(RegRdf.type).toList()) {
			if(!st.getObject().isURIResource()) continue;
			if (RegSchemaorg.creativeWorkClasses.contains(st.getObject().asResource()))
				return true;
			if (RegEdm.ProvidedCHO.equals(st.getObject().asResource()))			
				return true;
		}
		return false;
	}

	public boolean hasProperties() {
		for(Statement st: res.listProperties().toList()) {
			if(st.getPredicate().equals(RegRdf.type)) return true;
			if(!st.getPredicate().getURI().startsWith(RegSchemaorg.NS) &&
					!st.getPredicate().getURI().startsWith(RegRdf.NS)) continue;
			return true;
		}
		return false;
	}

	public Resource getResource() {
		return res;
	}

	public ValidationReport getValidation() {
		return validation;
	}

	public void setValidation(ValidationReport validation) {
		this.validation = validation;
	}
}