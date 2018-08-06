package inescid.dataaggregation.dataset.job;

import inescid.dataaggregation.dataset.Dataset;

public abstract class JobWorker implements Runnable{
	protected Exception failureCause;
	protected boolean running=false;
	protected boolean successful=false;
	Dataset dataset;

	public Thread start() {
		Thread thread = new Thread(this);
		thread.start();
		return thread;
	}


	public Exception getFailureCause() {
		return failureCause;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isSuccessful() {
		return successful;
	}
	
	public boolean isFaiure() {
		return failureCause!=null;
	}

	public void setDataset(Dataset dataset) {
		this.dataset=dataset;
	}

	public Dataset getDataset() {
		return dataset;
	}

}