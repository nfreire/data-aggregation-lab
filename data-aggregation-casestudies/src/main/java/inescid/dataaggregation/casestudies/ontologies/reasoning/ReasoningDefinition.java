package inescid.dataaggregation.casestudies.ontologies.reasoning;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;

import inescid.dataaggregation.casestudies.wikidata.RdfRegWikidata;
import inescid.dataaggregation.data.RegOwl;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegRdfs;
import inescid.dataaggregation.data.RegSkos;
import inescid.util.RdfUtil.Jena;

public class ReasoningDefinition {
	public static ReasoningDefinition WikidataModelDefinition=new ReasoningDefinition(
			new Property[] {
					RdfRegWikidata.SUBCLASS_OF, RdfRegWikidata.EQUIVALENT_CLASS, 
					RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RdfRegWikidata.BROADER_CONCEPT,
					
					RdfRegWikidata.SUBPROPERTY_OF, RdfRegWikidata.EQUIVALENT_PROPERTY,
					RdfRegWikidata.MAPPING_RELATION_TYPE, RdfRegWikidata.EXTERNAL_SUBPROPERTY,
					RdfRegWikidata.EXTERNAL_SUPERPROPERTY 
			},
			new Statement[] {
					Jena.createStatement(RdfRegWikidata.EQUIVALENT_CLASS, RegOwl.equivalentProperty,
							RegOwl.equivalentClass),
					Jena.createStatement(RdfRegWikidata.EQUIVALENT_PROPERTY, RegOwl.equivalentProperty,
							RegOwl.equivalentProperty),
					Jena.createStatement(RdfRegWikidata.SUBCLASS_OF, RegOwl.equivalentProperty,
							RegRdfs.subClassOf),
					Jena.createStatement(RdfRegWikidata.SUBPROPERTY_OF, RegOwl.equivalentProperty,
							RegRdfs.subPropertyOf),
					Jena.createStatement(RdfRegWikidata.INSTANCE_OF, RegOwl.equivalentProperty,
							RegRdf.type),
					Jena.createStatement(RdfRegWikidata.NARROWER_EXTERNAL_CLASS, RegOwl.equivalentProperty,
							RegSkos.narrowMatch),
					Jena.createStatement(RdfRegWikidata.BROADER_CONCEPT, RegOwl.equivalentProperty,
							RegSkos.broadMatch),
					Jena.createStatement(RdfRegWikidata.MAPPING_RELATION_TYPE, RegOwl.equivalentProperty,
							RegSkos.mappingRelation),
					Jena.createStatement(RdfRegWikidata.EXTERNAL_SUBPROPERTY, RegOwl.equivalentProperty,
							RegSkos.broadMatch),// there is no superProperty in rdfs
					Jena.createStatement(RdfRegWikidata.EXTERNAL_SUPERPROPERTY, RegOwl.equivalentProperty,
							RegRdfs.subPropertyOf)
			});
	
	Property[] propertySubset;
	Statement[] additionalStatements;
	
	public ReasoningDefinition(Property[] propertySubset, Statement[] additionalStatements) {
		super();
		this.propertySubset = propertySubset;
		this.additionalStatements = additionalStatements;
	}
	
	
}
