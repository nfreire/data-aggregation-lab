package inescid.dataaggregation.dataset.convert.rdfconverter;

import java.io.File;
import java.io.FileReader;
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
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import crawlercommons.sitemaps.SiteMapURL;
import inescid.dataaggregation.crawl.sitemap.CrawlResourceHandler;
import inescid.dataaggregation.crawl.sitemap.SitemapResourceCrawler;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.store.PublicationRepository;
import inescid.util.RdfUtil;
import inescid.util.googlesheets.GoogleSheetsCsvUploader;

public class ConversionSpecificationAnalyzer {

	MappingReport rpt;
	public void process(Dataset dataset, PublicationRepository pubRepo) throws Exception {
		rpt=new MappingReport();
		File profileFolder=GlobalCore.getPublicationRepository().getProfileFolder(dataset);
		File schemaorgFile = new File(profileFolder, "schema.org-profile.csv");
		CSVParser csvParser=CSVParser.parse(FileUtils.readFileToString(schemaorgFile, GlobalCore.UTF8), CSVFormat.DEFAULT);
		
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
							Map<Resource, Integer> mappedRes = new HashMap<>();
							rpt.usageCounts.put(aClass, mappedRes);
							mappedRes.put(aClass, Integer.parseInt(rec.get(1)));							
						}
						break;
					case 2: //skip blank line, read class
						if(!rec.get(0).trim().isEmpty()) {
							currentClass = RdfUtil.Jena.createResource(rec.get(0));
							currentClassSpec.clear();
							Resource[] mappedClasses=getMappedClassesAndSpecs(currentClassSpec, currentClass);
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
								rpt.usageCounts.get(currentClass).put(prop, usageAsSubjectCnt);	
								Set<Property> mappedProps = currentClassMappings.get(prop);
								if(mappedProps==null) {
									mappedProps=new HashSet<>();
									currentClassMappings.put(prop, mappedProps);
								}
								mappedProps.addAll(getAllMappedProperties(prop, currentClass, currentClassSpec));
								mappedProps.addAll(getAllMappedPropertiesInMerge(prop, currentClass, currentClassSpec));
							}
						}
						break;
				}
