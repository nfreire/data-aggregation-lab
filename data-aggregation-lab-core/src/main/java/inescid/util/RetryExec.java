package inescid.util;

import java.util.ArrayList;

public abstract class RetryExec<RET> {
	private int maxRetries=3;
	
	public RetryExec(int maxRetries) {
		super();
		this.maxRetries = maxRetries;
	}

	public RetryExec() {
	}

	ArrayList<Exception> retryErrors=null;
	
	protected abstract RET doRun() throws Exception;
	
	public RET run() throws Exception {
		int attempt=0;
		while (attempt<maxRetries) {
			try {
				attempt++;
				return doRun();
			} catch (Exception e) {
				if (attempt<maxRetries) {
					if(retryErrors==null) retryErrors=new ArrayList<>();
					retryErrors.add(e);
				} else
					throw e;
			}
		}
		return null;
	}
}
