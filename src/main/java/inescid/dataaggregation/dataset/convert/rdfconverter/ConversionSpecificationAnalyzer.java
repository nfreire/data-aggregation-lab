package inescid.dataaggregation.dataset.convert.rdfconverter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.store.PublicationRepository;
import inescid.util.MapOfInts;
import inescid.util.RdfUtil;
import inescid.util.googlesheets.GoogleSheetsCsvUploader;

public class ConversionSpecificationAnalyzer {

	public static  class MappingReport {
		Map<Resource,Integer> usageCounts=new MapOfInts<>(); 
		Map<Resource, Resource[]> rdfTypeMaps=new HashMap<>(); 
		Map<Resource, Map<Property, Set<Property>>> propertiesMaps=new HashMap<>(); //properties, propertiesMerge and derived properties
//		Map<ImmutablePair<Property, Resource>, ResourceTypeConversionSpecification> propertiesMappingFromReferencedResources;
	
		public String toString() {
			StringBuilder sb=new StringBuilder();
			for(Entry<Resource, Resource[]> e: rdfTypeMaps.entrySet()) {
				System.out.print(e.getKey()+" ("+usageCounts.get(e.getKey())+" uses) -");
				if(e.getValue().length==0) {
					System.out.println(" NOT MAPPED");
				} else {
					for(Resource r: e.getValue()) {
						System.out.print(" "+r);
					}
					System.out.println();
				}
				Map<Property, Set<Property>> map = propertiesMaps.get(e.getKey());
				for(Entry<Property, Set<Property>> e2: map.entrySet()) {
					System.out.print("    # "+e2.getKey()+" ("+usageCounts.get(e2.getKey())+"  uses) -");
					if(e2.getValue().size()==0) {
						System.out.println(" NOT MAPPED");
					} else {
						for(Property r: e2.getValue()) {
							System.out.print(" "+r);
						}
						System.out.println();
					}
				}
				System.out.println();
			}
			System.out.println();
			
			return sb.toString();
		}
		
