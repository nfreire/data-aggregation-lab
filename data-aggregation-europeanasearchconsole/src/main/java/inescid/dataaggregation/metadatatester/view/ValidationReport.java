package inescid.dataaggregation.metadatatester.view;

import java.util.ArrayList;
import java.util.List;

public class ValidationReport{
	java.util.List<String> messages=new ArrayList<>();
	
	public boolean isClean() {
		return messages.isEmpty();
	}

	public void addError(String message) {
		messages.add(message);
	}

	public java.util.List<String> getErrors() {
		return messages;
	}

	public void addErrors(List<String> errors) {
		messages.addAll(errors);
	}
	
	
}