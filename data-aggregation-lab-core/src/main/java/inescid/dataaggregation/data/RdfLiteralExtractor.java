package inescid.dataaggregation.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Rdfs;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;

public class RdfLiteralExtractor {
	public enum Option { WITH_ALT_LABELS };
//		, ALL_LANGUAGES}

	public static List<Literal> extract(Resource res, Option... options) {
		HashSet<Option> settings=new HashSet<RdfLiteralExtractor.Option>(options.length);
		for(Option o: options)
			settings.add(o);
		
		List<Literal> ret=new ArrayList<Literal>();
		for(Statement st: res.listProperties().toList()) {
			if(st.getPredicate().equals(Rdfs.label) || st.getPredicate().equals(Skos.prefLabel) ||
					(st.getPredicate().equals(Skos.altLabel) && settings.contains(Option.WITH_ALT_LABELS)) ||
					st.getPredicate().equals(Schemaorg.name) ) {
				if(st.getObject().isLiteral())
					ret.add(st.getObject().asLiteral());
			}
		}
		return ret;
	};
}
