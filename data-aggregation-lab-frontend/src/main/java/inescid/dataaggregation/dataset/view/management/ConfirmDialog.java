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

public class ConfirmDialog extends View{
	public String title=null;
	public String datasetLocalId=null;
	public String operation=null;
	
	public ConfirmDialog() {
	}

	public ConfirmDialog(String title, String message, String datasetLocalId, String operation) {
		super(message);
		this.title = title;
		this.datasetLocalId = datasetLocalId;
		this.operation = operation;
	}


	public String output() throws Exception {
			StringWriter w=new StringWriter();
			Template temp = GlobalFrontend.FREE_MARKER.getTemplate("datasetmanagement/"+getClass().getSimpleName()+".html");
			temp.process(this, w);
			w.close();
			return w.toString();
	}

	public String getTitle() {
		return title;
	}

	public String getDatasetLocalId() {
		return datasetLocalId;
	}

	public String getOperation() {
		return operation;
	}
	

}
