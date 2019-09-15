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
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.RDFSFBRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.vocabulary.ReasonerVocabulary;

import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.dataset.Global;
import inescid.util.RdfUtil.Jena;

public class ReasonerUtil {

	public static class ReasonerStabilizer {
		
		Model unstableSatementsMdl=null;
		Model augmentedModel=Jena.createModel();
		Reasoner reasoner;
		
		public ReasonerStabilizer(Reasoner reasoner, Model toStabelizeMdl) {
			this.reasoner=reasoner;
			long prevDeductions=0;
			augmentedModel.add(toStabelizeMdl);
			while (unstableSatementsMdl==null) {
				InfModel inferedPre = ReasonerUtil.infer(reasoner, augmentedModel);
				if (inferedPre.getDeductionsModel().size()==0 || inferedPre.getDeductionsModel().size() == prevDeductions) {
					augmentedModel.add(inferedPre.getDeductionsModel().listStatements());
					inferedPre = ReasonerUtil.infer(reasoner, augmentedModel);
					augmentedModel.add(inferedPre.getDeductionsModel().listStatements());
					inferedPre = ReasonerUtil.infer(reasoner, augmentedModel);
					unstableSatementsMdl=Jena.createModel();
					unstableSatementsMdl.add(inferedPre.getDeductionsModel().listStatements());
				}else {
					augmentedModel.add(inferedPre.getDeductionsModel().listStatements());
					prevDeductions=inferedPre.getDeductionsModel().size();
				}
			}
		}
		
		public Model inferNewDeductions(Model model) {
			InfModel inf = ReasonerUtil.infer(reasoner, augmentedModel, model);
			Model deductionsModel=Jena.createModel();
			deductionsModel.add(inf.getDeductionsModel());
//			System.out.println("Ded: "+ deductionsModel.size());
			deductionsModel.remove(unstableSatementsMdl);
//			System.out.println("Ded after: "+ deductionsModel.size());
			for (Statement stm: deductionsModel.listStatements(null, RegRdfs.subPropertyOf, (RDFNode)null).toList()){
				if(stm.getObject().equals(stm.getSubject()))
					deductionsModel.remove(stm);				
			}
			for (Statement stm: deductionsModel.listStatements(null, RegRdfs.subClassOf, (RDFNode)null).toList()){
				if(stm.getObject().equals(stm.getSubject()) || stm.getObject().equals(RegRdfs.Resource))
					deductionsModel.remove(stm);				
			}
			for (Statement stm: deductionsModel.listStatements(null, RegRdf.type, RegRdfs.Resource).toList()){
				if(stm.getObject().equals(stm.getSubject()) || stm.getObject().equals(RegRdfs.Resource))
					deductionsModel.remove(stm);				
			}
//			System.out.println("Ded after: "+ deductionsModel.size());
			return deductionsModel;
		}
	}
	
	
//		public static GenericRuleReasoner instanciateRdfsBased(String rulesClasspath) {
//			RDFSFBRuleReasoner reasoner;
//			InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(rulesClasspath);
//			BufferedReader br = IOUtils.toBufferedReader(new InputStreamReader(systemResourceAsStream, Global.UTF8));
//			List<Rule> rules = Rule.parseRules( Rule.rulesParserFromReader(br) );
//			try {
//				br.close();
//			} catch (IOException e) {
//				throw new RuntimeException(e.getMessage(), e);
//			}
//			reasoner = new GenericRuleReasoner(rules);
//			reasoner.setMode(GenericRuleReasoner.HYBRID);
//			reasoner.setTransitiveClosureCaching(true);
////			reasoner.setOWLTranslation(true);               // not needed in RDFS case
//			return reasoner;
//		}
		public static GenericRuleReasoner instanciateRdfsBased(String rulesClasspath) {
			GenericRuleReasoner reasoner;
			InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(rulesClasspath);
			BufferedReader br = IOUtils.toBufferedReader(new InputStreamReader(systemResourceAsStream, Global.UTF8));
			List<Rule> rules = Rule.parseRules( Rule.rulesParserFromReader(br) );
			try {
				br.close();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			reasoner = new GenericRuleReasoner(rules);
			reasoner.setMode(GenericRuleReasoner.HYBRID);
			reasoner.setTransitiveClosureCaching(true);
//			reasoner.setOWLTranslation(true);               // not needed in RDFS case
			return reasoner;
		}
		

		public static InfModel infer(Reasoner reasoner, Model model) {
			InfModel inf = ModelFactory.createInfModel(reasoner, model);
			return inf;
		}
		
		public static InfModel infer(Reasoner reasoner, Model schema, Model model) {
			InfModel inf = ModelFactory.createInfModel(reasoner, schema, model);
			return inf;
		}
		
		
}
