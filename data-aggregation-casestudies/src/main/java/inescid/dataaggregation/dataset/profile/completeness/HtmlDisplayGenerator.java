package inescid.dataaggregation.dataset.profile.completeness;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HtmlDisplayGenerator {

	public static void generateChartsDisplay(File outputFolder, File[] collectionsUriFiles, int processedCollections, int sampledRecords) throws IOException{
		StringBuilder sb=new StringBuilder();
		sb.append("<html>\r\n" + 
				"<body>\r\n" + 
				"	<h2>Tiers/DQC Completeness Measure: results from samples of Europeana records</h2>\r\n" + 
				"\r\n" + 
				"	<p>The charts display the results of the calculation of the Tiers/DQC Completeness Measure and a comparison with the values of the current completeness measure of Europeana on the same records.</p>\r\n" + 
				"	<p>The test was performed on "+
				processedCollections+
				" collections, using at most "+
				sampledRecords+				
				" records from each collection</p>\r\n" + 
				"	<table>");

				for(File colUris : collectionsUriFiles) {
					String col=colUris.getName().substring(0, colUris.getName().indexOf('.'));
					sb.append(
					"     <tr>\r\n" + 
					"     <tr><td><b>Dataset "+
					col+
					"     </td><td></td></tr>"+
					"		<td ><img src=\""+
					col+
					".png\" /></td>\r\n" + 
					"		<td ><img src=\""+ 
					col+ 
					"_old_completeness.png\"/></td>\r\n" + 
					"	  </tr>");
				}
				
				sb.append(
				"  </table>\r\n" + 
				"</body>\r\n" + 
				"</html>");
				
				FileWriter w=new FileWriter(new File(outputFolder, "index.html"));
				w.write(sb.toString());
				w.close();
	}

}
