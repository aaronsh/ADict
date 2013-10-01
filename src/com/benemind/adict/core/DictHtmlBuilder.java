package com.benemind.adict.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;


import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import android.content.Context;
import au.com.bytecode.opencsv.CSVReader;

public class DictHtmlBuilder {

	private static final String TAG = "DictHtmlBuilder";

	private static HtmlSection[] mHtmlSections = null;
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

	public static StringBuilder buildDictionary(String text, int dictId, String jsText) {
		String funcName = "publishDict" + dictId;

		StringBuilder innerHtml = new StringBuilder();
		if( jsText != null  && jsText.length()>0 && jsText.contains("publishDict") ){
			innerHtml.append(text);
			jsText = jsText.replaceFirst("publishDict", funcName);
			StringBuilder jsCode = new StringBuilder();
			jsCode.append(jsText);
			jsCode.append("\n");
			jsCode.append(build(HtmlSectionType.PluginJs1, funcName));
			jsCode.append("\n");
			jsCode.append(build(HtmlSectionType.PluginJs2, funcName));
			innerHtml.append(build(HtmlSectionType.JsOuter, jsCode.toString()));
			text = innerHtml.toString();
		}
		StringBuilder header = build(HtmlSectionType.DictionaryHeader, funcName);
		header.append(build(HtmlSectionType.DictionaryBody, text));
		return header;
	}


	public static StringBuilder buildHeader(ArrayList<String> cssLinks) {
		// TODO Auto-generated method stub
		StringBuilder b = new StringBuilder();
		Iterator<String> it = cssLinks.iterator();
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
			File file = new File(Folder, "html.csv");
			CSVReader reader = new CSVReader(new FileReader(file));
			String [] nextLine;
			String name, code;
			boolean foundTitle = false;
			while ((nextLine = reader.readNext()) != null) {
				name = null;
				code = null;
				if( nextLine.length >= 2 ){
					name = nextLine[0].trim();
					code = nextLine[1].trim();
				}
				if( foundTitle ){
					if( name != null && name.length() > 0 && code != null  ){
						HtmlSectionType type = getHtmlSectionType(name);
						if (type != null) {
							try {
								HtmlSection section = new HtmlSection(code);
								int index = getHtmlSectionTypeIndex(type);
								mHtmlSections[index] = section;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				else{
					if( name != null && name.equals("Name") && code != null && code.equals("Code") ){
						foundTitle = true;
					}
				}
			}
			reader.close();
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
}
