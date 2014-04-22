package com.benemind.adict.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;


import android.content.Context;
import au.com.bytecode.opencsv.CSVReader;



public class DefaultPlugIn {
	enum Field{
		BookName, WordCount, IndexFileSize, JavaScript, CssFile, Comment
	}

	private String[] Fields;

	DefaultPlugIn(){
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
		Fields[index] = val==null ? null:val.trim();
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
	
	private static ArrayList<DefaultPlugIn> mPlugins= null;
	public static void loadPlugins(Context cntx) {
		if (mPlugins == null) {
			mPlugins = new ArrayList<DefaultPlugIn>();
		}
		else{
			mPlugins.clear();
		}

		try {
			CSVReader reader;
			File pluginsDir = null;
			try {
				
				InputStream in = cntx.getResources().getAssets().open("plugins.csv"); 
				InputStreamReader inReader = new InputStreamReader(in, "GB2312");
				BufferedReader br = new BufferedReader(inReader);
				reader = new CSVReader(br);
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("file not found!");
			}
			
			String [] nextLine = null;
			String[] Titles;
			String bookname = DefaultPlugIn.Field.BookName.name();
			boolean foundTitle = false;
			while (!foundTitle &&(nextLine = reader.readNext()) != null) {
				for(String s:nextLine){
					s =s.trim();
					if( s.equals(bookname) ){
						foundTitle = true;
						break;
					}
				}
				//System.out.println("Name: [" + nextLine[0] + "]\nAddress: [" + nextLine[1] + "]\nEmail: [" + nextLine[2] + "]");
			}
			if( foundTitle ){
				Titles = new String[nextLine.length];
				for(int i=0; i<nextLine.length; i++ ){
					Titles[i] = nextLine[i].trim();
				}
				while ((nextLine = reader.readNext()) != null) {
					DefaultPlugIn plugin = new DefaultPlugIn();
					for(int col=0; col<Titles.length; col++){
						if( Titles[col] == null ){
							continue;
						}
						
						String text ="";
						if( col < nextLine.length ){
							text = nextLine[col].trim();
						}
						try{
							DefaultPlugIn.Field field = DefaultPlugIn.Field.valueOf(Titles[col]);
							switch(field){
							case JavaScript:
								text = readJavascript(cntx, text);
								plugin.set(field, text);
								break;
							case CssFile:
								if( text.length() > 0 ){
									text = readCss(cntx, text);
									plugin.set(field, text);
								}
								else{
									plugin.set(field, "");
								}
								break;
							case BookName:
								if( text.length() == 0 ){
									col = Titles.length;
									plugin = null;
									break;
								}
								plugin.set(field, text);
								break;
							case WordCount:
							case IndexFileSize:
								text = text.replaceAll(",", "");
								/*flow through */
							default:
								plugin.set(field, text);
								break;
							}
							
						}
						catch(IllegalArgumentException ex){
							ex.printStackTrace();
							throw ex;
						}
					}
					if( plugin != null ){
						mPlugins.add(plugin);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static DefaultPlugIn findMatchPluagIn(String bookName, String wordCount, String indexFileSize) {
		// TODO Auto-generated method stub
		if( wordCount == null ){
			wordCount = "-";
		}
		if( indexFileSize == null ){
			indexFileSize = "-";
		}
		if( mPlugins != null ){
			Iterator<DefaultPlugIn> it = mPlugins.iterator();
			while(it.hasNext()){
				DefaultPlugIn plugin = it.next();
				if( plugin.get(Field.BookName).equals(bookName) && plugin.get(Field.WordCount).equals(wordCount)
						&& plugin.get(Field.IndexFileSize).equals(indexFileSize) ){
					return plugin;
				}
			}
		}
		return null;
	}
	
	private static String readJavascript(Context cntx, String jsName) {
		return readAssets(cntx, jsName);
	}
	private static String readCss(Context cntx, String cssName) {
		return readAssets(cntx, cssName);
	}
	private static String readAssets(Context cntx, String name) {
		try{
			InputStream in = cntx.getResources().getAssets().open("plugins/"+name); 
			int length = in.available();   

			byte [] buffer = new byte[length];
			in.read(buffer);
			String s = new String(buffer);
			in.close();
			return s;
		}

		catch(Exception e){   
			e.printStackTrace();   
		}   
		return "";
	}
}
