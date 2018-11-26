package inescid.dataaggregation.dataset.job;

public interface JobObserver {
	void started();

	void signalResourceFailure(String uri, Exception resourceException);
	
	void signalResourceSuccess(String uri);	
	
	void finishedWithFailure(Exception failureCause);

	void finishedSuccsessfuly();
	
}
