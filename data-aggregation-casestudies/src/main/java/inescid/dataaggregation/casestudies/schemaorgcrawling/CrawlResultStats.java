package inescid.dataaggregation.casestudies.schemaorgcrawling;

import java.lang.reflect.Field;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.dataset.Global;
import inescid.opaf.data.profile.MapOfInts;

public class CrawlResultStats extends CrawlResult {

	public void addToStats(CrawlResult result) {
		try {
			for (Field f : CrawlResult.class.getDeclaredFields()) {
				if (f.getType().equals(int.class)) {
					f.setInt(this, f.getInt(this) + CrawlResult.class.getDeclaredField(f.getName()).getInt(result));
				}
			}
			for (Field f : CrawlResult.class.getDeclaredFields()) {
				if (f.getType().equals(MapOfInts.class)) {
					MapOfInts<Resource> resultMap=(MapOfInts<Resource>)CrawlResult.class.getDeclaredField(f.getName()).get(result);					
					MapOfInts<Resource> statsMap = (MapOfInts<Resource>) f.get(this);
					for (Entry<Resource, Integer> classEntry : resultMap.entrySet()) {
						Resource uri = classEntry.getKey();
						Integer resultValue = classEntry.getValue();
						statsMap.addTo(uri, resultValue);
						
					}
				}
			}
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e.getMessage(), e); 
		}
	}
}
