package inescid.dataaggregation.dataset;

import inescid.dataaggregation.data.ContentTypes;

public enum DatasetProfile {
	EDM, SCHEMA_ORG, ANY_TRIPLES, IIIF_PRESENTATION;

	public static DatasetProfile fromNamespace(String context) {
		if(context.equals("https://www.europeana.eu/schemas/context/edm.jsonld") ||
				context.equals("https://www.europeana.eu/schemas/edm/")
				) {
			return DatasetProfile.EDM;
		} else if(context.equals("http://schema.org") ||
				context.equals("http://schema.org/") ||
				context.equals("https://schema.org") ||
				context.equals("https://schema.org/") ||
				context.equals("http://schema.org/docs/jsonldcontext.json") ||
			context.equals("https://schema.org/docs/jsonldcontext.json")) {
			return DatasetProfile.SCHEMA_ORG;
		}
		return null;
	}

	public static DatasetProfile fromContentType(String format) {
		ContentTypes fromMime = ContentTypes.fromMime(format);
		if(fromMime!=null && fromMime==ContentTypes.JSON_LD)
			return DatasetProfile.ANY_TRIPLES;
		return null;
	}
	
	public String getDisplay() {
		switch (this) {
		case ANY_TRIPLES:
			return "RDF" ;
		case EDM:
			return "Europeana Data Model (EDM)";
		case IIIF_PRESENTATION:
			return "IIIF Presentation";
		case SCHEMA_ORG:
			return "Schema.org";
		default:
			throw new RuntimeException("Not implemented");
		}
	}

	public static DatasetProfile fromString(String dataFormatAsEnumValueOrNamespace) {
		DatasetProfile ret=null;
		if(dataFormatAsEnumValueOrNamespace!=null) {
			try {
				ret=valueOf(dataFormatAsEnumValueOrNamespace);
			} catch (Exception e) {
				//can't parse. try as namespace
				ret=fromNamespace(dataFormatAsEnumValueOrNamespace);
			}
		}
		return ret;
	}
}
