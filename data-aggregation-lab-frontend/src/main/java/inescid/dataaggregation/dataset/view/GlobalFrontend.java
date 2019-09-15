package inescid.dataaggregation.dataset.view;

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
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.job.JobRunner;
import inescid.dataaggregation.dataset.view.registry.RegistryServlet;
import inescid.dataaggregation.dataset.view.registry.View;
import inescid.dataaggregation.store.DatasetRegistryRepository;
import inescid.dataaggregation.store.PublicationRepository;
import inescid.dataaggregation.store.Repository;

public class GlobalFrontend {
//	public static Pattern urlPattern=Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
//	public static final Charset UTF8 = Charset.forName("UTF8");
	public static final Configuration FREE_MARKER=new Configuration(Configuration.VERSION_2_3_27);
//	public static final String SEE_ALSO_DATASET_PREFIX = "seeAlso_"; 
//	public static final String CONVERTED_EDM_DATASET_PREFIX = "convertedEdm_"; 
//	public static String GOOGLE_API_CREDENTIALS = ""; 
	
	private static File webappRoot=null;

	static {
		GlobalFrontend.FREE_MARKER.setClassLoaderForTemplateLoading(RegistryServlet.class.getClassLoader(), "inescid/dataaggregation/view/template");
		GlobalFrontend.FREE_MARKER.setDefaultEncoding(Global.UTF8.toString());
		GlobalFrontend.FREE_MARKER.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		GlobalFrontend.FREE_MARKER.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);	
	}
	public static synchronized void init(Properties prop) {
			if(webappRoot==null) {
				String webAppRoot = prop.getProperty("dataaggregation.webapp.root-folder");
				if(webAppRoot==null)
					webAppRoot = prop.getProperty("dataaggregation.publication-repository.folder");
				webappRoot=new File(webAppRoot);
			}
			Global.init(prop);
	}
	public static synchronized void shutdown() {
	}

	public static synchronized void init_developement() {
		Properties props=new Properties();
		props.setProperty("dataaggregation.webapp.root-folder", "C:\\Users\\nfrei\\workspace-eclipse\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\data-aggregation-lab");
		init(props);
		Global.init_developement();
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
