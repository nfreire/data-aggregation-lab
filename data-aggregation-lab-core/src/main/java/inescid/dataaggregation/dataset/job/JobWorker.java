package inescid.dataaggregation.dataset.job;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.job.Job.JobStatus;
import inescid.dataaggregation.dataset.observer.JobObserverList;

public abstract class JobWorker extends JobObserverList implements Runnable{
	private Exception failureCause;
	private boolean running=false;
	private boolean successful=false;
	protected Dataset dataset;
	protected Job job;

	protected JobWorker(Job job, Dataset dataset) {
		this.dataset = dataset;
		this.job = job;
	}
	
	public Thread start() {
		Thread thread = new Thread(this);
		thread.start();
		running=true;
		job.status=JobStatus.RUNNING;
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

//	public void setDataset(Dataset dataset) {
//		this.dataset=dataset;
//	}

	public Dataset getDataset() {
		return dataset;
	}

	public void addObserver(JobObserver obs) {
		observers.add(obs);
	}
	
	public void finishedSuccsessfuly() {
		successful=true;
		super.finishedSuccsessfuly();
		running = false;
		job.status=JobStatus.COMPLETED;
	}
	
	public void finishedWithFailure(Exception failureCause) {
		this.failureCause=failureCause;
		running = false;
		super.finishedWithFailure(failureCause);
		job.status=JobStatus.FAILED;
	}
	
	@Override
	public final void run() {
		try {
			started();
			runJob();
			finishedSuccsessfuly();
		} catch (Exception e) {
			finishedWithFailure(e);
		}
		
	}


	abstract protected void runJob() throws Exception ;
}