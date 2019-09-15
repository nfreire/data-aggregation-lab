package inescid.dataaggregation.dataset.job;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.job.Job.JobStatus;
import inescid.dataaggregation.store.DatasetRegistryRepository;

public class JobRunner implements Runnable {
	private static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(JobRunner.class);
	
	int simultaneousJobs=1;
	int reportIntervalInHours=48;
	ThreadManager threads=new ThreadManager();
	ArrayList<Job> runningJobs=new ArrayList<>();
	File operationsLogFile;
	DatasetRegistryRepository datasetRepository;
	
	Thread runnerThread;
	
	public JobRunner(String storeragePath, DatasetRegistryRepository datasetRepository) {
		operationsLogFile=new File(storeragePath, "job-runner-jobs.csv");
		this.datasetRepository=datasetRepository;
	}

	public synchronized void addJob(Job job) throws IOException {
		FileUtils.write(operationsLogFile, new JobLog(job).toCsv(), Global.UTF8, true);
	}
	public synchronized void addJob(JobLog job) throws IOException {
		FileUtils.write(operationsLogFile, job.toCsv(), Global.UTF8, true);
	}

	public void shutdown() {
		runnerThread.interrupt();
	}
	
	public synchronized List<JobLog> listJobHistoric(String localId) throws IOException {
		List<JobLog> jobHistoric=new ArrayList<>();
		if(operationsLogFile.exists()) {
			List<String> lines = FileUtils.readLines(operationsLogFile, Global.UTF8);
			for(int i=lines.size()-1 ; i>=0 ; i--) {
				JobLog ds;
				try {
					ds = JobLog.fromCsv(lines.get(i));
					if(ds.datasetLocalId.equals(localId))
						jobHistoric.add(ds);
				} catch (ParseException e) {
					log.warn("Parse of logline failed. discarding it.", e);
				}
			}
		}
		return jobHistoric;
	}

	@Override
	public void run() {
		try {
			runnerThread=Thread.currentThread();
			//resume running jobs (were interrupted)
			try {
				for(JobLog job: listJobProcessingStatus()) {
					job.status=JobStatus.PENDING;
					addJob(job);
					log.info("Restarting job: "+job.type+" on "+job.datasetLocalId);
				};
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			
			
			while(true) {
				try {
					Job job=getNextPendingJob();
					if(job==null)
						Thread.sleep(5000);
					else {
						log.info("Job "+job.type +" for dataset "+job.worker.getDataset().getTitle());
						Thread thr = job.worker.start();
						job.status=JobStatus.RUNNING;
						addJob(job);
						threads.addThreads(thr);
						threads.waitForFinishOfThreads();
						job.status=job.worker.isSuccessful() ? JobStatus.COMPLETED : JobStatus.FAILED;
						addJob(job);
						if(job.status==JobStatus.FAILED) {
							if(job.worker.getFailureCause()!=null) {
								job.worker.getFailureCause().printStackTrace();
								log.warn("Job failed for dataset "+job.worker.getDataset().getTitle(), job.worker.getFailureCause());
							}else
								log.warn("Job failed for dataset "+job.worker.getDataset().getTitle());
						} else
							log.info("Job "+job.type +" ended successfuly for dataset "+job.worker.getDataset().getTitle());
					}
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		} catch (InterruptedException e) {
			log.warn("JobRunner interrupted. Exiting.");
		}		
	}

	private synchronized Job getNextPendingJob() throws IOException {
		if(operationsLogFile.exists()) {
			List<String> lines = FileUtils.readLines(operationsLogFile, Global.UTF8);
			HashSet<String> datasetsListed=new HashSet<>();
			for(int i=lines.size()-1 ; i>=0 ; i--) {
				try {
					JobLog ds= JobLog.fromCsv(lines.get(i));
					if(!datasetsListed.contains(ds.datasetLocalId)) {
						if(ds.status==JobStatus.PENDING)
							return new Job(ds, datasetRepository.getDataset(ds.datasetLocalId));
						datasetsListed.add(ds.datasetLocalId);
					}
				} catch (ParseException e) {
						log.warn("Parse of logline failed. discarding it.", e);
				}
			}
		}
		return null;
	}


	public synchronized List<JobLog> listJobProcessingStatus() throws IOException {
		List<JobLog> jobList=new ArrayList<>();
		HashSet<String> datasetsListed=new HashSet<>();
		GregorianCalendar nowMinusReportInterval=new GregorianCalendar();
		nowMinusReportInterval.add(Calendar.HOUR, reportIntervalInHours);
		if(operationsLogFile.exists()) {
			List<String> lines = FileUtils.readLines(operationsLogFile, Global.UTF8);
			for(int i=lines.size()-1 ; i>=0 ; i--) {
				try {
					JobLog ds= JobLog.fromCsv(lines.get(i));
					if(!datasetsListed.contains(ds.datasetLocalId)) {
						if ((ds.status!=JobStatus.COMPLETED && ds.status!=JobStatus.FAILED) || 
						(ds.statusTime.after(nowMinusReportInterval))) {
							jobList.add(ds);
						}
						datasetsListed.add(ds.datasetLocalId);
					}
				} catch (ParseException e) {
					log.warn("Parse of logline failed. discarding it.", e);
				}
			}
		}
		return jobList;
	}
	
	public String getStatusMessage() throws IOException {
		int runningCount=0;
		int pendingCount=0;
		String runningTitle="";
		
		List<JobLog> jobs=listJobProcessingStatus();
		for(JobLog j: jobs) {
			if(j.status==JobStatus.PENDING)
				pendingCount++;
			else if(j.status==JobStatus.RUNNING) {
				runningCount++;
				if(runningTitle.isEmpty())
					runningTitle=datasetRepository.getDataset(j.datasetLocalId).getTitle();
				else
					runningTitle += ", " + datasetRepository.getDataset(j.datasetLocalId).getTitle();
			}
		}

		if(runningCount==0 && pendingCount==0)
			return "idle";
		if(runningCount>0) {
			String msg="Processing "+runningTitle;
			if(pendingCount>0)
				msg+="<br />"+pendingCount+" jobs waiting";
			return msg;
		}
		if(pendingCount >0) {
			return pendingCount+" jobs waiting";
		}
		return "";
	}

	public boolean isDatasetWithJob(Dataset dataset) throws IOException {
		List<JobLog> jobs=listJobProcessingStatus();
		for(JobLog j: jobs) {
			if(j.status==JobStatus.PENDING || j.status==JobStatus.RUNNING) {
				if(j.datasetLocalId.equals(dataset.getLocalId()))
					return true;
			}
		}
		return false;
	}
}
