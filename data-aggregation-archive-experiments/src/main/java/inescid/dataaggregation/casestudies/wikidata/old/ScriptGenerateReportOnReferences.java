package inescid.dataaggregation.casestudies.wikidata.old;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang.StringUtils;

import inescid.dataaggregation.casestudies.wikidata.old.WikidataEntityCache.WikidataEntitySummary;

public class ScriptGenerateReportOnReferences {

	public static void main(String[] args) throws Exception {
		WikidataEntityCache wikidataClient=new WikidataEntityCache(WikidataEuropeanaConstants.CACHE_WIKIDATA_PROPERTIES);
		
		WikidataEdmMappings wdMappings=new WikidataEdmMappings(WikidataEuropeanaConstants.MAPPINGS_WD_EDM_CSV);
		
		Reader csvReader=new InputStreamReader(new FileInputStream(WikidataEuropeanaConstants.REFERENCES_CSV), "UTF-8");
		BufferedReader csvBufferedReader = new BufferedReader(csvReader);
		
		FileWriterWithEncoding htmlWriter=new FileWriterWithEncoding(WikidataEuropeanaConstants.REFERENCES_REPORT_HTML, "UTF-8");
		htmlWriter.write("<html><body>");

		boolean isTitleRow=true;
		int sectionOfReport=1;
		int europeanaIdsCount=0;
		int wikidataItemsCount=0;
		while(csvBufferedReader.ready()) {
			String prop = csvBufferedReader.readLine();
			if(isTitleRow) {
				isTitleRow=false;
				htmlWriter.write("<h1>"+prop+"</h1>\n");
				if (sectionOfReport==3) {
					htmlWriter.write("<table border='1' cellpadding='2'>\n");
					htmlWriter.write("<tr>\n");
					htmlWriter.write("\t<td><b>Europeana ID</b></td>\n");
					htmlWriter.write("\t<td><b>Wikidata ID</b></td>\n");
					htmlWriter.write("\t<td><b>Wikidata Label (en)</b></td>\n");
					htmlWriter.write("\t<td><b>Instance of</b></td>\n");
				} else {
					htmlWriter.write("<table border='1' cellpadding='2'>\n");
					htmlWriter.write("<tr>\n");
					htmlWriter.write("\t<td><b>Property ID</b></td>\n");
					htmlWriter.write("\t<td><b>Label (en)</b></td>\n");
					htmlWriter.write("\t<td><b>Data type</b></td>\n");
					htmlWriter.write("\t<td><b>Schema.org mapping</b></td>\n");
					htmlWriter.write("\t<td><b>EDM mapping</b></td>\n");
					htmlWriter.write("\t<td><b>count</b></td>\n");
				}
				htmlWriter.write("</tr>\n");
			} else if (StringUtils.isEmpty(prop)){
				isTitleRow=true;
				sectionOfReport++;
				htmlWriter.write("</table>\n");
			}else {
				if (sectionOfReport==3) {
					String wdId=prop.substring(prop.indexOf(',')+1);
					if(wdId.contains(","))
						wdId=wdId.substring(0,wdId.indexOf(',')-1);
					WikidataEntitySummary summary = wikidataClient.getSummary(wdId);
					String europeanaUrl = prop.substring(1,  prop.indexOf(',')-1);
					htmlWriter.write("\t<td><a href='"+europeanaUrl+"'>"+europeanaUrl+"</a></td>\n");
					htmlWriter.write("\t<td><a href='https://www.wikidata.org/wiki/"+summary.id+"'>"+summary.id+"</a></td>\n");
					htmlWriter.write("\t<td>"+(summary.labelEn == null ? "" : summary.labelEn)+"</td>\n");
					WikidataEntitySummary summaryOfDataType = wikidataClient.getSummary(summary.dataType);
					htmlWriter.write("\t<td>"+(summaryOfDataType==null ? "" : summaryOfDataType.labelEn)+"</td>\n");
					wikidataItemsCount++;
				} else {
					WikidataEntitySummary summary = wikidataClient.getSummary(prop.substring(0,  prop.indexOf(',')));
					if(summary.id.equals("P727"))
						europeanaIdsCount=Integer.parseInt(prop.substring(prop.indexOf(',')+1));
					else {
						htmlWriter.write("\t<td><a href='https://www.wikidata.org/wiki/Property:"+summary.id+"'>"+summary.id+"</a></td>\n");
						htmlWriter.write("\t<td>"+(summary.labelEn == null ? "" : summary.labelEn)+"</td>\n");
						htmlWriter.write("\t<td>"+summary.dataType.replaceFirst("http://www.wikidata.org/ontology#", "wdto:")+"</td>\n");
						htmlWriter.write("\t<td>"+(summary.schemaOrgEquivalent==null ? "" : summary.schemaOrgEquivalent.replaceFirst("http://schema.org/", "schema:"))+"</td>\n");
						String edmMapping=wdMappings.getFromWdId(summary.id);
						htmlWriter.write("\t<td>"+(edmMapping!=null ? edmMapping : "")+"</td>\n");
						htmlWriter.write("\t<td>"+prop.substring(prop.indexOf(',')+1)+"</td>\n");
					}
				}
				htmlWriter.write("</tr>\n");
			}
		}
		
		htmlWriter.write("</table>\n");

		htmlWriter.write("<p>Europeana IDs count:"+europeanaIdsCount+"<br/>\n");
		htmlWriter.write("Europeana CHOs count:"+wikidataItemsCount+"</p>\n");
		htmlWriter.write("</body></html>");
		
		csvBufferedReader.close();
		htmlWriter.close();
	}
	
}
