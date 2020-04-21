package inescid.dataaggregation.store;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;

import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.job.ZipArchiveExporter;

public class Repository {
	private static final Charset UTF8 = Charset.forName("UTF-8");

	static private class FilenameManager {
		public static String getDatasetFolderName(String datasetUri) {
			try {
				return URLEncoder.encode(datasetUri, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				/* impossible */ return null;
			}
		}

		public static String getResourceFolderAndFilename(String resourcetUri) {
			try {
				String filename = URLEncoder.encode(resourcetUri, "UTF-8");
				String folder = String.valueOf(Math.abs(filename.hashCode() % 1000));
				return folder + "/" + filename;
			} catch (UnsupportedEncodingException e) {
				/* impossible */ return null;
			}
		}

		public static String getMetaResourceFolderAndFilename(String resourcetUri) {
			try {
				String uriUrlEncoded = URLEncoder.encode(resourcetUri, "UTF-8");
				String filename = uriUrlEncoded + ".meta.csv";
				String folder = String.valueOf(Math.abs(uriUrlEncoded.hashCode() % 1000));
				return folder + "/" + filename;
			} catch (UnsupportedEncodingException e) {
				/* impossible */ return null;
			}
		}

		public static String getResourcetUri(File resourceFile) {
			try {
				return URLDecoder.decode(resourceFile.getName(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				/* impossible */ return null;
			}
		}
	}

	File homeFolder = new File("target");
	// File tmpFolder=new File(homeFolder, "tmp");

	public void init(String homeFolderPath) {
		homeFolder = new File(homeFolderPath);
		if (!homeFolder.exists())
			homeFolder.mkdirs();
		// tmpFolder=new File(homeFolder, "tmp");
		// if (!tmpFolder.exists())
		// tmpFolder.mkdirs();
	}

	public File getDatasetLogFile(String datasetUri) {
		return new File(homeFolder, FilenameManager.getDatasetFolderName(datasetUri) + "/dataset_log.txt");
	}

	public File getDatasetFolder(String datasetUri) {
		return new File(homeFolder, FilenameManager.getDatasetFolderName(datasetUri));
	}

	public File getFile(String datasetUri, String resourceUri) {
		return new File(getDatasetFolder(datasetUri), FilenameManager.getResourceFolderAndFilename(resourceUri));
	}
	public boolean contains(String datasetUri, String resourceUri) {
		return getFile(datasetUri, resourceUri).exists();
	}
	public File getMetaFile(String datasetUri, String resourceUri) {
		return new File(getDatasetFolder(datasetUri), FilenameManager.getMetaResourceFolderAndFilename(resourceUri));
	}
	public List<Entry<String, String>> getMeta(String datasetUri, String resourceUri) throws IOException {
		List<Entry<String, String>> meta=new ArrayList<>();
		File metaFile = getMetaFile(datasetUri, resourceUri);
		if(metaFile.exists()) {
			CSVParser parser=CSVParser.parse(metaFile, UTF8, CSVFormat.DEFAULT);
			parser.forEach(entry -> {
				meta.add(new AbstractMap.SimpleEntry<String, String>(entry.get(0),entry.get(1)));
			});
			parser.close();
		}
		return meta;
	}
	public List<Entry<String, File>> getAllDatasetResourceFiles(String datasetUri) {
		final List<Entry<String, File>> files = new ArrayList<>();
		File datasetFolder = getDatasetFolder(datasetUri);
		if (datasetFolder.listFiles() != null)
			for (File folder : datasetFolder.listFiles()) {
				if (!folder.isDirectory() || folder.getName().equals("seealso"))
					continue;
				for (File resourceFile : folder.listFiles()) {
					if (!resourceFile.isFile() || resourceFile.getName().endsWith(".meta.csv"))
						continue;
					files.add(new AbstractMap.SimpleEntry<String, File>(FilenameManager.getResourcetUri(resourceFile),
							resourceFile));
				}
			}
		return files;
	}

	private List<Entry<String, File>> getAllDatasetMetaFiles(String datasetUri) {
		final List<Entry<String, File>> files = new ArrayList<>();
		File datasetFolder = getDatasetFolder(datasetUri);
		if (datasetFolder.listFiles() != null)
			for (File folder : datasetFolder.listFiles()) {
				if (!folder.isDirectory() || folder.getName().equals("seealso"))
					continue;
				for (File resourceFile : folder.listFiles()) {
					if (!resourceFile.isFile() || !resourceFile.getName().endsWith(".meta.csv"))
						continue;
					files.add(new AbstractMap.SimpleEntry<String, File>(FilenameManager.getResourcetUri(resourceFile),
							resourceFile));
				}
			}
		return files;
	}

	public void save(String datasetUri, String resourceUri, String content) throws IOException {
		File manifestFile = getFile(datasetUri, resourceUri);
		FileUtils.write(manifestFile, content, UTF8);
	}

	public void save(String datasetUri, String resourceUri, String content, List<Entry<String, String>> meta)
			throws IOException {
		save(datasetUri, resourceUri, content);
		saveMeta(datasetUri, resourceUri, meta);
	}
	
	public void save(String datasetUri, String resourceUri, byte[] content, String oneMetaParam, String oneMetaValue)
			throws IOException {
		save(datasetUri, resourceUri, content);
		saveMeta(datasetUri, resourceUri, new ArrayList<Map.Entry<String,String>>(){{
			add(new AbstractMap.SimpleEntry<String, String>(oneMetaParam, oneMetaValue));
		}});
	}

	
	
	public void saveMeta(String datasetUri, String resourceUri, List<Entry<String, String>> meta) throws IOException {
		if(meta==null) return;
		
		File metaFile = getMetaFile(datasetUri, resourceUri);
		FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(metaFile, Global.UTF8);
		CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
		for (Entry<String, String> field : meta) {
			printer.printRecord(field.getKey(), field.getValue());
		}
		printer.close();
		fileWriter.close();
	}

	public void save(String datasetUri, String resourceUri, byte[] content) throws IOException {
		File manifestFile = getFile(datasetUri, resourceUri);
		FileUtils.writeByteArrayToFile(manifestFile, content);
	}

	public void save(String datasetUri, String resourceUri, byte[] content, List<Entry<String, String>> meta)
			throws IOException {
		save(datasetUri, resourceUri, content);
		saveMeta(datasetUri, resourceUri, meta);
	}

	public Set<String> getAllDatasetResourceUris(String datasetUri) {
		final Set<String> uris = new HashSet<>();
		File datasetFolder = getDatasetFolder(datasetUri);
		if (datasetFolder.exists())
			for (File folder : datasetFolder.listFiles()) {
				if (!folder.isDirectory() || folder.getName().equals("seealso"))
					continue;
				for (File intermediateFolder : folder.listFiles()) {
					if (!intermediateFolder.isDirectory())
						continue;
					for (File resourceFile : intermediateFolder.listFiles()) {
						if (!resourceFile.isFile())
							continue;
						if (!resourceFile.getName().endsWith(".meta.xml"))
							uris.add(FilenameManager.getResourcetUri(resourceFile));
					}
				}
			}
		return uris;
	}

	public boolean remove(String datasetUri, String resourceUri) {
		File manifestFile = getFile(datasetUri, resourceUri);
		boolean exists=manifestFile.delete();
		File metaFile = getMetaFile(datasetUri, resourceUri);
		if (metaFile.exists())
			metaFile.delete();
		if (manifestFile.getParentFile().exists() && manifestFile.getParentFile().list().length == 0)
			manifestFile.getParentFile().delete();
		return exists;
	}

	
	public void clear(String datasetUri) {
		File logFile = getDatasetLogFile(datasetUri);
		if (logFile.exists())
			logFile.delete();
		List<Entry<String, File>> files = getAllDatasetMetaFiles(datasetUri);
		for (Entry<String, File> manifEntry : files) {
			if (manifEntry.getValue().exists())
				manifEntry.getValue().delete();
		}
		files = getAllDatasetResourceFiles(datasetUri);
		for (Entry<String, File> manifEntry : files) {
			if (manifEntry.getValue().exists())
				manifEntry.getValue().delete();
		}
		for (Entry<String, File> manifEntry : files) {
			if (manifEntry.getValue().getParentFile().exists())
				manifEntry.getValue().getParentFile().delete();
		}
	}
	
	public class IteratorOfResourceFiles implements Iterator<File> {
		File next = null;
		int idx1 = -1;
		int idx2 = 0;
		File[] col1;
		File[] col2;
		
		public IteratorOfResourceFiles (String datasetUri){
			File datasetFolder = getDatasetFolder(datasetUri);
			if (datasetFolder.exists()) {
				col1 = datasetFolder.listFiles();
				prepareNext();
			}
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public File next() {
			File ret = next;
			prepareNext();
			return ret;
		}

		private void prepareNext() {
			if (col2!=null && idx2 < col2.length) {
				idx2++;
				while (idx2 < col2.length) {
					if (!col2[idx2].isFile()) {
						idx2++;
						continue;
					}
					if (!col2[idx2].getName().endsWith(".meta.xml")) {
						next = col2[idx2];
	//								next = FilenameManager.getResourcetUri(col2[idx2]);
						return;
					}
					idx2++;
				}
			}
			next = null;
			OUT: while (next == null && idx1 < col1.length - 1) {
				idx1++;
				idx2 = 0;
				while (true) {
					if (!col1[idx1].isDirectory() || col1[idx1].getName().equals("seealso"))
						idx1++;
					else
						break;
					if (idx1 >= col1.length)
						break OUT;
				}
				col2 = col1[idx1].listFiles();
				while (idx2 <= col2.length) {
					if (!col2[idx2].isFile()) {
						idx2++;
						continue;
					}
					if (!col2[idx2].getName().endsWith(".meta.xml")) {
						next = col2[idx2];
//									next = FilenameManager.getResourcetUri(col2[idx2]);
						break OUT;
					}
					idx2++;
				}
			}
		};
	};
		
	public Iterable<String> getIterableOfResourceUris(String datasetUri) {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				IteratorOfResourceFiles it=new IteratorOfResourceFiles(datasetUri);
				return new Iterator<String>() {
					@Override
					public boolean hasNext() {
						return it.hasNext();
					}

					@Override
					public String next() {
						File next = it.next();
						return next==null ? null :FilenameManager.getResourcetUri(next) ;
					}
				};				
			}
		};
	}
		
	public Iterable<RepositoryResource> getIterableOfResources(String datasetUri) {
		return new Iterable<RepositoryResource>() {
			@Override
			public Iterator<RepositoryResource> iterator() {
				IteratorOfResourceFiles it=new IteratorOfResourceFiles(datasetUri);
				return new Iterator<RepositoryResource>() {
					@Override
					public boolean hasNext() {
						return it.hasNext();
					}

					@Override
					public RepositoryResource next() {
						File next = it.next();
						return next==null ? null : new RepositoryResource(FilenameManager.getResourcetUri(next), next) ;
					}
				};				
			}
		};
	}
		
	public byte[] getContent(String datasetId, String url) throws IOException {
		File file = getFile(datasetId, url);
		return FileUtils.readFileToByteArray(file);
	}
	public InputStream getContentStream(String datasetId, String url) throws IOException {
		File file = getFile(datasetId, url);
		return new BufferedInputStream(FileUtils.openInputStream(file));
	}

	public void exportDatasetToZip(String datasetUri, File targetZipFile, ContentTypes format_opt) throws IOException {
		ZipArchiveExporter ziper=new ZipArchiveExporter(targetZipFile);
		List<Entry<String, File>> allDatasetManifestFiles = Global.getDataRepository().getAllDatasetResourceFiles(datasetUri);

		String fileExtension=format_opt!=null ? "."+format_opt.getFilenameExtension() : "";
		for(Entry<String, File> manifEntry: allDatasetManifestFiles) {
			ziper.addFile(manifEntry.getValue().getName()+fileExtension);
			FileInputStream fis = new FileInputStream(manifEntry.getValue());
			IOUtils.copy(fis, ziper.outputStream());
			fis.close();
		}
		ziper.close();
	}

	public int getSize(String datasetUri) {
		return getAllDatasetResourceUris(datasetUri).size();
	}

	public void save(String datasetId, String resourceUri, InputStream bodyStream, List<Entry<String, String>> headers) {
		File cacheFile = getFile(datasetId, resourceUri);
		if(!cacheFile.getParentFile().exists())
			cacheFile.getParentFile().mkdirs();
		try {
			FileOutputStream output = new FileOutputStream(cacheFile);
			IOUtils.copy(bodyStream, output);
			output.close();
			saveMeta(datasetId, resourceUri, headers);
		} catch (IOException e) {
			remove(datasetId, resourceUri);
		}
	}

	public String getContentType(String datasetId, String resourceUri) throws IOException {
		List<Entry<String, String>> meta = getMeta(datasetId, resourceUri);
		if (meta==null)	return null;
		for(Entry<String, String> e:meta) {
			if (e.getKey().equalsIgnoreCase("content-type"))
				return e.getValue();
		}
		return null;
	}

}
