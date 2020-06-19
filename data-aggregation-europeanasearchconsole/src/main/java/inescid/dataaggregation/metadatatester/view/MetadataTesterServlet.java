package inescid.dataaggregation.metadatatester.view;

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
import inescid.dataaggregation.store.DatasetRegistryRepository;

public class MetadataTesterServlet extends HttpServlet {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MetadataTesterServlet.class);
	
	public enum RequestOperation {
		DISPLAY_START_PAGE, 
		DISPLAY_IIIF_FORM, ANALYSE_IIIF,
		DISPLAY_SCHEMAORG_FORM, ANALYSE_SCHEMAORG, VALIDATE_SCHEMAORG,
		DISPLAY_WIKIDATA_FORM, ANALYSE_WIKIDATA, VALIDATE_WIKIDATA,
		DISPLAY_EUROPEANA_FORM, ANALYSE_EUROPEANA,
		DISPLAY_SITEMAP_FORM, ANALYSE_SITEMAP;

		public static RequestOperation fromHttpRequest(HttpServletRequest req) {
//			System.out.println("req.getPathInfo() " + req.getPathInfo());
//			System.out.println("req.getServletPath() " + req.getServletPath());
			if (req.getPathInfo()!=null) {
				if (req.getPathInfo().endsWith("/check_iiif_manifest")) {
					if(!StringUtils.isEmpty(req.getParameter("manifestURI")))
						return RequestOperation.ANALYSE_IIIF;
					return RequestOperation.DISPLAY_IIIF_FORM;
				} else if (req.getPathInfo().endsWith("/check_schemaorg")) {
					if(!StringUtils.isEmpty(req.getParameter("webpageURL")))
						return RequestOperation.ANALYSE_SCHEMAORG;
					return RequestOperation.DISPLAY_SCHEMAORG_FORM;
				} else if (req.getPathInfo().endsWith("/check_wikidata")) {
					if(!StringUtils.isEmpty(req.getParameter("wikidataID")))
						return RequestOperation.ANALYSE_WIKIDATA;
					return RequestOperation.DISPLAY_WIKIDATA_FORM;
				} else if (req.getPathInfo().endsWith("/check_europeana")) {
					if(!StringUtils.isEmpty(req.getParameter("europeanaID")))
						return RequestOperation.ANALYSE_EUROPEANA;
					return RequestOperation.DISPLAY_EUROPEANA_FORM;
				} else if (req.getPathInfo().endsWith("/check_sitemap")) {
					if(!StringUtils.isEmpty(req.getParameter("sitemapURL")))
						return RequestOperation.ANALYSE_SITEMAP;
					return RequestOperation.DISPLAY_SITEMAP_FORM;
				} else if (req.getPathInfo().endsWith("/validate_schemaorg")) {
					return RequestOperation.VALIDATE_SCHEMAORG;
				} else if (req.getPathInfo().endsWith("/validate_wikidata")) {
					return RequestOperation.VALIDATE_WIKIDATA;
				}
			}
			return RequestOperation.DISPLAY_START_PAGE;
		}
	};


	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		GlobalMetadataTester.init(getInitParameters(config.getServletContext()));
		View.initContext(config.getServletContext().getContextPath());
	}
	
	@Override
	public void destroy() {
		super.destroy();
		try {
			GlobalMetadataTester.shutdown();
			System.out.println("Destroying servlet");
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private Properties getInitParameters(ServletContext servletContext) {
		Properties props=new Properties();
		Enumeration initParameterNames = servletContext.getInitParameterNames();
		while (initParameterNames.hasMoreElements()) {
			Object pName = initParameterNames.nextElement();
			String initParameter = servletContext.getInitParameter(pName.toString());
			props.setProperty(pName.toString(), initParameter);
		}
		props.setProperty("dataaggregation.webapp.root-folder", servletContext.getRealPath(""));
		return props;
	}
	protected void doGetOrPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			RequestOperation operation = RequestOperation.fromHttpRequest(req);
			System.out.println(operation);
			switch (operation) {
			case ANALYSE_IIIF:{
				IiifForm form = new IiifForm(req);
				form.checkUri();
				sendResponse(resp, 200, form.output());
				break;
			}case ANALYSE_SCHEMAORG:{
				SchemaorgForm form = new SchemaorgForm(req);
				form.checkUri();
				sendResponse(resp, 200, form.output());
				break;
			}case ANALYSE_WIKIDATA:{
				WikidataForm form = new WikidataForm(req);
				form.checkUri();
				sendResponse(resp, 200, form.output());
				break;
			}case ANALYSE_EUROPEANA:{
				EuropeanaForm form = new EuropeanaForm(req);
				form.checkUri();
				sendResponse(resp, 200, form.output());
				break;
			}case ANALYSE_SITEMAP:{
				SitemapForm form = new SitemapForm(req);
				form.check();
				sendResponse(resp, 200, form.output());
				break;
			}case DISPLAY_IIIF_FORM: {
				IiifForm form = new IiifForm();
				sendResponse(resp, 200, form.output());
				break;
			} case DISPLAY_SCHEMAORG_FORM: {
				SchemaorgForm form = new SchemaorgForm();
				sendResponse(resp, 200, form.output());
				break;
			} case DISPLAY_WIKIDATA_FORM: {
				WikidataForm form = new WikidataForm();
				sendResponse(resp, 200, form.output());
				break;
			} case DISPLAY_EUROPEANA_FORM: {
				EuropeanaForm form = new EuropeanaForm();
				sendResponse(resp, 200, form.output());
				break;
			} case DISPLAY_SITEMAP_FORM: {
				SitemapForm form = new SitemapForm();
				sendResponse(resp, 200, form.output());
				break;
			} case VALIDATE_SCHEMAORG: {
				SchemaorgForm form = new SchemaorgForm(req);
				form.validateSchemaorgUri();
				sendResponse(resp, 200, form.output());
				break;
			} case VALIDATE_WIKIDATA: {
				WikidataForm form = new WikidataForm(req);
				form.validateEdmUri();
				sendResponse(resp, 200, form.output());
				break;
			} case DISPLAY_START_PAGE:
				StartPage form = new StartPage();
				sendResponse(resp, 200, form.output());
				break;
			}
//			switch (operation) {
//			case DISPLAY_CONTACT_FORM:
//				break;
//			case DISPLAY_LOD_DATASET_REGISTRATION_FORM:{
//				LodForm form = new LodForm();
//				sendResponse(resp, 200, form.output());
//				break;
//			}case DISPLAY_IIIF_DATASET_REGISTRATION_FORM:{
//				IiifForm form = new IiifForm();
//				sendResponse(resp, 200, form.output());
//				break;
//			}case DISPLAY_WWW_DATASET_REGISTRATION_FORM:{
//				WwwForm form = new WwwForm();
//				sendResponse(resp, 200, form.output());
//				break;
//			}case REGISTER_CONTACT:
//				break;
//			case REGISTER_DATASET:{
//				DatasetForm datasetToRegister=null;
//				if("lod".equals(req.getParameter("type"))) {
//					datasetToRegister = new LodForm(req);
//				}else if("iiif".equals(req.getParameter("type"))) {
//					datasetToRegister = new IiifForm(req);
//				}else if("www".equals(req.getParameter("type"))) {
//					datasetToRegister = new WwwForm(req);
//				}
//				
//				if(datasetToRegister==null)
//					sendResponse(resp, 404, "type ofdataset not supported or empty");
//				else {
////				if(! "import".equals(req.getParameter("registration"))) 
//					if(datasetToRegister!=null && datasetToRegister.register() && datasetToRegister.validate()) {
//						Dataset dataset = datasetToRegister.toDataset();
//						repository.registerDataset(dataset);
//						DatasetRegistrationResultForm formRes = new DatasetRegistrationResultForm(dataset);
//						formRes.setMessage("Dataset registered");
//						sendResponse(resp, 200, formRes.output());
//					}else
//						sendResponse(resp, 200, datasetToRegister.output());
//				}
//				break;
//			} case DISPLAY_START_PAGE:{
//			} case VIEW_DATASET_STATUS:
//				break;
//			case BROWSE_IIIF_COLLECTIONS:{
//				Dataset dataset = null;
//				IiifCollectionTreeForm form=new IiifCollectionTreeForm(req, dataset);
//				sendResponse(resp, 200, form.output());
//				break;
//			}
//			}
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
