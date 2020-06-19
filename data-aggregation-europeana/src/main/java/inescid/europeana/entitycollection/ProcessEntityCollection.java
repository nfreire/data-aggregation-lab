package inescid.europeana.entitycollection;

import java.io.File;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import inescid.europeana.dataprocessing.EdmMeasurementSet;
import inescid.europeana.dataprocessing.ProcessRepositoryForEdmMeasurements;
import inescid.europeana.dataprocessing.ProgressTrackerOnFile;
import inescid.europeanarepository.EdmMongoServer;
import inescid.europeanarepository.EdmMongoServer.Handler;

public class ProcessEntityCollection {

	
	public static void main(String[] args) throws Exception {
		String outputFolder = "c://users/nfrei/desktop/data/";

		if (args != null) {
			if (args.length >= 1) {
				outputFolder = args[0];
			}
		}

//		Global.init_componentDataRepository(repoFolder);
//		Global.init_enableComponentHttpRequestCache();
//		Repository repository = Global.getDataRepository();
		EdmMongoServer edmMongo = new EdmMongoServer("mongodb://rnd-2.eanadev.org:27017/admin",
				"metis-preview-production-2");

		EdmMeasurementSet measurements = EdmMeasurementSet.getMeasurementSetA();

		File mapsFile = new File(outputFolder, "edm-measurements.mvstore.bin");
		if (!mapsFile.getParentFile().exists())
			mapsFile.getParentFile().mkdirs();
		MVStore mvStore = new MVStore.Builder().fileName(mapsFile.getPath()).open();

		MVMap<String, String> idToCsv = mvStore.openMap(mapsFile.getName());

		final ProgressTrackerOnFile tracker=new ProgressTrackerOnFile(new File(outputFolder, ProcessRepositoryForEdmMeasurements.class.getSimpleName()+"_progress.txt"));
		final int offset=tracker.getTokenAsInt();
		System.out.println("Starting at mongo offset "+offset);

	}
	
	
}
