package inescid.dataaggregation.crawl.ld;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.crawl.http.CachedHttpRequestService;
import inescid.dataaggregation.crawl.http.HttpRequestService;
import inescid.dataaggregation.crawl.http.HttpResponse;
import inescid.util.AccessException;
import inescid.util.RdfUtil;

public class LdCrawlerGeneric {
	public interface Frontier {
		public boolean isCrawlPredicates();

		public boolean isToCrawl(String sourceUri, Property predicate, int depth);
		public boolean isToCrawl(String sourceUri, String uri, int depth);
	}
	
	public interface ResourceHandler {
		void handle(String uri, Model model);
	}

	Frontier frontier;
	ResourceHandler resourceHandler;
	CachedHttpRequestService httpService;
	HashSet<String> harvested=new HashSet<String>();
	HashSet<String> errorsNotHarvested=new HashSet<String>();
	
	public LdCrawlerGeneric(CachedHttpRequestService httpService) {
		this.httpService = httpService;
		if(!httpService.isFollowRedirects())
			throw new RuntimeException("HttpService must be configured for following redirects");
	}
	
	public void startCrawl(String... startingSeeds) {
		for (String uri: startingSeeds) {
			try {
				crawl(uri, 0);
			} catch (AccessException | InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void crawl(String uri, int depth) throws AccessException, InterruptedException, IOException {
		try {
			HttpResponse response = httpService.fetchRdf(uri);
			Model model = RdfUtil.readRdf(response);
			if(model==null) {
				errorsNotHarvested.add(uri);
				harvested.add(uri);
			} else {
				resourceHandler.handle(uri, model);
				harvested.add(uri);
				depth++;
				HashSet<String> nextCrawl=new HashSet<String>();
				for (StmtIterator it=model.listStatements() ; it.hasNext() ; ) {
					Statement st=it.next();
					if(frontier.isCrawlPredicates() && !harvested.contains(st.getPredicate().getURI()) && frontier.isToCrawl(uri, st.getPredicate(), depth))  
						nextCrawl.add(st.getPredicate().getURI());
					String objUri = RdfUtil.getUriIfResource(st.getObject());
					if(objUri!=null && !harvested.contains(objUri) && frontier.isToCrawl(uri, objUri, depth))
						nextCrawl.add(objUri);
				}
				harvested.add(uri);
				for(String toCrawlUri:nextCrawl) {
					try {
						crawl(toCrawlUri, depth);
					} catch (AccessException | InterruptedException | IOException e) {
						System.err.println("error crawling URI "+ toCrawlUri);
						errorsNotHarvested.add(toCrawlUri);
						harvested.add(toCrawlUri);
						
					}
				}
			}
		} catch (AccessException | InterruptedException | IOException e) {
			System.err.println("error crawling URI "+ uri);
			e.printStackTrace();
			errorsNotHarvested.add(uri);
			harvested.add(uri);
			throw e;
		}
	}

	public Frontier getFrontier() {
		return frontier;
	}



	public void setFrontier(Frontier frontier) {
		this.frontier = frontier;
	}



	public ResourceHandler getResourceHandler() {
		return resourceHandler;
	}



	public void setResourceHandler(ResourceHandler resourceHandler) {
		this.resourceHandler = resourceHandler;
	}

}
