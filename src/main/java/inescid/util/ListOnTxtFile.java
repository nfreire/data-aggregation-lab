package inescid.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.output.FileWriterWithEncoding;

public class ListOnTxtFile {
	File persistFile;
	BufferedReader reader;
	BufferedWriter writer;

	public ListOnTxtFile(File persistFile) {
		super();
		this.persistFile = persistFile;
	}
	
	public void openForRead() throws UnsupportedEncodingException, FileNotFoundException {
		reader=new BufferedReader(new InputStreamReader(new FileInputStream(persistFile), "UTF-8"));
	}
	
	public void openForWrite() throws IOException {
		writer=new BufferedWriter(new FileWriterWithEncoding(persistFile, "UTF-8", true));
	}
	
	
	public boolean isOpen() {
		return reader!=null || writer!=null;
	}
	
	public void close() throws IOException {
		if(reader!=null) {
			reader.close();
			reader=null;
		} else if (writer!=null) {
			writer.close();
			writer=null;
		}
	}
	
	public void add(String element) throws IOException {
		writer.write(element);
		writer.write('\n');
	}

	public String next() throws IOException {
		return reader.readLine();
	}
	
	public boolean hasNext() throws IOException {
		return reader.ready();
	}
	
	public int getSize() throws IOException {
		int totalCsvRecs=1;
		InputStream is = new BufferedInputStream(new FileInputStream(persistFile));
		for (int aChar = 0; aChar != -1;aChar = is.read())
			totalCsvRecs += aChar == '\n' ? 1 : 0;
		is.close();
		return totalCsvRecs;
	}

	public void clear() throws IOException {
		if(reader!=null)
			throw new IllegalStateException("Cannot clear list while open for reading");
		if(writer!=null) {
			close();
			if (persistFile.exists())
				persistFile.delete();
			openForWrite();
		}else if (persistFile.exists())
				persistFile.delete();
		
	}
}
