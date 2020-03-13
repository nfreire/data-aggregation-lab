package inescid.europeanarepository;

import java.util.Iterator;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.MappingException;
import org.mongodb.morphia.query.FindOptions;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCursor;

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

public class EdmMongoServer {
	public interface FullBeanHandler{
		public void handle(FullBeanImpl fb); 
	}
	
	public interface AggregationHandler{
		public void handle(AggregationImpl fb); 
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
		
		public void forEachFullBean(FullBeanHandler handler) {
			Iterator<FullBeanImpl> cursor = datastore.find(FullBeanImpl.class).iterator();
		    while (cursor.hasNext()) {
		    	handler.handle(cursor.next());
		    }
		}
		
		public void forEachFullBean(FullBeanHandler handler, int offset) {
			int lastOfset=offset;
			boolean finished=false;
			while (!finished) {
				try {
					FindOptions fo=new org.mongodb.morphia.query.FindOptions();
					fo.skip(lastOfset);
					Iterator<FullBeanImpl> cursor = datastore.find(FullBeanImpl.class).fetch(fo).iterator();
					while (cursor.hasNext()) {
						handler.handle(cursor.next());
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
		
		public void forEachAggregation(AggregationHandler handler) {
			Iterator<AggregationImpl> cursor = datastore.find(AggregationImpl.class).iterator();
			while (cursor.hasNext()) {
				handler.handle(cursor.next());
			}
		}
		
		public void forEachAggregation(AggregationHandler handler, int offset) {
			FindOptions fo=new org.mongodb.morphia.query.FindOptions();
			fo.skip(offset);
			Iterator<AggregationImpl> cursor = datastore.find(AggregationImpl.class).fetch(fo).iterator();
			while (cursor.hasNext()) {
				handler.handle(cursor.next());
			}
		}
		
		public void close() {
			if (mongoClient != null) 
				mongoClient.close();
		}

	public static void main(String[] args) throws Exception {
		EdmMongoServer edmMongo=new EdmMongoServer("mongodb://rnd-2.eanadev.org:27017/admin", "metis-preview-production-2");
		
//		FullBeanImpl fullBean = (FullBeanImpl) edmMongo.getFullBean("/2022608/AFM_AFM_DI0606_10_00");		
//		String edm = EdmUtils.toEDM(fullBean);
//		System.out.println(edm);
		
		edmMongo.forEachFullBean(new FullBeanHandler() {
			public void handle(FullBeanImpl fb) {
				String edm = EdmUtils.toEDM(fb);
				System.out.println(edm);				
			}
		});
		edmMongo.close();
	}
}
