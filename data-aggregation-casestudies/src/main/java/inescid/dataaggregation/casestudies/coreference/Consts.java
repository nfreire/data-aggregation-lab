package inescid.dataaggregation.casestudies.coreference;

import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.jena.riot.Lang;

public class Consts {
	public static String wikidata_datasetId="wikidata";

	public static String europeanaProviders_datasetId="europeana-providers";

	public static String europeanaProvidersConsolidated_datasetId="europeana-providers-consolidated";

	public static String europeanaProviders2nd_datasetId="europeana-providers-2nd";
	public static String europeanaProviders2ndLoc_datasetId="europeana-providers-2nd-loc";

	public static String viaf_datasetId="viaf";
	public static String idLocGov_datasetId="id.loc.gov";
	public static String bne_datasetId="datos.bne.es";

	
	public static String dbPedia_datasetId="dbpedia";
	public static String dbPedia_sparql="https://dbpedia.org/sparql";

	public static String dataBnfFr_datasetId="data.bnf.fr";
	public static String dataBnfFr_sparql="http://data.bnf.fr/sparql";
	
	public static String gnd_datasetId="gnd";
	public static String gnd_sparql="http://zbw.eu/beta/sparql/gnd/query";
	
	public static String getty_datasetId="getty";
	public static String getty_sparql="http://vocab.getty.edu/sparql";

	public static String wikidata_sparql="https://query.wikidata.org/sparql";
	
	public static final HashSet<String> RDF_HOSTS=new HashSet<String>() {{
//	rdfHosts.add("contribute.europeana.eu");	
//	add("services.fom.gr");
//	add("services.veniaminlesviossociety.gr");
	add("datos.bne.es");
//	add("services.edik-archives.gr");
	add("vocab.getty.edu");
	add("dbpedia.org");
	add("viaf.org");
	add("d-nb.info");
//	add("catalogue.bnf.fr");
	add("data.bnf.fr");
	add("imslp.org");
	}};
	public static final HashSet<String> RDF_HOSTS_RESOLVABLE=new HashSet<String>() {{
		add("contribute.europeana.eu");	
		add("services.fom.gr");
		add("services.veniaminlesviossociety.gr");
		add("datos.bne.es");
		add("services.edik-archives.gr");
		add("vocab.getty.edu");
		add("dbpedia.org");
		add("viaf.org");
//		add("d-nb.info");
//		add("data.bnf.fr");
	}};
	public static final HashSet<String> RDF_HOSTS_NON_RESOLVABLE=new HashSet<String>() {{
		add("d-nb.info");
		add("data.bnf.fr");
	}};

	public static final HashSet<String> SPARQL_INGESTED_HOSTS=new HashSet<String>() {{
		add("vocab.getty.edu");
		add("dbpedia.org");
		add("d-nb.info");
		add("data.bnf.fr");
		add("www.wikidata.org");
	}};

	public static final Pattern HOST_PATTERN=Pattern.compile("^https?://([^/]+)/");

	public static final Lang RDF_SERIALIZATION = Lang.RDFTHRIFT;
	
	public static boolean DEBUG=true;


}
