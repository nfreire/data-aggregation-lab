package inescid.dataaggregation.dataset;

import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

public abstract class Dataset {
	public enum DatasetType {
		LOD,
		IIIF, WWW;

		public Dataset newInstanceFromCsv(CSVRecord rec) {
			switch (this) {
			case IIIF:
				return new IiifDataset(rec);
			case LOD:
				return new LodDataset(rec);
			case WWW:
				return new WwwDataset(rec);
			}
			throw new RuntimeException("Missing implementation for: "+this);
		}
	};
	
	protected String localId;
	protected String uri;
	protected DatasetType type;
	protected String organization;
	protected String title;
	protected String dataFormat;
	protected String dataProfile;
	protected String metadataUri;
//	protected transient String status;
//	protected transient boolean published;

//	protected Map<String, String> meta;

	public Dataset(String localId, DatasetType type) {
		this.localId = localId;
		this.type = type;
	}

	public Dataset(DatasetType type) {
		this.type = type;
	}

	protected Dataset(CSVRecord csvRecord, DatasetType dsType) {
		this(csvRecord.get(0), dsType);
		organization=csvRecord.get(2);
		title=csvRecord.get(3);
		uri=csvRecord.get(4);
		dataFormat=StringUtils.isEmpty(csvRecord.get(5)) ? null :csvRecord.get(5);
		dataProfile=StringUtils.isEmpty(csvRecord.get(6)) ? null :csvRecord.get(6);
		metadataUri=StringUtils.isEmpty(csvRecord.get(7)) ? null :csvRecord.get(7);
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

	
//	protected void readMetaFromCsv(CSVRecord csvRecord, int startIdx) {
//		for(int i=startIdx; i<csvRecord.size() ; i=i+2)
//			meta.put(csvRecord.get(6), csvRecord.get(6));
//	}
//
//	public void toCsvPrintMeta(CSVPrinter prt) throws IOException {
//		for(Entry<String, String> e : meta.entrySet()) {
//			prt.print(e.getKey());
//			prt.print(e.getValue());
//			prt.println();
//		}
//	}

	public void toCsvPrint(CSVPrinter prt) throws IOException {
		prt.print(localId);
		prt.print(type.toString());
		prt.print(organization);
		prt.print(title);
		prt.print(uri);
		prt.print(dataFormat);
		prt.print(dataProfile);
		prt.print(metadataUri);
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getDataProfile() {
		return dataProfile;
	}

	public void setDataProfile(String dataProfile) {
		this.dataProfile = dataProfile;
	}

	public String getConvertedEdmDatasetUri() {
			return Global.CONVERTED_EDM_DATASET_PREFIX+localId;
	}

	public String getMetadataUri() {
		return metadataUri;
	}

	public void setMetadataUri(String metadataUri) {
		this.metadataUri = metadataUri;
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
