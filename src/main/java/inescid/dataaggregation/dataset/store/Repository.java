package inescid.dataaggregation.dataset.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.plaf.FileChooserUI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;

public class Repository {
	private static final Charset UTF8 = Charset.forName("UTF-8"); 
	
	static private class FilenameManager {
		public static String getDatasetFolderName(String datasetUri) {
			try {
				return URLEncoder.encode(datasetUri, "UTF-8");
			} catch (UnsupportedEncodingException e) {/* impossible */ return null;}
		}
		public static String getResourceFolderAndFilename(String resourcetUri) {
			try {
				String filename = URLEncoder.encode(resourcetUri, "UTF-8");
				String folder=String.valueOf(Math.abs(filename.hashCode() % 1000));
				return folder+"/"+filename;
			} catch (UnsupportedEncodingException e) {/* impossible */ return null;}
		}
		public static String getResourcetUri(File resourceFile) {
			try {
				return URLDecoder.decode(resourceFile.getName(), "UTF-8");
			} catch (UnsupportedEncodingException e) {/* impossible */ return null;}
		}
	}
	
	File homeFolder=new File("target");
//	File tmpFolder=new File(homeFolder, "tmp");
	
	public void init(String homeFolderPath) {
		homeFolder=new File(homeFolderPath);
		if (!homeFolder.exists()) 
			homeFolder.mkdirs();
//		tmpFolder=new File(homeFolder, "tmp");
//		if (!tmpFolder.exists()) 
//			tmpFolder.mkdirs();
	}
	
	public File getDatasetLogFile(String datasetUri) {
		return new File(homeFolder, FilenameManager.getDatasetFolderName(datasetUri)+"/dataset_log.txt");	
	}

	public File getDatasetFolder(String datasetUri) {
		return new File(homeFolder, FilenameManager.getDatasetFolderName(datasetUri));	
	}
	
	public File getFile(String datasetUri, String resourceUri) {
		return new File(getDatasetFolder(datasetUri), FilenameManager.getResourceFolderAndFilename(resourceUri));
	}

	public List<Entry<String,File>> getAllDatasetResourceFiles(String datasetUri) {
		final List<Entry<String,File>> files=new ArrayList<>();
		File datasetFolder = getDatasetFolder(datasetUri);
		if(datasetFolder.listFiles()!=null)
			for (File folder: datasetFolder.listFiles()) {
				if(!folder.isDirectory() || folder.getName().equals("seealso"))
					continue;
				for (File resourceFile: folder.listFiles()) {
					if(!resourceFile.isFile())
						continue;
					files.add(new AbstractMap.SimpleEntry<String, File>(FilenameManager.getResourcetUri(resourceFile), resourceFile));
				}
			}
		return files;
	}
	
	public void save(String datasetUri, String resourceUri, String content) throws IOException {
		File manifestFile = getFile(datasetUri, resourceUri);
		FileUtils.write(manifestFile, content, UTF8);
	}
	public void save(String datasetUri, String resourceUri, byte[] content) throws IOException {
		File manifestFile = getFile(datasetUri, resourceUri);
		FileUtils.writeByteArrayToFile(manifestFile, content);
	}

	public Set<String> getAllDatasetResourceUris(String datasetUri) {
		final Set<String> uris=new HashSet<>();
		File datasetFolder = getDatasetFolder(datasetUri);
		if (datasetFolder.exists())
			for (File folder: datasetFolder.listFiles()) {
				if(!folder.isDirectory() || folder.getName().equals("seealso"))
					continue;
				for (File intermediateFolder: folder.listFiles()) {
					if(!intermediateFolder.isDirectory())
						continue;
					for (File resourceFile: intermediateFolder.listFiles()) {
						if(!resourceFile.isFile())
							continue;
						uris.add(FilenameManager.getResourcetUri(resourceFile));
					}
				}
			}
		return uris;
	}

	public void remove(String datasetUri, String resourceUri) {
		File manifestFile = getFile(datasetUri, resourceUri);
		manifestFile.delete();
		if(manifestFile.getParentFile().list().length==0) 
			manifestFile.getParentFile().delete();
	}


	public void clear(Dataset dataset) {
		File logFile = getDatasetLogFile(dataset.getUri());
		if(logFile.exists())
			logFile.delete();
		List<Entry<String, File>> files = getAllDatasetResourceFiles(dataset.getUri());
		for(Entry<String, File> manifEntry: files) {
			if(manifEntry.getValue().exists())
				manifEntry.getValue().delete();
		}
		for(Entry<String, File> manifEntry: files) {
			if(manifEntry.getValue().getParentFile().exists())
				manifEntry.getValue().getParentFile().delete();
		}
	}
}
