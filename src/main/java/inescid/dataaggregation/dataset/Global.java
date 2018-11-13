package inescid.dataaggregation.dataset;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import eu.europeana.research.iiif.crawl.ManifestRepository;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import inescid.dataaggregation.crawl.http.HttpRequestService;
import inescid.dataaggregation.dataset.job.JobRunner;
import inescid.dataaggregation.dataset.view.registry.RegistryServlet;
import inescid.dataaggregation.dataset.view.registry.View;
import inescid.dataaggregation.store.DatasetRegistryRepository;
import inescid.dataaggregation.store.PublicationRepository;
import inescid.dataaggregation.store.Repository;

public class Global {
	public static Pattern urlPattern=Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	public static final Charset UTF8 = Charset.forName("UTF8");
	public static final Configuration FREE_MARKER=new Configuration(Configuration.VERSION_2_3_27);
	public static final String SEE_ALSO_DATASET_PREFIX = "seeAlso_"; 
	public static final String CONVERTED_EDM_DATASET_PREFIX = "convertedEdm_"; 
	public static final String DAL_URI = "https://example.org/dal"; 
	public static String GOOGLE_API_CREDENTIALS = ""; 
	
	private static DatasetRegistryRepository registryRepository=null;
	private static Repository dataRepository=null;
	private static TimestampTracker timestampTracker=null;
	private static PublicationRepository publicationRepository=null;
	private static JobRunner jobRunner;
	private static HttpRequestService httpRequestService=new HttpRequestService();

	private static File webappRoot=null;

	public static HttpRequestService getHttpRequestService() {
		return httpRequestService;
	}
	static {
		Global.FREE_MARKER.setClassLoaderForTemplateLoading(RegistryServlet.class.getClassLoader(), "inescid/dataaggregation/view/template");
		Global.FREE_MARKER.setDefaultEncoding(Global.UTF8.toString());
		Global.FREE_MARKER.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		Global.FREE_MARKER.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);	
	}
	public static synchronized void init(Properties prop) {
		try {
			if(registryRepository==null) {
				String webAppRoot = prop.getProperty("dataaggregation.webapp.root-folder");
				if(webAppRoot==null)
					webAppRoot = prop.getProperty("dataaggregation.publication-repository.folder");
				webappRoot=new File(webAppRoot);
				
				initDatasetRegistryRepository(prop);
				initDataRepository(prop);
				initTimestampTracker(prop);

				publicationRepository=new PublicationRepository();
				File repoHomeFolder = new File(prop.getProperty("dataaggregation.publication-repository.folder"), prop.getProperty("dataaggregation.publication-repository.url"));
				System.out.println("Init of repository at "+repoHomeFolder.getAbsolutePath());
				publicationRepository.init(repoHomeFolder, "static/data/");
				httpRequestService.init();

				initJobRunner(prop, registryRepository);
				
				GOOGLE_API_CREDENTIALS=prop.getProperty("googleapi.credentials");
				if (GOOGLE_API_CREDENTIALS.equals("${googleapi.credentials}"))
//					GOOGLE_API_CREDENTIALS = "c:/users/nfrei/.credentials/credentials-google-api.json";
				GOOGLE_API_CREDENTIALS = "c:/users/nfrei/.credentials/Data Aggregation Lab-b1ec5c3705fc.json";
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
		props.setProperty("dataaggregation.webapp.root-folder", "C:\\Users\\nfrei\\workspace-eclipse\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\data-aggregation-lab");
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
			if (repositoryFolder.equals("${dataaggregation.data-repository.folder}"))
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
		return new File(webappRoot, "WEB-INF/classes");
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
