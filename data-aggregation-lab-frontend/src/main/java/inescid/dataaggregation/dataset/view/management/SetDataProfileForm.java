package inescid.dataaggregation.dataset.view.management;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.Template;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.detection.DataProfileDetector;
import inescid.dataaggregation.dataset.detection.DataTypeResult;
import inescid.dataaggregation.dataset.view.GlobalFrontend;
import inescid.dataaggregation.dataset.view.registry.DatasetView;

public class SetDataProfileForm extends DatasetView{
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(SetDataProfileForm.class);

	String dataProfileParam;
	String detectFormOfDataParam;
	
	public SetDataProfileForm(Dataset dataset, String dataFormatParam, String detectParam) {
		super(dataset);
		this.dataProfileParam = dataFormatParam;
		this.detectFormOfDataParam = detectParam;
	}

	public DatasetProfile[] getFormOfDataValues(){
		return new DatasetProfile[] {DatasetProfile.EDM, DatasetProfile.SCHEMA_ORG, DatasetProfile.ANY_TRIPLES};
//		return FormOfData.values();
	}
//	public List<String> getFormOfDataValues(){
//		List<String> ret=new ArrayList<String>();
//		for(FormOfData v: FormOfData.values()) {
//			ret.add(v.name());
//		}
//		return ret;
//	}
	
	
	public String getDataFormat() {
		return dataset.getDataFormat();
	}
	
	
	public String getDataProfile() {
		return dataset.getDataProfile();
	}
	
	@Override
	public boolean validate() {
		if(!StringUtils.isEmpty(dataProfileParam)) {
			try {
				DatasetProfile.valueOf(dataProfileParam);
			}catch (Exception e) {
				setMessage(dataProfileParam + " is unsupported by the system");				
				return false;
			}
		}
		return true;
	}

	public void executeJob() {
		try {
			if(! StringUtils.isEmpty(detectFormOfDataParam)) {
				DataTypeResult detected = DataProfileDetector.detect(dataset.getUri(), Global.getDataRepository());
				if (detected!=null && detected.profile!=null) {
//					Global.getDatasetRegistryRepository().updateDataset(dataset);
					dataset.setDataProfile(detected.profile.toString());
					setMessage("Data profile detected was "+detected.profile.getDisplay()+". Press 'Save' to accept this profile, or select another.." );					
				}else
					setMessage("Detection of data profile was inconclusive.");					
			}else if(dataProfileParam!=null) {
				dataset.setDataProfile(dataProfileParam.equals("") ? null : dataProfileParam);
				Global.getDatasetRegistryRepository().updateDataset(dataset);
				setMessage("Dataset format/profile was saved.");					
			} else {
					//display the form
			}
		} catch (IOException e) {
			setMessage("An error has occoured while updating: "+e.getMessage());
			log.error(e.getMessage(), e);
		}
	}
	
	public String output() throws Exception {
		StringWriter w=new StringWriter();
		Template temp = GlobalFrontend.FREE_MARKER.getTemplate("datasetmanagement/"+getClass().getSimpleName()+".html");
		temp.process(this, w);
		w.close();
		return w.toString();
	}
}
