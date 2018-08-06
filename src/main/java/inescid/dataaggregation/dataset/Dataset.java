package inescid.dataaggregation.dataset;

import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public abstract class Dataset {
	public enum DatasetType {
		LOD,
		IIIF;

		public Dataset newInstanceFromCsv(CSVRecord rec) {
			switch (this) {
			case IIIF:
				return new IiifDataset(rec);
			case LOD:
				return new LodDataset(rec);
			}
			throw new RuntimeException("Missing implementation for: "+this);
		}
	};
	
	protected String localId;
	protected String uri;
	protected DatasetType type;
	protected String organization;
	protected String title;
//	protected transient String status;
//	protected transient boolean published;


	public Dataset(String localId, DatasetType type) {
		this.localId = localId;
		this.type = type;
	}

	public Dataset(DatasetType type) {
		this.type = type;
	}

	public DatasetType getType() {
		return type;
	}

	public void setType(DatasetType type) {
		this.type = type;
	}
	
	public abstract String toCsv();

	public void setLocalId(String localId) {
		this.localId = localId;
	}

	public String getLocalId() {
		return localId;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public static Dataset fromCsv(String csvString) throws IOException {
			CSVParser parser=CSVParser.parse(csvString, CSVFormat.DEFAULT);
			CSVRecord csvRecord = parser.getRecords().get(0);
			String type = csvRecord.get(1);
			Dataset ds = DatasetType.valueOf(type).newInstanceFromCsv(csvRecord);
			parser.close();
			return ds;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	
//	public String getStatus() {
//		return status;
//	}
//
//	public void setStatus(String status) {
//		this.status = status;
//	}
//
//	public boolean isPublished() {
//		return published;
//	}
//
//	public void setPublished(boolean published) {
//		this.published = published;
//	}
	
	
}
