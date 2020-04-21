package inescid.dataaggregation.casestudies.ontologies.reasoning;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import inescid.dataaggregation.data.RegOwl;
import inescid.dataaggregation.data.RegRdf;
import inescid.dataaggregation.data.RegSchemaorg;
import inescid.dataaggregation.dataset.profile.ClassUsageStats;
import inescid.dataaggregation.dataset.profile.ClassUsageStats.Sort;
import inescid.util.StatisticCalcMean;
import inescid.util.datastruct.MapOfInts;

public class ReasoningStats {
	StatisticCalcMean reasoningTime= new StatisticCalcMean();
	StatisticCalcMean reasoningTimeTotal= new StatisticCalcMean();
	StatisticCalcMean reasoningTimeSubsetSelect= new StatisticCalcMean();
	StatisticCalcMean wikidataTriples=new StatisticCalcMean();
	ClassUsageStats wikidataTriplesProfile=new ClassUsageStats();
	StatisticCalcMean schemaorgDeductedTriples=new StatisticCalcMean();
	StatisticCalcMean schemaorgDeductedTriplesPredicate=new StatisticCalcMean();
	StatisticCalcMean schemaorgDeductedTriplesType=new StatisticCalcMean();
	StatisticCalcMean deductedTriples=new StatisticCalcMean();
	StatisticCalcMean subSchemaTriples=new StatisticCalcMean();
	StatisticCalcMean subSchemaTriplesWikidata=new StatisticCalcMean();
	StatisticCalcMean subSchemaTriplesSchemaorg=new StatisticCalcMean();
	StatisticCalcMean subSchemaTriplesOwl=new StatisticCalcMean();
	ClassUsageStats deductedTriplesProfile=new ClassUsageStats();
	HashMap<String, StatisticCalcMean> deductedRdfTypesPerNamespace=new HashMap<String, StatisticCalcMean>();
	
	public ReasoningStats() { }

	public void addWikidataResource(Resource subject) {
		wikidataTriplesProfile.collect(subject);
		wikidataTriples.enter(subject.listProperties().toList().size());
	}
	
	
	long reasoningTotalStartNanos=0;
	public void startReasoningTotalChronometer() {
		reasoningTotalStartNanos=System.nanoTime();
	}
	public void endReasoningTotalChronometer() {
		reasoningTimeTotal.enter(System.nanoTime() - reasoningTotalStartNanos);
	}
	
	
	long reasoningStartNanos=0;
	public void startReasoningChronometer() {
		reasoningStartNanos=System.nanoTime();
	}
	public void endReasoningChronometer() {
		reasoningTime.enter(System.nanoTime() - reasoningStartNanos);
	}
	
	long reasoningSubsetSelectStartNanos=0;
	public void startReasoningSubsetSelectChronometer() {
		reasoningSubsetSelectStartNanos=System.nanoTime();
	}
	public void endReasoningSubsetSelectChronometer() {
		reasoningTimeSubsetSelect.enter(System.nanoTime() - reasoningSubsetSelectStartNanos);
	}

	public void addDeductions(Resource subject) {
		deductedTriplesProfile.collect(subject);
		deductedTriples.enter(subject.listProperties().toList().size());

		MapOfInts<String> typesPerNamespaceCount=new MapOfInts<String>();
		for(Statement st : IteratorUtils.asIterable(subject.listProperties(RegRdf.type))) {
			if(st.getObject().isURIResource()) 
				typesPerNamespaceCount.incrementTo(st.getObject().asResource().getNameSpace());
		}
		for(Entry<String, Integer> nsStat : typesPerNamespaceCount.entrySet()) {
			StatisticCalcMean statCalc = deductedRdfTypesPerNamespace.get(nsStat.getKey());
			if(statCalc==null) {
				statCalc=new StatisticCalcMean();
				deductedRdfTypesPerNamespace.put(nsStat.getKey(), statCalc);
			}
			statCalc.enter(nsStat.getValue());
		}
		Integer schemaorgTypeCnt = typesPerNamespaceCount.get(RegSchemaorg.NS);
		if(schemaorgTypeCnt==null)
			schemaorgTypeCnt=0;
		schemaorgDeductedTriplesType.enter(schemaorgTypeCnt);
		int schemaOrgTripleCnt=0;
		StmtIterator properties = subject.listProperties();
		for(Statement st : properties.toList()) {
			if(st.getPredicate().getNameSpace().equals(RegSchemaorg.NS))
					schemaOrgTripleCnt++;
		}
		schemaorgDeductedTriplesPredicate.enter(schemaOrgTripleCnt);
		schemaOrgTripleCnt+=schemaorgTypeCnt;
		schemaorgDeductedTriples.enter(schemaOrgTripleCnt);
	}
	public void addSubSchema(Model subSchema) {
		subSchemaTriples.enter(subSchema.size());
		int wdCnt=0;
		int schemaorgCnt=0;
		int owlCnt=0;
		
		for(Statement r: IteratorUtils.asIterable(subSchema.listStatements())){
			if(r.getSubject().isURIResource()) {
				if(r.getSubject().getURI().startsWith(RegSchemaorg.NS))
					schemaorgCnt++;
				else if(r.getSubject().getURI().startsWith(RegOwl.NS))
					owlCnt++;
				else 
					wdCnt++;
			}
		}
		subSchemaTriplesWikidata.enter(wdCnt);
		subSchemaTriplesSchemaorg.enter(schemaorgCnt);
		subSchemaTriplesOwl.enter(owlCnt);
	}

