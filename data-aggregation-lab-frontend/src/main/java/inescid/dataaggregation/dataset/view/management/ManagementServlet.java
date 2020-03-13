package inescid.dataaggregation.dataset.view.management;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest; 
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import eu.europeana.research.iiif.crawl.ManifestRepository;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.job.Job;
import inescid.dataaggregation.dataset.job.Job.JobType;
import inescid.dataaggregation.dataset.job.JobRunner;
import inescid.dataaggregation.dataset.view.GlobalFrontend;
import inescid.dataaggregation.dataset.view.registry.DatasetRegistrationResultForm;
import inescid.dataaggregation.dataset.view.registry.IiifForm;
import inescid.dataaggregation.dataset.view.registry.LodForm;
import inescid.dataaggregation.dataset.view.registry.StartPage;
import inescid.dataaggregation.dataset.view.registry.View;
import inescid.dataaggregation.store.DatasetRegistryRepository;
import inescid.dataaggregation.store.PublicationRepository;
import opennlp.tools.util.StringUtil;

public class ManagementServlet extends HttpServlet {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(ManagementServlet.class);
	enum RequestOperation {
		DISPLAY_DATASETS,
		DISPLAY_DATASET_STATUS, 
		HARVEST_START,
		HARVEST_START_SAMPLE, 
		PUBLISH_DATASET_DATA, 
		CLEAR_DATASET_DATA, 
		PROFILE_DATASET_RDF, 
		PROFILE_DATASET_MANIFESTS, 
		DIAGNOSE_DATASET, 
		CONVERT_DATASET_DATA, 
		REMOVE_DATASET, 
		HARVEST_START_SEEALSO, 
		PUBLISH_DATASET_SEEALSO_DATA, 
		VALIDATE_EDM,
		SET_FORM_OF_DATA, 
		BROWSE_IIIF_COLLECTIONS;

		public static RequestOperation fromHttpRequest(HttpServletRequest req) {
			if (req.getPathInfo()!=null) {
				if (req.getPathInfo().endsWith("/display-datasets")) {
					return DISPLAY_DATASETS;
				} else if (req.getPathInfo().endsWith("/harvest-start")) {
					return HARVEST_START;
				} else if (req.getPathInfo().endsWith("/harvest-start-sample")) {
					return HARVEST_START_SAMPLE;
				} else if (req.getPathInfo().endsWith("/publish-dataset-data")) {
					return PUBLISH_DATASET_DATA;
				} else if (req.getPathInfo().endsWith("/clear-dataset-data")) {
					return CLEAR_DATASET_DATA;
				} else if (req.getPathInfo().endsWith("/profile-dataset-manifests")) {
					return PROFILE_DATASET_MANIFESTS;
				} else if (req.getPathInfo().endsWith("/diagnose-dataset")) {
					return DIAGNOSE_DATASET;
				} else if (req.getPathInfo().endsWith("/profile-dataset-rdf")) {
					return PROFILE_DATASET_RDF;
				} else if (req.getPathInfo().endsWith("/convert-dataset")) {
					return CONVERT_DATASET_DATA;
				} else if (req.getPathInfo().endsWith("/validate-edm-dataset")) {
					return RequestOperation.VALIDATE_EDM;
				} else if (req.getPathInfo().endsWith("/remove-dataset")) {
					return REMOVE_DATASET;
				} else if (req.getPathInfo().endsWith("/dataset-detail")) {
					return RequestOperation.DISPLAY_DATASET_STATUS;
				} else if (req.getPathInfo().endsWith("/dataset-iiif-harvest-seealso")) {
					return RequestOperation.HARVEST_START_SEEALSO;
				} else if (req.getPathInfo().endsWith("/publish-dataset-seealso-data")) {
					return RequestOperation.PUBLISH_DATASET_SEEALSO_DATA;
				} else if (req.getPathInfo().endsWith("/set-form-of-data")) {
					return RequestOperation.SET_FORM_OF_DATA;
				}
			}
			return DISPLAY_DATASETS;
		}
	};

