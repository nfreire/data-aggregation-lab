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
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner.RuleMode;
import org.apache.jena.reasoner.rulesys.RDFSFBRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.vocabulary.ReasonerVocabulary;

import inescid.dataaggregation.dataset.Global;

public class ReasonerUtil {

		public static GenericRuleReasoner instanciateRuleBased(String rulesClasspath) {
			return instanciateRuleBased(rulesClasspath, GenericRuleReasoner.HYBRID);
		}
		public static GenericRuleReasoner instanciateRuleBased(String rulesClasspath, RuleMode reasonerModel) {
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
			reasoner.setMode(reasonerModel);
//			reasoner.setMode(GenericRuleReasoner.HYBRID);
//			reasoner.setTransitiveClosureCaching(true);
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
