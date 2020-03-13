package inescid.util.googlesheets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class TestGoogleAnalyticsApi {

		public static void main(String[] args) {
//			try {
//				Global.init_developement();
//				GoogleSheetsCsvUploader.create("NunoTest", "NunotestSheet");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			String VIEW_ID="93301281";
//			String VIEW_ID="ga:93301281";
			try {
//				Global.init_developement();
				GoogleApi.init("C:\\Users\\nfrei\\.credentials\\europeana-analytics_google.json");
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
			    do {
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
				    
				    printResponse(response);
					System.out.println(response.isEmpty());
					
			    } while (!StringUtils.isEmpty(nextPageToken));				
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(0);
			
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
