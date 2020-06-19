package inescid.europeanarepository;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.MappingException;
import org.mongodb.morphia.query.FindOptions;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;

import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.exceptions.MongoRuntimeException;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.corelib.edm.utils.EdmUtils;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.BasicProxyImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.ConceptSchemeImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.EventImpl;
import eu.europeana.corelib.solr.entity.PhysicalThingImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.corelib.web.exception.EuropeanaException;
import eu.europeana.corelib.web.exception.ProblemType;
import inescid.dataaggregation.data.model.RdfReg;
import inescid.util.RdfUtil;
import inescid.util.europeana.EdmRdfUtil;

public class EdmMongoServer {
	public interface Handler<T>{
		public boolean handle(T fb); 
	}
	
	public class HandlerChained<T> implements Handler<T> {
		Handler[] handlers;

		public HandlerChained(Handler<T>... handlers) {
			super();
			this.handlers = handlers;
		}
		
		@Override
		public boolean handle(T fb) {
			boolean proceedToNext=true;
			for(Handler<T> h: handlers)
				proceedToNext= proceedToNext && h.handle(fb);
			return proceedToNext;
		}
	}
	
	
		private MongoClient mongoClient;
		private String databaseName;
		private Datastore datastore;

		/**
		 * Create a new Morphia datastore to do get/delete/save operations on the database
		 * Any required login credentials as well as connection options (like timeouts) should be set in advance in the
		 * provided mongoClient
		 * @param mongoClient
		 * @param databaseName
		 * @param createIndexes, if true then it will try to create the necessary indexes if needed
		 * @throws MongoDBException
		 */
		public EdmMongoServer(MongoClient mongoClient, String databaseName) throws MongoDBException {
			this.mongoClient = mongoClient;
			this.databaseName = databaseName;
			createDatastore();
		}
		public EdmMongoServer(String mongoConnectUrl, String databaseName) throws MongoDBException {
			this.mongoClient = new MongoClient(new MongoClientURI(mongoConnectUrl));
			this.databaseName = databaseName;
			createDatastore();
		}


		private void createDatastore() {
			Morphia morphia = new Morphia();

			morphia.map(FullBeanImpl.class);
			morphia.map(ProvidedCHOImpl.class);
			morphia.map(AgentImpl.class);
			morphia.map(AggregationImpl.class);
			morphia.map(ConceptImpl.class);
			morphia.map(ProxyImpl.class);
			morphia.map(PlaceImpl.class);
			morphia.map(TimespanImpl.class);
			morphia.map(WebResourceImpl.class);
			morphia.map(EuropeanaAggregationImpl.class);
			morphia.map(EventImpl.class);
			morphia.map(PhysicalThingImpl.class);
			morphia.map(ConceptSchemeImpl.class);
			morphia.map(BasicProxyImpl.class);
			morphia.map(WebResourceMetaInfoImpl.class);

			datastore = morphia.createDatastore(mongoClient, databaseName);
		}

		public Datastore getDatastore() {
			return this.datastore;
		}

		public FullBeanImpl getFullBean(String id) throws EuropeanaException {
			try {
				return datastore.find(FullBeanImpl.class).field("about").equal(id).get();
			} catch (RuntimeException re) {
				if (re.getCause() != null &&
						(re.getCause() instanceof MappingException || re.getCause() instanceof java.lang.ClassCastException)) {
					throw new MongoDBException(ProblemType.RECORD_RETRIEVAL_ERROR, re);
				} else {
					throw new MongoRuntimeException(ProblemType.MONGO_UNREACHABLE, re);
				}
			}
		}

		public <T> T searchByAbout(Class<T> clazz, String about) {
			return datastore.find(clazz).filter("about", about).get();
		}
		
		public <T> void forEach(Class<T> type, Handler<T> handler) {
			Iterator<T> cursor = datastore.find(type).iterator();
		    while (cursor.hasNext()) {
		    	if(!handler.handle(cursor.next()))
		    		break;
		    }
		}
		
