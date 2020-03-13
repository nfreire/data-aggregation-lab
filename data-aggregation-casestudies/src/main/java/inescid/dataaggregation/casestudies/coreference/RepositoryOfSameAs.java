package inescid.dataaggregation.casestudies.coreference;

import java.io.File;
import java.util.HashMap;

import org.h2.mvstore.MVStore;

public class RepositoryOfSameAs {
	File homeFolder;

	HashMap<String, SameAsSets> setsMap=new HashMap<String, SameAsSets>();
	
	public RepositoryOfSameAs(File homeFolder) {
		super();
		this.homeFolder = homeFolder;
	}
	
	public SameAsSets getSameAsSet(String datasetId) {
		SameAsSets set = setsMap.get(datasetId);
		if(set==null) {
			File mapsFile = new File(homeFolder, datasetId+"-mvstore.bin");
			if(!mapsFile.getParentFile().exists())
				mapsFile.getParentFile().mkdirs();
			MVStore mvStore = new MVStore.Builder().
				    fileName(mapsFile.getPath()).open();
			set=new SameAsSets(mvStore, datasetId);
			setsMap.put(datasetId, set);
		}
		return set;
	}
	
	public void close() {
		for (SameAsSets set: setsMap.values()) {
			set.closeStore();
		}
	}

	public File getHomeFolder() {
		return homeFolder;
	}
}
