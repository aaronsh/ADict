package com.benemind.voa.xdict.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class IfoFile {
	public static final String KEY_BOOKNAME = "bookname";
	public static final String KEY_wordcount = "wordcount";
	public static final String KEY_idxfilesize = "idxfilesize";
	public static final String KEY_sametypesequence = "sametypesequence";
	HashMap<String, String> mMap;
	
	public IfoFile(File file) {
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
			if( line != null && line.equals("StarDict's dict ifo file") ){
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
}
