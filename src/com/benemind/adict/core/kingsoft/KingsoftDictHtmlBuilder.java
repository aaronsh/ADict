package com.benemind.adict.core.kingsoft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.benemind.voa.util.StringUtils;
import com.benemind.adict.core.kingsoft.IcibaConvertParam.ExcelColumnIndex;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

public class KingsoftDictHtmlBuilder {
	private static final String TAG = "KingsoftDictHtmlBuilder";
	private static ArrayList<IcibaConvertParam> ConvertParamList = null; 

	public static String buildDictSearchContent(String xml) {
		// TODO Auto-generated method stub
		StringBuilder b = new StringBuilder();

		StringReader reader = new StringReader(xml);
		XmlPullParser parser = Xml.newPullParser();
		
		int eventType;
		try {
			parser.setInput( reader );
			eventType = parser.getEventType();
		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		} 
		
		while (eventType != XmlPullParser.END_DOCUMENT) {
			String tag = null;
				
			if( eventType == XmlPullParser.START_TAG ){
				tag = parser.getName();
				b = convertXmlSection(parser, tag);

			}
			
			try{
				eventType = parser.nextTag();
			}
			catch (XmlPullParserException e) {
				e.printStackTrace();
				break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
		return b.toString();
	}

	static private StringBuilder convertXmlSection(XmlPullParser parser, String endTag){
		StringBuilder b = new StringBuilder();
		int eventType;
		try {
			eventType = parser.getEventType();
		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return b;
		} 
		
		boolean enclosed = true;
		boolean exitLoop = false;
		while (eventType != XmlPullParser.END_DOCUMENT) {
			String tag = null;
			switch(eventType){
				case XmlPullParser.START_TAG:
					tag = parser.getName();
					if( endTag.equals(tag) ){
						b.append(buildHtmlTagBegin(tag));
						enclosed = false;
						break;
					}
					
					StringBuilder div = convertXmlSection(parser, tag);
					b.append(correctIndent(div));
					break;
				case XmlPullParser.END_TAG:
					tag = parser.getName();
					if( endTag.equals(tag) ){
						b.append(buildHtmlTagEnd(tag));
						enclosed = true;
						exitLoop = true;
					}
					break;
				case XmlPullParser.TEXT:
					String text = parser.getText();
					if( text != null ){
						text = StringUtils.htmEncode(text.trim());
						b.append(text);
					}
					break;
			}	

			if( exitLoop ){
				break;
			}
			try {
				eventType = parser.next();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
		if( !enclosed ){
			b.append("</div>\n");
		}
		return b;
	}

	private static StringBuilder buildHtmlTagBegin(String tag) {
		// TODO Auto-generated method stub
		StringBuilder b = new StringBuilder();
		IcibaConvertParam param = getConvertParam(tag);
		if( param == null ){
			b.append("<div>");
			b.append(tag);
			b.append("\n\t");
			return b;
		}
		String s = param.get(IcibaConvertParam.ExcelColumnIndex.ParentDivBeginTag);
		b.append(s);
		s = param.get(IcibaConvertParam.ExcelColumnIndex.BeginDiv).trim();
		if( s.length() == 0 ){
			b.append("<div");
			s = param.get(IcibaConvertParam.ExcelColumnIndex.ClassName).trim();
			if( s.length() > 0 ){
				b.append(" class=\"");
				b.append(s);
				b.append('"');
			}
			s = param.get(IcibaConvertParam.ExcelColumnIndex.OnClick).trim();
			if( s.length() > 0 ){
				b.append(" onclick= \"");
				b.append(s);
				b.append('"');
			}
			b.append('>');
		}

		s = param.get(IcibaConvertParam.ExcelColumnIndex.BeginText);
		b.append(s);

		s = param.get(IcibaConvertParam.ExcelColumnIndex.ShowTag).trim();
		if( "yes".equalsIgnoreCase(s) ){
			b.append(StringUtils.htmEncode(tag));
		}
		 
		s = param.get(IcibaConvertParam.ExcelColumnIndex.FollowText);
		b.append(s);
		
		return b;
	}

	private static StringBuilder buildHtmlTagEnd(String tag) {
		// TODO Auto-generated method stub
		StringBuilder b = new StringBuilder();
		IcibaConvertParam param = getConvertParam(tag);
		if( param == null ){
			b.append("\n</div>\n");
			return b;
		}

		String s = param.get(IcibaConvertParam.ExcelColumnIndex.EndText);
		b.append(s);
		b.append('\n');

		s = param.get(IcibaConvertParam.ExcelColumnIndex.EndDiv).trim();
		if( s.length() == 0 ){
			b.append("</div>\n");
		}

		s = param.get(IcibaConvertParam.ExcelColumnIndex.ParentDivEndTag);
		b.append(s);
		return b;
	}
	
	private static IcibaConvertParam getConvertParam(String tag){
		if( ConvertParamList == null ){
			return null;
		}
		Iterator<IcibaConvertParam> it = ConvertParamList.iterator();
		while(it.hasNext()){
			IcibaConvertParam param = it.next();
			String predefined = param.get(IcibaConvertParam.ExcelColumnIndex.Tag);
			if( tag.equalsIgnoreCase(predefined) ){
				return param;
			}
		}
		return null;
	}

	static private String correctIndent(StringBuilder builder){
		String s = builder.toString();
		return s.replaceAll("\\n", "\n\t");
	}
	
	public static void loadConvertParam(File Folder){
		ArrayList<IcibaConvertParam> list = new ArrayList<IcibaConvertParam>(); 
		try {  
            Workbook workbook = null;  
            InputStream in = null;  
            try {
            	File file = new File(Folder, "iciba.xls");    
            	in = new FileInputStream(file);    
                workbook = Workbook.getWorkbook(in); 
                workbook.getNumberOfSheets();
            } catch (Exception e) {  
                e.printStackTrace();  
                throw new Exception("file not found!");  
            }  
            Sheet sheet = workbook.getSheet(0);  
            Cell cell = null;  
            int rowCount = sheet.getRows();
            IcibaConvertParam.ExcelColumnIndex[] columns = IcibaConvertParam.ExcelColumnIndex.values();
            for(int row=2; row<rowCount; row++){ //skip first two rows
            	IcibaConvertParam paramItem = new IcibaConvertParam();
            	for(IcibaConvertParam.ExcelColumnIndex col:columns){
            		cell = sheet.getCell(paramItem.getIndex(col), row);
            		String s = cell.getContents();
            		if( col == IcibaConvertParam.ExcelColumnIndex.Tag ){
            			s = s.trim();
            		}
            		paramItem.set(col, s);
            	}
            	list.add(paramItem);
            }
            workbook.close();
            
            ConvertParamList = list;
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
	}
}

