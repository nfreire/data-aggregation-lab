package eu.europeana.research.iiif.profile;

public class UsageCount {
	public UsageCount() {
		this.total = 0;
		this.onceOrMore = 0;		
	}
	public UsageCount(int total, int onceOrMore) {
		super();
		this.total = total;
		this.onceOrMore = onceOrMore;
	}
	public int onceOrMore;
	public int total;
}
