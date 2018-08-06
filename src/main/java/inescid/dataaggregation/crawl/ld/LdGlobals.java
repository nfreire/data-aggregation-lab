package inescid.dataaggregation.crawl.ld;

import java.io.File;
import java.nio.charset.Charset;

public class LdGlobals {
	public static final Charset charset=Charset.forName("UTF-8");
	public static TaskSyncManager taskSyncManager=new TaskSyncManager();
}
