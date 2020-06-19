package inescid.dataaggregation.casestudies.wikidata.iiif;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import eu.europeana.ld.edm.ORE;
import inescid.dataaggregation.data.model.Rdf;
import inescid.dataaggregation.data.model.RdfReg;
import inescid.dataaggregation.data.model.DcTerms;
import inescid.dataaggregation.data.model.Edm;
import inescid.dataaggregation.data.model.Ore;
import inescid.dataaggregation.dataset.Global;
import inescid.dataaggregation.dataset.profile.ProfileOfValueDistribution.ValueDistribution;
import inescid.dataaggregation.dataset.profile.PropertyProfiler;

public class IiifResourcesEnrichmentReportViewer {
	File outputFolder;
	IiifResourcesEnrichmentReport rep;
	
	public IiifResourcesEnrichmentReportViewer(File outputFolder, IiifResourcesEnrichmentReport rep) {
		this.outputFolder = outputFolder;
		this.rep = rep;
	}
	
	public void run() throws IOException {
		File repFolder=new File(outputFolder, new SimpleDateFormat("yyyy-MM-dd").format(new Date())+"_IIIF-enrichment"); 
		if(!repFolder.exists())
			repFolder.mkdir();
		{
			StringBuilder sb=new StringBuilder();
			sb.append("<html>\n" + 
					"<body>\n" + 
					"	<center><h2>Wikidata for enrichment of Europeana Objects with IIIF WebResources:<br />report of early results</h2></center>\n" + 
					"	<p>This report lists Europeana Objects that have the possibility to be enriched with IIIF WebResources that are present in Wikidata entities." +
					" The list was prepared by querying the <a href=\"https://query.wikidata.org/\">Wikidata query service</a> and retrieving the Wikidata resources that contain values for two properties:" + 
					" <a href=\"https://www.wikidata.org/wiki/Property:P6108\">IIIF manifest</a></li>\n" + 
					" and <a href=\"https://www.wikidata.org/wiki/Property:P727\">Europeana ID</a>.</p>\n" + 
					"	<p><b>IIIF resources at data providers</b><br />A total of "+
					rep.enrichements.sizeTotal() +
					" IIIF Webresources may be added to "+
					rep.enrichements.size() +
					" Europeana collections. These resources can be found in the following links (per collection).<br />" + 
					"<i>For each collection we also report on the rights statements found at Europeana and in the providers' IIIF manifests (NB: currently the <a href=\"https://www.wikidata.org/wiki/Property:P6426\">Wikidata property to relate to RightsStatements.org</a> is not used yet).</i>:</p>\n"+
					"	<ul>\n");
				for(String c: rep.enrichements.keySet()) {
					sb.append("    <li><a href=\""+c+".html\">"+c+"</a> ("+
							rep.enrichements.get(c).size()+" CHOs)\n"); 
					sb.append("<br />(rights in Europeana: ");
					PropertyProfiler propertyProfiler = rep.edmUsageProfilers.get(c).getUsageStats().getClassStats(Ore.Aggregation.getURI()).getPropertiesProfiles().get(Edm.rights.getURI());
					if (propertyProfiler==null) {
						sb.append("data unavailable");
					} else {
						List<ValueDistribution> rightsValDistribution = propertyProfiler.getProfileOfValueDistribution().getDistribution();
	
						if (rightsValDistribution.size()==0) {
							sb.append("data unavailable");
						} else if (rightsValDistribution.size()==1) {
							sb.append(rightsValDistribution.get(0).value);
						} else {
							for(ValueDistribution vd: rightsValDistribution) {
								sb.append(String.format("%s - %.1f%% ; ", vd.value, vd.distribution*100));
							}
						}
					}
					sb.append(")");

					sb.append("<br />(rights in IIIF manifests: ");
					propertyProfiler = rep.iiifUsageProfilers.get(c).getUsageStats().getClassStats(RdfReg.IIIF_MANIFEST.getURI()).getPropertiesProfiles().get(DcTerms.rights.getURI());
					if (propertyProfiler==null) {
						sb.append("data unavailable");
					} else {
						List<ValueDistribution> rightsValDistribution = propertyProfiler.getProfileOfValueDistribution().getDistribution();
						if (rightsValDistribution.size()==0) {
							sb.append("data unavailable");
						} else if (rightsValDistribution.size()==1) {
							sb.append(rightsValDistribution.get(0).value);
						} else {
							for(ValueDistribution vd: rightsValDistribution) {
								sb.append(String.format("%s - %.1f%%; ", vd.value, vd.distribution*100));
							}
						}
					}
					sb.append(")</li>\n");
				}
				sb.append("</ul>\n");
				
				sb.append("<br />\n" + 
				"	<p><b>IIIF resources at Wikimedia</b><br />"
				+ "A total of "+ rep.enrichementsByWikimedia.sizeTotal() +
				" IIIF Webresources may be added to "+
				rep.enrichementsByWikimedia.size() +
				" Europeana collections. These resources can be found in the following links (per collection).<br />" + 
				"<i>For each collection we also report on the rights statements found at Europeana:</p>\n"+
				"	<ul>\n");
			for(String c: rep.enrichementsByWikimedia.keySet()) {
				sb.append("    <li><a href=\""+c+"_wikimedia.html\">"+c+"</a> ("+
						rep.enrichementsByWikimedia.get(c).size()+" CHOs)\n"); 
				sb.append("<br />(rights in Europeana: ");
				PropertyProfiler propertyProfiler = rep.edmUsageProfilers.get(c).getUsageStats().getClassStats(ORE.Aggregation.getURI()).getPropertiesProfiles().get(Edm.rights.getURI());
				if (propertyProfiler==null) {
					sb.append("data unavailable");
				} else {
					List<ValueDistribution> rightsValDistribution = propertyProfiler.getProfileOfValueDistribution().getDistribution();

					if (rightsValDistribution.size()==0) {
						sb.append("data unavailable");
					} else if (rightsValDistribution.size()==1) {
						sb.append(rightsValDistribution.get(0).value);
					} else {
						for(ValueDistribution vd: rightsValDistribution) {
							sb.append(String.format("%s - %.1f%% ; ", vd.value, vd.distribution*100));
						}
					}
				}
				sb.append(")");
				sb.append(")</li>\n");
			}
				sb.append("	</ul>\n"+
				"<br />\n" + 
				"	<p><b>IIIF resources already in Europeana</b><br />"+
						"	<p>Wikidata has additional entities linked to IIIF WebResources. Some of them are also linked to Europeana CHOs, which already contain the IIIF resources. These are the collections:</p>" +
						"	<ul>\n");
				for(String c: rep.existingInEuropeana.keySet()) {
					sb.append("    <li><a href=\""+c+"_in_europeana.html\">"+c+"</a> ("+
							rep.existingInEuropeana.get(c).size()+" CHOs)</li>\n"); 
				}
				sb.append("	</ul>\n"+
						"	<p>Additional Wikimedia IIIF WebResources are also available. Some of them are also linked to Europeana CHOs, which already contain IIIF resources. These are the collections:</p>" +
						"	<ul>\n");
				for(String c: rep.existingInEuropeanaFromWikimedia.keySet()) {
					sb.append("    <li><a href=\""+c+"_wikimedia_in_europeana.html\">"+c+"</a> ("+
							rep.existingInEuropeanaFromWikimedia.get(c).size()+" CHOs)</li>\n"); 
				}
				
				sb.append("	</ul>\n"+

				"<br />\n" + 
				"	<p><b>IIIF resources in unlinked objects</b><br />"+
						"	<p>The number of Wikidata entities linked to IIIF WebResources, but are not linked to Europeana CHOs, is "+rep.wikidataCollectionsWithIiifManifests.total()+". In this analysis, IIIF resources provided by Wikimedia are not considered.<br />They are present in the following Wikidata collections:</p>" +
						"	<ul>\n");
				for(String c: rep.wikidataCollectionsWithIiifManifests.keySet()) {
					sb.append("    <li><a href=\""+c+"\">"+c.substring(c.lastIndexOf('/')+1)+"</a> ("+
							rep.wikidataCollectionsWithIiifManifests.get(c)+" entities)</li>\n"); 
				}
						sb.append("	</ul>\n"+
						"</body>\n" + 
				"</html>");
				FileUtils.write(new File(repFolder, "index.html"), sb, Global.UTF8);
		}
		{
			for(String col: rep.enrichements.keySet()) {
				StringBuilder sb=new StringBuilder();
				sb.append("<html>\n"
						+ "<head><style>\n" + 
						"table {border: 1px solid black; border-collapse: collapse;}\n" + 
						"td   {padding: 3px;}\n" + 
						"</style></head>" + 
						"<body>\n" + 
						"	<h2>Collection "+ col +"</h2>\n" + 
						"	<h3>(Report: Wikidata for enrichment of Europeana Objects with IIIF WebResources)</h3>\n" +
						"   <p>This list of CHOs contains those tha have IIIF Webresources registered in Wikidata but not in Europeana.</p>\n" +
						"	<table border=\"1\">\r\n" + 
						"	  <tr><td>Europeana CHO</td><td>Wikidata entity</td><td>IIIF Manifest(s)</td></tr>"); 
				for(String europeanaUri: rep.enrichements.get(col).keySet()) {
					SimpleEntry<String, Collection<String>> map = rep.enrichements.get(col).get(europeanaUri);
					String europeanaId = europeanaUri.substring(europeanaUri.indexOf("/item/")+6);
					sb.append("    <tr><td><a href=\""+europeanaUri+"\">"+europeanaId+"</a></td><td>"+
					"<a href=\""+map.getKey()+"\">"+map.getKey().substring(map.getKey().lastIndexOf('/')+1)+"</a></td><td>"+StringUtils.join(map.getValue(), "<br />")+"</td></tr>");
				}
				sb.append("	</table>\n"+
						"</body>\n" + 
						"</html>");
				FileUtils.write(new File(repFolder, col+".html"), sb, Global.UTF8);
			}
			for(String col: rep.enrichementsByWikimedia.keySet()) {
				StringBuilder sb=new StringBuilder();
				sb.append("<html>\n"
						+ "<head><style>\n" + 
						"table {border: 1px solid black; border-collapse: collapse;}\n" + 
						"td   {padding: 3px;}\n" + 
						"</style></head>" + 
						"<body>\n" + 
						"	<h2>Collection "+ col +"</h2>\n" + 
						"	<h3>(Report: Wikidata for enrichment of Europeana Objects with IIIF WebResources)</h3>\n" +
						"   <p>This list of CHOs contains those that have IIIF Webresources that may be provided via Wikimedia's IIIF Image API and Europeana has no IIIF WebResource.</p>\n" +
						"	<table border=\"1\">\r\n" + 
						"	  <tr><td>Europeana CHO</td><td>Wikidata entity</td></tr>"); 
				for(String europeanaUri: rep.enrichementsByWikimedia.get(col).keySet()) {
					String map = rep.enrichementsByWikimedia.get(col).get(europeanaUri);
					String europeanaId = europeanaUri.substring(europeanaUri.indexOf("/item/")+6);
					sb.append("    <tr><td><a href=\""+europeanaUri+"\">"+europeanaId+"</a></td><td>"+
							"<a href=\""+map+"\">"+map.substring(map.lastIndexOf('/')+1)+"</a></td></tr>");
				}
				sb.append("	</table>\n"+
						"</body>\n" + 
						"</html>");
				FileUtils.write(new File(repFolder, col+"_wikimedia.html"), sb, Global.UTF8);
			}
			{
				for(String col: rep.existingInEuropeana.keySet()) {
					StringBuilder sb=new StringBuilder();
					sb.append("<html>\n"
							+ "<head><style>\n" + 
							"table {border: 1px solid black; border-collapse: collapse;}\n" + 
							"td   {padding: 3px;}\n" + 
							"</style></head>" + 
							"<body>\n" +  
							"	<h2>Collection "+ col +"</h2>\n" + 
							"	<h3>(Report: Wikidata for enrichment of Europeana Objects with IIIF WebResources)</h3>\n" +
							"   <p>This list of CHOs contains those tha have IIIF Webresources registered in both Wikidata and Europeana.</p>\n" +
							"	<table border=\"1\">\r\n" + 
							"	  <tr><td>Europeana CHO</td><td>Wikidata entity</td><td>IIIF Manifest(s)</td></tr>"); 
					for(String europeanaUri: rep.existingInEuropeana.get(col).keySet()) {
						SimpleEntry<String, Collection<String>> map = rep.existingInEuropeana.get(col).get(europeanaUri);
						String europeanaId = europeanaUri.substring(europeanaUri.indexOf("/item/")+6);
						sb.append("    <tr><td><a href=\""+europeanaUri+"\">"+europeanaId+"</a></td><td>"+
								"<a href=\""+map.getKey()+"\">"+map.getKey().substring(map.getKey().lastIndexOf('/')+1)+"</a></td><td>"+StringUtils.join(map.getValue(), "<br />")+"</td></tr>");
					}
					sb.append("	</table>\n"+
							"</body>\n" + 
							"</html>");
					FileUtils.write(new File(repFolder, col+"_in_europeana.html"), sb, Global.UTF8);
				}
			}
			{
				for(String col: rep.existingInEuropeanaFromWikimedia.keySet()) {
					StringBuilder sb=new StringBuilder();
					sb.append("<html>\n"
							+ "<head><style>\n" + 
							"table {border: 1px solid black; border-collapse: collapse;}\n" + 
							"td   {padding: 3px;}\n" + 
							"</style></head>" + 
							"<body>\n" +  
							"	<h2>Collection "+ col +"</h2>\n" + 
							"	<h3>(Report: Wikidata for enrichment of Europeana Objects with IIIF WebResources)</h3>\n" +
							"   <p>This list of CHOs contains those tha have IIIF Webresources provided by Wikimedia and Europeana already has a IIIF WebResource.</p>\n" +
							"	<table border=\"1\">\r\n" + 
							"	  <tr><td>Europeana CHO</td><td>Wikidata entity</td></tr>"); 
					for(String europeanaUri: rep.existingInEuropeanaFromWikimedia.get(col).keySet()) {
						String map = rep.existingInEuropeanaFromWikimedia.get(col).get(europeanaUri);
						String europeanaId = europeanaUri.substring(europeanaUri.indexOf("/item/")+6);
						sb.append("    <tr><td><a href=\""+europeanaUri+"\">"+europeanaId+"</a></td><td>"+
								"<a href=\""+map+"\">"+map.substring(map.lastIndexOf('/')+1)+"</a></td></tr>");
					}
					sb.append("	</table>\n"+
							"</body>\n" + 
							"</html>");
					FileUtils.write(new File(repFolder, col+"_wikimedia_in_europeana.html"), sb, Global.UTF8);
				}
			}
		}
	}
	

}
