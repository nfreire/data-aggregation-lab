package inescid.dataaggregation.dataset;

import java.io.File;
import java.nio.charset.Charset;
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
import inescid.dataaggregation.crawl.ld.HttpRequestService;
import inescid.dataaggregation.dataset.job.JobRunner;
import inescid.dataaggregation.dataset.store.DatasetRegistryRepository;
import inescid.dataaggregation.dataset.store.PublicationRepository;
import inescid.dataaggregation.dataset.store.Repository;
import inescid.dataaggregation.dataset.view.registry.RegistryServlet;
import inescid.dataaggregation.dataset.view.registry.View;

public class Global {
	public static Pattern urlPattern=Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	public static final Charset UTF8 = Charset.forName("UTF8");
	public static final Configuration FREE_MARKER=new Configuration(Configuration.VERSION_2_3_27);
	public static final String SEE_ALSO_DATASET_PREFIX = "seeAlso_"; 
	public static final String CONVERTED_EDM_DATASET_PREFIX = "convertedEdm_"; 
	
	private static DatasetRegistryRepository registryRepository=null;
	private static Repository dataRepository=null;
	private static TimestampTracker timestampTracker=null;
	private static PublicationRepository publicationRepository=null;
	private static JobRunner jobRunner;
	private static HttpRequestService httpRequestService=new HttpRequestService();

	public static HttpRequestService getHttpRequestService() {
		return httpRequestService;
	}
	static {
		Global.FREE_MARKER.setClassLoaderForTemplateLoading(RegistryServlet.class.getClassLoader(), "inescid/dataaggregation/view/template");
		Global.FREE_MARKER.setDefaultEncoding(Global.UTF8.toString());
		Global.FREE_MARKER.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		Global.FREE_MARKER.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);	
	}
	public static synchronized void init(ServletContext servletContext) {
		if(registryRepository==null) {
			initDatasetRegistryRepository(servletContext);
			initDataRepository(servletContext);
			initTimestampTracker(servletContext);
			initJobRunner(servletContext, registryRepository);
			
			publicationRepository=new PublicationRepository();
			File repoHomeFolder = new File(servletContext.getRealPath(""), "/static/data");
			System.out.println("Init of repository at "+repoHomeFolder.getAbsolutePath());
			publicationRepository.init(repoHomeFolder, "static/data/");
			View.initContext(servletContext.getContextPath());
			httpRequestService.init();
		}
	}
	public static DatasetRegistryRepository getDatasetRegistryRepository() {
		return registryRepository;
	}
	private static synchronized void initDatasetRegistryRepository(ServletContext servletContext) {
		if(registryRepository==null) {
			String repositoryFolder = servletContext
					.getInitParameter("dataaggregation.dataset-registry.repository.folder");
			if (repositoryFolder.equals("${dataaggregation.dataset-registry.repository.folder}"))
				repositoryFolder = "C:\\Users\\nfrei\\Desktop\\data-aggregation-lab";
			File requestsLogFile = new File(repositoryFolder, "dataset-registry-requests.csv");
			registryRepository=new DatasetRegistryRepository(requestsLogFile);
		}
	}

		public static Repository getDataRepository() {
			return dataRepository;
		}
		private static synchronized void initDataRepository(ServletContext servletContext) {
		if(dataRepository==null) {
			String repositoryFolder = servletContext
					.getInitParameter("dataaggregation.data-repository.folder");
			if (repositoryFolder.equals("${dataaggregation.data-repository.folder}"))
				repositoryFolder = "C:\\Users\\nfrei\\Desktop\\data-aggregation-lab\\data-repository";
			dataRepository=new Repository();
			dataRepository.init(repositoryFolder);
		}
	}
	
	public static TimestampTracker getTimestampTracker() {
		return timestampTracker;
	}
	
	private static synchronized void initTimestampTracker(ServletContext servletContext) {
		if(timestampTracker==null) {
			String repositoryFolder = servletContext
					.getInitParameter("dataaggregation.timestamp.repository.folder");
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
	private static synchronized void initJobRunner(ServletContext servletContext, DatasetRegistryRepository registryRepository) {
		if(jobRunner==null) {
			String jobRunnerFolder = servletContext
					.getInitParameter("dataaggregation.dataset-registry.repository.folder");
			if (jobRunnerFolder.equals("${dataaggregation.dataset-registry.repository.folder}"))
				jobRunnerFolder = "C:\\Users\\nfrei\\Desktop\\data-aggregation-lab";
			jobRunner=new JobRunner(jobRunnerFolder, registryRepository);
			new Thread(jobRunner).start();
		}
	}
	public static PublicationRepository getPublicationRepository() {
		return publicationRepository;
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
