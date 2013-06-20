package com.benemind.adict.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import android.content.Context;

public class DictHtmlBuilder {

	private static final String TAG = "DictHtmlBuilder";

	private static HtmlSection[] mHtmlSections = null;
	private static ArrayList<HtmlPlugIn> mHtmlPlugins = null;
	private static ArrayList<String> mCssFiles = new ArrayList<String>();

	public static StringBuilder buildWordText(String text) {
		return build(HtmlSectionType.WordTextOuter, text);
	}

	public static StringBuilder buildWordExplanation(String text) {
		return build(HtmlSectionType.WordExplanationOuter, text);
	}

	public static StringBuilder buildWordItem(String text) {
		return build(HtmlSectionType.WordItemOuter, text);
	}

	public static StringBuilder buildWordList(String text) {
		return build(HtmlSectionType.WordsOuter, text);
	}

	public static StringBuilder buildBookname(String text) {
		return build(HtmlSectionType.BooknameOuter, text);
	}

	public static StringBuilder buildDictionary(String text, Dictionary dict) {
		DictEng eng = DictEng.getInstance(null);
		String funcName = "publishDict" + eng.getDictId(dict);

		HtmlPlugIn plugin = findMatchPluagIn(dict);
		StringBuilder innerHtml = new StringBuilder();
		if( plugin != null ){
			String js = plugin.get(HtmlPlugIn.Field.JavaScript);
			if( js!=null && js.length()>0 ){
				innerHtml.append(text);
				js = js.replaceFirst("publishDict", funcName);
				StringBuilder jsCode = new StringBuilder();
				jsCode.append(js);
				jsCode.append(build(HtmlSectionType.PluginJs1, funcName));
				jsCode.append(build(HtmlSectionType.PluginJs2, funcName));
				innerHtml.append(build(HtmlSectionType.JsOuter, jsCode.toString()));
				text = innerHtml.toString();
			}
			String css = plugin.get(HtmlPlugIn.Field.CssFile);
			if( css!= null && css.length() > 0 ){
				if( !mCssFiles.contains(css) ){
					mCssFiles.add(css);
				}
			}
		}
		StringBuilder header = build(HtmlSectionType.DictionaryHeader, funcName);
		header.append(build(HtmlSectionType.DictionaryBody, text));
		return header;
	}

	private static HtmlPlugIn findMatchPluagIn(Dictionary dict) {
		// TODO Auto-generated method stub
		if( mHtmlPlugins != null ){
			Iterator<HtmlPlugIn> it = mHtmlPlugins.iterator();
			while(it.hasNext()){
				HtmlPlugIn plugin = it.next();
				if( dict.isMatch(plugin.get(HtmlPlugIn.Field.BookName),
						plugin.get(HtmlPlugIn.Field.WordCount),
						plugin.get(HtmlPlugIn.Field.IndexFileSize)) ){
					return plugin;
				}
			}
		}
		return null;
	}

	public static StringBuilder buildHeader() {
		// TODO Auto-generated method stub
		StringBuilder b = new StringBuilder();
		Iterator<String> it = mCssFiles.iterator();
		while(it.hasNext()){
			String css = it.next();
			b.append(build(HtmlSectionType.CssOuter,css));
		}
		return build(HtmlSectionType.HeaderOuter, b.toString());
	}

	public static StringBuilder buildBody(String text) {
		// TODO Auto-generated method stub
		return build(HtmlSectionType.BodyOuter, text);
	}
	
	public static StringBuilder buildHtml(String text) {
		mCssFiles.clear();
		return build(HtmlSectionType.HtmlOuter, text);
	}

	private static StringBuilder build(HtmlSectionType type, String text) {
		HtmlSection section = getSection(type);
		return section.buildHtmlWithContent(text);
	}

