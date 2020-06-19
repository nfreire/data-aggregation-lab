package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;

import inescid.dataaggregation.casestudies.coreference.Consts;
import inescid.dataaggregation.wikidata.RdfRegWikidata;
import inescid.dataaggregation.wikidata.WikidataUtil;
import inescid.dataaggregation.data.model.Owl;
import inescid.dataaggregation.data.model.Schemaorg;
import inescid.dataaggregation.data.model.Skos;
import inescid.util.SparqlClient;
import inescid.util.SparqlClient.Handler;

class WikidataIngest {
	public static HashMap<String, String> idsToUrisAtEuropeanaResolvable=new HashMap<String, String>(){{
		put("P214", "http://viaf.org/viaf/$1");
		put("P227", "http://d-nb.info/gnd/$1");
		put("P244", "http://id.loc.gov/authorities/names/$1");
		put("P245", "http://vocab.getty.edu/ulan/$1");
		put("P950", "http://datos.bne.es/resource/$1");
		put("P3348", "http://nlg.okfn.gr/resource/authority/record$1");
		put("P3832", "http://thesaurus.europeanafashion.eu/thesaurus/$1");
		put("P268", "http://data.bnf.fr/ark:/12148/cb$1");
		put("P839", "http(s)://imslp.org/wiki/$1");
	}};
		
		
	public static HashMap<String, String> idsToUris=new HashMap<String, String>(){{
		put("P214", "http://viaf.org/viaf/$1");
		put("P227", "http://d-nb.info/gnd/$1");
		put("P244", "http://id.loc.gov/authorities/names/$1");
		put("P245", "http://vocab.getty.edu/ulan/$1");
		put("P269", "http://www.idref.fr/$1/id");
		put("P349", "http://id.ndl.go.jp/auth/ndlna/$1");
		put("P486", "http://id.nlm.nih.gov/mesh/$1");
		put("P508", "http://purl.org/bncf/tid/$1");
		put("P646", "http://g.co/kg$1");
		put("P648", "http://openlibrary.org/works/$1");
		put("P672", "http://id.nlm.nih.gov/mesh/$1");
		put("P906", "http://libris.kb.se/resource/auth/$1");
		put("P950", "http://datos.bne.es/resource/$1");
		put("P1006", "http://data.bibliotheken.nl/id/thes/p$1");
		put("P1014", "http://vocab.getty.edu/aat/$1");
		put("P1015", "https://livedata.bibsys.no/authority/$1");
		put("P1036", "http://dewey.info/class/$1/");
		put("P1256", "http://iconclass.org/$1");
		put("P1260", "http://kulturarvsdata.se/$1");
		put("P1422", "http://ta.sandrart.net/-person-$1");
		put("P1566", "http://sws.geonames.org/$1/");
		put("P1584", "http://pleiades.stoa.org/places/$1/rdf");
		put("P1667", "http://vocab.getty.edu/tgn/$1");
		put("P1936", "http://dare.ht.lu.se/places/$1");
		put("P2163", "http://id.worldcat.org/fast/$1");
		put("P2347", "http://www.yso.fi/onto/yso/p$1");
		put("P2452", "http://www.geonames.org/ontology#$1");
		put("P2581", "http://babelnet.org/rdf/s$1");
		put("P2671", "http://g.co/kg$1");
		put("P2799", "http://data.cervantesvirtual.com/person/$1");
		put("P2950", "http://nomisma.org/id/$1");
		put("P3120", "http://data.ordnancesurvey.co.uk/id/$1");
		put("P3348", "http://nlg.okfn.gr/resource/authority/record$1");
		put("P3832", "http://thesaurus.europeanafashion.eu/thesaurus/$1");
		put("P3763", "http://www.mimo-db.eu/InstrumentsKeywords/$1");
		put("P3911", "http://zbw.eu/stw/descriptor/$1");
		put("P3916", "http://vocabularies.unesco.org/thesaurus/$1");
		put("P4104", "http://data.carnegiehall.org/names/$1");
		put("P4307", "https://id.erfgoed.net/thesauri/erfgoedtypes/$1");
		put("P4953", "http://id.loc.gov/authorities/genreForms/$1");
		put("P5034", "http://lod.nl.go.kr/resource/$1");
		put("P5429", "http://cv.iptc.org/newscodes/$1");
		put("P5587", "https://libris.kb.se/$1");
		put("P5748", "http://uri.gbv.de/terminology/bk/$1");
		put("P6293", "http://www.yso.fi/onto/ysa/$1");
		put("P268", "http://data.bnf.fr/ark:/12148/cb$1");
		put("P434", "http://musicbrainz.org/artist/$1");
		put("P839", "http(s)://imslp.org/wiki/$1");
		put("P1051", "https://psh.techlib.cz/skos/PSH$1");
		put("P1149", "http://id.loc.gov/authorities/classification/$1");
		put("P1150", "http://rvk.uni-regensburg.de/nt/$1");
		put("P1617", "http://www.bbc.co.uk/things/$1#id");
		put("P1741", "http://data.beeldengeluid.nl/gtaa/$1");
		put("P1900", "http://www.eagle-network.eu/voc/$1");
		put("P1938", "http://www.gutenberg.org/ebooks/author/$1");
		put("P3040", "https://soundcloud.com/$1");
		put("P5739", "http://catalogo.pusc.it/auth/$1");
	}};
	
