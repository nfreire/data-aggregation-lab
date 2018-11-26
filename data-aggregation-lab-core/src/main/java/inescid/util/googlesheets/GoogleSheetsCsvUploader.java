package inescid.util.googlesheets;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Permissions;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Get;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Clear;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Update;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.DimensionGroup;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.UpdateSpreadsheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import eu.europeana.research.iiif.profile.UsageCount;
import inescid.dataaggregation.dataset.Dataset;
import inescid.dataaggregation.dataset.GlobalCore;
import inescid.dataaggregation.dataset.Dataset.DatasetType;

public class GoogleSheetsCsvUploader {
	public static void update(String spreadsheetId, File csvFile) throws IOException {
		update(spreadsheetId, sheetTitleFromFileName(csvFile), csvFile);
	}
	
	public static void update(String spreadsheetId, String sheetTitle, File csvFile) throws IOException {
		Sheets service = SheetsApi.getSheetsService();

		Get get = service.spreadsheets().get(spreadsheetId);
		get.setFields("sheets.properties");
		
		boolean sheetExists=false;
		Spreadsheet result = get.execute();
		for(Sheet sheet : result.getSheets()) {
			SheetProperties sheetProps = sheet.getProperties();
			if(!sheetProps.getTitle().equals(sheetTitle))
				continue;
			sheetExists=true;
			String range=makeRangeExpression(sheetTitle, sheetProps.getGridProperties().getRowCount(), sheetProps.getGridProperties().getColumnCount());
			ClearValuesRequest requestBody = new ClearValuesRequest();
			Clear clear = service.spreadsheets().values().clear(spreadsheetId, range, requestBody);
			clear.execute();
//			System.out.println("Cleared: "+ executeClear.getClearedRange()/*row with blanks*/+" range");
		}
		if(!sheetExists) {
			addSheet(spreadsheetId, sheetTitle);
		}
		 
		Entry<ValueRange, Integer> createValues = createValues(csvFile);
		ValueRange vRange=createValues.getKey();
		int cols=createValues.getValue();
		int rows=vRange.getValues().size();
		
		Update append = service.spreadsheets().values().update(spreadsheetId, makeRangeExpression(sheetTitle, rows, cols) , vRange);
		append.setValueInputOption("RAW");
		append.execute();
	}
	
//	public static String create(String spreadsheetTitle, String sheetTitle, File csvFile) throws IOException {
//		Sheets sheetsService = SheetsApi.getSheetsService();
//        Spreadsheet requestBody = new Spreadsheet();
//        SpreadsheetProperties properties = new SpreadsheetProperties();
//        properties.setTitle(spreadsheetTitle);
//        requestBody.setProperties(properties);
//        Sheets.Spreadsheets.Create request = sheetsService.spreadsheets().create(requestBody);
//        Spreadsheet spreadsheet = request.execute();
//		
//        Sheet sheet = spreadsheet.getSheets().get(0);    
//        sheet.getProperties().setTitle(sheetTitle);
//		
//		Entry<ValueRange, Integer> createValues = createValues(csvFile);
//		ValueRange vRange=createValues.getKey();
//		int cols=createValues.getValue();
//		int rows=vRange.getValues().size();
//		Update append = sheetsService.spreadsheets().values().update(spreadsheet.getSpreadsheetId(), makeRangeExpression(sheetTitle, rows, cols) , vRange);
//		append.setValueInputOption("RAW");
//		UpdateValuesResponse execute = append.execute();
//		System.out.println("Updated: " + execute.getUpdatedRows()/* row with blanks */ + " rows");		
//		
//		return spreadsheet.getSpreadsheetId();
//	}
	public static String create(String spreadsheetTitle, String sheetTitle) throws IOException {
		Sheets sheetsService = SheetsApi.getSheetsService();
		Spreadsheet spreadsheet = null;
		{
			Spreadsheet requestBody = new Spreadsheet();
			SpreadsheetProperties properties = new SpreadsheetProperties();
			properties.setTitle(spreadsheetTitle);
			requestBody.setProperties(properties);
			Sheets.Spreadsheets.Create request = sheetsService.spreadsheets().create(requestBody);
			spreadsheet = request.execute();
		}
		{
			Drive driveService = SheetsApi.getDriveService();
	        Permission permission = new Permission();
	        permission.setType("anyone");
	        permission.setRole("commenter");
			Permissions.Create permissionsRequest=driveService.permissions().create(spreadsheet.getSpreadsheetId(), permission);
			permissionsRequest.execute();
		}
		{
			List<Request> requests = new ArrayList<>();
			// Change the spreadsheet's title.
			requests.add(new Request()
			        .setUpdateSheetProperties(new UpdateSheetPropertiesRequest()
			                .setProperties(new SheetProperties()
			                        .setTitle(sheetTitle))
			                .setFields("title")));
			
			BatchUpdateSpreadsheetRequest body =
			        new BatchUpdateSpreadsheetRequest().setRequests(requests);
			        sheetsService.spreadsheets().batchUpdate(spreadsheet.getSpreadsheetId(), body).execute();
		}
		return spreadsheet.getSpreadsheetId();
	}
	public static void addSheet(String spreadsheetId, String sheetTitle) throws IOException {
		Sheets sheetsService = SheetsApi.getSheetsService();
		List<Request> requests = new ArrayList<>();
		// Change the spreadsheet's title.
		AddSheetRequest addSheetRequest = new AddSheetRequest();
		addSheetRequest.setProperties(new SheetProperties());
		addSheetRequest.getProperties().setTitle(sheetTitle);
		requests.add(new Request()
		        .setAddSheet(addSheetRequest));
		
		BatchUpdateSpreadsheetRequest body =
		        new BatchUpdateSpreadsheetRequest().setRequests(requests);
		        sheetsService.spreadsheets().batchUpdate(spreadsheetId, body).execute();
	}

