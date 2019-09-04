package eu.europeana.research.iiif.profile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;

import eu.europeana.research.iiif.profile.model.Manifest;
import inescid.util.JsonUtil;

public class LicenseProfile {

	HashMap<String, Integer> values;
	int profiledManifestsCount=0;
	int with1plusLicenses=0;
	
	public LicenseProfile() {
		values=new HashMap<>();
	}
	
	public void profileManifest(Manifest manifest) {
		profiledManifestsCount++;
		boolean hasLicense=false;
		if(manifest.license!=null){
			List<String> vals = JsonUtil.readArrayOrValue(manifest.license);
			for(String val: vals) {
				if(StringUtils.isEmpty(val)) continue;
				hasLicense=true;
				Integer lbProf = values.get(val);
				if(lbProf==null) 
					values.put(val, 1);
				else
					values.put(val, lbProf+1);
			}
		}
		if(hasLicense)
			with1plusLicenses++;
	}

	public void save(File csvFile) throws IOException {
		if(!csvFile.getParentFile().exists())
			csvFile.getParentFile().mkdirs();
		FileWriterWithEncoding writer=new FileWriterWithEncoding(csvFile, "UTF-8");
		CSVPrinter printer=new CSVPrinter(writer, CSVFormat.DEFAULT);
		
		printer.print("Value");
		printer.print("Total uses");
		printer.println();
			
		for(Entry<String, Integer> entry : values.entrySet()) {
			printer.print(entry.getKey());
			printer.print(entry.getValue());
			printer.println();
		}
		
		printer.close();
	}

	public HashMap<String, Integer> getValues() {
		return values;
	}

	public int getProfiledManifestsCount() {
		return profiledManifestsCount;
	}

	public int getWith1plusLicenses() {
		return with1plusLicenses;
	}
	
	public float getLicensingCoverage(){
		return (float)with1plusLicenses / (float)profiledManifestsCount;
	}
}
