package com.benemind.adict.core;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import com.benemind.adict.R;

import android.util.Log;

public class Dictionary {
	private static final String TAG = "Dictionary";

	public final static int DICT_STATE_UNINSTALLED = 0;
	public final static int DICT_STATE_DOWNLOADING = 1;
	public final static int DICT_STATE_INSTALLED = 2;
	public final static int DICT_STATE_SKIPPED = 3;
	public final static int DICT_STATE_DESTROYED = 4;

	private String DictPath = null; //the path name which contains the dictionary files(*.ifo, *.idx, *.dict)
	private String DictName = null; //the name of dictionary file
	private String BookName = null;
	private boolean Reserved = false;
	private String Link="";
	
	private IfoFile mFileInfo;
	private IdxFile mFileIndex;
	private DictFile mFileDict;
	private boolean mLoaded;
	
	private long mDictManagementID;
	
	private int DictState = DICT_STATE_UNINSTALLED;

	// private boolean GalaxDictSupport = false;

	public Dictionary() {
		mFileInfo = null;
		mFileIndex = null;
		mFileDict = null;
		mLoaded = false;
		mDictManagementID = 0;
	}

	
	//for test

	public Dictionary(String s) {
		// TODO Auto-generated constructor stub
		BookName = s;
	}



	public String getBookName() {
		return BookName;
	}
	
	public Dictionary checkDict(Dictionary saved)
	{
		if( BookName == null ){
			return null;
		}
		if( !BookName.equalsIgnoreCase(saved.BookName) ){
			return null;
		}
		if( !DictPath.equalsIgnoreCase(saved.DictPath) ){
			return null;
		}
		if( !DictName.equalsIgnoreCase(saved.DictName) ){
			return null;
		}

		if( DICT_STATE_INSTALLED == DictState ){
			if(  saved.DictState == DICT_STATE_SKIPPED ){
				DictState = DICT_STATE_SKIPPED;
			}
			return this;
		}
		
		return this;
	}


