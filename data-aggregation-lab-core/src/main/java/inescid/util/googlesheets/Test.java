package inescid.util.googlesheets;

import inescid.dataaggregation.dataset.GlobalCore;

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
