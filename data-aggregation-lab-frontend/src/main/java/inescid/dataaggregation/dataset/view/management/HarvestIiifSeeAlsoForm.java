package inescid.dataaggregation.dataset.view.management;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import eu.europeana.research.iiif.profile.SeeAlsoProfile;
import freemarker.template.Template;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.LodDataset;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;
import inescid.dataaggregation.dataset.job.JobRunner;
import inescid.dataaggregation.dataset.view.GlobalFrontend;
import inescid.dataaggregation.dataset.view.registry.DatasetView;
import inescid.dataaggregation.store.PublicationRepository;

public class HarvestIiifSeeAlsoForm extends DatasetView {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(HarvestIiifSeeAlsoForm.class);

	protected List<String> seeAlso;
	protected String seeAlsoSelect;
	protected PublicationRepository publicationRepository;
	boolean execute=false;
	
	public HarvestIiifSeeAlsoForm(Dataset dataset, String seeAlsoSelect, PublicationRepository publicationRepository, boolean execute) throws IOException {
		super(dataset);
		this.publicationRepository = publicationRepository;
		this.seeAlsoSelect = seeAlsoSelect;
		if(!execute || !validate()) {
			seeAlso=readSeeAlso();
			execute=false;
		}else
			this.execute=true;
	}

	public List<String> getSeeAlsoTypes() {
		return seeAlso;
	}

	public List<String> readSeeAlso() throws IOException {
		File profileFolder=publicationRepository.getProfileFolder(this.dataset);
		File csvFile=new File(profileFolder, "seeAlso-profile.csv");
		SeeAlsoProfile loaded = SeeAlsoProfile.load(csvFile);
		ArrayList<String> formatAndProfileValues = new ArrayList<>(loaded.formatAndProfileValues());
//		for(int i=0 ; i<formatAndProfileValues.size() ; i++) {
//			formatAndProfileValues.set(i, formatAndProfileValues.get(i).replaceAll("", "  ");)
//		}
		if(formatAndProfileValues.isEmpty())
			setMessage("The profile results of the dataset contains no valid 'seeAlso' references to allow harvesting");
		else 
			Collections.sort(formatAndProfileValues);
		return formatAndProfileValues;
	}
	
//	public HarvestIiifSeeAlsoForm(HttpServletRequest req) {
//		super(new IiifDataset());
//
//		IiifDataset dataset=(IiifDataset) super.dataset;
//		dataset.setUri(req.getParameter("uri"));
//		try {
//			dataset.setCrawlMethod(IiifCrawlMethod.valueOf( req.getParameter("crawlMethod")));
//		} catch (Exception e) {
//			System.err.println("WARNING: " + e.getMessage());
//			e.printStackTrace();
//		}
//		dataset.setOrganization(req.getParameter("organization"));
//		dataset.setTitle(req.getParameter("title"));
//	}

	public void setUri(String uri) {
		((IiifDataset)dataset).setUri(uri);
	}


	public String getUri() {
		return dataset.getUri();
	}
	
	@Override
	public boolean validate() {
		ArrayList<String> errors=new ArrayList<>();
		if(StringUtils.isEmpty(seeAlsoSelect)) 
			errors.add("Provide a format/profile to harvest");
		if(errors.isEmpty())
			return true;
		StringBuilder sb=new StringBuilder();
		sb.append("The form contains errors. It was not possible to register the dataset. Please check the following:</br><ul>");
		for(String e: errors) {
			sb.append("\n<li>").append(e).append("</li>");
		}
		sb.append("</ul>\n");
		setMessage(sb.toString());
		return false;
	}


	public String output() throws Exception {
		StringWriter w=new StringWriter();
		Template temp = GlobalFrontend.FREE_MARKER.getTemplate("datasetmanagement/"+getClass().getSimpleName()+".html");
		temp.process(this, w);
		w.close();
		return w.toString();
	}

	public boolean executeJob() {
		return execute;
	}


}
