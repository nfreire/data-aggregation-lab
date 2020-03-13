package inescid.dataaggregation.metadatatester.view;

import java.io.StringWriter;

import freemarker.template.Template;

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
		Template temp = GlobalMetadataTester.FREE_MARKER.getTemplate("metadatatester/"+getClass().getSimpleName()+".html");
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