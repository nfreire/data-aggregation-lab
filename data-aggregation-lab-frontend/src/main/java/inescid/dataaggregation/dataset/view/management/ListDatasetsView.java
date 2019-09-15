package inescid.dataaggregation.dataset.view.management;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.job.Job;
import inescid.dataaggregation.dataset.job.JobLog;
import inescid.dataaggregation.dataset.job.JobRunner;
import inescid.dataaggregation.dataset.view.GlobalFrontend;
import inescid.dataaggregation.dataset.view.registry.View;
import inescid.dataaggregation.store.DatasetRegistryRepository;

public class ListDatasetsView extends View {
	
	public ArrayList<DatasetStatusView> datasetsViews;
	public String running;
	
	public ListDatasetsView(DatasetRegistryRepository repository, JobRunner jobRunner, HttpServletRequest req) throws IOException {
		datasetsViews=new ArrayList<>();
		
		for(Dataset ds: repository.listDatasets()) {
			List<JobLog> jobs=jobRunner.listJobHistoric(ds.getLocalId());
			DatasetStatusView dsView=new DatasetStatusView(ds, jobs);
			datasetsViews.add(dsView);
		}
		
		running=jobRunner.getStatusMessage();
	}
	
	public String output() throws Exception {
		StringWriter w=new StringWriter();
		Template temp = GlobalFrontend.FREE_MARKER.getTemplate("datasetmanagement/"+getClass().getSimpleName()+".html");
		temp.process(this, w);
		w.close();
		return w.toString();
	}

	public ArrayList<DatasetStatusView> getDatasetsViews() {
		return datasetsViews;
	}

	public String getRunning() {
		return running;
	}

}
