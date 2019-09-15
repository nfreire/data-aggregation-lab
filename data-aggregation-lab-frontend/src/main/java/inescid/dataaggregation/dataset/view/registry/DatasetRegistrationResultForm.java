package inescid.dataaggregation.dataset.view.registry;

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

public class DatasetRegistrationResultForm extends View {
	Dataset dataset;
	
	public DatasetRegistrationResultForm(Dataset dataset) {
		this.dataset = dataset;
	}

	public String output() throws Exception{
		StringWriter w=new StringWriter();
		Template temp = GlobalFrontend.FREE_MARKER.getTemplate("datasetregistry/"+getClass().getSimpleName()+".html");
		temp.process(this, w);
		w.close();
		return w.toString();
	}

	public String getDatasetTitle() {
		return dataset.getTitle() == null ? "[no dataset title available]" : dataset.getTitle();
	}
	
	@Override
	public boolean validate() {
		return true;
	}


}
