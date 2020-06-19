package inescid.dataaggregation.casestudies.edm.alignment;

import java.io.File;
import java.util.Iterator;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.europeanarepository.EdmMongoServer;

public class ScriptViewMvstore {
	
	public static void main(String[] args) throws Exception {
		String outputFolder = "c://users/nfrei/desktop/data/";
		if (args != null) {
			if (args.length >= 1) {
				outputFolder = args[0];
			}
		}
	//	Global.init_componentDataRepository(repoFolder);
	//	Global.init_enableComponentHttpRequestCache();
	//	Repository repository = Global.getDataRepository();
		EdmMongoServer edmMongo = new EdmMongoServer("mongodb://rnd-2.eanadev.org:27017/admin",
				"metis-preview-production-2");
	
		File mapsFile = new File(outputFolder, "context_uris.mvstore.bin");
		if (!mapsFile.getParentFile().exists())
			mapsFile.getParentFile().mkdirs();
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();
	
		MVMap<String, String> urisConcept = mvStore.openMap("Concept");
		MVMap<String, String> urisPlace = mvStore.openMap("Place");
		MVMap<String, String> urisAgent = mvStore.openMap("Agent");
		MVMap<String, String> urisTimespan = mvStore.openMap("Timespan");
		
		System.out.println(urisConcept.size());
		System.out.println(urisAgent.size());
		System.out.println(urisPlace.size());
		System.out.println(urisTimespan.size());
		
		for(Iterator<String> it = urisTimespan.keyIterator(null) ; it.hasNext() ; ) {
			String uri=it.next();
			System.out.println(uri);
		}
		
		mvStore.close();
		
		
	}
}
