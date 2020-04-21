package inescid.dataaggregation.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class RdfLiteralExtractor {
	public enum Option { WITH_ALT_LABELS };
//		, ALL_LANGUAGES}

	public static List<Literal> extract(Resource res, Option... options) {
		HashSet<Option> settings=new HashSet<RdfLiteralExtractor.Option>(options.length);
		for(Option o: options)
			settings.add(o);
		
		System.out.println("Getting literals from "+ res.getURI());
		
		List<Literal> ret=new ArrayList<Literal>();
		for(Statement st: res.listProperties().toList()) {
			if(st.getPredicate().equals(RegRdfs.label) || st.getPredicate().equals(RegSkos.prefLabel) ||
					(st.getPredicate().equals(RegSkos.altLabel) && settings.contains(Option.WITH_ALT_LABELS)) ||
					st.getPredicate().equals(RegSchemaorg.name) ) {
				if(st.getObject().isLiteral())
					ret.add(st.getObject().asLiteral());
			}
		}
		return null;
	};
}
