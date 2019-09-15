package inescid.dataaggregation.store;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;

public class PublicationRepository {
//	private static final Pattern uriProtocolSnip=Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);
	private static final Charset UTF8 = Charset.forName("UTF-8"); 
	
	static private class FilenameManager {
		public static String getDatasetFolderName(Dataset dataset) {
			try {
				return URLEncoder.encode(dataset.getLocalId(), "UTF-8");
			} catch (UnsupportedEncodingException e) {/* impossible */ return null;}
		}
		public static String getDatasetFolderNameForUrl(Dataset dataset) {
			try {
				return  URLEncoder.encode(URLEncoder.encode(dataset.getLocalId(), "UTF-8"), "UTF-8");
			} catch (UnsupportedEncodingException e) {/* impossible */ return null;}
		}
	}
	
	File homeFolder=new File("target");
	String homeUrl="data";
	
	public void init(File homeFolder, String homeUrl) {
		this.homeFolder=homeFolder;
		this.homeUrl = homeUrl;
		if (!homeFolder.exists()) 
			homeFolder.mkdirs();
	}
	
	public File getDatasetFolder(Dataset dataset) {
		return new File(homeFolder, FilenameManager.getDatasetFolderName(dataset));	
	}
	public File getDatasetFolderNameForUrl(Dataset dataset) {
		return new File(homeFolder, FilenameManager.getDatasetFolderNameForUrl(dataset));	
	}
	
	public File getProfileFolder(Dataset dataset) {
		return new File(getDatasetFolder(dataset), "profile");
	}
	public File getReportsFolder(Dataset dataset) {
		return new File(getDatasetFolder(dataset), "reports");
	}

	public File getExportZipFile(Dataset dataset) {
		return new File(getDatasetFolder(dataset), FilenameManager.getDatasetFolderName(dataset)+".zip");
	}
	
	public String getExportZipFileForUrl(Dataset dataset) {
		return homeUrl+FilenameManager.getDatasetFolderNameForUrl(dataset)+"/"+ FilenameManager.getDatasetFolderNameForUrl(dataset)+".zip";
	}

	public File getExportSeeAlsoZipFile(Dataset dataset) {
		return new File(getDatasetFolder(dataset), Global.SEE_ALSO_DATASET_PREFIX+FilenameManager.getDatasetFolderName(dataset)+".zip");
	}
	public File getIiifDiagnosisForEuropeanaFile(Dataset dataset) {
		return new File(getDatasetFolder(dataset), FilenameManager.getDatasetFolderName(dataset)+"-iiif_source_diagnosis.html");
	}
	
	public String getExportSeeAlsoZipFileForUrl(Dataset dataset) {
		return homeUrl+FilenameManager.getDatasetFolderNameForUrl(dataset)+"/"+  Global.SEE_ALSO_DATASET_PREFIX+FilenameManager.getDatasetFolderNameForUrl(dataset)+".zip";
	}
	public File getExportEdmZipFile(Dataset dataset) {
		return new File(getDatasetFolder(dataset), Global.CONVERTED_EDM_DATASET_PREFIX+FilenameManager.getDatasetFolderName(dataset)+".zip");
	}
	
	public String getExportEdmZipFileForUrl(Dataset dataset) {
		return homeUrl+FilenameManager.getDatasetFolderNameForUrl(dataset)+"/"+  Global.CONVERTED_EDM_DATASET_PREFIX+FilenameManager.getDatasetFolderNameForUrl(dataset)+".zip";
	}
	
	public boolean isPublished(Dataset dataset) {
		return getExportZipFile(dataset).exists();
	}
	public boolean isPublishedForSeeAlso(Dataset dataset) {
		return getExportSeeAlsoZipFile(dataset).exists();
	}
	public boolean isProfiled(Dataset dataset) {
		File profileFolder = getProfileFolder(dataset);
		return profileFolder.exists() && profileFolder.list().length>0;
	}
	public boolean isConverted(Dataset dataset) {
		File convertedZipFile = getExportEdmZipFile(dataset);
		return convertedZipFile.exists() && convertedZipFile.length()>0;
	}
	public boolean isDiagnosed(Dataset dataset) {
		File dFile = getIiifDiagnosisForEuropeanaFile(dataset);
		return dFile.exists() && dFile.length()>0;
	}
	public boolean isConvertedAnalysis(Dataset dataset) {
		File f=new File(getProfileFolder(dataset), "schema.org-mapping-analysis.csv");
		return  isConverted(dataset) && f.exists() && f.length()>0;
	}
	
	public String getPublicationUrl(Dataset dataset) {
		return getExportZipFileForUrl(dataset);
	}
	public String getPublicationSeeAlsoUrl(Dataset dataset) {
		return getExportSeeAlsoZipFileForUrl(dataset);
	}
	public String getIiifDiagnosisForEuropeanaUrl(Dataset dataset) {
		return homeUrl+FilenameManager.getDatasetFolderNameForUrl(dataset)+"/"+ FilenameManager.getDatasetFolderNameForUrl(dataset)+"-iiif_source_diagnosis.html";
	}
	public String getPublicationConvertedUrl(Dataset dataset) {
		return getExportEdmZipFileForUrl(dataset);
	}
	
	public String getProfileUrl(Dataset dataset) throws FileNotFoundException, IOException {
//		return homeUrl+getDatasetFolderNameForUrl(dataset).getName()+"/"+getProfileFolder(dataset).getName();
		File sheetIdFile = new File(getProfileFolder(dataset), "google-sheet-id.txt");
		if(!sheetIdFile.exists())
			throw new FileNotFoundException("Not found: "+sheetIdFile.getPath());
		String sheetId=FileUtils.readFileToString(sheetIdFile, "UTF-8");
		return "https://docs.google.com/spreadsheets/d/"+sheetId;
	}

	public void clear(Dataset dataset) {
		File exportZipFile = getExportZipFile(dataset);
		if(exportZipFile.exists())
			exportZipFile.delete();
		File exportEdmZipFile = getExportEdmZipFile(dataset);
		if(exportEdmZipFile.exists())
			exportEdmZipFile.delete();
		File exportSeeAlsoZipFile = getExportSeeAlsoZipFile(dataset);
		if(exportSeeAlsoZipFile.exists())
			exportSeeAlsoZipFile.delete();
		File iiifDiagnosisFile = getIiifDiagnosisForEuropeanaFile(dataset);
		if(iiifDiagnosisFile.exists())
			iiifDiagnosisFile.delete();
		if (getProfileFolder(dataset).exists()) {
			for(File f: getProfileFolder(dataset).listFiles()) 
				f.delete();
			getProfileFolder(dataset).delete();
		}
	}
}