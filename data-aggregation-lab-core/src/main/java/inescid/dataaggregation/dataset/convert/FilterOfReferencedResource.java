package inescid.dataaggregation.dataset.convert;

import org.apache.jena.rdf.model.Statement;

public interface FilterOfReferencedResource {

	public boolean filterOut(Statement reference);
}
