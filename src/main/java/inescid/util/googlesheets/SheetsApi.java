package inescid.util.googlesheets;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;


public class SheetsApi {
//	Client ID
//	738984540801-o2kfpf2do2iqpipdm803trarq0bd52j9.apps.googleusercontent.com
//	Client secret
//	xzdtHC0Aw9aeXRcJwKRDrERE
	//
	
	private static final String APPLICATION_NAME = "data-aggregation-lab-googleapi";
   /** Directory to store user credentials for this application. */
   private static final java.io.File CREDENTIALS_FILE_PATH = new java.io.File(
       System.getProperty("user.home"), ".credentials/credentials-google-api.json");

   /** Global instance of the JSON factory. */
   private static final JsonFactory JSON_FACTORY =
       JacksonFactory.getDefaultInstance();
   private static NetHttpTransport HTTP_TRANSPORT;
   
   private static Credential getCredentials() throws IOException {
       // Load client secrets.
       InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
       GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

       // Build flow and trigger user authorization request.
       GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
    		   HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
               .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("google-api-tokens")))
               .setAccessType("offline")
               .build();
       return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
   }
   /** Global instance of the scopes required by this quickstart.
   *
   * If modifying these scopes, delete your previously saved credentials
   * at ~/.credentials/sheets.googleapis.com-java-quickstart
   */
  private static final List<String> SCOPES =
      Arrays.asList(SheetsScopes.SPREADSHEETS);


  static {
      try {
          HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//          DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
      } catch (Throwable t) {
          t.printStackTrace();
          System.exit(1);
      }
  }
  
  public static Sheets getSheetsService() throws IOException {
      Credential credential = getCredentials();
      return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
              .setApplicationName(APPLICATION_NAME)
              .build();
  }
   
  protected SheetsApi() {
   }
}
