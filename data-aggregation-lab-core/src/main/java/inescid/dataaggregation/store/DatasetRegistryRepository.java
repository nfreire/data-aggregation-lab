package inescid.dataaggregation.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;

public class DatasetRegistryRepository {
	
	File requestsLogFile;
	File requestsLogArchiveFile;

	public DatasetRegistryRepository(File requestsLogFile) throws IOException {
		this.requestsLogFile = requestsLogFile;
		if(!requestsLogFile.getParentFile().exists())
			requestsLogFile.getParentFile().mkdirs();
		if(!requestsLogFile.exists())
			FileUtils.write(requestsLogFile, "", Global.UTF8);
		this.requestsLogArchiveFile = new File(requestsLogFile.getParentFile(), requestsLogFile.getName()+".archive");
		if(!requestsLogArchiveFile.exists())
			FileUtils.write(requestsLogArchiveFile, "", Global.UTF8);
	}

	public void registerDataset(Dataset dataset) throws IOException {
		dataset.setLocalId(generateId());
		FileUtils.write(requestsLogFile, dataset.toCsv(), Global.UTF8, true);
	}
	public void updateDataset(Dataset dataset) throws IOException {
		FileUtils.write(requestsLogFile, dataset.toCsv(), Global.UTF8, true);
	}

	protected static String generateId() {
		return UUID.randomUUID().toString();
	}
	
	public synchronized  Dataset getDataset(String localId) throws IOException {
		List<String> lines = FileUtils.readLines(requestsLogFile, Global.UTF8);
		for(int i=lines.size()-1 ; i>=0 ; i--) {
			if(lines.get(i).startsWith(localId)) 
				return Dataset.fromCsv(lines.get(i));
		}
		return null;
	}
	public synchronized Dataset removeDataset(String localId) throws IOException {
		Dataset removed=null;
		List<String> lines = FileUtils.readLines(requestsLogFile, Global.UTF8);
		List<String> toArchiveLines = new ArrayList<>(lines.size());
		FileUtils.write(requestsLogFile, "", Global.UTF8, false);
		HashSet<String> deduplicator=new HashSet<>();
		for(int i=lines.size()-1 ; i>=0 ; i--) {
			Dataset ds= Dataset.fromCsv(lines.get(i));
			if(deduplicator.contains(ds.getLocalId()))
				toArchiveLines.add(lines.get(i));
			else if(ds.getLocalId().equals(localId)) {
				if(removed==null)
					removed=ds;
				toArchiveLines.add(lines.get(i));
			} else
				FileUtils.write(requestsLogFile, lines.get(i)+"\n", Global.UTF8, true);
		}
		for(int i=0 ; toArchiveLines.size()>i ; i++) {
			FileUtils.write(requestsLogArchiveFile, toArchiveLines.get(i)+"\n", Global.UTF8, true);
		}
		return removed;
	}

	public synchronized List<Dataset> listDatasets() throws IOException {
		List<Dataset> datasets=new ArrayList<>();
		if(requestsLogFile.exists()) {
			List<String> lines = FileUtils.readLines(requestsLogFile, Global.UTF8);
			HashSet<String> deduplicator=new HashSet<>();
			for(int i=lines.size()-1 ; i>=0 ; i--) {
				Dataset ds= Dataset.fromCsv(lines.get(i));
				if(deduplicator.contains(ds.getLocalId()))
					continue;
				deduplicator.add(ds.getLocalId());
				datasets.add(ds);
			}
		}
		return datasets;
	}

	public synchronized Dataset getDatasetByUri(String uri) throws IOException {
		List<String> lines = FileUtils.readLines(requestsLogFile, Global.UTF8);
		HashSet<String> deduplicator=new HashSet<>();
		for(int i=lines.size()-1 ; i>=0 ; i--) {
			Dataset ds= Dataset.fromCsv(lines.get(i));
			if(deduplicator.contains(ds.getLocalId()))
				continue;
			if(ds.getUri().equals(uri))
			 return ds;
		}
		return null;
	}
	
	protected void cleanup() throws IOException {
		List<String> lines = FileUtils.readLines(requestsLogFile, Global.UTF8);
		List<String> toArchiveLines = new ArrayList<>(lines.size());
		FileUtils.write(requestsLogFile, "", Global.UTF8, false);
		HashSet<String> deduplicator=new HashSet<>();
		for(int i=lines.size()-1 ; i>=0 ; i--) {
			Dataset ds= Dataset.fromCsv(lines.get(i));
			if(deduplicator.contains(ds.getLocalId()))
				toArchiveLines.add(lines.get(i));
			else
				FileUtils.write(requestsLogFile, lines.get(i)+"\n", Global.UTF8, true);
		}
		for(int i=0 ; toArchiveLines.size()>i ; i++) {
			FileUtils.write(requestsLogArchiveFile, toArchiveLines.get(i)+"\n", Global.UTF8, true);
		}
	}
}
