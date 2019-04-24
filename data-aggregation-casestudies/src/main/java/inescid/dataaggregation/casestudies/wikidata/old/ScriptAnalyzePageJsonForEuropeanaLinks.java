package inescid.dataaggregation.casestudies.wikidata.old;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScriptAnalyzePageJsonForEuropeanaLinks {

	public static void main(String[] args) throws Exception {
		FileInputStream fileInStream=new FileInputStream(new File("src/data/wikidata/latest-all.json.europeana.txt.gz"));
		GZIPInputStream gzipInStream=new GZIPInputStream(fileInStream);
		InputStreamReader inStreamReader=new InputStreamReader(gzipInStream);
		BufferedReader bufReader=new BufferedReader(inStreamReader);
		
		int cnt=0;
		while(cnt<1 && bufReader.ready()) {
			cnt++;
			
			String jsonRec=bufReader.readLine();
			System.out.println(jsonRec);
			JsonParser p=new JsonFactory(new ObjectMapper()).createParser(jsonRec);
			TreeNode jsonTree = p.readValueAsTree();
			TreeNode claims = jsonTree.get("claims");
			for(Iterator<String> claimFieldNames = claims.fieldNames() ; claimFieldNames.hasNext() ;) {
				String claimFieldName =claimFieldNames.next();
				System.out.println(claimFieldName);
				TreeNode claimsProp = claims.get(claimFieldName);
				
				if(claimsProp.isArray()) {
					System.out.println(
							claimsProp.size());
					for(int i=0 ; i<claimsProp.size() ; i++) {
						TreeNode propVal = claimsProp.get(i);
						
						for(Iterator<String> propValFieldNames = propVal.fieldNames() ; propValFieldNames.hasNext() ;) {
							String propFieldName =propValFieldNames.next();
							System.out.println(" -  - " + propFieldName);
							
						}
					}
					
				}
				for(Iterator<String> propFieldNames = claimsProp.fieldNames() ; propFieldNames.hasNext() ;) {
					String propFieldName =propFieldNames.next();
					System.out.println(" - " + propFieldName);
					
					
					
				}
				
				
			}
			
			
			
		}
		
		bufReader.close();
		inStreamReader.close();
		gzipInStream.close();
		fileInStream.close();
	}
}
