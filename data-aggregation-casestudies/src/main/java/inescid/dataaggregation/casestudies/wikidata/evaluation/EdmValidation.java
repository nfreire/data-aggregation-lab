package inescid.dataaggregation.casestudies.wikidata.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.data.validation.ValidationResult;
import inescid.dataaggregation.data.validation.EdmXmlValidator;
import inescid.dataaggregation.data.validation.EdmXmlValidator.Schema;
import inescid.dataaggregation.dataset.Global;
import inescid.util.datastruct.MapOfInts;
import inescid.util.europeana.EdmRdfToXmlSerializer;

public class EdmValidation {
	final  EdmXmlValidator validator;
	final FileWriterWithEncoding fileWriter;
	final CSVPrinter csvPrinter;
	int cntProcessed=0;
	int cntValidationFails=0;
	MapOfInts<String> statsValidationFails=new MapOfInts<>();
	MapOfInts<Integer> statsValidationMessagesDistrinutions=new MapOfInts<>();

	
	public EdmValidation(File outputFolder) throws IOException {
		this(outputFolder, null);
	}
	
	public EdmValidation(File validationCsvFile, EdmXmlValidator validator) throws IOException {
		if(validator==null)
			this.validator=new EdmXmlValidator(Global.getValidatorResourceFolder(), Schema.EDM);
		else 
			this.validator=validator;
		fileWriter = new FileWriterWithEncoding(validationCsvFile, Global.UTF8);
		csvPrinter=new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
	}		
	
	
	public void evaluateValidation(String choUri, Resource cho) {
		cntProcessed++;
//		String datasetRepoUri=null;
//		if (dataset.getDataProfile().equals(DatasetProfile.EDM)) {
//			datasetRepoUri=dataset.getType()==DatasetType.IIIF ? 					
//					((IiifDataset)dataset).getSeeAlsoDatasetUri()
//					: dataset.getUri();
//		} else {
//			datasetRepoUri=dataset.getConvertedEdmDatasetUri();
//		}
			try {
				EdmRdfToXmlSerializer edmXmlSerializer=new EdmRdfToXmlSerializer(cho);
				ValidationResult validate = validator.validate(choUri, edmXmlSerializer.getXmlDom());
				if(!validate.isSuccess()) {
					csvPrinter.print(choUri);
					for(String m: validate.getMessages()) {
						csvPrinter.print(m);
						statsValidationFails.incrementTo(m);
					}
					csvPrinter.println();
					cntValidationFails++;
					statsValidationMessagesDistrinutions.incrementTo(validate.getMessages().size());
				} else 
					statsValidationMessagesDistrinutions.incrementTo(0);
			} catch (Exception e) {
				System.err.println("WARN: Failed to validate resource: "+choUri);
				e.printStackTrace();
			}
	}
	
	public void finalize() throws IOException {
		csvPrinter.println();			
		csvPrinter.printRecord("Total processed","Total records with validation errors");			
		csvPrinter.printRecord(cntProcessed, cntValidationFails);			
		csvPrinter.println();			
		csvPrinter.printRecord("ERROR TYPE STATS");			
		csvPrinter.printRecord("Error","Count");			
		for(Entry<String, Integer> msg :  statsValidationFails.entrySet()) {
			csvPrinter.printRecord(msg.getKey(), msg.getValue());			
		}
		csvPrinter.println();			
		csvPrinter.printRecord("ERRORS/RECORD DISTRIBUTION");			
		csvPrinter.printRecord("ERRORS COUNT","RECORD COUNT");			
		for(Entry<Integer, Integer> msg :  statsValidationMessagesDistrinutions.entrySet()) {
			csvPrinter.printRecord(msg.getKey(), msg.getValue());			
		}
		csvPrinter.close();
		fileWriter.close();
	}
}
