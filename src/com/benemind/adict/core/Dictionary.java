package com.benemind.adict.core;


import java.io.File;
import java.io.FileInputStream;
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
	protected boolean mLoaded;
	
	private String mJavascriptText;
	private String mCssText;
	private String mListWebApi;
	
	private long mDictManagementID;
	
	protected int DictState = DICT_STATE_UNINSTALLED;

	// private boolean GalaxDictSupport = false;

	public Dictionary() {
		mFileInfo = null;
		mFileIndex = null;
		mFileDict = null;
		mLoaded = false;
		mDictManagementID = 0;
		
		mJavascriptText = "";
		mCssText = "";
	}

	
	//for test

	public Dictionary(String s) {
		// TODO Auto-generated constructor stub
		BookName = s;
	}


	protected String getDictPath(){
		return DictPath;
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
		if( OnlineDictionary.isOnlineDictionary(jsonObj) ){
			return OnlineDictionary.fromJsonObj(jsonObj);
		}
		
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
					dictName = name.replace(".ifo", "");
					String dictPath = dictRoot.getName();
					dict = load(dictPath, dictName);
					if(  dict != null ){
						break;
					}
					dict = null;
				}
			}
		}

		return dict;
	}

	//load dictionary from disk
	static Dictionary load(String dictPath, String dictName) {
		// TODO Auto-generated method stub
		Dictionary dict = null;
		
		DictEng eng = DictEng.getInstance(null);
		
		File dir = new File(eng.getBookFolder(), dictPath);
		if( !dir.exists() || !dir.isDirectory() ){
			return null;
		}

		//load 'ifo' file
		File file = new File(dir, dictName+".ifo");
		if( !file.exists() || file.isDirectory() ){
			return null;
		}
		
		IfoFile FileInfo = new IfoFile(file);
		String BookName = FileInfo.getValue(IfoFile.KEY_BOOKNAME);
		
		String ListWebApi = null;
		String JavascriptText = "";
		String CssText = "";
		
		String wordCount = FileInfo.getValue(IfoFile.KEY_wordcount);
		String indexFileSize = FileInfo.getValue(IfoFile.KEY_idxfilesize);
		//load css and js
		file = new File(dir, dictName+".css");
		if( file.exists() ){
			CssText = readFromSd(file);
		}
		else{
			DefaultPlugIn plugin = DefaultPlugIn.findMatchPluagIn(BookName, wordCount, indexFileSize);
			if( plugin != null ){
				CssText = plugin.get(DefaultPlugIn.Field.CssFile);
			}
		}
		file = new File(dir, dictName+".js");
		if( file.exists() ){
			JavascriptText = readFromSd(file);
		}
		else{
			//load plugin form table
			DefaultPlugIn plugin = DefaultPlugIn.findMatchPluagIn(BookName, wordCount, indexFileSize);
			if( plugin != null ){
				JavascriptText = plugin.get(DefaultPlugIn.Field.JavaScript);
			}
		}
		
		
		if( wordCount == null || wordCount.length() == 0 || indexFileSize == null || indexFileSize.length() == 0 ){
			dict =  new OnlineDictionary(BookName);
			dict.DictName = dictName;
			dict.DictPath = dictPath;
			dict.mLoaded = true;
			dict.mFileInfo = FileInfo;
			dict.mJavascriptText = JavascriptText;
			dict.mCssText = CssText;
			dict.mListWebApi = ListWebApi;
			return dict;
		}
		//load 'dict' file
		file = new File(dir, dictName + ".dict");
		if( !file.exists() || file.isDirectory() ){
			file = new File(dir, dictName + ".dict.dz");
			if( !file.exists() || file.isDirectory() ){
				dict =  new Dictionary(BookName);
				dict.setState(DICT_STATE_DESTROYED);
				return dict;
			}
		}
		DictFile FileDict = new DictFile(file, FileInfo.getValue(IfoFile.KEY_sametypesequence));
		
		//load 'idx' file
		file = new File(dir, dictName + ".idx");
		if( !file.exists() || file.isDirectory() ){
			dict =  new Dictionary(BookName);
			dict.setState(DICT_STATE_DESTROYED);
			return dict;
		}
		
		IdxFile FileIndex = new IdxFile(file);
		if( !FileIndex.check(FileInfo.getValue(IfoFile.KEY_idxfilesize)) ){
			dict =  new Dictionary(BookName);
			dict.setState(DICT_STATE_DESTROYED);
			return dict;
		}
		try {
			FileIndex.load();
		} catch (IllegalIndexFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			dict =  new Dictionary(BookName);
			dict.setState(DICT_STATE_DESTROYED);
			return dict;
		}
		

		dict =  new Dictionary(BookName);
		dict.setState(DICT_STATE_INSTALLED);
		dict.DictName = dictName;
		dict.DictPath = dictPath;
		dict.mLoaded = true;
		dict.mFileInfo = FileInfo;
		dict.mFileIndex = FileIndex;
		dict.mFileDict = FileDict;
		dict.mJavascriptText = JavascriptText;
		dict.mCssText = CssText;
		dict.mListWebApi = ListWebApi;
		return dict;
	}



	public String toString() {
		return toJsonStr();
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


	public String getCss() {
		// TODO Auto-generated method stub
		return mCssText;
	}


	public String getJavascript() {
		// TODO Auto-generated method stub
		return mJavascriptText;
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
		return "";
	}
}
