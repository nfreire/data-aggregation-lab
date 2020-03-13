package inescid.europeana.dataprocessing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

public class ProgressTrackerOnFile {
	File trackFile;
	int saveTokenInterval=1000;
	
	int tokenMidIntervalCnt=0;
	
	public ProgressTrackerOnFile(File trackFile) {
		super();
		this.trackFile = trackFile;
	}
	
	public ProgressTrackerOnFile(File trackFile, int saveTokenInterval) {
		super();
		this.trackFile = trackFile;
		this.saveTokenInterval = saveTokenInterval;
	}
	
	public void track(Object token) throws IOException {
		tokenMidIntervalCnt++;
		
		if(tokenMidIntervalCnt==saveTokenInterval) {
			FileUtils.write(trackFile, token.toString(), StandardCharsets.UTF_8);
			tokenMidIntervalCnt=0;
		}
	}

	public int getTokenAsInt() throws IOException {
		if(trackFile.exists()) {
			String token=FileUtils.readFileToString(trackFile, StandardCharsets.UTF_8);
			try {
				return Integer.parseInt(token);
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}
	
	
}
