package inescid.dataaggregation.dataset.view.registry;

import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.Template;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.view.GlobalFrontend;

public class View {
	protected static String WEBAPP_CONTEXT="/";

	protected String message=null;
	
	public View() {
	}
	
	public View(String message) {
		super();
		this.message = message;
	}

	public static void initContext(String webappContext) {
		WEBAPP_CONTEXT=webappContext;
	}
	
	public String output() throws Exception {
		StringWriter w=new StringWriter();
		Template temp = GlobalFrontend.FREE_MARKER.getTemplate("datasetregistry/"+getClass().getSimpleName()+".html");
		temp.process(this, w);
		w.close();
		return w.toString();
	}

	public boolean validate() {
		return true;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getContext() {
		return WEBAPP_CONTEXT;
	}
}