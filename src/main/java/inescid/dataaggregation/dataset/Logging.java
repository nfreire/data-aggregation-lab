package inescid.dataaggregation.dataset;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class Logging {
//	
//	
//	static public class WebappConfigurationFactory extends ConfigurationFactory {
//		 
//	    /**
//	     * Valid file extensions for XML files.
//	     */
//	    public static final String[] SUFFIXES = new String[] {".xml", "*"};
//	 
//	    /**
//	     * Return the Configuration.
//	     * @param source The InputSource.
//	     * @return The Configuration.
//	     */
//	    public Configuration getConfiguration(InputSource source) {
//	        return new WebappConfiguration(source, configFile);
//	    }
//	 
//	    /**
//	     * Returns the file suffixes for XML files.
//	     * @return An array of File extensions.
//	     */
//	    public String[] getSupportedTypes() {
//	        return SUFFIXES;
//	    }
//	}
//	 
//	public class WebappConfiguration extends XmlConfiguration {
//	    public WebappConfiguration(final LoggerContext loggerContext, final ConfigurationSource configSource) {
//	      super(loggerContext, configSource);
//	    }
//	 
//	    @Override
//	    protected void doConfigure() {
//	        super.doConfigure();
//	        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
//	        final Layout<Serializable> layout = PatternLayout.createLayout(PatternLayout.SIMPLE_CONVERSION_PATTERN, config, null,
//	              null,null, null);
//	        final Appender appender = FileAppender.createAppender("target/test.log", "false", "false", "File", "true",
//	              "false", "false", "4000", layout, null, "false", null, config);
//	        appender.start();
//	        addAppender(appender);
//	        LoggerConfig loggerConfig = LoggerConfig.createLogger("false", "info", "org.apache.logging.log4j",
//	              "true", refs, null, config, null );
//	        loggerConfig.addAppender(appender, null, null);
//	        addLogger("org.apache.logging.log4j", loggerConfig);
//	    }
//	}
}
