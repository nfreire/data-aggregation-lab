package inescid.europeana.dataprocessing.googleanalytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.ColumnHeader;
import com.google.api.services.analyticsreporting.v4.model.DateRange;
import com.google.api.services.analyticsreporting.v4.model.DateRangeValues;
import com.google.api.services.analyticsreporting.v4.model.Dimension;
import com.google.api.services.analyticsreporting.v4.model.DimensionFilter;
import com.google.api.services.analyticsreporting.v4.model.DimensionFilterClause;
import com.google.api.services.analyticsreporting.v4.model.GetReportsRequest;
import com.google.api.services.analyticsreporting.v4.model.GetReportsResponse;
import com.google.api.services.analyticsreporting.v4.model.Metric;
import com.google.api.services.analyticsreporting.v4.model.MetricHeaderEntry;
import com.google.api.services.analyticsreporting.v4.model.OrderBy;
import com.google.api.services.analyticsreporting.v4.model.Report;
import com.google.api.services.analyticsreporting.v4.model.ReportRequest;
import com.google.api.services.analyticsreporting.v4.model.ReportRow;
import com.google.api.services.sheets.v4.Sheets;

import inescid.dataaggregation.dataset.Global;
import inescid.util.datastruct.MapOfInts;
import inescid.util.googlesheets.GoogleApi;

public class GoogleAnalyticsRecordViewsApi {
	private static final Pattern uriToIdPattern=Pattern.compile("/record/(.*/.*)\\.html");
	private static final Pattern uriToIdPatternNoExtension=Pattern.compile("/record/([^?/]*/[^?/]*)");