	public String toCsv() {
		DecimalFormat nanosFormatForHuman=new DecimalFormat("#,###.0");
		DecimalFormat nanosFormat=new DecimalFormat("#0.0");
		DecimalFormat meanFormatForHuman=new DecimalFormat("#,###.0");
		DecimalFormat meanFormat=new DecimalFormat("#0.0");
		try {
			StringBuilder sb=new StringBuilder();
			CSVPrinter printer=new CSVPrinter(sb, CSVFormat.DEFAULT);
			printer.printRecord("Reasoning time (ms)", nanosFormat.format(reasoningTime.getMean()/1000000), nanosFormatForHuman.format(reasoningTime.getMean()/1000000) +"\u00B1" + nanosFormat.format(reasoningTime.getStandardDeviation()/1000000));  
			printer.printRecord("Reasoning selection time (ms)", nanosFormat.format(reasoningTimeSubsetSelect.getMean()/1000000),  nanosFormatForHuman.format(reasoningTimeSubsetSelect.getMean()/1000000)+"\u00B1"+nanosFormat.format(reasoningTimeSubsetSelect.getStandardDeviation()/1000000));  
			printer.printRecord("Reasoning total time (ms)", nanosFormat.format(reasoningTimeTotal.getMean()/1000000), nanosFormatForHuman.format(reasoningTimeTotal.getMean()/1000000)+"\u00B1"+nanosFormat.format(reasoningTimeTotal.getStandardDeviation()/1000000));  
			printer.printRecord("Wikidata Cho resource count in sample", wikidataTriplesProfile.getClassUseCount());
			printer.printRecord("Wikidata Cho resource triples", meanFormat.format(wikidataTriples.getMean()), meanFormatForHuman.format(wikidataTriples.getMean()) +"\u00B1"+meanFormat.format(wikidataTriples.getStandardDeviation()));
			printer.printRecord("Subschema triples", meanFormat.format(subSchemaTriples.getMean()), meanFormatForHuman.format(subSchemaTriples.getMean())+"\u00B1"+meanFormat.format(subSchemaTriples.getStandardDeviation()));  
			printer.printRecord("Subschema triples Wikidata", meanFormat.format(subSchemaTriplesWikidata.getMean()), meanFormatForHuman.format(subSchemaTriplesWikidata.getMean()) +"\u00B1"+meanFormat.format(subSchemaTriplesWikidata.getStandardDeviation()));  
			printer.printRecord("Subschema triples Schema.org", meanFormat.format(subSchemaTriplesSchemaorg.getMean()), meanFormatForHuman.format(subSchemaTriplesSchemaorg.getMean())+"\u00B1"+meanFormat.format(subSchemaTriplesSchemaorg.getStandardDeviation()));  
			printer.printRecord("Subschema triples OWL", meanFormat.format(subSchemaTriplesOwl.getMean()), meanFormatForHuman.format(subSchemaTriplesOwl.getMean())+"\u00B1"+meanFormat.format(subSchemaTriplesOwl.getStandardDeviation()));  
			printer.printRecord("Wikidata triples profile");
			wikidataTriplesProfile.toCsv(printer, null, true, Sort.COUNT);
//			wikidataTriplesProfile.toCsv(printer, null, true, Sort.COUNT);
			printer.printRecord("Deducted triples", meanFormat.format(deductedTriples.getMean()), meanFormatForHuman.format(deductedTriples.getMean())+"\u00B1"+meanFormat.format(deductedTriples.getStandardDeviation()));  
			printer.printRecord("Deducted Schema.org predicate triples", meanFormat.format(schemaorgDeductedTriplesPredicate.getMean()), meanFormatForHuman.format(schemaorgDeductedTriplesPredicate.getMean())+"\u00B1"+meanFormat.format(schemaorgDeductedTriplesPredicate.getStandardDeviation()));  
			printer.printRecord("Deducted Schema.org type triples", meanFormat.format(schemaorgDeductedTriplesType.getMean()), meanFormatForHuman.format(schemaorgDeductedTriplesType.getMean())+"\u00B1"+meanFormat.format(schemaorgDeductedTriplesType.getStandardDeviation()));  
			printer.printRecord("Deducted Schema.org triples total", meanFormat.format(schemaorgDeductedTriples.getMean()), meanFormatForHuman.format(schemaorgDeductedTriples.getMean())+"\u00B1"+meanFormat.format(schemaorgDeductedTriples.getStandardDeviation()));  
			printer.printRecord("Deducted triples profile");
			deductedTriplesProfile.toCsv(printer, null, true, Sort.COUNT);
			printer.printRecord("Deducted rdf:type per namespace");
			MapOfInts<String> toSort=new MapOfInts<String>();
			for(Entry<String, StatisticCalcMean> ns: deductedRdfTypesPerNamespace.entrySet()) {
				toSort.put(ns.getKey(), (int)ns.getValue().getCount());
			}
			for(Entry<String, Integer> ns: toSort.getSortedEntries()) {
				StatisticCalcMean nsOrigStats=deductedRdfTypesPerNamespace.get(ns.getKey());
				StatisticCalcMean nsStats=null;
				if(nsOrigStats.getCount()<wikidataTriplesProfile.getClassUseCount()) {
					nsStats=nsOrigStats.copy();
					for(double i=nsOrigStats.getCount() ; i<wikidataTriplesProfile.getClassUseCount(); i++) 
						nsStats.enter(0);
				} else
					nsStats=nsOrigStats;
				printer.printRecord(ns.getKey(), nsOrigStats.getCount(), meanFormat.format(nsStats.getMean()), meanFormatForHuman.format(nsStats.getMean())+"\u00B1"+meanFormat.format(nsStats.getStandardDeviation()));  
			}
			return sb.toString();
		} catch (IOException e) {
			//should not happen on a StringBuilder
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
