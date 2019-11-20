package inescid.util.googlesheets;

import inescid.dataaggregation.dataset.Global;

public class Test {

		public static void main(String[] args) {
			try {
				Global.init_developement();
				GoogleSheetsCsvUploader.create("NunoTest", "NunotestSheet");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(0);
			
		}
	
	
}
