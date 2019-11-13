package inescid.util;

import org.apache.commons.lang3.StringUtils;

public class AccessException extends Exception {
	private static final long serialVersionUID = 1L;
	
	String address;
	String code;
	String response;
	boolean interrupted=false;

	public AccessException(String address) {
		super();
		this.address=address;
	}

	public AccessException(String address, String message, Throwable cause) {
		super(cause);
		this.address=address;
	}

	public AccessException(String address, String message) {
		super(message);
		this.address=address;
	}

	public AccessException(String address, Throwable cause) {
		super(cause);
		this.address=address;
	}
	

	public AccessException(String address, Number code) {
		super();
		this.address = address;
		this.code = String.valueOf(code);
	}
	
	
	public AccessException(String address, Number code, String response) {
		super();
		this.address = address;
		this.code = String.valueOf(code);
		this.response = response;
	}
	
	public AccessException(String address, String message, Number code) {
		super(message);
		this.address = address;
		this.code = String.valueOf(code);
	}
	
	public AccessException(String address, String code, String response) {
		super();
		this.address = address;
		this.code = code;
		this.response = response;
	}

	public AccessException(String address, boolean interrupted) {
		this.address = address;
		this.interrupted = interrupted;
		
	}

	@Override
	public String getMessage() {
		if(StringUtils.isEmpty(super.getMessage()))
			return address;
		if(super.getMessage().contains(address))
			return super.getMessage();
		return super.getMessage()+" ["+address+"]";
	}

	@Override
	public String getLocalizedMessage() {
		return getMessage();
	}
	
	public String getAddress() {
		return address;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		if (!StringUtils.isEmpty(response) && response.length()>1000)
			this.response=response.substring(0, 1000);
		else	
			this.response = response;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getExceptionSummary() {
		String s=getAddress();
		if(!StringUtils.isEmpty(code))
			s+=("\n  Code: "+ getCode());
		if(!StringUtils.isEmpty(response))
			s+=("\n  Response: "+ getResponse().replace('\n', ' '));
		if(interrupted)
			s+=("\n  Interrupted");
		return s;
	}

	public boolean isInterrupted() {
		return interrupted;
	}

	public void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}
}