	public final static Property[] idsAndAlignsProperties;
	static {
		ArrayList<Property> list=new ArrayList<Property>();
		list.add(Owl.sameAs);
		list.add(RdfRegWikidata.EXACT_MATCH);
//		list.add(RdfRegWikidata.EQUIVALENT_CLASS);
		list.add(Skos.exactMatch);
		list.add(Skos.closeMatch); 
		list.add(Schemaorg.sameAs);
		for(String p:idsToUrisAtEuropeanaResolvable.keySet())
			list.add(ResourceFactory.createProperty(RdfRegWikidata.NsWd+p));
		idsAndAlignsProperties=(Property[]) list.toArray();
	}
	
	
	void runIngestWikidata(String datasetId, File repoFolder) throws Exception {
		FileOutputStream fos=new FileOutputStream(new File(repoFolder, datasetId+"."+Consts.RDF_SERIALIZATION.getFileExtensions().get(0)));
		StreamRDF writer = StreamRDFWriter.getWriterStream(fos, Consts.RDF_SERIALIZATION) ;
		ArrayList<Triple> toAddTripleAux=new ArrayList<Triple>(1) {{ add(null); }};
		try {
			SparqlClient endpoint=new SparqlClient(Consts.wikidata_sparql, "");
			for(Property p: new Property[] { Owl.sameAs, RdfRegWikidata.EXACT_MATCH, RdfRegWikidata.EQUIVALENT_CLASS,
					Skos.exactMatch, Skos.closeMatch, Schemaorg.sameAs}) {
				endpoint.queryWithPaging("SELECT ?s ?o WHERE { {?s <" + p + "> ?o} .}",
						50000, null, new Handler() {
					int cnt=0;
					@Override
					public boolean handleSolution(QuerySolution solution) throws Exception {
						try {
							if(!solution.get("s").isURIResource() || !solution.get("o").isURIResource()) 
								return true;
							cnt++;
							Triple t=new Triple(NodeFactory.createURI(solution.get("s").asResource().getURI()), 
									NodeFactory.createURI(p.getURI()), NodeFactory.createURI(solution.get("o").asResource().getURI()));
							toAddTripleAux.set(0, t);
							StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);
							if(cnt % 10000 == 0) {
								System.out.println(cnt);
								fos.flush();
							}
							return true;
						} catch (Exception e) {
							e.printStackTrace();
							return false;
						}
					}
				});
			}
			fos.flush();
			for(String wdProp: idsToUris.keySet()) {
				Property p=ResourceFactory.createProperty(RdfRegWikidata.NsWdt+wdProp);
				String pattern=idsToUris.get(wdProp);
				endpoint.queryWithPaging("SELECT ?s ?o WHERE { {?s <" + p + "> ?o} .}",
						50000, null, new Handler() {
					int cnt=0;
					@Override
					public boolean handleSolution(QuerySolution solution) throws Exception {
						try {
							if(!solution.get("s").isURIResource() || !solution.get("o").isLiteral()) 
								return true;
							cnt++;
							String objUri=pattern.replaceAll("\\$1", solution.get("o").asLiteral().getString());
							Triple t=new Triple(NodeFactory.createURI(solution.get("s").asResource().getURI()), 
									NodeFactory.createURI(p.getURI()), NodeFactory.createURI(objUri));
							toAddTripleAux.set(0, t);
							StreamOps.sendTriplesToStream(toAddTripleAux.iterator(), writer);
							if(cnt % 10000 == 0) {
								System.out.println(cnt);
								fos.flush();
							}
							return true;
						} catch (Exception e) {
							e.printStackTrace();
							return false;
						}
					}
				});
			}
		}finally {
			fos.close();
		}
		System.out.println("Finished Wikidata.");
	}
}