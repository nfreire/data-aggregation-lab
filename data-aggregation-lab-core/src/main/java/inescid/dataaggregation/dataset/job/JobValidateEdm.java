package inescid.dataaggregation.dataset.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.w3c.dom.Document;

import eu.europeana.research.iiif.crawl.CollectionCrawler;
import eu.europeana.research.iiif.crawl.ManifestHarvester;
import eu.europeana.research.iiif.crawl.ManifestSeeAlsoHarvester;
import eu.europeana.research.iiif.discovery.ProcesssingAlgorithm;
import eu.europeana.research.iiif.discovery.demo.TimestampCrawlingHandler;
import eu.europeana.research.iiif.discovery.syncdb.InMemoryTimestampStore;
import eu.europeana.research.iiif.discovery.syncdb.TimestampTracker;
import eu.europeana.research.iiif.profile.SeeAlsoProfile;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.IiifDataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.DatasetProfile;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;
import inescid.dataaggregation.dataset.convert.EdmRdfToXmlSerializer;
import inescid.dataaggregation.dataset.convert.RdfDeserializer;
import inescid.dataaggregation.dataset.convert.RdfReg;
import inescid.dataaggregation.dataset.convert.SchemaOrgToEdmDataConverter;
import inescid.dataaggregation.dataset.validate.ValidationResult;
import inescid.dataaggregation.dataset.validate.Validator;
import inescid.dataaggregation.dataset.validate.Validator.Schema;
import inescid.dataaggregation.store.PublicationRepository;
import inescid.util.LinkedDataUtil;
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
			File profileFolder = GlobalCore.getPublicationRepository().getProfileFolder(dataset);
			if(!profileFolder.exists())
				profileFolder.mkdirs();
			Validator validator=new Validator(GlobalCore.getValidatorResourceFolder(), validationSchema);
			File validationCsvFile = new File(profileFolder, "edm-validation.csv");
			FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(validationCsvFile, GlobalCore.UTF8);
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
					GlobalCore.getDataRepository().getAllDatasetResourceFiles(datasetRepoUri);
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
