package inescid.dataaggregation.dataset.view.registry;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.view.GlobalFrontend;
import inescid.dataaggregation.store.DatasetRegistryRepository;

public class RegistryServlet extends HttpServlet {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(RegistryServlet.class);
	
	enum RequestOperation {
		DISPLAY_LOD_DATASET_REGISTRATION_FORM, DISPLAY_IIIF_DATASET_REGISTRATION_FORM, DISPLAY_CONTACT_FORM, 
		REGISTER_DATASET, REGISTER_CONTACT, VIEW_DATASET_STATUS, DISPLAY_START_PAGE, 
		DISPLAY_WWW_DATASET_REGISTRATION_FORM, BROWSE_IIIF_COLLECTIONS;

		public static RequestOperation fromHttpRequest(HttpServletRequest req) {
//			System.out.println("req.getPathInfo() " + req.getPathInfo());
//			System.out.println("req.getServletPath() " + req.getServletPath());
			if (req.getPathInfo()!=null) {
				if (req.getPathInfo().endsWith("/lod-dataset-register")) {
					if(StringUtils.isEmpty(req.getParameter("registration")))
						return RequestOperation.DISPLAY_LOD_DATASET_REGISTRATION_FORM;
					return RequestOperation.REGISTER_DATASET;
				} else if (req.getPathInfo().endsWith("/iiif-dataset-register")) {
						if(StringUtils.isEmpty(req.getParameter("registration")))
							return RequestOperation.DISPLAY_IIIF_DATASET_REGISTRATION_FORM;
						return RequestOperation.REGISTER_DATASET;
				} else if (req.getPathInfo().endsWith("/www-dataset-register")) {
					if(StringUtils.isEmpty(req.getParameter("registration")))
						return RequestOperation.DISPLAY_WWW_DATASET_REGISTRATION_FORM;
					return RequestOperation.REGISTER_DATASET;
				} else if (req.getPathInfo().endsWith("/dataset-register")) {
					return RequestOperation.REGISTER_DATASET;
				} else if (req.getPathInfo().endsWith("/browse-iiif-service-collections")) {
					return BROWSE_IIIF_COLLECTIONS;
				}
			}
			return RequestOperation.DISPLAY_START_PAGE;
		}
	};

	DatasetRegistryRepository repository;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		GlobalFrontend.init(getInitParameters(config.getServletContext()));
		repository=Global.getDatasetRegistryRepository();
	}
	
	@Override
	public void destroy() {
		super.destroy();
		GlobalFrontend.shutdown();
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
		props.setProperty("dataaggregation.webapp.root-folder", servletContext.getRealPath(""));
		return props;
	}
	protected void doGetOrPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			RequestOperation operation = RequestOperation.fromHttpRequest(req);
			System.out.println(operation);
			switch (operation) {
			case DISPLAY_CONTACT_FORM:
				break;
			case DISPLAY_LOD_DATASET_REGISTRATION_FORM:{
				LodForm form = new LodForm();
				sendResponse(resp, 200, form.output());
				break;
			}case DISPLAY_IIIF_DATASET_REGISTRATION_FORM:{
				IiifForm form = new IiifForm();
				sendResponse(resp, 200, form.output());
				break;
			}case DISPLAY_WWW_DATASET_REGISTRATION_FORM:{
				WwwForm form = new WwwForm();
				sendResponse(resp, 200, form.output());
				break;
			}case REGISTER_CONTACT:
				break;
			case REGISTER_DATASET:{
				DatasetForm datasetToRegister=null;
				if("lod".equals(req.getParameter("type"))) {
					datasetToRegister = new LodForm(req);
				}else if("iiif".equals(req.getParameter("type"))) {
					datasetToRegister = new IiifForm(req);
				}else if("www".equals(req.getParameter("type"))) {
					datasetToRegister = new WwwForm(req);
				}
				
				if(datasetToRegister==null)
					sendResponse(resp, 404, "type ofdataset not supported or empty");
				else {
//				if(! "import".equals(req.getParameter("registration"))) 
					if(datasetToRegister!=null && datasetToRegister.register() && datasetToRegister.validate()) {
						Dataset dataset = datasetToRegister.toDataset();
						repository.registerDataset(dataset);
						DatasetRegistrationResultForm formRes = new DatasetRegistrationResultForm(dataset);
						formRes.setMessage("Dataset registered");
						sendResponse(resp, 200, formRes.output());
					}else
						sendResponse(resp, 200, datasetToRegister.output());
				}
				break;
			} case DISPLAY_START_PAGE:{
				StartPage form = new StartPage();
				sendResponse(resp, 200, form.output());
				break;
			} case VIEW_DATASET_STATUS:
				break;
			case BROWSE_IIIF_COLLECTIONS:{
				Dataset dataset = null;
				IiifCollectionTreeForm form=new IiifCollectionTreeForm(req, dataset);
				sendResponse(resp, 200, form.output());
				break;
			}
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
