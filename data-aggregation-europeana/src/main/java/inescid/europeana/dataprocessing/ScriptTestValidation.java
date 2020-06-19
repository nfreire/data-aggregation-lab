package inescid.europeana.dataprocessing;

import java.util.Locale;

public class ScriptTestValidation {
	public static void main(String[] args) throws Exception {
		Locale loc;
		loc=Locale.forLanguageTag("en-US");
		loc=Locale.forLanguageTag("zz-xx-Hxxx-XX");
		System.out.println(loc.getLanguage());
		System.out.println(loc.getCountry());
		System.out.println(loc.getVariant());
		System.out.println(loc.getScript());
		System.out.println(loc.getExtension('x'));
		System.out.println(loc.getExtension('c'));
		
	} 
}
