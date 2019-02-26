package inescid.dataaggregation.dataset.profile.completeness;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class CompletenessSpecResources {
	public Map<Resource, CompletenessSpecOfProperties> resources;
	
	public CompletenessSpecResources() {
		resources=new  HashMap<>();
	}

	public CompletenessSpecOfProperties addResource(Resource edmProvideresourcedCho) {
		CompletenessSpecOfProperties propsSpec = null;
		if (resources.containsKey(edmProvideresourcedCho))
			propsSpec= resources.get(edmProvideresourcedCho);
		else {
			propsSpec = new CompletenessSpecOfProperties();
			resources.put(edmProvideresourcedCho, propsSpec);
		}
		return propsSpec;
	}
}
