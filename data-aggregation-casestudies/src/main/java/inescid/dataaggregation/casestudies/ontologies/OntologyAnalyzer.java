package inescid.dataaggregation.casestudies.ontologies;

import java.io.IOException;
import java.util.HashSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.dataaggregation.data.RdfReg;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.dataset.profile.UsageProfiler;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class OntologyAnalyzer {
	public String namespace;
	public String sourceUri;
	public String title;
	
	ReportOfOntology report;
	
	CachedHttpRequestService rdfCache;
	
	public OntologyAnalyzer(CachedHttpRequestService rdfCache) {
		this.rdfCache = rdfCache;
		report=new ReportOfOntology();
	}
	
	public void runAnalyzis(String namespace, String sourceUri, UsageProfiler profilerAllOntologies, UsageProfiler profilerAllDataElements) throws AccessException, InterruptedException, IOException {
		if(this.namespace==null) {
			this.namespace=namespace;
			this.sourceUri=sourceUri;
		}
		Model modelOnt = null;
		HttpResponse rdf = rdfCache.fetchRdf(namespace);
		if(rdf.isSuccess()) {
			try {
				modelOnt = RdfUtil.readRdf(rdf);
			} catch (RiotException e) {
				System.out.println("Invalid RDF: "+namespace);
				e.printStackTrace(System.out);
				modelOnt = RdfUtil.readRdf(rdf, Lang.TURTLE);
			}
			if (modelOnt == null || modelOnt.size()==0) {
				report.namespaceResolvable=false;
				if(sourceUri==null)
					report.ontologyExists=false;
				else {
					rdf = rdfCache.fetchRdf(sourceUri);
					if(rdf.isSuccess()) {
						modelOnt = RdfUtil.readRdf(rdf);
							report.ontologyExists=true;
					} else {
						report.ontologyExists=false;
						if(sourceUri!=null)
							throw rdf.throwException(sourceUri);
					}
				}
			} else {
				report.namespaceResolvable=true;
				report.ontologyExists=true;
			}
		} else {
			report.namespaceResolvable=false;
			if(sourceUri==null) {
				report.ontologyExists=false;
//				throw rdf.throwException(sourceUri);
			}
			rdf = rdfCache.fetchRdf(sourceUri);
			if(rdf.isSuccess()) {
				modelOnt = RdfUtil.readRdf(rdf);
//					report.namespaceResolvable=true;
					report.ontologyExists=true;
			} else {
				report.ontologyExists=false;
				throw rdf.throwException(sourceUri);
			}
		}
		if(report.ontologyExists) {
			if(modelOnt==null || modelOnt.size()==0) {
				report.ontologyExists=false;
				throw rdf.throwException(sourceUri);				
			} else {
				Resource ontRes=Jena.getResourceIfExists(namespace, modelOnt);
				if(ontRes==null && namespace.endsWith("#")) 
					ontRes=Jena.getResourceIfExists(namespace.substring(0, namespace.length()-1), modelOnt);
				if(ontRes!=null  && modelOnt.size()!=0) {
					report.rdfResourceForNamespaceExists=true;
					UsageProfiler profilerOnt=new UsageProfiler();
					profilerOnt.collect(ontRes);
//					profiler.collect(modelOnt);
					if(profilerAllOntologies!=null)
//						allOntologiesProfiler.collect(modelOnt);
						profilerAllOntologies.collect(ontRes);
					report.profileOfOntology=profilerOnt.getUsageStats();
					report.profileOfDataElements=profilerOnt.getUsageStats();
					
					HashSet<String> dataElementsInOntology=new HashSet<String>();
					for(Resource dataElement: new Resource[] {RegRdf.Property, RegRdfs.Class, RegRdfs.Datatype,
							RegRdfs.Resource, RdfReg.OWL_CLASS, RdfReg.OWL_DATA_RANGE, RdfReg.OWL_DATA_TYPE_PROPERTY, 
							RdfReg.OWL_FUNCTIONAL_PROPERTY, RdfReg.OWL_OBJECT_PROPERTY, RdfReg.OWL_ONTOLOGY_PROPERTY}) {
						StmtIterator ontStms = modelOnt.listStatements(null, RegRdf.type, dataElement);
						while (ontStms.hasNext()) {
							Statement stm = ontStms.next();
							if(stm.getSubject().isURIResource()) {
									dataElementsInOntology.add(stm.getSubject().getURI());
 							}
						}
					}
					StmtIterator ontStms = modelOnt.listStatements(null, RegRdfs.isDefinedBy, ontRes);
					while (ontStms.hasNext()) {
						Statement stm = ontStms.next();
						if(stm.getSubject().isURIResource()) 
							dataElementsInOntology.add(stm.getSubject().getURI());
					}
					dataElementsInOntology.remove(ontRes);
					report.dataElementResources=dataElementsInOntology.size();

					UsageProfiler profilerElements=new UsageProfiler();
					
					for(String dataElUri : dataElementsInOntology) {
						Resource dataEl = Jena.getResourceIfExists(dataElUri, modelOnt);
						if(dataEl!=null) {
							profilerElements.collect(dataEl);
							if(profilerAllDataElements!=null) 
								profilerAllDataElements.collect(dataEl);
						}
					}
					report.profileOfDataElements=profilerElements.getUsageStats();
 				} else {
					report.rdfResourceForNamespaceExists=false;
				}
			}
		}
	}

	@Override
	public String toString() {
		return "OntologyAnalyzer [sourceUri=" + sourceUri + ", namespace=" + namespace + ", report=" + report + "]";
	}
	
}
