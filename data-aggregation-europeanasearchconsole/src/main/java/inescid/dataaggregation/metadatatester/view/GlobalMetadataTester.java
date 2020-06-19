package inescid.dataaggregation.metadatatester.view;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import inescid.dataaggregation.crawl.http.HttpRequestService;
import inescid.dataaggregation.data.validation.EdmXmlValidator;
import inescid.dataaggregation.data.validation.EdmXmlValidator.Schema;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.wikidata.WikidataEdmConverter;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.util.AccessException;

public class GlobalMetadataTester {
//	public static Pattern urlPattern=Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
//	public static final Charset UTF8 = Charset.forName("UTF8");
	public static final Configuration FREE_MARKER=new Configuration(Configuration.VERSION_2_3_27);
//	public static final String SEE_ALSO_DATASET_PREFIX = "seeAlso_"; 
//	public static final String CONVERTED_EDM_DATASET_PREFIX = "convertedEdm_"; 
//	public static String GOOGLE_API_CREDENTIALS = ""; 
	
	private static File webappRoot=null;
	public static WikidataEdmConverter wikidataEdmConverter;
	public static EdmXmlValidator edmValidator;
	
	
	static {
		GlobalMetadataTester.FREE_MARKER.setClassLoaderForTemplateLoading(MetadataTesterServlet.class.getClassLoader(), "inescid/dataaggregation/view/template");
		GlobalMetadataTester.FREE_MARKER.setDefaultEncoding(StandardCharsets.UTF_8.toString());
		GlobalMetadataTester.FREE_MARKER.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		GlobalMetadataTester.FREE_MARKER.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);	
	}
	public static synchronized void init(Properties prop) {
		if(webappRoot==null) {
			String webappRootPath = prop.getProperty("dataaggregation.webapp.root-folder");
			if(webappRootPath==null)
				webappRootPath = prop.getProperty("dataaggregation.publication-repository.folder");
			
			System.out.println("Metadata tester initializing");
			
			webappRoot=new File(webappRootPath);
			Global.init_componentHttpRequestService();

			String resourcesPath = webappRootPath+"/WEB-INF/resources/";
			try {
				wikidataEdmConverter=new WikidataEdmConverter(
						new File(resourcesPath, "wikidata/wikidata_edm_mappings_classes.csv"), 
						new File(resourcesPath, "wikidata/wikidata_edm_mappings.csv"),
						new File(resourcesPath, "wikidata/wikidata_edm_mappings_hierarchy.csv"),
						new File(prop.getProperty("wikidata-triple-store")),
						new File(resourcesPath, "owl/edm.owl"));
			} catch (IOException | AccessException | InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage(), e);
			}

			WikidataUtil.enableMemoryCache(200);
			
			edmValidator=new EdmXmlValidator(new File(resourcesPath), Schema.EDM);
		}
	}
	public static synchronized void shutdown() throws IOException {
		webappRoot=null;
		wikidataEdmConverter.close();
	}

	public static synchronized void init_developement() {
		Properties props=new Properties();
		props.setProperty("dataaggregation.webapp.root-folder", "C:\\Users\\nfrei\\workspace-eclipse\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp1\\wtpwebapps\\data-aggregation-metadatatester");
		props.setProperty("wikidata-triple-store", "C:\\Users\\nfrei\\Desktop\\data\\wikidata-to-edm\\triplestore-wikidata");
		init(props);
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
