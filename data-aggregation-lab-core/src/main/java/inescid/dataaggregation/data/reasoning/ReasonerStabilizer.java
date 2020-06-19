package inescid.dataaggregation.data.reasoning;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.Reasoner;

import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.Rdfs;
import inescid.util.RdfUtil.Jena;

public class ReasonerStabilizer {
		
		Model unstableSatementsMdl=null;
		final Model augmentedModel;
		final Reasoner reasoner;
		
		public ReasonerStabilizer(Reasoner reasoner, Model savedStableModel, Model unstableSatementsMdl) {
			this.reasoner=reasoner;
			this.augmentedModel = savedStableModel;
			this.unstableSatementsMdl = unstableSatementsMdl;
		}
		
		public ReasonerStabilizer(Reasoner reasoner, Model toStabelizeMdl) {
			this.reasoner=reasoner;
			this.augmentedModel = toStabelizeMdl;
		}
		
		public void runElaborateStabelization() {
			long prevDeductions=0;
			while (unstableSatementsMdl==null) {
				InfModel inferedPre = ReasonerUtil.infer(reasoner, augmentedModel);
				if (inferedPre.size() == prevDeductions) {
					inferedPre = ReasonerUtil.infer(reasoner, augmentedModel);
//					inferedPre = ReasonerUtil.infer(reasoner, augmentedModel);
					unstableSatementsMdl=Jena.createModel();
					unstableSatementsMdl.add(inferedPre.getDeductionsModel().listStatements());
				}else {
					prevDeductions=inferedPre.size();
				}
			}
		}

		public void runSimpleStabelization(int iterations) {
			for(int i=0; i<iterations; i++) {
				InfModel inferedPre = ReasonerUtil.infer(reasoner, augmentedModel);
				augmentedModel.add(inferedPre);
//				inferedPre = ReasonerUtil.infer(reasoner, augmentedModel);
//				inferedPre = ReasonerUtil.infer(reasoner, augmentedModel);
			}
			unstableSatementsMdl=Jena.createModel();
		}
		
		public Model inferNewDeductions(Model model) {
			InfModel inf = ReasonerUtil.infer(reasoner, augmentedModel, model);
			Model deductionsModel=Jena.createModel();
			deductionsModel.add(inf.getDeductionsModel());
//			System.out.println("Ded: "+ deductionsModel.size());
			deductionsModel.remove(unstableSatementsMdl);
//			System.out.println("Ded after: "+ deductionsModel.size());
			for (Statement stm: deductionsModel.listStatements(null, Rdfs.subPropertyOf, (RDFNode)null).toList()){
				if(stm.getObject().equals(stm.getSubject()))
					deductionsModel.remove(stm);				
			}
			for (Statement stm: deductionsModel.listStatements(null, Rdfs.subClassOf, (RDFNode)null).toList()){
				if(stm.getObject().equals(stm.getSubject()) || stm.getObject().equals(Rdfs.Resource))
					deductionsModel.remove(stm);				
			}
			for (Statement stm: deductionsModel.listStatements(null, Rdf.type, Rdfs.Resource).toList()){
				if(stm.getObject().equals(stm.getSubject()) || stm.getObject().equals(Rdfs.Resource))
					deductionsModel.remove(stm);				
			}
//			System.out.println("Ded after: "+ deductionsModel.size());
			return deductionsModel;
		}
		public static Model cleanDeductions(Model model) {
			Model deductionsModel=Jena.createModel();
			deductionsModel.add(model);
			for (Statement stm: deductionsModel.listStatements(null, Rdfs.subPropertyOf, (RDFNode)null).toList()){
				if(stm.getObject().equals(stm.getSubject()))
					deductionsModel.remove(stm);				
			}
			for (Statement stm: deductionsModel.listStatements(null, Rdfs.subClassOf, (RDFNode)null).toList()){
				if(stm.getObject().equals(stm.getSubject()) || stm.getObject().equals(Rdfs.Resource))
					deductionsModel.remove(stm);				
			}
			for (Statement stm: deductionsModel.listStatements(null, Rdf.type, Rdfs.Resource).toList()){
				if(stm.getObject().equals(stm.getSubject()) || stm.getObject().equals(Rdfs.Resource))
					deductionsModel.remove(stm);				
			}
			return deductionsModel;
		}

		public Model getUnstableSatements() {
			return unstableSatementsMdl;
		}
	}