		public <T> void forEach(Class<T> type, Handler<T> handler, int offset) {
			int lastOfset=offset;
			boolean finished=false;
			while (!finished) {
				try {
					FindOptions fo=new org.mongodb.morphia.query.FindOptions();
					fo.skip(lastOfset);
					Iterator<T> cursor = datastore.find(type).fetch(fo).iterator();
					while (cursor.hasNext()) {
						if (!handler.handle(cursor.next())) 
							break;
						lastOfset++;
					}
					finished=true;
				} catch (MongoException e) {
					e.printStackTrace();
					System.err.println("\nRETRYING...");
					try {
						Thread.sleep(3*60000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						return;
					}
				}
			}
		}
		
		
		public void close() {
			if (mongoClient != null) 
				mongoClient.close();
		}
		
		public Map<String, WebResourceMetaInfoImpl> retrieveWebMetaInfos(List<String> hashCodes) {
	        Map<String, WebResourceMetaInfoImpl> metaInfos = new HashMap<>();
	        final BasicDBObject basicObject = new BasicDBObject("$in", hashCodes);   // e.g. {"$in":["1","2","3"]}
	        List<WebResourceMetaInfoImpl> metaInfoList = getDatastore().find(WebResourceMetaInfoImpl.class)
	                .disableValidation()
	                .field("_id").equal(basicObject).asList();

	        metaInfoList.forEach(cursor -> {
	            String id= cursor.getId();
	            metaInfos.put(id, cursor);
	        });
	        return metaInfos;
	    }
		public Map<String, WebResourceMetaInfoImpl> retrieveWebMetaInfosByAbout(List<String> abouts) {
	        Map<String, WebResourceMetaInfoImpl> metaInfos = new HashMap<>();
	        final BasicDBObject basicObject = new BasicDBObject("$in", abouts);   // e.g. {"$in":["1","2","3"]}
	        List<WebResourceMetaInfoImpl> metaInfoList = getDatastore().find(WebResourceMetaInfoImpl.class)
	                .disableValidation()
	                .field("about").equal(basicObject).asList();

	        
	        
	        metaInfoList.forEach(cursor -> {
	            String id= cursor.getId();
	            metaInfos.put(id, cursor);
	        });
	        return metaInfos;
	    }
		
		
	public static void main(String[] args) throws Exception {
		EdmMongoServer edmMongo=new EdmMongoServer("mongodb://rnd-2.eanadev.org:27017/admin", "metis-preview-production-2");
		
//		FullBeanImpl fullBean = (FullBeanImpl) edmMongo.getFullBean("/2022608/AFM_AFM_DI0606_10_00");		
//		String edm = EdmUtils.toEDM(fullBean);
//		System.out.println(edm);
		
		edmMongo.forEach(FullBeanImpl.class, new Handler<FullBeanImpl>() {
			public boolean handle(FullBeanImpl fb) {
				String edmRdfXml = EdmUtils.toEDM(fb);
				edmRdfXml=Normalizer.normalize(edmRdfXml, Form.NFC);
				Model edmCho = RdfUtil.readRdf(edmRdfXml, org.apache.jena.riot.Lang.RDFXML);
				

				String contentTier="0";
				Model recMdl = edmCho;
				Resource agg = EdmRdfUtil.getEuropeanaAggregationResource(recMdl);
				for (StmtIterator qAnnStms=agg.listProperties(RdfReg.DQV_HAS_QUALITY_ANNOTATION) ; qAnnStms.hasNext() ; ) {
					Statement stm = qAnnStms.next();
					Resource qAnnotRes = stm.getObject().asResource();
					contentTier = RdfUtil.getUriOrLiteralValue(qAnnotRes.getProperty(RdfReg.OA_HAS_BODY).getObject());
					if(contentTier.contains("contentTier")) 
						break;
				}

				System.out.println(contentTier.substring("http://www.europeana.eu/schemas/epf/contentTier".length()));
				
				
				
				

				ArrayList<String> webResourcesIds=new ArrayList<String>();
				for (WebResource wr : fb.getAggregations().get(0).getWebResources()) {
//					System.out.println(wr.getClass().getCanonicalName());
					webResourcesIds.add(wr.getAbout());
					System.out.println((wr.getEbucoreFileByteSize()));
//					webResourcesIds.add(wr.getId().toString());
//					System.out.println(wr.getId());
//					System.out.println(wr.getAbout());
				}
//				Map<String, WebResourceMetaInfoImpl> retrieveWebMetaInfos = edmMongo.retrieveWebMetaInfos(webResourcesIds);
//				Map<String, WebResourceMetaInfoImpl> retrieveWebMetaInfos = edmMongo.retrieveWebMetaInfosByAbout(webResourcesIds);
//				System.out.println(retrieveWebMetaInfos);
				
				
				
				String edm = EdmUtils.toEDM(fb);
				System.out.println(edm);				
				return false;
			}
		});
		edmMongo.close();
	}
}
