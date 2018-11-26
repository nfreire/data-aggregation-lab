package inescid.util.googlesheets;

import java.net.URLDecoder;

import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.convert.rdfconverter.ConversionSpecificationAnalyzer;

public class Test {


		public static void main(String[] args) {
			try {
				GlobalCore.init_developement();
				GoogleSheetsCsvUploader.create("NunoTest", "NunotestSheet");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(0);
			
		}
	
	
}
