package inescid.dataaggregation.casestudies.coreference.semanticweb;

import java.util.regex.Matcher;

import inescid.dataaggregation.casestudies.coreference.Consts;

public class Util {

	public static String getHost(String uri) {
		Matcher matcher = Consts.HOST_PATTERN.matcher(uri);
		if(matcher.find())
			return matcher.group(1);
		return null;
	}

}
