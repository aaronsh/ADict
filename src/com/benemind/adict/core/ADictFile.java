package com.benemind.adict.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ADictFile {
	public static final String KEY_LIST_URL = "listWebApi";
	public static final String KEY_JS = "js";
	public static final String KEY_CSS = "css";
	
	HashMap<String, String> mMap;
	
	public ADictFile(File file) {
		// TODO Auto-generated constructor stub
		mMap = new HashMap<String, String>();
		readIniFile(file);
	}

	void readIniFile(File file)
	{
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			if( line != null )
				line = line.trim();
			if( line != null && line.equals("﻿ADict extension file") ){
				try {
					while( (line = reader.readLine()) != null ){
						String[] parts = line.split("=");
						if( parts.length != 2 ){
							continue;
						}
						mMap.put(parts[0].trim().toLowerCase(), parts[1].trim());
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			reader.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	String getValue(String key)
	{
		return mMap.get(key);
	}

	public String getJs() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getJs(File dictPath) {
		// TODO Auto-generated method stub
		String s = getValue(KEY_JS);
		if( s != null && s.length() > 0 ){
			File file = new File(dictPath, s);
			return readFromSd(file);
		}
		return null;
	}
	

	private static String readFromSd(File file) {
		try{
			FileInputStream fin = new FileInputStream(file);   

			int length = fin.available();   

			byte [] buffer = new byte[length];
			fin.read(buffer);
			String s = new String(buffer);
			fin.close();
			return s;
		}

		catch(Exception e){   
			e.printStackTrace();   
		}   
		return null;
	}
}