	public JSONObject toJsonObj() {
		JSONObject param = new JSONObject();
		try {
			param.put("DictPath", DictPath);
			param.put("DictName", DictName);
			param.put("BookName", BookName);
			param.put("DictState", DictState);
			param.put("Link", Link);
			return param;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public String toJsonStr() {
		JSONObject param = toJsonObj();
		if( param != null ){
			return param.toString();
		}
		return null;
	}
	
	public static Dictionary fromJsonObj(JSONObject jsonObj)
	{
		try {
			Dictionary dict = new Dictionary();
			dict.DictPath = jsonObj.getString("DictPath");
			dict.DictName = jsonObj.getString("DictName");
			dict.BookName = jsonObj.getString("BookName");
			dict.DictState = jsonObj.getInt("DictState");
			dict.Link = jsonObj.getString("Link");
			dict.mLoaded = false;
			return dict;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Dictionary fromJsonStr(String jsonStr) {
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			return fromJsonObj(jsonObj);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public static Dictionary getFromDictPath(File dictRoot) {
		String dictName = null;
		Dictionary dict = null;
		
		File[] files = dictRoot.listFiles();
		for( File f:files){
			if( f.isFile() ){
				String name = f.getName();
				if( name.endsWith(".ifo") ){
					dict = new Dictionary();
					dictName = name.replace(".ifo", "");
					dict.DictPath = dictRoot.getName();
					dict.DictName = dictName;
					dict.load();
					if(  dict.getDictState() != DICT_STATE_UNINSTALLED ){
						break;
					}
					dict = null;
				}
			}
		}

		return dict;
	}

	//load dictionary from disk
	boolean load() {
		// TODO Auto-generated method stub
		mLoaded = true;
		
		DictEng eng = DictEng.getInstance(null);
		
		File dir = new File(eng.getBookFolder(), DictPath);
		if( !dir.exists() || !dir.isDirectory() ){
			DictState = DICT_STATE_UNINSTALLED;
			return false;
		}

		//load 'ifo' file
		File file = new File(dir, DictName+".ifo");
		if( !file.exists() || file.isDirectory() ){
			DictState = DICT_STATE_UNINSTALLED;
			return false;
		}
		
		mFileInfo = new IfoFile(file);
		BookName = mFileInfo.getValue(IfoFile.KEY_BOOKNAME);
		
		//load 'dict' file
		file = new File(dir, DictName + ".dict");
		if( !file.exists() || file.isDirectory() ){
			file = new File(dir, DictName + ".dict.dz");
			if( !file.exists() || file.isDirectory() ){
				DictState = DICT_STATE_DESTROYED;
				return false;
			}
		}
		mFileDict = new DictFile(file, mFileInfo.getValue(IfoFile.KEY_sametypesequence));
		
		//load 'idx' file
		file = new File(dir, DictName + ".idx");
		if( !file.exists() || file.isDirectory() ){
			DictState = DICT_STATE_DESTROYED;
			return false;
		}
		
		mFileIndex = new IdxFile(file);
		if( !mFileIndex.check(mFileInfo.getValue(IfoFile.KEY_idxfilesize)) ){
			DictState = DICT_STATE_DESTROYED;
			return false;
		}
		try {
			mFileIndex.load();
		} catch (IllegalIndexFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			DictState = DICT_STATE_DESTROYED;
			return false;
		}
		
		DictState = DICT_STATE_INSTALLED;
		return true;
	}



	public String toString() {
		return toJsonStr();
	}


	private int getDictState() {
		if( !mLoaded ){
			load();
		}
		return DictState;
	}
	

	public int getState() {
		// TODO Auto-generated method stub
		return DictState;
	}

	public void setState(int newState) {
		// TODO Auto-generated method stub
		DictState = newState;
	}

	public boolean isReserved() {
		// TODO Auto-generated method stub
		return Reserved;
	}



	public ArrayList<String> listWords(String key) {
		// TODO Auto-generated method stub
		if( DictState == DICT_STATE_INSTALLED ){
			return mFileIndex.listWords(key);
		}
		return null;
	}



	public String lookupWord(String key) {
		// TODO Auto-generated method stub
		StringBuilder html = new StringBuilder();
		StringBuilder wordText, wordExplanation, wordList;
		
		if( DictState == DICT_STATE_INSTALLED ){
			LinkedList<MapNode> list = mFileIndex.lookupWord(key);
			if( list.size() > 0 ){
				html = DictHtmlBuilder.buildBookname(BookName);
				
				wordList = new StringBuilder();
				Iterator<MapNode> it = list.iterator();
				while(it.hasNext()){
					MapNode node = it.next();
					wordText = DictHtmlBuilder.buildWordText(node.getWord());
					
					long ts = System.currentTimeMillis();
					String text = mFileDict.fetchExplanation(node);
					Log.v(TAG,  "used "+(System.currentTimeMillis() -ts) + "ms to read dict file");
					wordExplanation = DictHtmlBuilder.buildWordExplanation(text);
					wordText.append(wordExplanation);
					wordList.append(DictHtmlBuilder.buildWordItem(wordText.toString()));
					
				}
				html.append(DictHtmlBuilder.buildWordList(wordList.toString()));
				return html.toString();
			}
		}
		return "";
	}
	
	public void setDictManagementID(long id)
	{
		mDictManagementID = id;
	}
	
	public long getDictManagementID()
	{
		return mDictManagementID;
	}


	public boolean isMatch(String bookName, String wordCount, String indexFileSize) {
		// TODO Auto-generated method stub
		if( !bookName.equalsIgnoreCase(BookName) ){
			return false;
		}
		
		if( wordCount == null) wordCount = "";
		wordCount = wordCount.trim();
		if(wordCount.length() > 0 ){
			wordCount = wordCount.replaceAll(",", "");
			if( !wordCount.equals(mFileInfo.getValue(IfoFile.KEY_wordcount))){
				return false;
			}
		}
		if( indexFileSize == null ) indexFileSize = "";
		indexFileSize = indexFileSize.trim();
		if(indexFileSize.length() > 0 ){
			indexFileSize = indexFileSize.replaceAll(",", "");
			if( !indexFileSize.equals(mFileInfo.getValue(IfoFile.KEY_idxfilesize))){
				return false;
			}
		}		
		return true;
	}
}
