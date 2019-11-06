package inescid.dataaggregation.casestudies.schemaorgcrawling;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.dataset.Global;
import inescid.opaf.data.profile.MapOfInts;
import inescid.util.RdfUtil.Jena;

class CrawlResult {
	final Model crawledModel=Jena.createModel();
	int obtainedResources=0;
	int notFound;
	int urisNotFollowed;
	int propsNotFollowed;
	int obtainedResourcesToGetLiterals=0;
	int literalsFound=0;
	int referencesNotFound=0;
	int referencesFound=0;
	int inModelResourcesTotal=0;
	int inModelResourcesAnon=0;
	int inModelResourcesAnomNot=0;
	int notRdf=0;
	int urisTooDeep=0;
	int propsNotFollowedWithUri=0;
	MapOfInts<Resource> crawledByObjectClass=new MapOfInts<>();
	MapOfInts<Resource> urisNotFollowedByObjectClass=new MapOfInts<>();
	MapOfInts<Resource> anonResourcesByObjectClass=new MapOfInts<>();
	MapOfInts<Resource> propsNotFollowedByProperty=new MapOfInts<>();
	MapOfInts<Resource> domainsWithoutRdf=new MapOfInts<>();
	MapOfInts<Resource> domainsNotResolvable=new MapOfInts<>();
	
	public static CrawlResult deSerialize(byte[] serialized) throws IOException {
		try {
			CSVParser p=new CSVParser(new StringReader(new String(serialized, Global.UTF8)), CSVFormat.DEFAULT);
			CrawlResult cr=new CrawlResult();
			int i=0;
			CSVRecord r=p.iterator().next();
			int rIdx=0;
			for (Field f : CrawlResult.class.getDeclaredFields()) {
				if (f.getType().equals(int.class)) {
					f.setInt(cr, Integer.parseInt(r.get(rIdx)));
					rIdx++;
				}
			}
			r=p.iterator().next();
			for (Field f : CrawlResult.class.getDeclaredFields()) {
				if (f.getType().equals(MapOfInts.class)) {
					String uri=null;
					MapOfInts<Object> fldMap=((MapOfInts<Object>) f.get(cr));
					for(String v: r) {
						if (uri==null)
							uri=v;
						else {
							fldMap.put(
									cr.crawledModel.createResource(uri),
									Integer.parseInt(v));					
							uri=null;
						}
					}
					r=p.iterator().next();
				}
			}
			p.close();
			return cr;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	public byte[] serialize() throws IOException {
		try {
			StringBuilder sb = new StringBuilder();
			CSVPrinter p=new CSVPrinter(sb, CSVFormat.DEFAULT);
			
			for (Field f : CrawlResult.class.getDeclaredFields()) {
				if (f.getType().equals(int.class)) {
					p.print(f.getName());
				}
			}
			p.println();
			for (Field f : CrawlResult.class.getDeclaredFields()) {
				if (f.getType().equals(int.class)) {
					p.print(f.getInt(this));
				}
			}
			p.println();
			for (Field f : CrawlResult.class.getDeclaredFields()) {
				if (f.getType().equals(MapOfInts.class)) {
					p.print(f.getName());					
					for (Entry<Resource, Integer> classEntry : ((MapOfInts<Resource>) f.get(this)).entrySet()) {
						p.print(classEntry.getKey().getURI());
						p.print(classEntry.getValue());
					}
					p.println();
				}
			}
			p.close();
			return sb.toString().getBytes(Global.UTF8);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	public void incNotRdf(String uri) {
		notRdf++;
		domainsWithoutRdf.incrementTo(uriToDomain(uri));
	}
	public void incNotFound(String uri) {
		notFound++;
		domainsNotResolvable.incrementTo(uriToDomain(uri));
	}
	private Resource uriToDomain(String uri) {
		try {
			return Jena.createResource("http://"+new URI(uri).getHost());
		} catch (URISyntaxException e) {
			return Jena.createResource("http://invalid.uri");
		}
	}
	
	
}