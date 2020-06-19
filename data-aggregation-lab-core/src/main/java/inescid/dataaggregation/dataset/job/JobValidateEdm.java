package inescid.dataaggregation.dataset.job;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.FileWriterWithEncoding;

import inescid.dataaggregation.data.validation.ValidationResult;
import inescid.dataaggregation.data.validation.EdmXmlValidator;
import inescid.dataaggregation.data.validation.EdmXmlValidator.Schema;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.util.XmlUtil;
import inescid.util.googlesheets.GoogleSheetsCsvUploader;

public class JobValidateEdm extends JobWorker implements Runnable {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger(JobValidateEdm.class);
	Schema validationSchema=Schema.EDM;

	public JobValidateEdm(Job job, Dataset dataset) {
		super(job, dataset);
	}

	@Override
	public void runJob()  throws Exception {
			File profileFolder = Global.getPublicationRepository().getProfileFolder(dataset);
			if(!profileFolder.exists())
				profileFolder.mkdirs();
			EdmXmlValidator validator=new EdmXmlValidator(Global.getValidatorResourceFolder(), validationSchema);
			File validationCsvFile = new File(profileFolder, "edm-validation.csv");
			FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(validationCsvFile, Global.UTF8);
			CSVPrinter csvPrinter=new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
			String datasetRepoUri=null;
			if (dataset.getDataProfile().equals(DatasetProfile.EDM)) {
				datasetRepoUri=dataset.getType()==DatasetType.IIIF ? 					
						((IiifDataset)dataset).getSeeAlsoDatasetUri()
						: dataset.getUri();
			} else {
				datasetRepoUri=dataset.getConvertedEdmDatasetUri();
			}
			List<Entry<String, File>> allDatasetResourceFiles = 			
					Global.getDataRepository().getAllDatasetResourceFiles(datasetRepoUri);
			int validationFails=0;
			for (Entry<String, File> seeAlsoFile : allDatasetResourceFiles) {
				try {
					ValidationResult validate = validator.validate(seeAlsoFile.getKey(), XmlUtil.parseDomFromFile(seeAlsoFile.getValue()));
					if(!validate.isSuccess()) {
						csvPrinter.print(seeAlsoFile.getKey());
						for(String m: validate.getMessages()) 
							csvPrinter.print(m);
						csvPrinter.println();
						validationFails++;
					}
				} catch (Exception e) {
					log.warn("Failed to validate resource: "+seeAlsoFile.getKey(), e);
				}
			}
			csvPrinter.println();			
			csvPrinter.printRecord("Total records with validation errors: ", validationFails);			
			csvPrinter.close();
			fileWriter.close();
			
			String spreadsheetId=GoogleSheetsCsvUploader.getDatasetAnalysisSpreadsheet(dataset, GoogleSheetsCsvUploader.sheetTitleFromFileName(validationCsvFile));
			GoogleSheetsCsvUploader.update(spreadsheetId, validationCsvFile);			
	}


	public void setValidationSchemal(Schema validationSchema) {
		this.validationSchema = validationSchema;
	}

}
