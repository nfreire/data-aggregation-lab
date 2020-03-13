package inescid.europeanarepository;

import java.io.IOException;

import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.Repository;
import inescid.dataaggregation.store.RepositoryResource;

public class ProcessRepository {

	public static void main(String[] args) {
    	String repoFolder = "c://users/nfrei/desktop/data/EuropeanaRepository";
    	String datasetId = "data.europeana.eu"; //"https://api.europeana.eu/oai/record"
		
		if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
				if(args.length>=2) 
					datasetId = args[1];
			}
		}
		
		Global.init_componentDataRepository(repoFolder);
		Global.init_enableComponentHttpRequestCache();
		Repository repository = Global.getDataRepository();
		
		int tmpCnt=0;
		
		Iterable<RepositoryResource> it = repository.getIterableOfResources(datasetId);
		for(RepositoryResource r: it) {
			try {
				String uri = r.getUri();
				byte[] content = r.getContent();
				tmpCnt++;
				if(tmpCnt % 1000 == 0)
					System.out.println(tmpCnt);
			} catch (IOException e) {
				System.err.println("Error reading from repository: "+r.getUri());
				e.printStackTrace();
			}
		}
		
		System.out.println(tmpCnt);
	}

}
