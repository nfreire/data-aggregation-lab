package inescid.dataaggregation.casestudies.schemaorgcrawling;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RiotException;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.ld.RulesSchemaorgCrawlGraphOfCho;
import inescid.dataaggregation.crawl.ld.RulesSchemaorgCrawlGraphOfCho.AllowedValue;
import inescid.dataaggregation.crawl.ld.RulesSchemaorgCrawlGraphOfCho.MappingAllowedForClass;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegSchemaorg;
import inescid.util.AccessException;
import inescid.util.RdfUtil;

public class SchemaOrgLodCrawler {
/* 	-- crawl of a URI
		getCho, 
			check Resorce class
			for each prop of cho 
				if prop is mappable, follow it if mappable to a resource. if just a reference use the URI. if literal, harvest linked resource and keep if schema:name is present
				    harvest the resource (if not just used as ref)
				    	if to be used as literal, keep if schema:name is present (may be further elaborated in future work)
				    	if resource check supported type. 
				    		if supported continue harvesting and apply resource harvest algorithm
				    		if not supported type, try to get a schema:name for use as literal, or discard.
	*/
	
	private RulesSchemaorgCrawlGraphOfCho rules=new RulesSchemaorgCrawlGraphOfCho();
	private CrawlResult crawl;
	private int maxDepth=10;
	private boolean alwaysCountDepth=false;
	private HashSet<String> alreadyCrawled=new HashSet<>();
	private HashSet<String> alreadyCrawledNonRdf=new HashSet<>();
//	private HashSet<String> alreadyCrawledNotFound=new HashSet<>();
	private CachedHttpRequestService rdfCache;
	private NoRdfDomainsManager noRdfDomains=new NoRdfDomainsManager();
	
	public SchemaOrgLodCrawler() {
		rdfCache=null;
	}
	
	public SchemaOrgLodCrawler(CachedHttpRequestService rdfCache) {
		this.rdfCache = rdfCache;
	}

	public CrawlResult crawlSchemaorgForCho(String uri) throws AccessException, InterruptedException, IOException {
		crawl=new CrawlResult();
		crawlResource(uri.trim(), 0);
		return crawl;
	}	

	protected void crawlResourceForLiteral(Resource anonOrNamed, int depth) throws AccessException, InterruptedException, IOException {
		crawl.obtainedResourcesToGetLiterals++;		
		//TODO later
	}
	protected void crawlResourceForLiteral(String uri, int depth) throws AccessException, InterruptedException, IOException {
		crawl.obtainedResourcesToGetLiterals++;
		//TODO later
//		Resource r = RdfUtil.readRdfResourceFromUri(uri);
//		if (r==null ) {
//			crawl.notFound++;
//			return ;
//		}
//		crawl.literalsFound++;
	}
	protected void crawlResource(String uri, int depth) throws AccessException, InterruptedException, IOException {
		if(maxDepth<depth) { 
			crawl.urisTooDeep++;
			return;
		}
		if(alreadyCrawled.contains(uri)) 
			return;
		if(alreadyCrawledNonRdf.contains(uri)) {
			crawl.incNotRdf(uri, depth);
			return;
		}
//		if(alreadyCrawledNotFound.contains(uri)) {
//			crawl.incNotFound(uri, depth);
//			return;
//		}
		if(noRdfDomains.isBlackListed(uri)) {
			crawl.incNotRdf(uri, depth);
			return;
		}
		
//		System.out.println(depth+" "+uri);
		Resource r=null;
		try {
			r = RdfUtil.readRdfResourceFromUri(uri);
			alreadyCrawled.add(uri);
			noRdfDomains.reportSuccess(uri);
		} catch (AccessException e) {
			if(e.getCode()!=null && e.getCode().equals("200")) {
				crawl.incNotRdf(uri, depth);
				alreadyCrawledNonRdf.add(uri);
				noRdfDomains.reportFailure(uri);
				System.out.println("Not rdf (depth "+depth+") "+uri);
			} else {
				crawl.incNotRdf(uri, depth);
				alreadyCrawledNonRdf.add(uri);
				noRdfDomains.reportFailure(uri);
				System.out.println("Not found (depth "+depth+") "+uri);
			}
		} catch (IOException e) {
			crawl.incNotRdf(uri, depth);
			alreadyCrawledNonRdf.add(uri);
			noRdfDomains.reportFailure(uri);
			System.out.println("Not found (depth "+depth+") "+uri);			
		} catch (RiotException e) {
			crawl.incNotRdf(uri, depth);
			alreadyCrawledNonRdf.add(uri);
			noRdfDomains.reportFailure(uri);
			System.out.println("Not VALID rdf (depth "+depth+") "+uri);			
		}
		if (r==null ) return ;
		crawl.obtainedResources++;
		processedCrawledResource(r, depth, true);
	}		

