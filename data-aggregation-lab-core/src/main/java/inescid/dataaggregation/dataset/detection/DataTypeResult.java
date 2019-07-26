package inescid.dataaggregation.dataset.detection;

import inescid.dataaggregation.data.ContentTypes;
import inescid.dataaggregation.dataset.DatasetProfile;

public class DataTypeResult {
	public DataTypeResult(ContentTypes format) {
		this.format=format;
	}
	public DataTypeResult() {
	}
	public ContentTypes format;
	public DatasetProfile profile;
}
