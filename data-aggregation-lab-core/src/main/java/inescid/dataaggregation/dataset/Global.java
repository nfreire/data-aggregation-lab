package inescid.dataaggregation.dataset;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.regex.Pattern;

import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.dataaggregation.crawl.http.HttpRequestService;
import inescid.dataaggregation.dataset.job.JobRunner;
import inescid.dataaggregation.store.DatasetRegistryRepository;
import inescid.dataaggregation.store.PublicationRepository;
import inescid.dataaggregation.store.Repository;
import inescid.util.googlesheets.GoogleApi;

public class Global {
	public static Pattern urlPattern=Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	public static final Charset UTF8 = Charset.forName("UTF8");
	public static final String SEE_ALSO_DATASET_PREFIX = "seeAlso_"; 
	public static final String CONVERTED_EDM_DATASET_PREFIX = "convertedEdm_"; 
//	public static String GOOGLE_API_CREDENTIALS = ""; 
	
	private static DatasetRegistryRepository registryRepository=null;
	private static Repository dataRepository=null;
	private static TimestampTracker timestampTracker=null;
	private static PublicationRepository publicationRepository=null;
	private static JobRunner jobRunner;
	private static HttpRequestService httpRequestService=new HttpRequestService();

	public static HttpRequestService getHttpRequestService() {
		return httpRequestService;
	}
	public static synchronized void init(Properties prop) {
		try {
			if(registryRepository==null) {
				initDatasetRegistryRepository(prop);
				initDataRepository(prop);
				initTimestampTracker(prop);

				publicationRepository=new PublicationRepository();
				File repoHomeFolder = new File(prop.getProperty("dataaggregation.publication-repository.folder"), prop.getProperty("dataaggregation.publication-repository.url"));
				System.out.println("Init of repository at "+repoHomeFolder.getAbsolutePath());
				publicationRepository.init(repoHomeFolder, "static/data/");
				httpRequestService.init();

				initJobRunner(prop, registryRepository);
				
				String GoogleApiCredentials=prop.getProperty("googleapi.credentials");
				if (GoogleApiCredentials.equals("${googleapi.credentials}"))
//					GOOGLE_API_CREDENTIALS = "c:/users/nfrei/.credentials/credentials-google-api.json";
				GoogleApiCredentials = "c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json";
				init_componentGoogleApi(GoogleApiCredentials);
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	public static synchronized void shutdown() {
		if(jobRunner!=null) 
			jobRunner.shutdown();
	}

	public static synchronized void init_developement() {
		Properties props=new Properties();
		props.setProperty("dataaggregation.dataset-registry.repository.folder", "${dataaggregation.dataset-registry.repository.folder}");
		props.setProperty("dataaggregation.data-repository.folder", "${dataaggregation.data-repository.folder}");
		props.setProperty("dataaggregation.timestamp.repository.folder", "${dataaggregation.timestamp.repository.folder}");
		props.setProperty("dataaggregation.publication-repository.folder", "C:\\Users\\nfrei\\workspace-eclipse\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\data-aggregation-lab");
		props.setProperty("dataaggregation.publication-repository.url", "/static/data");
		props.setProperty("googleapi.credentials", "C:\\Users\\nfrei\\.credentials\\Data Aggregation Lab-b1ec5c3705fc.json");
		init(props);
	}
	
	public static DatasetRegistryRepository getDatasetRegistryRepository() {
		return registryRepository;
	}
	private static synchronized void initDatasetRegistryRepository(Properties prop) throws IOException {
		if(registryRepository==null) {
			String repositoryFolder = prop
					.getProperty("dataaggregation.dataset-registry.repository.folder");
			if (repositoryFolder.equals("${dataaggregation.dataset-registry.repository.folder}"))
				repositoryFolder = "C:\\Users\\nfrei\\Desktop\\data-aggregation-lab";
			File requestsLogFile = new File(repositoryFolder, "dataset-registry-requests.csv");
			registryRepository=new DatasetRegistryRepository(requestsLogFile);
		}
	}

		public static Repository getDataRepository() {
			return dataRepository;
		}
		private static synchronized void initDataRepository(Properties prop) {
		if(dataRepository==null) {
			String repositoryFolder = prop
					.getProperty("dataaggregation.data-repository.folder");
			if (repositoryFolder==null || repositoryFolder.equals("${dataaggregation.data-repository.folder}"))
				repositoryFolder = "C:\\Users\\nfrei\\Desktop\\data-aggregation-lab\\data-repository";
			dataRepository=new Repository();
			dataRepository.init(repositoryFolder);
		}
	}
	
	public static TimestampTracker getTimestampTracker() {
		return timestampTracker;
	}
	
	private static synchronized void initTimestampTracker(Properties prop) {
		if(timestampTracker==null) {
			String repositoryFolder = prop
					.getProperty("dataaggregation.timestamp.repository.folder");
			if (repositoryFolder.equals("${dataaggregation.timestamp.repository.folder}"))
				repositoryFolder = "C:\\Users\\nfrei\\Desktop\\data-aggregation-lab\\timestamp-db";
			timestampTracker=new InMemoryTimestampStore(repositoryFolder);
			try {
				timestampTracker.open();
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}
	
	public static JobRunner getJobRunner() {
		return jobRunner;
	}
	private static synchronized void initJobRunner(Properties prop, DatasetRegistryRepository registryRepository) {
		if(jobRunner==null) {
			String jobRunnerFolder = prop
					.getProperty("dataaggregation.dataset-registry.repository.folder");
			if (jobRunnerFolder.equals("${dataaggregation.dataset-registry.repository.folder}"))
				jobRunnerFolder = "C:\\Users\\nfrei\\Desktop\\data-aggregation-lab";
			jobRunner=new JobRunner(jobRunnerFolder, registryRepository);
			new Thread(jobRunner).start();
		}
	}
	public static PublicationRepository getPublicationRepository() {
		return publicationRepository;
	}

	public static File getValidatorResourceFolder() {
		URL resource = Global.class.getClassLoader().getResource("edmschema/EDM.xsd");
		try {
			return Paths.get(resource.toURI()).toFile().getParentFile().getParentFile();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	public static void init_componentHttpRequestService() {
		httpRequestService.init();
	}
	public static void init_enableComponentHttpRequestCache() {
		if((dataRepository==null))
			throw new IllegalArgumentException("Data repository is not initialized."); 
		if((httpRequestService==null))
			throw new IllegalArgumentException("HTTP request service is not initialized."); 
		httpRequestService.initEnableCache();
	}
	public static void init_componentDataRepository(String repositoryFolder) {
		Properties repoProps=new Properties();
		repoProps.setProperty("dataaggregation.data-repository.folder", repositoryFolder);
		initDataRepository(repoProps);
	}
	public static void init_componentGoogleApi(String credentialsFilePath) {
		GoogleApi.init(credentialsFilePath);
	}
//	private static void initLogging() {
//        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
//        final org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
//        Layout layout = PatternLayout.createLayout(PatternLayout.SIMPLE_CONVERSION_PATTERN, config, null,
//            null,null, null);
//        Appender appender = FileAppender.createAppender("target/test.log", "false", "false", "File", "true",
//            "false", "false", "4000", layout, null, "false", null, config);
//        appender.start();
//        config.addAppender(appender);
//        AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
//        AppenderRef[] refs = new AppenderRef[] {ref};
//        LoggerConfig loggerConfig = LoggerConfig.createLogger("false", "info", "org.apache.logging.log4j",
//            "true", refs, null, config, null );
//        loggerConfig.addAppender(appender, null, null);
//        config.addLogger("org.apache.logging.log4j", loggerConfig);
//        ctx.updateLoggers();
//	}
}
