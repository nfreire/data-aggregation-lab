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
package inescid.dataaggregation.data.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Validation result bean
 * Created by ymamakis on 12/22/15.
 */

public class ValidationResult {

    /**
     * The record id that generated the issue. Null if success
     */
    private String recordId;

    /**
     * The error code. Null if success
     */
    private List<String> messages=new ArrayList<>();

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public boolean isSuccess() {
        return messages.isEmpty();
    }

	public void addMessages(List<String> messages) {
		this.messages.addAll(messages);
	}
}
