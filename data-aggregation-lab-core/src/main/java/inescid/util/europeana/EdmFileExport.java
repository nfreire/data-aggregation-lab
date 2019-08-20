package inescid.util.europeana;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import inescid.util.XmlUtil;

public class EdmFileExport {

	static final Charset UTF8=Charset.forName("UTF8");	
	static final Pattern STRIP_XML_TOP_ELEMENT=Pattern.compile("^.*<rdf:RDF[^>]+>", Pattern.DOTALL);

	
		File exportFolder;
		int maxExportRecordsPerFile=10;
		int exportRecordsFileCnt=0;
		int exportRecordsCnt=0;
		boolean transformToEdmInternal=false;

		FileOutputStream fileOutputStream = null;
		int fileCnt=0;

		public EdmFileExport(int maxExportRecordsPerFile, File exportFolder) {
			this.maxExportRecordsPerFile=maxExportRecordsPerFile;
			this.exportFolder = exportFolder;
		}		
		
		public void init() {
			
		}
		public void close() throws IOException {
			if(fileOutputStream!=null) {
				IOUtils.write("</rdf:RDF>".getBytes(UTF8), fileOutputStream);
				fileOutputStream.flush();
				fileOutputStream.close();
				fileOutputStream=null;
			}

		}
		
		public void export(String contentBytes) throws IOException {
//			System.out.println(contentBytes);
//			System.out.println(new String(contentBytes, UTF8));
			
			String contentStr=null;
			if(transformToEdmInternal) {
				Document dom = XmlUtil.parseDom(new StringReader(contentBytes));
				InputStream xsltStream = this.getClass().getClassLoader().getResourceAsStream("EDM_external2internal_v2_udc.xsl");
				Document xsltDom = XmlUtil.parseDom(xsltStream);
				xsltStream.close();
				Document edmInternalDom = XmlUtil.transform(dom, xsltDom);
				contentStr=XmlUtil.writeDomToString(edmInternalDom);
			}else
				contentStr=contentBytes;
			contentStr=contentStr.substring(0, contentStr.lastIndexOf('<'));

			Matcher matcher = STRIP_XML_TOP_ELEMENT.matcher(contentStr);
			if(!matcher.find()) 
				throw new RuntimeException("could not clean xml");
			String rdfDecl=matcher.group();
			contentStr=matcher.replaceFirst("");
			
			if(fileOutputStream==null) {
				fileCnt++;
				exportRecordsFileCnt=0;
				File exportFile=new File(exportFolder, String.format("%02d-%s.xml", fileCnt, transformToEdmInternal ? "edm_internal" : "edm"));
				fileOutputStream=new FileOutputStream(exportFile);
				IOUtils.write(rdfDecl.getBytes(UTF8), fileOutputStream);
			}
			IOUtils.write("<ore:aggregates>\n".getBytes(UTF8), fileOutputStream);
			IOUtils.write(contentStr.getBytes(UTF8), fileOutputStream);
			IOUtils.write("</ore:aggregates>\n".getBytes(UTF8), fileOutputStream);
//			IOUtils.write("\n".getBytes(UTF8), fileOutputStream);
			
			exportRecordsCnt++;		
			exportRecordsFileCnt++;		

			if(exportRecordsFileCnt>=maxExportRecordsPerFile) {
				IOUtils.write("</rdf:RDF>".getBytes(UTF8), fileOutputStream);
				fileOutputStream.flush();
				fileOutputStream.close();
				fileOutputStream=null;
			}
		}
		
		public int getExportRecordsCount() {
			return exportRecordsCnt;
		}

		public void setTransformToEdmInternal(boolean transformToEdmInternal) {
			this.transformToEdmInternal = transformToEdmInternal;
		}
	}