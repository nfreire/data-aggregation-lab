package inescid.dataaggregation.dataset.profile;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;

public class CompletenessSpecResources {
	public Map<Resource, CompletenessSpecProperties> resources;
	
	public CompletenessSpecResources() {
		resources=new  HashMap<>();
	}

	public CompletenessSpecProperties addResource(Resource edmProvideresourcedCho) {
		CompletenessSpecProperties propsSpec = null;
		if (resources.containsKey(edmProvideresourcedCho))
			propsSpec= resources.get(edmProvideresourcedCho);
		else {
			propsSpec = new CompletenessSpecProperties();
			resources.put(edmProvideresourcedCho, propsSpec);
		}
		return propsSpec;
	}
}
