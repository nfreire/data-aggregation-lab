package inescid.dataaggregation.dataset.job;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Dataset.DatasetType;
import inescid.dataaggregation.dataset.IiifDataset.IiifCrawlMethod;
import inescid.dataaggregation.dataset.job.Job.JobStatus;
import inescid.dataaggregation.dataset.job.Job.JobType;

public class JobLog {
	
	public JobType type;
	public JobStatus status;
	public GregorianCalendar statusTime;
	public String datasetLocalId;
	public String parameters;
	
	public JobLog(Job job) {
		super();
		this.datasetLocalId = job.worker.getDataset().getLocalId();
		this.type = job.type;
		this.status = job.status;
		this.statusTime=new GregorianCalendar();
		this.parameters=job.parameters;
	}
	
	public JobLog(String datasetLocalId, JobType type, JobStatus status, long statusTimeInMillis, String parameters) {
		super();
		this.datasetLocalId = datasetLocalId;
		this.type = type;
		this.status = status;
		this.statusTime=new GregorianCalendar();
		this.statusTime.setTimeInMillis(statusTimeInMillis);
		this.parameters = parameters;
	}


	public String toCsv() {
		try {
			StringBuilder sb=new StringBuilder();
			CSVPrinter rec=new CSVPrinter(sb, CSVFormat.DEFAULT);
			rec.printRecord(datasetLocalId, type.toString(), status.toString(), statusTime.getTimeInMillis(), parameters);
			rec.close();
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	public String parametersToCsv(String[] parameters) {
		try {
			StringBuilder sb=new StringBuilder();
			CSVPrinter rec=new CSVPrinter(sb, CSVFormat.DEFAULT);
			rec.printRecord(datasetLocalId, type.toString(), status.toString(), statusTime.getTimeInMillis(), parameters);
			rec.close();
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	

	public static JobLog fromCsv(String csvString) throws IOException {
		CSVParser parser=CSVParser.parse(csvString, CSVFormat.DEFAULT);
		CSVRecord csvRecord = parser.getRecords().get(0);
		JobLog j=new JobLog(csvRecord.get(0), JobType.valueOf(csvRecord.get(1)), JobStatus.valueOf(csvRecord.get(2)), Long.parseLong(csvRecord.get(3)), csvRecord.size()>=5 ? csvRecord.get(4) : null);
		parser.close();
		return j;
	}
}
