package inescid.dataaggregation.casestudies.dataquality;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.any23.extractor.csv.CSVReaderBuilder;
import org.apache.any23.writer.BenchmarkTripleHandler;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RiotException;

import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.profile.completeness.TiersDqcCompletenessCalculator;
import inescid.dataaggregation.dataset.profile.multilinguality.MultilingualSaturation;
import inescid.dataaggregation.dataset.profile.multilinguality.MultilingualSaturationResult;
import inescid.dataaggregation.dataset.profile.tiers.EpfTiersCalculator;
import inescid.dataaggregation.dataset.profile.tiers.TiersCalculation;
import inescid.dataaggregation.store.Repository;
import inescid.dataaggregation.store.RepositoryResource;
import inescid.util.RdfUtil;

public class ProcessRepository {

public interface EdmMeasurement {
	public String[] getCsvResult(Resource edmCho, String edmRdfXml) throws Exception;

	public String[] getHeaders();
}

	public static void main(String[] args) throws IOException {
    	String repoFolder = "c://users/nfrei/desktop/data/EuropeanaRepository";
    	String datasetId = "data.europeana.eu"; //"https://api.europeana.eu/oai/record"
    	String outputFolder = "c://users/nfrei/desktop/data/";
		
		if(args!=null) {
			if(args.length>=1) {
				repoFolder = args[0];
				if(args.length>=2) 
					datasetId = args[1];
				if(args.length>=3) 
					outputFolder = args[2];
			}
		}
		
		Global.init_componentDataRepository(repoFolder);
		Global.init_enableComponentHttpRequestCache();
		Repository repository = Global.getDataRepository();
		
		EdmMeasurement[] measurements=new EdmMeasurement[] {
			createEdmMeasurementCompleteness(),
			createEdmMeasurementMetadataTiers(),
			createEdmMeasurementLanguageSaturation()
		};
		
		BufferedWriter csvBuffer = Files.newBufferedWriter(new File(outputFolder, "edm-measurements.csv").toPath(), StandardCharsets.UTF_8);
		CSVPrinter csvOut=new CSVPrinter(csvBuffer, CSVFormat.DEFAULT);
		for(EdmMeasurement measurement : measurements) {
			String[] csvHeaders = measurement.getHeaders();
			for(String m:csvHeaders) 
				csvOut.print(m);
		}
		csvOut.println();
		
		
		Date start=new Date();
		int tmpCnt=0;
		int okRecs=0;
		Iterable<RepositoryResource> it = repository.getIterableOfResources(datasetId);
		for(RepositoryResource r: it) {
			try {
				String uri = r.getUri();
				String edmRdfXml = new String(r.getContent(), StandardCharsets.UTF_8);
				
				tmpCnt++;
				if(tmpCnt % 1000 == 0) {
					csvOut.flush();
					csvBuffer.flush();
					Date now=new Date();
					long elapsedPerRecord=(now.getTime()-start.getTime())/tmpCnt;
					double recsMinute=60000/elapsedPerRecord;
					double minutesToEnd=(58000000-tmpCnt)/recsMinute;
					System.out.printf("%d recs. (%d ok) - %d recs/min - %d mins. to end\n",tmpCnt, okRecs, (int) recsMinute, (int)minutesToEnd);
				}
				Model readRdf = RdfUtil.readRdf(edmRdfXml, org.apache.jena.riot.Lang.RDFXML);
				csvOut.print(uri);
				for(EdmMeasurement measurement : measurements) {
					try {
						String[] csvResult = measurement.getCsvResult(readRdf.getResource(uri), edmRdfXml);
						for(String m:csvResult) 
							csvOut.print(m);
					} catch (Exception e) {
						System.err.println("Error in: "+r.getUri());
						System.err.println(e.getMessage());
					}
				}
				okRecs++;
				csvOut.println();
			} catch (RiotException e) {
				System.err.println("Error reading RDF: "+r.getUri());
				System.err.println(e.getMessage());
//				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Error reading from repository: "+r.getUri());
				System.err.println(e.getMessage());
//				e.printStackTrace();
			}
		}
		csvOut.close();
		csvBuffer.close();
		System.out.println(tmpCnt);
	}

	private static EdmMeasurement createEdmMeasurementLanguageSaturation() {
		return new EdmMeasurement() {
			@Override
			public String[] getCsvResult(Resource edmCho, String edmRdfXml) {
				MultilingualSaturationResult score;
				try {
					score = MultilingualSaturation.calculate(edmCho.getModel());
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage(), e);
				}
				return new String[] {String.valueOf(score.getLanguagesCount()) , String.valueOf(score.getLangTagCount())};
			};
			@Override
			public String[] getHeaders() {
				return new String[] {"ls_languagesCount", "ls_langTagCount"};
			}
		};
	}
	
	private static EdmMeasurement createEdmMeasurementCompleteness() {
		return new EdmMeasurement() {
			@Override
			public String[] getCsvResult(Resource edmCho, String edmRdfXml) {
				TiersDqcCompletenessCalculator calculator=new TiersDqcCompletenessCalculator();
				double calculate = calculator.calculate(edmCho.getModel());
				return new String[] {String.valueOf(calculate)};
			};
			@Override
			public String[] getHeaders() {
				return new String[] {"completeness"};
			}
		};
	}

	private static EdmMeasurement createEdmMeasurementMetadataTiers() {
		return new EdmMeasurement() {
			@Override
			public String[] getCsvResult(Resource edmCho, String edmRdfXml) throws Exception {
				TiersCalculation calculate = EpfTiersCalculator.calculate(edmRdfXml);
				return new String[] {String.valueOf(calculate.getContent().getLevel()), 
						String.valueOf(calculate.getMetadata().getLevel()),
						String.valueOf(calculate.getContextualClass().getLevel()),
						String.valueOf(calculate.getEnablingElements().getLevel()),
						String.valueOf(calculate.getLanguage().getLevel())
				};
			};
			@Override
			public String[] getHeaders() {
				return new String[] {"epf_media", "epf_metadata", "epf_metadata_contextual", "epf_metadata_enabling", "epf_metadata_language"};
			}
		};
	}
	
}
