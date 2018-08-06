package inescid.dataaggregation.crawl.ld;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import inescid.util.ListOnTxtFile;

public class RepositoryOldLdImplementation {
//	File homeFolder=new File("target");
//	File tmpFolder=new File(homeFolder, "tmp");
//	
//	public void init(String homeFolderPath) {
//		homeFolder=new File(homeFolderPath);
//		if (!homeFolder.exists()) 
//			homeFolder.mkdirs();
//		tmpFolder=new File(homeFolder, "tmp");
//		if (!tmpFolder.exists()) 
//			tmpFolder.mkdirs();
//	}
//	
//	public File getDatasetListOfMemberUris(String datasetUri) {
//		try {
//			return new File(homeFolder, "dataset_members_"+URLEncoder.encode(datasetUri, "UTF-8")+".txt");	
//		} catch (UnsupportedEncodingException e) {/*impossible*/ }
//		return null;
//	}
//	
//	public File getDatasetLogFile(String datasetUri) {
//		try {
//			return new File(homeFolder, "dataset_log_"+URLEncoder.encode(datasetUri, "UTF-8")+".txt");	
//		} catch (UnsupportedEncodingException e) {/*impossible*/ }
//		return null;
//	}
//
//	public File getDatasetFolder(String datasetUri) {
//		try {
//			return new File(homeFolder, "dataset_harvest_"+URLEncoder.encode(datasetUri, "UTF-8"));
//		} catch (UnsupportedEncodingException e) {/*impossible*/ }
//		return null;
//	}
//	
//	public File getRdfResourceFile(String datasetUri, String resourceUri) {
//		try {
//			File subfolder=new File(getDatasetFolder(datasetUri), resourceUri.substring(resourceUri.length()-3));
//			return new File(subfolder, URLEncoder.encode(resourceUri, "UTF-8"));
//		} catch (UnsupportedEncodingException e) {/*impossible*/ }
//		return null;
//	}
//
//	public Map<String, File> getAllDatasetResourcesFiles(String datasetUri) {
//		final Map<String, File> files=new HashMap<String, File>();
//		for (File folder: getDatasetFolder(datasetUri).listFiles()) {
//			if(!folder.isDirectory())
//				continue;
//			for (File resourceFile: folder.listFiles()) {
//				if(!resourceFile.isFile())
//					continue;
//				try {
//					files.put(URLDecoder.decode(resourceFile.getName(), "UTF-8"), resourceFile);
//				} catch (UnsupportedEncodingException e) {/*impossible*/ }
//			}
//		}
//		return files;
//	}
}
