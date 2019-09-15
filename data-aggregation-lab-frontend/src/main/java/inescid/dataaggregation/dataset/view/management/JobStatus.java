package inescid.dataaggregation.dataset.view.management;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.view.GlobalFrontend;
import inescid.dataaggregation.dataset.view.registry.View;

public class JobStatus extends View{
	public String datasetTitle=null;
	public String datasetLocalId=null;
	
	public JobStatus() {
	}
	
	public JobStatus(String message, Dataset dataset) {
//		public JobStatus(String message, String datasetTitle, String datasetLocalId) {
		super(message);
		this.datasetTitle=dataset.getTitle();
		this.datasetLocalId = dataset.getLocalId();
//		this.datasetTitle=datasetTitle;
//		this.datasetLocalId = datasetLocalId;
	}

	public String output() throws Exception {
			StringWriter w=new StringWriter();
			Template temp = GlobalFrontend.FREE_MARKER.getTemplate("datasetmanagement/"+getClass().getSimpleName()+".html");
			temp.process(this, w);
			w.close();
			return w.toString();
	}

	public String getDatasetTitle() {
		return datasetTitle;
	}

	public String getDatasetLocalId() {
		return datasetLocalId;
	}
	

}
