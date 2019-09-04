package eu.europeana.research.iiif.crawl;

public class ManifestRepository {
////	private static final Pattern uriProtocolSnip=Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);
//	private static final Charset UTF8 = Charset.forName("UTF-8"); 
//	
//	static private class FilenameManager {
//		public static String getDatasetFolderName(String datasetUri) {
//			try {
//				return URLEncoder.encode(datasetUri, "UTF-8");
//			} catch (UnsupportedEncodingException e) {/* impossible */ return null;}
//		}
//		public static String getManifestFolderAndFilename(String resourcetUri) {
//			try {
//				String filename = URLEncoder.encode(resourcetUri, "UTF-8");
//				String folder=String.valueOf(Math.abs(filename.hashCode() % 1000));
//				return folder+"/"+filename;
//			} catch (UnsupportedEncodingException e) {/* impossible */ return null;}
//		}
//		public static String getSeeAlsoFolderAndFilename(String resourcetUri) {
//			return getManifestFolderAndFilename(resourcetUri);
//		}
//		public static String getManifestId(File resourceFile) {
//			try {
//				return URLDecoder.decode(resourceFile.getName(), "UTF-8");
//			} catch (UnsupportedEncodingException e) {/* impossible */ return null;}
//		}
//		public static String getSeealsoId(File resourceFile) {
//			return getManifestId(resourceFile);
//		}
//	}
//	
//	File homeFolder=new File("target");
////	File tmpFolder=new File(homeFolder, "tmp");
//	
//	public void init(String homeFolderPath) {
//		homeFolder=new File(homeFolderPath);
//		if (!homeFolder.exists()) 
//			homeFolder.mkdirs();
////		tmpFolder=new File(homeFolder, "tmp");
////		if (!tmpFolder.exists()) 
////			tmpFolder.mkdirs();
//	}
//	
//	public File getDatasetLogFile(String datasetUri) {
//		return new File(homeFolder, FilenameManager.getDatasetFolderName(datasetUri)+"/dataset_log.txt");	
//	}
//
//	public File getDatasetFolder(String datasetUri) {
//		return new File(homeFolder, FilenameManager.getDatasetFolderName(datasetUri));	
//	}
//	
//	public File getManifestFile(String datasetUri, String resourceUri) {
//		return new File(getDatasetFolder(datasetUri), FilenameManager.getManifestFolderAndFilename(resourceUri));
//	}
//	public File getSeeAlsoFile(String datasetUri, String resourceUri) {
//		return new File(getDatasetFolder(datasetUri), "seealso/"+FilenameManager.getSeeAlsoFolderAndFilename(resourceUri));
//	}
//
//	public List<Entry<String,File>> getAllDatasetManifestFiles(String datasetUri) {
//		final List<Entry<String,File>> files=new ArrayList<>();
//		File datasetFolder = getDatasetFolder(datasetUri);
//		if(datasetFolder.listFiles()!=null)
//			for (File folder: datasetFolder.listFiles()) {
//				if(!folder.isDirectory() || folder.getName().equals("seealso"))
//					continue;
//				for (File resourceFile: folder.listFiles()) {
//					if(!resourceFile.isFile())
//						continue;
//					files.add(new AbstractMap.SimpleEntry<String, File>(FilenameManager.getManifestId(resourceFile), resourceFile));
//				}
//			}
//		return files;
//	}
//	
//	public List<Entry<String,File>> getAllDatasetManifestSeeAlsoFiles(String datasetUri) {
//		final List<Entry<String,File>> files=new ArrayList<>();
//		File datasetFolder = new File(getDatasetFolder(datasetUri), "seealso");
//		if(datasetFolder.listFiles()!=null)
//			for (File folder: datasetFolder.listFiles()) {
//				for (File resourceFile: folder.listFiles()) {
//					if(!resourceFile.isFile())
//						continue;
//					files.add(new AbstractMap.SimpleEntry<String, File>(FilenameManager.getManifestId(resourceFile), resourceFile));
//				}
//			}
//		return files;
//	}
//
//	public void saveManifest(String datasetUri, String resourceUri, String manifestJson) throws IOException {
//		File manifestFile = getManifestFile(datasetUri, resourceUri);
//		FileUtils.write(manifestFile, manifestJson, UTF8);
//	}
//
//	public Set<String> getAllDatasetManifestUris(String datasetUri) {
//		final Set<String> uris=new HashSet<>();
//		File datasetFolder = getDatasetFolder(datasetUri);
//		if (datasetFolder.exists())
//			for (File folder: datasetFolder.listFiles()) {
//				if(!folder.isDirectory() || folder.getName().equals("seealso"))
//					continue;
//				for (File intermediateFolder: folder.listFiles()) {
//					if(!intermediateFolder.isDirectory())
//						continue;
//					for (File resourceFile: intermediateFolder.listFiles()) {
//						if(!resourceFile.isFile())
//							continue;
//						uris.add(FilenameManager.getManifestId(resourceFile));
//					}
//				}
//			}
//		return uris;
//	}
//
//	public void removeManifest(String datasetUri, String resourceUri) {
//		File manifestFile = getManifestFile(datasetUri, resourceUri);
//		manifestFile.delete();
//		if(manifestFile.getParentFile().list().length==0) 
//			manifestFile.getParentFile().delete();
//	}
//
//	public Set<String> getAllDatasetManifestSeeAlsoUris(String datasetUri) {
//		final Set<String> uris=new HashSet<>();
//		File datasetSeealsoFolder = new File(getDatasetFolder(datasetUri), "seealso");
//		if (datasetSeealsoFolder.exists())
//			for (File folder: datasetSeealsoFolder.listFiles()) {
//				if(!folder.isDirectory())
//					continue;
//				for (File intermediateFolder: folder.listFiles()) {
//					if(!intermediateFolder.isDirectory())
//						continue;
//					for (File resourceFile: intermediateFolder.listFiles()) {
//						if(!resourceFile.isFile())
//							continue;
//						uris.add(FilenameManager.getSeealsoId(resourceFile));
//					}
//				}
//			}
//		return uris;
//	}
//
//	public void saveManifestSeeAlso(String datasetUri, String resourceUri, byte[] seeAlsoContent) throws IOException {
//		File seeAlsoFile = getSeeAlsoFile(datasetUri, resourceUri);
//		FileUtils.writeByteArrayToFile(seeAlsoFile, seeAlsoContent);
//	}
//
//	public void removeManifestSeeAlso(String datasetUri, String resourceUri) {
//		File seealsoFile = getSeeAlsoFile(datasetUri, resourceUri);
//		seealsoFile.delete();
//		if(seealsoFile.getParentFile().list().length==0) 
//			seealsoFile.getParentFile().delete();		
//	}
//
//	public void clear(Dataset dataset) {
//		File logFile = getDatasetLogFile(dataset.getUri());
//		if(logFile.exists())
//			logFile.delete();
//		List<Entry<String, File>> files = getAllDatasetManifestFiles(dataset.getUri());
//		for(Entry<String, File> manifEntry: files) {
//			if(manifEntry.getValue().exists())
//				manifEntry.getValue().delete();
//		}
//		for(Entry<String, File> manifEntry: files) {
//			if(manifEntry.getValue().getParentFile().exists())
//				manifEntry.getValue().getParentFile().delete();
//		}
//		files = getAllDatasetManifestSeeAlsoFiles(dataset.getUri());
//		for(Entry<String, File> manifEntry: files) {
//			if(manifEntry.getValue().exists())
//				manifEntry.getValue().delete();
//		}
//		for(Entry<String, File> manifEntry: files) {
//			if(manifEntry.getValue().getParentFile().exists())
//				manifEntry.getValue().getParentFile().delete();
//		}
//	}
}
