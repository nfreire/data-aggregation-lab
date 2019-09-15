package inescid.dataaggregation.data.reasoning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;

import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil.Jena;

public class DataModelReasoner {
	final protected GenericRuleReasoner reasoner;
	
	public DataModelReasoner() throws IOException {
		this("inescid/dataaggregation/data/reasoning/DataModelReasoner-rules.txt");
	}
	
	public DataModelReasoner(String rulesClasspath) throws IOException {
		InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(rulesClasspath);
		BufferedReader br = IOUtils.toBufferedReader(new InputStreamReader(systemResourceAsStream, Global.UTF8));
		List<Rule> rules = Rule.parseRules( Rule.rulesParserFromReader(br) );
		br.close();
		reasoner = new GenericRuleReasoner(rules);
		reasoner.setOWLTranslation(true);               // not needed in RDFS case
		reasoner.setTransitiveClosureCaching(true);
	}
	
	
	public InfModel infer(Model data) {
		InfModel inf = ModelFactory.createInfModel(reasoner, data);
		return inf;
	}
	
	
	public static void main(String[] args) throws Exception {
		Model m = Jena.createModel();

//[rdfs9:  (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)] 
		inescid.util.RdfUtil.Jena.createStatementAddToModel(m, m.createResource("http://n/x"), RegRdfs.subClassOf, m.createResource("http://n/y"));
		inescid.util.RdfUtil.Jena.createStatementAddToModel(m, m.createResource("http://n/a"), RegRdf.type,  m.createResource("http://n/x"));
//		inescid.util.RdfUtil.Jena.createStatementAddToModel(m, m.createResource("http://i/PropInst"), RdfRegRdfs.subClassOf, m.createResource("http://c/Prop"));
//		inescid.util.RdfUtil.Jena.createStatementAddToModel(m, m.createResource("http://sc/Prop"), RdfRegRdf.type, RdfRegRdf.Property);
//		inescid.util.RdfUtil.Jena.createStatementAddToModel(m, m.createResource("http://c/Prop"), RdfRegRdf.type, RdfRegRdf.Property);
//		inescid.util.RdfUtil.Jena.createStatementAddToModel(m, m.createResource("http://i/PropInst"), RdfRegRdfs.subClassOf, m.createResource("http://c/Prop"));

		System.out.println(	inescid.util.RdfUtil.statementsToString(m) );
		
		
		DataModelReasoner r=new DataModelReasoner();
		InfModel infer = r.infer(m);
		System.out.println( inescid.util.RdfUtil.statementsToString(infer.getDeductionsModel()) );
		
		
	}
}