	protected void crawlResourceInModel(Resource anonOrNamed, int depth) throws AccessException, InterruptedException, IOException {
		if(alreadyCrawled.contains(anonOrNamed.isAnon() ? anonOrNamed.getId() : anonOrNamed.getURI())) return;
		if(anonOrNamed.isAnon()) {
			alreadyCrawled.add(anonOrNamed.getId().toString()); 
			crawl.inModelResourcesAnon++;			
		} else {
			alreadyCrawled.add(anonOrNamed.getURI());
			crawl.inModelResourcesAnonNot++;
		}
		
		crawl.inModelResourcesTotal++;
		if(anonOrNamed.isAnon()) {
			Statement type = anonOrNamed.getProperty(RegRdf.type);
			if(type!=null)
				crawl.anonResourcesByObjectClass.incrementTo(type.getObject().asResource());
		}
		processedCrawledResource(anonOrNamed, depth, false);
	}
	protected void processedCrawledResource(Resource r, int depth, boolean countForDepth) throws AccessException, InterruptedException, IOException {
		HashMap<Resource, MappingAllowedForClass> mappableTypes=new HashMap<>();
		StmtIterator typeSts = r.listProperties(RegRdf.type);
		for(Statement st: typeSts.toList())  {
			Resource typeClass = st.getObject().asResource();
			MappingAllowedForClass mapping = rules.getMapping(typeClass);
			if (mapping!=null){
				if(depth>=0 || rules.isCho(typeClass))
					mappableTypes.put(typeClass, mapping);
				crawl.crawledByObjectClass.incrementTo(typeClass);
			}
		};
		if (countForDepth || alwaysCountDepth) depth++;
		
		if(!mappableTypes.isEmpty()) {
			StmtIterator stms = r.listProperties();
			for(Statement st: stms.toList())  {
				try {
					Property prop = st.getPredicate();
					if(prop.equals(RegRdf.type))
						continue;
					Set<AllowedValue> possibleMaps = RulesSchemaorgCrawlGraphOfCho.getPossibleMaps(prop, mappableTypes);
					if(possibleMaps.isEmpty()) {
						crawl.propsNotFollowed++;
						crawl.propsNotFollowedByProperty.incrementTo(prop);
						if (st.getObject().isResource() && !st.getObject().isAnon())
							crawl.propsNotFollowedWithUri++;
					} else {
//						if(prop.equals(RegSchemaorg.image))
//							System.out.println(st);
						crawl.propsFollowedByProperty.incrementTo(prop);
						if(st.getObject().isResource()) {
							if(st.getObject().isAnon()) {
								if(possibleMaps.contains(AllowedValue.RESOURCE)) 
									crawlResourceInModel(st.getObject().asResource(), depth);
								else if(possibleMaps.contains(AllowedValue.LITERAL))
									crawlResourceForLiteral(st.getObject().asResource(), depth);
								else //REFERENCE only
									crawl.referencesNotFound++;
							} else {// URI resource 
								if(possibleMaps.contains(AllowedValue.RESOURCE)) {
									if (RdfUtil.contains(st.getObject().asResource().getURI(), r.getModel())) {
										crawlResourceInModel(st.getObject().asResource(), depth);
									} else {
										crawlResource(st.getObject().asResource().getURI(), depth);									
									}
								} else if(possibleMaps.contains(AllowedValue.REFERENCE)) {
									crawl.referencesFound++;
								} else if(possibleMaps.contains(AllowedValue.LITERAL)) {
									crawlResourceForLiteral(st.getObject().asResource(), depth);
								}
							}
						} else { //Literal
							if(possibleMaps.contains(AllowedValue.LITERAL))
								if(possibleMaps.contains(AllowedValue.LITERAL))
									crawl.literalsFound++;
						}
					}
				} catch (AccessException | InterruptedException | IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		} else {
			crawl.urisNotFollowed++;
			typeSts = r.listProperties(RegRdf.type);
			typeSts.forEachRemaining( st -> {
				Resource typeClass = st.getObject().asResource();
				crawl.urisNotFollowedByObjectClass.incrementTo(typeClass);
			});
		}
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public void setAlwaysCountDepth(boolean alwaysCountDepth) {
		this.alwaysCountDepth = alwaysCountDepth;
	}
}
