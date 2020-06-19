package inescid.dataaggregation.casestudies.coreference.old;

import org.apache.jena.query.QuerySolution;

import inescid.dataaggregation.wikidata.SparqlClientWikidata;
import inescid.dataaggregation.data.model.Owl;
import inescid.util.SparqlClient.Handler;

public class SparqlCoreferenceIngester {

	public static void main(String[] args) throws Exception {
		
		final SameAsSets sameAsSets=new SameAsSets();
		
		SparqlClientWikidata.queryWithPaging("SELECT ?s ?o WHERE { ?s <" + Owl.sameAs + "> ?o .}", 10000, "s", new Handler() {
			@Override
			public boolean handleSolution(QuerySolution solution) throws Exception {
				sameAsSets.addSameAs(solution.getResource("s").getURI(), solution.getResource("o").getURI());
				if(sameAsSets.size() % 1000 ==0)
					System.out.println(sameAsSets.size());
				return true;
			}
		});
	}
}