		public static void main(String[] args) {
			MapOfInts<String> combinedUriCounts=new MapOfInts<String>();

			String VIEW_ID="93301281";
			try {
//				GoogleApi.init("C:\\Users\\nfrei\\.credentials\\europeana-analytics_google.json");
				GoogleApi.init("C:\\Users\\nfrei\\.credentials\\research_and_developement.json");
				AnalyticsReporting analyticsService = GoogleApi.getAnalyticsReportingService();
				
			    // Create the DateRange object.
			    DateRange dateRange = new DateRange();
			    dateRange.setStartDate("2019-01-01");
			    dateRange.setEndDate("2019-12-31");

			    // Create the Metrics object.
			    Metric sessions = new Metric()
			        .setExpression("ga:uniquePageviews")
			        .setAlias("uniquePageviews");

			    //Create the Dimensions object.
			    Dimension browser = new Dimension()
			        .setName("ga:pagePath");
			    
			    String nextPageToken=null;
			    
			    for (boolean getMoreResults=true; getMoreResults ;) {
				    // Create the ReportRequest object.
				    ReportRequest request = new ReportRequest()
				        .setViewId(VIEW_ID)
				        .setDateRanges(Arrays.asList(dateRange))
				        .setDimensions(Arrays.asList(browser))
//				        .setFiltersExpression("ga:pagePath=@record")
				        .setFiltersExpression("ga:pagePath=~^/portal/[^/]+/record/")
				        .setPageSize(10000)
				        .setOrderBys(Arrays.asList(new OrderBy().setFieldName("ga:uniquePageviews").setSortOrder("DESCENDING")))
				        .setMetrics(Arrays.asList(sessions));

				    if(nextPageToken!=null)
				    	request.setPageToken(nextPageToken);
				    
				    ArrayList<ReportRequest> requests = new ArrayList<ReportRequest>();
				    requests.add(request);
	
				    // Create the GetReportsRequest object.
				    GetReportsRequest getReport = new GetReportsRequest()
				        .setReportRequests(requests);
	
				    // Call the batchGet method.
				    GetReportsResponse response = analyticsService.reports().batchGet(getReport).execute();
				    nextPageToken=response.getReports().get(0).getNextPageToken();
				    
//				    printResponse(response);
					getMoreResults=processResponse(response, combinedUriCounts);					
			    } 				
			    
				BufferedWriter writer = Files.newBufferedWriter(new File("src/data/europeana_record_uviews_2019.csv").toPath(), StandardCharsets.UTF_8);
				CSVPrinter out=new CSVPrinter(writer, CSVFormat.DEFAULT);
				for(String recId: combinedUriCounts.keySet()) {
					out.printRecord(recId, combinedUriCounts.get(recId));
				}
				out.close();
				writer.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(0);
			
		}
	
		
//		private static void combineCsvExports(File firstCsvFile) throws IOException {
//			
//			
//				BufferedReader reader = Files.newBufferedReader(nextCsv.toPath(), StandardCharsets.UTF_8);
//				CSVParser parser=new CSVParser(reader, CSVFormat.DEFAULT);
//				for(CSVRecord r: parser) {
//					String url=r.get(0);
//					Matcher matcher=uriToIdPattern.matcher(url);
//					if(!matcher.find()) {
//						matcher=uriToIdPatternNoExtension.matcher(url);
//						if(!matcher.find()) {
//							System.out.println("WARNING: record id not matching - "+ url);
//							continue;
//						}
//					}
//					String recId=matcher.group(1);
//					int views=Integer.parseInt(r.get(2).replaceAll(",", ""));
//					combinedUriCounts.addTo(recId, views);
//				}
//				parser.close();
//				reader.close();
//				fileNum++;
//				nextCsv=new File(firstCsvFile.getParentFile(), baseFilename+" ("+fileNum+").csv");
//			
//			
//		}
		
		
		private static boolean processResponse(GetReportsResponse response, MapOfInts<String> combinedUriCounts) {
			int views=0;
		    for (Report report: response.getReports()) {
//		      ColumnHeader header = report.getColumnHeader();
//		      List<String> dimensionHeaders = header.getDimensions();
//		      List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
		      List<ReportRow> rows = report.getData().getRows();

		      if (rows == null) {
		         System.out.println("No data found");
		         return false;
		      }

		      for (ReportRow row: rows) {
//		        List<String> dimensions = row.getDimensions();
//		        List<DateRangeValues> metrics = row.getMetrics();
//
//		        for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
//		          System.out.println(dimensionHeaders.get(i) + ": " + dimensions.get(i));
//		        }
//
//		        for (int j = 0; j < metrics.size(); j++) {
////		          System.out.print("Date Range (" + j + "): ");
//		          DateRangeValues values = metrics.get(j);
//		          for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
//		            System.out.println(metricHeaders.get(k).getName() + ": " + values.getValues().get(k));
//		          }
//		        }
		        String url=row.getDimensions().get(0);
				Matcher matcher=uriToIdPattern.matcher(url);
				if(!matcher.find()) {
					matcher=uriToIdPatternNoExtension.matcher(url);
					if(!matcher.find()) {
						System.out.println("WARNING: record id not matching - "+ url);
						continue;
					}
				}
				String recId=matcher.group(1);
				views=Integer.parseInt(row.getMetrics().get(0).getValues().get(0));
//				System.out.println(views+" "+recId);
				combinedUriCounts.addTo(recId, views);
		      }
		      return views>=3 && !StringUtils.isEmpty(report.getNextPageToken()); 
		    }
		    return false;
		  }
		private static void printResponse(GetReportsResponse response) {
			for (Report report: response.getReports()) {
				ColumnHeader header = report.getColumnHeader();
				List<String> dimensionHeaders = header.getDimensions();
				List<MetricHeaderEntry> metricHeaders = header.getMetricHeader().getMetricHeaderEntries();
				List<ReportRow> rows = report.getData().getRows();
				
				if (rows == null) {
					System.out.println("No data found");
					return;
				}
				
				for (ReportRow row: rows) {
					List<String> dimensions = row.getDimensions();
					List<DateRangeValues> metrics = row.getMetrics();
					
					for (int i = 0; i < dimensionHeaders.size() && i < dimensions.size(); i++) {
						System.out.println(dimensionHeaders.get(i) + ": " + dimensions.get(i));
					}
					
					for (int j = 0; j < metrics.size(); j++) {
						System.out.print("Date Range (" + j + "): ");
						DateRangeValues values = metrics.get(j);
						for (int k = 0; k < values.getValues().size() && k < metricHeaders.size(); k++) {
							System.out.println(metricHeaders.get(k).getName() + ": " + values.getValues().get(k));
						}
					}
				}
			}
		}

}