//			}
							
		}			
		csvParser.close();
		
		processReferencedResourcesMappings();
		
		File reportFile = new File(profileFolder, "schema.org-mapping-analysis.csv");
		FileUtils.write(reportFile, rpt.toCsv(), GlobalCore.UTF8, false);
		
		String spreadsheetId=GoogleSheetsCsvUploader.getDatasetAnalysisSpreadsheet(dataset, GoogleSheetsCsvUploader.sheetTitleFromFileName(reportFile));
		GoogleSheetsCsvUploader.update(spreadsheetId, reportFile);			
	}

	private Resource[] getMappedClassesAndSpecs(List<ResourceTypeConversionSpecification> currentClassSpec, Resource currentClass) {
		Resource[] mappedClasses=SchemaOrgToEdmConversionSpecification.spec.getRootResourceTypeMapping(currentClass);
		if(mappedClasses==null) {
			Resource typeMapping = SchemaOrgToEdmConversionSpecification.spec.getTypeMapping(currentClass);
			if(typeMapping!=null)
				mappedClasses= new Resource[] {typeMapping};
		}
		if(mappedClasses==null)
			mappedClasses=new Resource[0];
		else {
			for(Resource trgClass: mappedClasses)
				currentClassSpec.add(SchemaOrgToEdmConversionSpecification.spec.getTypePropertiesMapping(trgClass));
		}
		return mappedClasses;
	}

	private void processReferencedResourcesMappings() {
		for(Resource currentClass: rpt.usageCounts.keySet()) {
			if(rpt.usageCounts.get(currentClass).get(currentClass)==0) continue;
			List<ResourceTypeConversionSpecification> currentClassSpec=new ArrayList<>();
//			Resource[] mappedClasses=
					getMappedClassesAndSpecs(currentClassSpec, currentClass);
			for(Resource currentProp: rpt.usageCounts.get(currentClass).keySet()) {
				if(currentProp.equals(currentClass) || rpt.usageCounts.get(currentClass).get(currentProp)==0) continue;
				System.out.println("RefRes in:"+currentClass+" - "+currentProp);
				
//				if(currentClass.getURI().equals("http://schema.org/GeoCoordinates")) {
//				if(currentClass.getURI().equals("http://schema.org/Place")) {
//					System.out.println("Debug here");
//				}
				
				
				Set<Entry<Property, Property>> allMappedPropertiesInReferencedResources = getAllMappedPropertiesInReferencedResources((Property)currentProp, currentClass, currentClassSpec);
//				System.out.println(allMappedPropertiesInReferencedResources);
				for(Entry<Property, Property> map: allMappedPropertiesInReferencedResources) {
					rpt.addPropertyMapInReferencedResource(map.getKey(), map.getValue());
				}
				
				
//				Set<Property> mappedProps = new HashSet<>();
//				for(ResourceTypeConversionSpecification spec: currentClassSpec) {
//					for(Entry<Property, Property> mapping : spec.getPropertiesMapping().entrySet()) {
//						if(mapping.getKey().equals(currentProp)) continue;
//						System.out.println("RefRes inner spec: "+mapping.getKey()+" - "+mapping.getValue());
//						
//						
////						Set<Property> allMappedPropertiesInReferencedResources = getAllMappedPropertiesInReferencedResources((Property)currentProp, currentClass, currentClassSpec);
////						System.out.println(allMappedPropertiesInReferencedResources);
//					}
//				}
//				
//				
//				
//				System.out.println("RefRes inner spec: "+mapping.getKey()+" - "+mapping.getValue());
//				for(ResourceTypeConversionSpecification spec: currentClassSpec) {
//					Property propertyMapping = spec.getPropertyMapping((Property)currentProp);
//					
//
//					
//					...
//					
//					List<ResourceTypeConversionSpecification> propertiesMappingFromReferencedResource = spec.searchPropertyMappingFromReferencedResource((Property) currentProp);
//					for(ResourceTypeConversionSpecification propertyMappingFromReferencedResource: propertiesMappingFromReferencedResource) {
//						Entry<Property, Property> srcPropMapping=propertyMappingFromReferencedResource.getPropertiesMapping().entrySet().iterator().next();
//						Property srcProp=srcPropMapping.getKey();
//						Property trg=srcPropMapping.getKey();
//						mappedProps.add(trg);
//			//			Resource[] mappedClasses=SchemaOrgToEdmConversionSpecification.spec.getRootResourceTypeMapping(currentClass);
//			//			currentClassSpec=SchemaOrgToEdmConversionSpecification.spec.getTypePropertiesMapping(currentClass);
//			//			if(mappedClasses==null)
//			//				mappedClasses=new Resource[0];
//			//			rpt.rdfTypeMaps.put(currentClass, mappedClasses);
//					}
				
				
				
//				
//				Set<Property> mappedProps = new HashSet<>();
//				for(ResourceTypeConversionSpecification spec: currentClassSpec) {
//					for(Entry<Property, Property> mapping : spec.getPropertiesMapping().entrySet()) {
//						if(mapping.getKey().equals(prop)) continue;
//						System.out.println("RefRes inner spec: "+mapping.getKey()+" - "+mapping.getValue());
//						
//						List<ResourceTypeConversionSpecification> propertiesMappingFromReferencedResource = spec.searchPropertyMappingFromReferencedResource(prop);
//						for(ResourceTypeConversionSpecification propertyMappingFromReferencedResource: propertiesMappingFromReferencedResource) {
//							Entry<Property, Property> srcPropMapping=propertyMappingFromReferencedResource.getPropertiesMapping().entrySet().iterator().next();
//							Property srcProp=srcPropMapping.getKey();
//							Property trg=srcPropMapping.getKey();
//							mappedProps.add(trg);
//				//			Resource[] mappedClasses=SchemaOrgToEdmConversionSpecification.spec.getRootResourceTypeMapping(currentClass);
//				//			currentClassSpec=SchemaOrgToEdmConversionSpecification.spec.getTypePropertiesMapping(currentClass);
//				//			if(mappedClasses==null)
//				//				mappedClasses=new Resource[0];
//				//			rpt.rdfTypeMaps.put(currentClass, mappedClasses);
//						}
//					}
//				}
//				return mappedProps;
				
			}
		}
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
//	System.out.println(currentClass);
//	System.out.println(prop);
//			List<ResourceTypeConversionSpecification> propertiesMappingFromReferencedResource = spec.searchPropertyMappingFromReferencedResource(prop);
//			for(ResourceTypeConversionSpecification propertyMappingFromReferencedResource: propertiesMappingFromReferencedResource) {
//				Entry<Property, Property> srcPropMapping=propertyMappingFromReferencedResource.getPropertiesMapping().entrySet().iterator().next();
//				Property srcProp=srcPropMapping.getKey();
//				Property trg=srcPropMapping.getKey();
//				mappedProps.add(trg);
//	//			Resource[] mappedClasses=SchemaOrgToEdmConversionSpecification.spec.getRootResourceTypeMapping(currentClass);
//	//			currentClassSpec=SchemaOrgToEdmConversionSpecification.spec.getTypePropertiesMapping(currentClass);
//	//			if(mappedClasses==null)
//	//				mappedClasses=new Resource[0];
//	//			rpt.rdfTypeMaps.put(currentClass, mappedClasses);
//			}
		}
		return mappedProps;
	}
	private static Set<Property> getAllMappedPropertiesInMerge(Property prop, Resource currentClass, List<ResourceTypeConversionSpecification> currentClassSpec){
		Set<Property> mappedProps = new HashSet<>();
		for(ResourceTypeConversionSpecification spec: currentClassSpec) {
			for(Entry<Property, Property> mapping : spec.getPropertiesMapping().entrySet()) {
				if(mapping.getKey().equals(prop)) continue;
				Property[] merdedProps=spec.getPropertyMerge(mapping.getKey());
				if(merdedProps!=null) {
					for(Property p: merdedProps) {
						if(p.equals(prop)) {
							mappedProps.add(mapping.getValue());
							break;
						}
					}
				}
			}
		}
		return mappedProps;
	}
	private static Set<Entry<Property, Property>> getAllMappedPropertiesInReferencedResources(Property prop, Resource currentClass, List<ResourceTypeConversionSpecification> currentClassSpec){
		System.out.println("RefRes in:"+currentClass+" - "+prop);
		
		if(
//				currentClass.getURI().equals("http://schema.org/GeoCoordinates") ||
				currentClass.getURI().equals("http://schema.org/Place")) {
			System.out.println("Debug here");
		}
		
		Set<Entry<Property, Property>> mappedProps = new HashSet<>();
		for(ResourceTypeConversionSpecification spec: currentClassSpec) {
			for(Entry<Property, Property> mapping : spec.getPropertiesMapping().entrySet()) {
				if(mapping.getKey().equals(prop)) continue;
				System.out.println("RefRes inner spec: "+mapping.getKey()+" - "+mapping.getValue());
				
				List<ResourceTypeConversionSpecification> propertiesMappingFromReferencedResource = spec.searchPropertyMappingFromReferencedResource(mapping.getKey());
				for(ResourceTypeConversionSpecification propertyMappingFromReferencedResource: propertiesMappingFromReferencedResource) {
					for(Entry<Property, Property> srcPropMapping: propertyMappingFromReferencedResource.getPropertiesMapping().entrySet()) {
						mappedProps.add(srcPropMapping);
					}
//					Entry<Property, Property> srcPropMapping=propertyMappingFromReferencedResource.getPropertiesMapping().entrySet().iterator().next();
//					mappedProps.add(srcPropMapping);

					
					
					//					Property srcProp=srcPropMapping.getKey();
//					Property trg=srcPropMapping.getValue();
//					mappedProps.add(srcProp);
					
		//			Resource[] mappedClasses=SchemaOrgToEdmConversionSpecification.spec.getRootResourceTypeMapping(currentClass);
		//			currentClassSpec=SchemaOrgToEdmConversionSpecification.spec.getTypePropertiesMapping(currentClass);
		//			if(mappedClasses==null)
		//				mappedClasses=new Resource[0];
		//			rpt.rdfTypeMaps.put(currentClass, mappedClasses);
				}
			}
		}
		return mappedProps;
	}
	
	
	
	public MappingReport getRpt() {
		return rpt;
	}



	public static void main(String[] args) {
		try {
//			GlobalCore.init_developement();
//
//			ConversionSpecificationAnalyzer c=new ConversionSpecificationAnalyzer();
//			Dataset dataset = GlobalCore.getDatasetRegistryRepository().getDatasetByUri("https://scrc.lib.ncsu.edu/sal_staging/iiif-discovery.json");
//			c.process(dataset, GlobalCore.getPublicationRepository());
//			System.out.println( c.getRpt().toCsv() );
//
//			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
		
	}


}
