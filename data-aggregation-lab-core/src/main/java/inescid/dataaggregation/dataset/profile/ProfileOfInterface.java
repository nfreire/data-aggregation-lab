package inescid.dataaggregation.dataset.profile;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public interface ProfileOfInterface {
	public void eventInstanceStart(Resource resource);
	public void eventInstanceEnd(Resource resource);
	public void eventProperty(Statement statement);
	public void finish();
	
}
