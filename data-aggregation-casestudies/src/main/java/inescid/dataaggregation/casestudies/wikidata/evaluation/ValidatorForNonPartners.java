/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package inescid.dataaggregation.casestudies.wikidata.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.w3c.dom.Document;

import inescid.dataaggregation.data.validation.ValidationResult;
import inescid.dataaggregation.data.validation.EdmXmlValidator;

/**
 * EDM Validator class
 */
public class ValidatorForNonPartners extends EdmXmlValidator {
    private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(ValidatorForNonPartners.class);
    final HashSet<String> ignoredValidations;
    
    public ValidatorForNonPartners(File resourceFolder, Schema schema, String... ignoredValidations) {
    	super(resourceFolder, schema);
    	this.ignoredValidations = new HashSet<String>(Arrays.asList(ignoredValidations));
    }

    /**
     * Validate method using JAXP
     *
     * @return The outcome of the Validation
     */
    public ValidationResult validate(String uri, Document doc) {
    	ValidationResult superResult = super.validate(uri, doc);
    	
    	if(superResult.isSuccess())
    		return superResult;
    	
    	
        List<String> messages=new ArrayList<>();
        for(String msg: superResult.getMessages()) {
        	if (!ignoredValidations.contains(msg)) 
        		messages.add(msg);
        }
    	
        if(messages.isEmpty())
        	return new ValidationResult();
        return constructValidationError(uri, messages);
    }

}