	DatasetRegistryRepository datasetRepository;
	JobRunner jobRunner;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		GlobalFrontend.init(getInitParameters(config.getServletContext()));
		View.initContext(config.getServletContext().getContextPath());
		datasetRepository=Global.getDatasetRegistryRepository();
		jobRunner=Global.getJobRunner();
	}
	
	private Properties getInitParameters(ServletContext servletContext) {
		Properties props=new Properties();
		Enumeration initParameterNames = servletContext.getInitParameterNames();
		while (initParameterNames.hasMoreElements()) {
			Object pName = initParameterNames.nextElement();
			String initParameter = servletContext.getInitParameter(pName.toString());
			props.setProperty(pName.toString(), initParameter);
			
		}
		props.setProperty("dataaggregation.publication-repository.folder", servletContext.getRealPath(""));
		props.setProperty("dataaggregation.publication-repository.url", "/static/data");
		return props;
	}

	protected void doGetOrPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			RequestOperation operation = RequestOperation.fromHttpRequest(req);
			System.out.println(operation);
			
			
			
			
			switch (operation) {
			case DISPLAY_DATASETS: {
				ListDatasetsView view=new ListDatasetsView(datasetRepository, jobRunner, req);
				sendResponse(resp, 200, view.output());
				break;
			} case DISPLAY_DATASET_STATUS:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
				DatasetStatusView view=new DatasetStatusView(dataset);
				sendResponse(resp, 200, view.output());
				break;
			}case HARVEST_START:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
				jobRunner.addJob(new Job(Job.JobType.HARVEST, dataset));
				sendResponse(resp, 200, new JobStatus("Harvesting of the dataset will be executed. The status of the harvesting process can be followed in the page of the dataset.", dataset).output());
				break;
			}case HARVEST_START_SAMPLE:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
				jobRunner.addJob(new Job(Job.JobType.HARVEST_SAMPLE, dataset));
				sendResponse(resp, 200, new JobStatus("Harvesting of a sample of the dataset will be executed. The status of the harvesting process can be followed in the page of the dataset.", dataset).output());
				break;
			} case PUBLISH_DATASET_DATA:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
				jobRunner.addJob(new Job(JobType.PUBLISH_DATA, dataset));
				sendResponse(resp, 200, new JobStatus("Publication of the dataset will be executed. The public link for the published data will later be available in the  page of the dataset.", dataset).output());
				break;
			} case PUBLISH_DATASET_SEEALSO_DATA:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
				jobRunner.addJob(new Job(JobType.PUBLISH_SEEALSO_DATA, dataset));
				sendResponse(resp, 200, new JobStatus("Publication of the dataset will be executed. The public link for the published data will later be available in the page of the dataset.", dataset).output());
				break;
			}case CLEAR_DATASET_DATA:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
				if(req.getParameter("confirmation")!=null && req.getParameter("confirmation").equals("yes")) {
					clearDatasetData(dataset);
					datasetRepository.updateDataset(dataset);
					sendResponse(resp, 200, new JobStatus("All data has been cleared.", dataset).output());
				} else {
					ConfirmDialog confirm=new ConfirmDialog();
					confirm.title=dataset.getTitle();
					confirm.operation="clear-dataset-data";
					confirm.setMessage("Clearing the dataset will remove all export(s), and all harvested metadata. Would you like to proceed?");
					confirm.datasetLocalId=dataset.getLocalId();
					sendResponse(resp, 200, confirm.output());
				}
				break;
			}case PROFILE_DATASET_MANIFESTS:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
				jobRunner.addJob(new Job(JobType.PROFILE_MANIFESTS, dataset));
				sendResponse(resp, 200, new JobStatus("Profiling of the dataset will be executed. The link to the profile results will later be available in the page of the dataset.", dataset).output());
				break;
			}case DIAGNOSE_DATASET:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
				jobRunner.addJob(new Job(JobType.DIAGNOSE_DATASET, dataset));
				sendResponse(resp, 200, new JobStatus("Diagnosis of the dataset for aggregation by Europeana will be executed. The link to the results will later be available in the page of the dataset.", dataset).output());
				break;
			}case PROFILE_DATASET_RDF:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
				jobRunner.addJob(new Job(JobType.PROFILE_RDF, dataset));
				sendResponse(resp, 200, new JobStatus("Profiling of the dataset will be executed. The link to the profile results will later be available in the page of the dataset.", dataset).output());
				break;
			}case CONVERT_DATASET_DATA:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
				jobRunner.addJob(new Job(JobType.CONVERT, dataset));
				sendResponse(resp, 200, new JobStatus("To EDM Conversion of the dataset will be executed. The link to the EDM export will later be available in the page of the dataset.", dataset).output());
				break;
			}case VALIDATE_EDM:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
				jobRunner.addJob(new Job(JobType.VALIDATE_EDM, dataset));
				sendResponse(resp, 200, new JobStatus("To EDM Validation of the dataset will be executed. The link to the report export will later be available in the page of the dataset.", dataset).output());
				break;
			}case REMOVE_DATASET:{
				if(req.getParameter("confirmation")!=null && req.getParameter("confirmation").equals("yes")) {
					Dataset dataset = datasetRepository.removeDataset(req.getParameter("dataset"));
					if(dataset!=null) 
						clearDatasetData(dataset);
					ListDatasetsView view=new ListDatasetsView(datasetRepository, jobRunner, req);
					view.setMessage("Dataset has been removed");
					sendResponse(resp, 200, view.output());
				} else {
					Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));
					ConfirmDialog confirm=new ConfirmDialog();
					confirm.title=dataset.getTitle();
					confirm.operation="remove-dataset";
					confirm.setMessage("Removing the dataset will remove all export(s), all harvested metadata, and its entry from the Dataset Registry. Would you like to proceed?");
					confirm.datasetLocalId=dataset.getLocalId();
					sendResponse(resp, 200, confirm.output());
				}
				break;
			}case HARVEST_START_SEEALSO:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));				
				String harvestParam = req.getParameter("harvestSeeAlso");
				String seeAlsoParam = req.getParameter("seeAlso");
				HarvestIiifSeeAlsoForm form=new HarvestIiifSeeAlsoForm(dataset, seeAlsoParam, Global.getPublicationRepository(), harvestParam!=null && harvestParam.equals("Initiate harvest")); 
				if(form.executeJob()) {
					jobRunner.addJob(new Job(JobType.HARVEST_SEEALSO, dataset, seeAlsoParam));
					form.setMessage("Harvesting of the dataset will be executed. The status of the harvesting process can be followed in the page of the dataset.");
				}
				sendResponse(resp, 200, form.output());
				break;
			}case SET_FORM_OF_DATA:{
				Dataset dataset = datasetRepository.getDataset(req.getParameter("dataset"));				
				String dataProfileParam = req.getParameter("dataProfile");
				String detectParam = req.getParameter("detectFormOfData");
				SetDataProfileForm form=new SetDataProfileForm(dataset, dataProfileParam, detectParam); 
				form.executeJob();
				sendResponse(resp, 200, form.output());							
				break;
			}default:
				break;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			sendResponse(resp, 500, "Internal error: " + e.getMessage());
		}

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGetOrPost(req, resp);
	}

	protected void clearDatasetData(Dataset dataset) throws Exception {
		Global.getPublicationRepository().clear(dataset);
		Global.getDataRepository().clear(dataset.getUri());
		Global.getTimestampTracker().clear(dataset.getUri());
		Global.getTimestampTracker().clear(dataset.getConvertedEdmDatasetUri());
		if(dataset.getType()==DatasetType.IIIF)
			Global.getTimestampTracker().clear(((IiifDataset)dataset).getSeeAlsoDatasetUri());
		Global.getTimestampTracker().commit();
		dataset.setDataFormat(null);
		dataset.setDataProfile(null);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGetOrPost(req, resp);
	}

	protected void sendResponse(HttpServletResponse resp, int httpStatus, String body) throws IOException {
		log.info("Response HTTP status: "+ httpStatus);
		resp.setStatus(httpStatus);
		if (body != null && !body.isEmpty()) {
			ServletOutputStream outputStream = resp.getOutputStream();
			outputStream.write(body.getBytes(Global.UTF8));
			resp.setContentType("text/html; charset=utf-8");
		}
	}
}
