package inescid.dataaggregation.data.reasoning;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.OWLMicroReasoner;
import org.apache.jena.riot.Lang;

import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class ReasonerByRulesAndSchemas {
	protected Reasoner reasoner;
	protected Model schemasModel=Jena.createModel();
	String rulesClasspath=null;
	
	public ReasonerByRulesAndSchemas() throws IOException {
	}
	public void loadSchemas(String... schemasClasspath) throws IOException {
		loadSchemas(schemasModel, schemasClasspath);
	}
	public void loadSchemas(Model loadedSchemas) throws IOException {
		schemasModel.add(loadedSchemas);
	}
	public void initReasoner() throws IOException {
//		reasoner=ReasonerRegistry.getOWLMicroReasoner();
//		reasoner=ReasonerRegistry.getRDFSSimpleReasoner();
		if(StringUtils.isEmpty(rulesClasspath))
			reasoner=new DataModelReasoner().reasoner;
		else
			reasoner=new DataModelReasoner(rulesClasspath).reasoner;
	}
	protected void loadSchemas(Model model, String... ontologiesClasspath) throws IOException {
		for(String oCp : ontologiesClasspath) {
			InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(oCp);
			Model readRdf;
				readRdf = RdfUtil.readRdf(systemResourceAsStream);
			systemResourceAsStream.close();
			model.add(readRdf);
		}
	}
	public InfModel infer() {
		InfModel inf = ModelFactory.createInfModel(reasoner, schemasModel);
		return inf;
	}
	public void setRulesClasspath(String rulesClasspath) {
		this.rulesClasspath = rulesClasspath;
	}

	
}