	private static HtmlSection getSection(HtmlSectionType type) {
		int index = getHtmlSectionTypeIndex(type);
		if (mHtmlSections[index] == null) {
			String html = "<div><!-- null section for " + type.name()
					+ " -->\n-ADict-</div>";
			try {
				mHtmlSections[index] = new HtmlSection(html);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mHtmlSections[index];
	}

	public static void loadHtmlSections(File Folder) {
		int count = getHtmlSectionTypeCount();
		if (mHtmlSections == null) {
			mHtmlSections = new HtmlSection[count];
		}
		for (int i = 0; i < count; i++) {
			mHtmlSections[i] = null;
		}
		try {
			Workbook workbook = null;
			InputStream in = null;
			try {
				File file = new File(Folder, "html.xls");    
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
			for (int row = 0; row < rowCount; row++) { // skip first two rows
				cell = sheet.getCell(0, row); // read the first cell of the row
				HtmlSectionType type = getHtmlSectionType(cell.getContents());
				if (type != null) {
					try {
						cell = sheet.getCell(1, row);
						String s = cell.getContents();
						HtmlSection section = new HtmlSection(s);
						int index = getHtmlSectionTypeIndex(type);
						mHtmlSections[index] = section;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private enum HtmlSectionType {
		HtmlOuter,
		HeaderOuter,
		CssOuter,
		BodyOuter,
		DictionaryHeader,
		DictionaryBody,
		BooknameOuter,
		WordsOuter,
		WordItemOuter,
		WordTextOuter,
		WordExplanationOuter,
		JsOuter,		
		PluginJs1,
		PluginJs2
	}

	static private int getHtmlSectionTypeIndex(HtmlSectionType type) {
		return type.ordinal();
	}

	static private HtmlSectionType getHtmlSectionType(String typeStr) {
		String s = typeStr.trim();
		HtmlSectionType types[] = HtmlSectionType.values();
		for (HtmlSectionType type : types) {
			if (s.equalsIgnoreCase(type.name())) {
				return type;
			}
		}
		return null;
	}

	static private int getHtmlSectionTypeCount() {
		HtmlSectionType types[] = HtmlSectionType.values();
		return types.length;
	}


	public static void loadHtmlPlugins(File Folder) {
		if (mHtmlPlugins == null) {
			mHtmlPlugins = new ArrayList<HtmlPlugIn>();
		}
		else{
			mHtmlPlugins.clear();
		}

		try {
			Workbook workbook = null;
			InputStream in = null;
			try {
				File file = new File(Folder, "plugins.xls");    
            	in = new FileInputStream(file);
				workbook = Workbook.getWorkbook(in);
				workbook.getNumberOfSheets();
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("file not found!");
			}
			
			Sheet sheet = workbook.getSheet(0);
			int rowCount = sheet.getRows();
			//get map
			int[] map = new int[HtmlPlugIn.getFieldCount()];
			for(int i=0; i<map.length; i++){
				map[i] = -1;
			}
			HtmlPlugIn.Field fields[] = HtmlPlugIn.Field.values();
			Cell[] cells = sheet.getRow(2);
			for(Cell c:cells){
				String text = c.getContents().trim();
				for(HtmlPlugIn.Field field:fields ){
					String name = field.name();
					if( name.equalsIgnoreCase(text) ){
						int index = HtmlPlugIn.getIndex(field); 
						map[index] = c.getColumn();
						break;
					}
				} 
			}
			for(int i=0; i<map.length; i++){
				if( map[i] == -1 ){
					throw new Exception("can NOT find col:"+HtmlPlugIn.getFiled(i).name() );
				}
			}
			for (int row = 3; row < rowCount; row++) {
				HtmlPlugIn plugin = new HtmlPlugIn();
				for(HtmlPlugIn.Field field:fields){
					int col = map[HtmlPlugIn.getIndex(field)];
					Cell c = sheet.getCell(col, row);
					plugin.set(field, c.getContents());
				}
				mHtmlPlugins.add(plugin);
			}
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class HtmlSection {
		String[] Pieces;

		HtmlSection(String fromExcel) throws Exception {
			Pieces = fromExcel.split("-ADict-");
			if (Pieces.length != 2) {
				throw new Exception("wrong text from excel:" + fromExcel);
			}
		}

		StringBuilder buildHtmlWithContent(String content) {
			StringBuilder b = new StringBuilder();
			b.append(Pieces[0]);
			b.append(content);
			b.append(Pieces[1]);
			return b;
		}
	}

	static class HtmlPlugIn{
		enum Field{
			BookName, WordCount, IndexFileSize, JavaScript, CssFile
		}

		private String[] Fields;

		HtmlPlugIn(){
			Field items[] = Field.values();
			Fields = new String[items.length];
		}
		
		String get(int columnIndex){
			if( columnIndex < Fields.length ){
				return Fields[columnIndex];
			}
			return null;
		}
		String get(Field columnName){
			int index = columnName.ordinal();
			return Fields[index];
		}
		void set(int index, String val){
			if( index < Fields.length ){
				Fields[index] = val.trim();
			}
		}
		void set(Field name, String val){
			int index = name.ordinal();
			Fields[index] = val.trim();
		}
		
		static int getIndex(Field name){
			return name.ordinal();
		}
		static int getFieldCount(){
			Field items[] = Field.values();
			return items.length;
		}
		static Field getFiled(int index){
			Field items[] = Field.values();
			for(Field item:items){
				if( item.ordinal() == index){
					return item;
				}
			}
			return null;
		}
	}



}
