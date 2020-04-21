package inescid.dataaggregation.casestudies.wikidata.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueVisitor;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

public class WikidataEntityCache {
	File persistenceFile;
	Map<String, WikidataEntitySummary> cache=new HashMap<>();
	WikibaseDataFetcher  wbdf = WikibaseDataFetcher.getWikidataDataFetcher();
	
	class WikidataEntitySummary {
		public WikidataEntitySummary(EntityDocument entityDoc) {
			entityType=entityDoc.getEntityId().getId();
			if (entityDoc instanceof ItemDocument) {
				ItemDocument item=(ItemDocument)entityDoc;
				id=item.getItemId().getId();
				MonolingualTextValue monolingualTextValue = item.getLabels().get("en");
				labelEn=monolingualTextValue==null ? "" : monolingualTextValue.getText();

				STMS: for(StatementGroup stGrp: item.getStatementGroups()) {
					if(stGrp.getProperty().getId().equals("P31")) {
						for(Statement st: stGrp.getStatements()) {
							dataType=getInstanceOfValueLabel(st.getValue());
							break STMS;
						}
					}
				}
			}else if (entityDoc instanceof PropertyDocument) {
				PropertyDocument prop=(PropertyDocument)entityDoc;
				id=prop.getPropertyId().getId();
				MonolingualTextValue monolingualTextValue = prop.getLabels().get("en");
				labelEn=monolingualTextValue==null ? "" : monolingualTextValue.getText();
				dataType=prop.getDatatype().getIri();
				
				STMS: for(StatementGroup stGrp: prop.getStatementGroups()) {
					if(stGrp.getProperty().getId().equals("P1628")) {
						for(Statement st: stGrp.getStatements()) {
							String equivUrl = st.getValue().toString().substring(1);
							if(equivUrl.startsWith("http://schema.org/")) {
								schemaOrgEquivalent=equivUrl.substring(0, equivUrl.length()-1);
								break STMS;
							}
						}
					}
				}
			} else {
				System.err.println("Unexpected wikidata document: "+entityDoc.getClass().getCanonicalName());
			}
			
		}
		
		private String getInstanceOfValueLabel(Value value) {
				String v=value.accept(new ValueVisitor<String>() {
				

				@Override
				public String visit(EntityIdValue arg0) {
					return arg0.getId();
				}

				@Override
				public String visit(GlobeCoordinatesValue arg0) {
					return arg0.toString();
				}

				@Override
				public String visit(MonolingualTextValue arg0) {
					return arg0.toString();
				}

				@Override
				public String visit(QuantityValue arg0) {
					return arg0.toString();
				}

				@Override
				public String visit(StringValue arg0) {
					return arg0.toString();
				}

				@Override
				public String visit(TimeValue arg0) {
					return arg0.toString();
				}
			});
			return v;
		}

		public WikidataEntitySummary(String cachedString) {
			try {
				String[] split = cachedString.split(",");
				id=split[0];
				entityType=split[1];
				if(split.length > 2) {
					dataType=split[2];
					if(split.length > 3) {
						schemaOrgEquivalent=split[3];
						if(split.length > 4) {
							labelEn=split[4];
							for(int i=5; i<split.length ; i++)
								labelEn+=", "+split[i];
						}
					}
				}
			} catch (RuntimeException e) {
				throw new RuntimeException(cachedString, e);
			}
		}
		
		public String toCacheString() {
			return toString();
		}
		@Override
		public java.lang.String toString() {
			return id+","+entityType+","+dataType+","+schemaOrgEquivalent+","+labelEn;
		}
		String id;
		String labelEn;
		String entityType;
		String dataType="";
		String schemaOrgEquivalent="";
		
	}
	
	public WikidataEntityCache() {		
	}
	
	public WikidataEntityCache(File persistenceFile) throws IOException {
		 this.persistenceFile = persistenceFile;
		 if(persistenceFile.exists()) {
			Reader csvReader=new InputStreamReader(new FileInputStream(persistenceFile), "UTF-8");
			BufferedReader csvBufferedReader = new BufferedReader(csvReader);
			while(csvBufferedReader.ready()) {
				String prop = csvBufferedReader.readLine();
				WikidataEntitySummary summary = new WikidataEntitySummary(prop);
				cache.put(summary.id, summary);
			}
			csvBufferedReader.close();
		 }
	}
	
	public WikidataEntitySummary getSummary(String id) throws MediaWikiApiErrorException, IOException {
		WikidataEntitySummary s=null;
		s=cache.get(id);
		if(s==null) {
			EntityDocument entityDoc = getEntityDocument(id);
			if (entityDoc==null)
				return null;
			s=new WikidataEntitySummary(entityDoc);
			if(s!=null) 
				addToCache(s);
		}
		return s;
	}
	
	private void addToCache(WikidataEntitySummary s) throws IOException {
		cache.put(s.id, s);
		if(persistenceFile!=null)
			FileUtils.write(persistenceFile, s.toCacheString()+"\n", "UTF-8", true);
	}

	public EntityDocument getEntityDocument(String id)  throws MediaWikiApiErrorException, IOException {
		EntityDocument doc = wbdf.getEntityDocument(id);
		if(doc!=null) {
			WikidataEntitySummary s=new WikidataEntitySummary(doc);
			if(s!=null) 
				addToCache(s);
		}
		return doc;
	}
	
}
