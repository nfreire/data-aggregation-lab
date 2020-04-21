package inescid.dataaggregation.dataset.profile.multilinguality;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

import inescid.util.RdfUtil;
import inescid.util.datastruct.MapOfLists;

public class ShapesDetectionReport {
	MapOfLists<String, ShapeInstance> shapes=new MapOfLists<String, ShapeInstance>();

	public void add(ShapeInstance instance) {
		Node shapeNode = instance.getShape().getShapeNode();
		shapes.put(shapeNode.toString(), instance);
	}

	public List<ShapeInstance> getInstancesOfShape(Node shapeNode) {
		return shapes.get(shapeNode.toString());
	}

	public Collection<ShapeInstance> getAllInstances() {
		return shapes.valuesOfAllLists();
	}
}
