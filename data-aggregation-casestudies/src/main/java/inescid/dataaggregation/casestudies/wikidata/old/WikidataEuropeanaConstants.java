package inescid.dataaggregation.casestudies.wikidata.old;

import java.io.File;

public class WikidataEuropeanaConstants {
	public static final File REFERENCES_JSON_DUMP=new File("src/data/wikidata/latest-all.json.europeana.txt.gz");
	public static final File REFERENCES_CSV=new File( "target/wikidata_europeana_references.csv");
	public static final File REFERENCES_REPORT_HTML=new File( "target/wikidata_europeana_references.html");
	public static final File CACHE_WIKIDATA_PROPERTIES=new File("target/wikidata_properties_summary.csv");
	public static final File MAPPINGS_WD_EDM_CSV = new File("src/data/wikidata_edm_mappings.csv");
}