	private static Entry<ValueRange, Integer> createValues(File csvFile) throws IOException {
		int cols=0;
        List<List<Object>> vals=new ArrayList<List<Object>>();
        CSVParser parser=CSVParser.parse(FileUtils.readFileToString(csvFile, "UTF-8"), CSVFormat.DEFAULT);
		for(Iterator<CSVRecord> it = parser.iterator() ; it.hasNext() ; ) {
			CSVRecord rec = it.next();
			cols=Math.max(cols, rec.size());
			
			List<Object> recVals=new ArrayList<>();
			for(String v:rec) {
				recVals.add(v);
			}
			vals.add(recVals);
		}	
		parser.close();
		ValueRange vRange=new ValueRange();
		vRange.setValues(vals);
		return new DefaultMapEntry<ValueRange, Integer>(vRange,cols);
	}
	
	private static String makeRangeExpression(String sheetTitle, int rows, int cols) {
		int lastCol=(int)'A'+cols;
		if(lastCol > (int)'Z')
			lastCol='Z';
		String range=(sheetTitle!=null ? sheetTitle+"!" : "")+"A1:"+((char)lastCol)+rows;
		return range;
	}
	
	public static String sheetTitleFromFileName(File csvFile) {
		String sheetTitle=csvFile.getName().substring(0, csvFile.getName().lastIndexOf('.'));
		return sheetTitle;
	}

	public static String getDatasetAnalysisSpreadsheet(Dataset dataset, String sheetTitle) throws IOException {
		File profileFolder=GlobalCore.getPublicationRepository().getProfileFolder(dataset);
		File sheetsIdFile = new File(profileFolder, "google-sheet-id.txt");
		String spreadsheetId=null;
		if(sheetsIdFile.exists()) {
			 spreadsheetId=FileUtils.readFileToString(sheetsIdFile, GlobalCore.UTF8);
		} else {
			spreadsheetId=GoogleSheetsCsvUploader.create("Schema.org conversion analysis - "+dataset.getTitle(), sheetTitle);
			FileUtils.write(sheetsIdFile, spreadsheetId, GlobalCore.UTF8);
		}
		return spreadsheetId;
	}
}
