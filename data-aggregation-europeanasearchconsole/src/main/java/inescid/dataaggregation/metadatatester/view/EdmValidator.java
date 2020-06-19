package inescid.dataaggregation.metadatatester.view;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.validation.ValidationResult;
import inescid.dataaggregation.data.validation.EdmXmlValidator;
import inescid.util.europeana.EdmRdfToXmlSerializer;

public class EdmValidator {
	public static ValidationReport validate(Resource choRes){
		ValidationReport report=new ValidationReport();

		EdmRdfToXmlSerializer edmXmlSerializer=new EdmRdfToXmlSerializer(choRes);
		ValidationResult validate = GlobalMetadataTester.edmValidator.validate(choRes.getURI(), edmXmlSerializer.getXmlDom());
		
		report.addErrors(validate.getMessages());
		return report;
	}
}
