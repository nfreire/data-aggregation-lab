package inescid.dataaggregation.metadatatester.view;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import eu.europeana.research.iiif.profile.UsageCount;
import eu.europeana.research.iiif.profile.model.Manifest;
import eu.europeana.research.iiif.profile.model.SeeAlso;
import inescid.dataaggregation.crawl.http.HttpRequest;
import inescid.util.AccessException;
import inescid.util.RdfUtil;
import inescid.util.RdfUtil.Jena;

public class IiifManifestTester {

	public static List<IiifSeeAlsoView> testManifest(String manifestUri) throws InterruptedException, IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
	
		HttpRequest httpReq=new HttpRequest(manifestUri);
		httpReq.fetch();
		String fileData = httpReq.getResponseContentAsString();
//		try {
			Manifest m = gson.fromJson(fileData, Manifest.class);
			if(m==null)
				return new ArrayList<IiifSeeAlsoView>();
			return profileManifest(m);
//		} catch (JsonSyntaxException e) {
//			"Error while parsing "+manifestUri;
//			e.printStackTrace();
//		}
	}

	private static List<IiifSeeAlsoView> profileManifest(Manifest manifest) {
		List<IiifSeeAlsoView> seeAlsos=new ArrayList<IiifSeeAlsoView>();
		if (manifest.seeAlso != null)
			for (SeeAlso md : manifest.seeAlso) {
				if (md.format != null) {
					IiifSeeAlsoView sav=new IiifSeeAlsoView();
					sav.format=md.format;
					sav.profile=md.profile;
					sav.uri=md.id;
					seeAlsos.add(sav);
					
					Model model=Jena.createModel();
					Resource readRdfResourceFromUri;
					try {
						readRdfResourceFromUri = RdfUtil.readRdfResourceFromUri(sav.uri);
					} catch (Exception e) {
//					} catch (AccessException | InterruptedException | IOException e) {
						//not RDF data. ignore it
						continue;
					}
					model.add(readRdfResourceFromUri.listProperties());
					
					sav.readData(model);
					seeAlsos.add(sav);
				}
			}
		return seeAlsos;
	}
}