		public String toCsv() {
			try {
				StringBuilder sb=new StringBuilder();
				CSVPrinter printer=new CSVPrinter(sb, CSVFormat.DEFAULT);
				printer.printRecord("Resource","Property","Usage count","Mapped/converted to");
				for(Entry<Resource, Resource[]> e: rdfTypeMaps.entrySet()) {
					printer.print(e.getKey());
					printer.print("");
					printer.print(usageCounts.get(e.getKey()));
					if(e.getValue().length==0) {
						printer.print("-");
					} else {
						for(Resource r: e.getValue()) {
							printer.print(r);
						}
					}
					printer.println();
					Map<Property, Set<Property>> map = propertiesMaps.get(e.getKey());
					for(Entry<Property, Set<Property>> e2: map.entrySet()) {
						printer.print("");
						printer.print(e2.getKey());
						printer.print(usageCounts.get(e2.getKey()));
						if(e2.getValue().size()==0) {
							printer.print("-");
						} else {
							for(Property r: e2.getValue()) {
								printer.print(r);
							}
						}
						printer.println();
					}
					printer.println();
				}
				printer.close();
				return sb.toString();
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	
	}
	
	MappingReport rpt;

	public static void main(String[] args) throws Exception {
		File profileFolder=new File("C:\\Users\\nfrei\\workspace-eclipse\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\data-aggregation-lab\\static\\data\\http%3A%2F%2Fdata.bibliotheken.nl%2Fid%2Fdataset%2Frise-centsprenten\\profile");
		
	}
	public void process(Dataset dataset, PublicationRepository pubRepo) throws Exception {
		rpt=new MappingReport();
		File profileFolder=Global.getPublicationRepository().getProfileFolder(dataset);
		File schemaorgFile = new File(profileFolder, "schema.org-profile.csv");
		CSVParser csvParser=CSVParser.parse(FileUtils.readFileToString(schemaorgFile, Global.UTF8), CSVFormat.DEFAULT);
		
		MappingReport rpt=new MappingReport();
		
		int csvFileSection=1;
		Resource currentClass=null;
		Map<Property, Set<Property>> currentClassMappings=null;
		List<ResourceTypeConversionSpecification> currentClassSpec=new ArrayList<>();
		for(Iterator<CSVRecord> it = csvParser.iterator() ; it.hasNext() ; ) {
			CSVRecord rec = it.next();
//			System.out.println(csvFileSection);
//			System.out.println(rec.get(0)+"-");
//			if(rec.size()==0) {
//				csvFileSection++;
//			} else {
				switch (csvFileSection) {
					case 1:
						if(rec.get(0).trim().isEmpty()) 
							csvFileSection=2;
						else if(!rec.get(0).equals("class")){
							Resource aClass = RdfUtil.Jena.createResource(rec.get(0));
							rpt.usageCounts.put(aClass, Integer.parseInt(rec.get(1)));							
						}
						break;
					case 2: //skip blank line, read class
						if(!rec.get(0).trim().isEmpty()) {
							currentClass = RdfUtil.Jena.createResource(rec.get(0));
							Resource[] mappedClasses=SchemaOrgToEdmConversionSpecification.spec.getRootResourceTypeMapping(currentClass);
							if(mappedClasses==null)
								mappedClasses=new Resource[0];
							else {
								currentClassSpec.clear();
								for(Resource trgClass: mappedClasses)
									currentClassSpec.add(SchemaOrgToEdmConversionSpecification.spec.getTypePropertiesMapping(trgClass));
							}
							rpt.rdfTypeMaps.put(currentClass, mappedClasses);
							currentClassMappings = new HashMap<>();
							rpt.propertiesMaps.put(currentClass, currentClassMappings);
							csvFileSection=3;					
						}
						break;
					case 3:
						if(rec.get(0).trim().isEmpty()) 
							csvFileSection=2;
						else if(!StringUtils.isEmpty(rec.get(0)) && !rec.get(0).trim().equals("property")){
							int usageAsSubjectCnt = Integer.parseInt(rec.get(1));
							if(usageAsSubjectCnt>0) {
								Property prop = RdfUtil.Jena.createProperty(rec.get(0));
								rpt.usageCounts.put(prop, usageAsSubjectCnt);
								Set<Property> mappedProps = currentClassMappings.get(prop);
								if(mappedProps==null) {
									mappedProps=new HashSet<>();
									currentClassMappings.put(prop, mappedProps);
								}
								mappedProps.addAll(getAllMappedProperties(prop, currentClass, currentClassSpec));
							}
						}
						break;
				}
//			}
							
		}			
		csvParser.close();
		
		File reportFile = new File(profileFolder, "schema.org-mapping-analysis.csv");
		FileUtils.write(reportFile, rpt.toCsv(), Global.UTF8, false);
		
		String spreadsheetId=GoogleSheetsCsvUploader.getDatasetAnalysisSpreadsheet(dataset, GoogleSheetsCsvUploader.sheetTitleFromFileName(reportFile));
		GoogleSheetsCsvUploader.update(spreadsheetId, reportFile);			
	}

	
	
	private static Set<Property> getAllMappedProperties(Property prop, Resource currentClass, List<ResourceTypeConversionSpecification> currentClassSpec){
		Set<Property> mappedProps = new HashSet<>();
		for(ResourceTypeConversionSpecification spec: currentClassSpec) {
			Property propertyMapping = spec.getPropertyMapping(prop);
			if (propertyMapping != null)
				mappedProps.add(propertyMapping);
			DerivedPropertyConversionSpecification derivedPropertyMapping = spec.getDerivedPropertyMapping(prop);
			if(derivedPropertyMapping!=null)
				mappedProps.add(derivedPropertyMapping.getDerivedProperty());
			Property[] propertyMerge = spec.getPropertyMerge(prop);
			if(propertyMerge!=null)
				for(Property map: propertyMerge) 
					mappedProps.add(map);
	
			List<ResourceTypeConversionSpecification> propertiesMappingFromReferencedResource = spec.searchPropertyMappingFromReferencedResource(prop);
			for(ResourceTypeConversionSpecification propertyMappingFromReferencedResource: propertiesMappingFromReferencedResource) {
				Entry<Property, Property> srcPropMapping=propertyMappingFromReferencedResource.getPropertiesMapping().entrySet().iterator().next();
				Property srcProp=srcPropMapping.getKey();
				Property trg=srcPropMapping.getKey();
				mappedProps.add(trg);
	//			Resource[] mappedClasses=SchemaOrgToEdmConversionSpecification.spec.getRootResourceTypeMapping(currentClass);
	//			currentClassSpec=SchemaOrgToEdmConversionSpecification.spec.getTypePropertiesMapping(currentClass);
	//			if(mappedClasses==null)
	//				mappedClasses=new Resource[0];
	//			rpt.rdfTypeMaps.put(currentClass, mappedClasses);
			}
		}
		return mappedProps;
	}
	public MappingReport getRpt() {
		return rpt;
	}
}
