package inescid.dataaggregation.dataset.job;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import eu.europeana.research.iiif.profile.LicenseProfile;
import eu.europeana.research.iiif.profile.ManifestMetadataProfiler;
import eu.europeana.research.iiif.profile.SeeAlsoProfile;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;

public class JobDiagnoseIiifSourceForEuropeana  extends JobWorker {
	float licensingCoverage;
	float seeAlsoCoverage;
	ArrayList<String> licenses;			
	ArrayList<String> seeAlsoFormats;			

	protected JobDiagnoseIiifSourceForEuropeana(Job job, Dataset dataset) {
		super(job, dataset);
	}

	@Override
	public void runJob() throws Exception {
		ManifestMetadataProfiler profiler=new ManifestMetadataProfiler(((IiifDataset)dataset)
				, Global.getDataRepository(), Global.getPublicationRepository().getProfileFolder(dataset));
		profiler.process(false);
		LicenseProfile licenseProfile = profiler.getLicenseProfile();
		licensingCoverage = licenseProfile.getLicensingCoverage();
		licenses=new ArrayList<String>(licenseProfile.getValues().keySet());	
		
		SeeAlsoProfile seeAlsoProfile = profiler.getSeeAlsoProfile();
		seeAlsoCoverage = seeAlsoProfile.getSeeAlsoCoverage();
		
		seeAlsoFormats=new ArrayList<String>(seeAlsoProfile.getFormatAndProfile().keySet());	
		
		File iiifDiagnosisForEuropeanaFile = Global.getPublicationRepository().getIiifDiagnosisForEuropeanaFile(dataset);
		if(!iiifDiagnosisForEuropeanaFile.getParentFile().exists())
			iiifDiagnosisForEuropeanaFile.getParentFile().mkdirs();
		PrintStream ps = new PrintStream(iiifDiagnosisForEuropeanaFile, "UTF-8");
		ps.println("<HTML><BODY>");
		ps.println("<H3>Diagnostic of IIIF Dataset for metadata aggregation by Europeana ("+dataset.getTitle()+")</H3>");
		ps.println("<p>Date: "+new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())+"</p>");
		ps.printf("<p>IIIF Manifests containing a license: %.1f%%", licensingCoverage*100);
		if(!licenses.isEmpty()) {
			ps.println("<br />Licenses in use:");
			ps.println("<ul>");			
			for(String lic: licenses) 
				ps.println("<li>"+lic+"</li>");
			ps.println("</ul>");			
		}
		ps.println("</p>");
		ps.printf("<p>IIIF Manifests containing structured metadata (seeAlso properties): %.1f%%", seeAlsoCoverage * 100);
		if(!seeAlsoFormats.isEmpty()) {
			ps.println("<br />Formats in use: (please veryfy if any of the above values refers to EDM or Schema.org)");
			ps.println("<ul>");			
			for(String lic: seeAlsoFormats)  {
				ps.print("<li>"+lic+" (");
				boolean first=true;
				for(String ex: seeAlsoProfile.getFormatAndProfileExamples().get(lic))  {
					if(!first)
						ps.print(", ");
					else
						first=false;
					ps.printf("<a href='%s' target='_blank'>example</a>", ex);
				}
				ps.println(")</li>");
			}
			ps.println("</ul>");			
		} else {
			ps.println("<BR />Dataset does not provide any format of metadata suitable for aggregation by Europeana");			
		}
		ps.println("</p>");
		ps.println("\n</BODY></HTML>");		
		ps.close();
	}

	public float getLicensingCoverage() {
		return licensingCoverage;
	}

	public float getSeeAlsoCoverage() {
		return seeAlsoCoverage;
	}

	public ArrayList<String> getSeeAlsoFormats() {
		return seeAlsoFormats;
	}


}
