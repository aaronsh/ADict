package com.benemind.adict.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.benemind.adict.R;
import com.benemind.util.FileUtils;
import com.benemind.adict.core.kingsoft.KingsoftDictHtmlBuilder;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class DictEng {
	private static final String TAG = "DictEng";
	
	public static final int LOAD_DICTS_SUCC = 0;
	public static final int NO_DICT_ON_CARD = 1;
	public static final int NO_ACTIVE_DICT = 2;
	

	static DictEng mIns = null;

	private Context mCntx;
	private File mRootFolder;
	private File mBookFolder;
	private File mHtmlFolder;
	private LinkedList<Dictionary> mActiveDicts;

	static public DictEng getInstance(Context cntx) {
		if (mIns == null) {
			if (cntx == null) {
				return null;
			}
			mIns = new DictEng(cntx);
		}
		return mIns;
	}

	private DictEng(Context cntx) {
		mCntx = cntx;
		mRootFolder = FileUtils.getDictDir();
		mBookFolder = new File(mRootFolder, "books");
		mHtmlFolder = mRootFolder;
		mActiveDicts = new LinkedList<Dictionary>();
		
		DefaultPlugIn.loadPlugins(cntx);
		
		final String PREF_KEY = "installed";
		PackageManager pm = cntx.getPackageManager();//context为当前Activity上下文 
		PackageInfo pi;
		int verCode = 1;
		try {
			pi = pm.getPackageInfo(cntx.getPackageName(), 0);
			verCode = pi.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SharedPreferences pref = cntx.getSharedPreferences("dict", Context.MODE_PRIVATE);
		int installed = pref.getInt(PREF_KEY, 0);
		if( installed < verCode ){
			if(!mHtmlFolder.exists()){
				mHtmlFolder.mkdirs();
			}
			File cssFolder = new File(mHtmlFolder, "css");
			if(!cssFolder.exists()){
				cssFolder.mkdirs();
			}
			File imgFolder = new File(mHtmlFolder, "img");
			if(!imgFolder.exists()){
				imgFolder.mkdirs();
			}

			
			File onlineDictFolder = new File(mBookFolder, "youdao_online");
			if(!onlineDictFolder.exists()){
				onlineDictFolder.mkdirs();
			}
			
			String[] files = {"html.csv","iciba.csv", /*"dict.js",
							"img/background.png",
							"img/close.png",
							"img/dot_brown.png",
							"img/down_brown.png",
							"img/examples1.png",
							"img/examples2.png",
							"img/expand.png",
							"img/line_blue.png",
							"img/line_brown.png",
							"img/minus.png",
							"img/plus.png",
							"img/up_brown.png",

							"css/default.css",
							"books/qq_online/qq_online.adict",
							"books/qq_online/qq_online.ifo",
							"books/qq_online/qq_online.js",
							"books/youdao_online/youdao_online.adict", */
							"books/youdao_online/youdao_online.ifo",
							"books/youdao_online/youdao_online.js"
					};
			for(String file:files){
				installFile(file);
			}
			
			SharedPreferences.Editor editor = pref.edit();
			editor.putInt(PREF_KEY, verCode);
			editor.commit();
		}

		reloadConfig();
	}

	public void reloadConfig()
	{
		DictHtmlBuilder.loadHtmlSections(mHtmlFolder);
		DefaultPlugIn.loadPlugins(mCntx);
		KingsoftDictHtmlBuilder.loadConvertParam(mHtmlFolder);
	}

	private void installFile(String fileName) {
		try{   
			InputStream in = mCntx.getResources().getAssets().open(fileName);   

			File sdFile = new File(mHtmlFolder, fileName);
			FileOutputStream out = new FileOutputStream(sdFile);

			final int BUFFER_SIZE = 2048;
			byte[] buffer = new byte[BUFFER_SIZE];
			while(true){
				int len = in.read(buffer);
				if( len == -1 ){
					break;
				}
				out.write(buffer, 0, len);
			}
			in.close();
			out.close();
		}catch(Exception e){   
			e.printStackTrace();           
		}   
	}

	public int loadDicts() {
		int retCode = LOAD_DICTS_SUCC;
		
		mActiveDicts.clear();
		ArrayList<Dictionary> onCardList = scanCard();
		if (onCardList.isEmpty()) {
			retCode = NO_DICT_ON_CARD;
		}
//		onCardList.add(new OnlineDictionary("有道在线词典"));
//		onCardList.add(new OnlineDictionary("QQ在线词典"));

		ArrayList<Dictionary> savedList = loadDictList();
		if (!savedList.isEmpty()) {
			Iterator<Dictionary> itSaved = savedList.iterator();
			while (itSaved.hasNext()) {
				Dictionary saved = itSaved.next();
				Iterator<Dictionary> itCard = onCardList.iterator();
				while (itCard.hasNext()) {
					Dictionary dict = itCard.next();
					if (dict.checkDict(saved) != null) {
						int state = dict.getState();
						if (state == Dictionary.DICT_STATE_INSTALLED) {
							mActiveDicts.add(dict);
						}
						onCardList.remove(dict);
						break;
					}
				}
			}

		}

		Iterator<Dictionary> it = onCardList.iterator();
		while (it.hasNext()) {
			Dictionary dict = it.next();
			if (dict.getState() == Dictionary.DICT_STATE_INSTALLED) {
				mActiveDicts.add(dict);
			}
		}
		if( mActiveDicts.isEmpty() ){
			retCode = NO_ACTIVE_DICT;
		}
		Log.v(TAG, "active dicts:" + mActiveDicts.size() + mActiveDicts);
		onCardList.clear();
		
		return retCode;
	}

	public ArrayList<String> listWords(String key) {
		key = key.trim();
		ArrayList<String> list = new ArrayList<String>();
		Iterator<Dictionary> it = mActiveDicts.iterator();
		while (it.hasNext()) {
			Dictionary dict = it.next();
			ArrayList<String> words = dict.listWords(key);
			if (words != null) {
				for (String word : words) {
					if (!list.contains(word)) {
						list.add(word);
					}
				}
			}
		}
		// sort
		Collections.sort(list, new TextComparator());
		return list;
	}

	public ArrayList<WordLookupResult> lookupWord(String key) {
		key = key.trim();
		StringBuilder body = new StringBuilder();
		
		ArrayList<WordLookupResult> results = new ArrayList<WordLookupResult>();
		
		
		ArrayList<String> CssLinks = new ArrayList<String>();
		Iterator<Dictionary> it = mActiveDicts.iterator();
		while (it.hasNext()) {
			WordLookupResult r = new WordLookupResult();
			Dictionary dict = it.next();
			String text = dict.lookupWord(key);
			if (text.length() > 0) {
				r.css = dict.getCss();
				r.js = dict.getJavascript();
				r.html = text;
				r.book = dict.getDictPath();
				results.add(r);
			}
		}

		return results;
	}

	public ArrayList<Dictionary> listDicts() {
		ArrayList<Dictionary> list = new ArrayList<Dictionary>();

		ArrayList<Dictionary> onCardList = scanCard();

		ArrayList<Dictionary> savedList = loadDictList();
		Iterator<Dictionary> itSaved = savedList.iterator();
		while (itSaved.hasNext()) {
			Dictionary saved = itSaved.next();
			Iterator<Dictionary> itCard = onCardList.iterator();
			while (itCard.hasNext()) {
				Dictionary dict = itCard.next();
				if (dict.checkDict(saved) != null) {
					int state = dict.getState();
					if (state == Dictionary.DICT_STATE_INSTALLED
							|| state == Dictionary.DICT_STATE_SKIPPED
							|| state == Dictionary.DICT_STATE_DESTROYED) {
						list.add(dict);
					}
					onCardList.remove(dict);
					break;
				}
			}

		}
		savedList.clear();

		Iterator<Dictionary> it = onCardList.iterator();
		while (it.hasNext()) {
			Dictionary dict = it.next();
			int state = dict.getState();
			if (state == Dictionary.DICT_STATE_INSTALLED
					|| state == Dictionary.DICT_STATE_SKIPPED
					|| state == Dictionary.DICT_STATE_DESTROYED) {
				list.add(dict);
			}
		}
		onCardList.clear();
		
		it = list.iterator();
		long id = 1;
		while(it.hasNext()){
			Dictionary dict = it.next();
			dict.setDictManagementID(id);
			id++;
		}
		return list;
	}

	public void saveDictList(List<Dictionary> list) {
		JSONArray jArray = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			Dictionary dict = list.get(i);
			int state = dict.getState();
			if (state == Dictionary.DICT_STATE_INSTALLED
					|| state == Dictionary.DICT_STATE_SKIPPED) {
				jArray.put(dict.toJsonStr());
			}
		}
		String jsonStr = jArray.toString();
		Log.v(TAG, "saveDictList:" + jsonStr);
		writeAppData(jsonStr);
	}

	private ArrayList<Dictionary> loadDictList() {
		String jsonStr = readAppData();

		ArrayList<Dictionary> list = new ArrayList<Dictionary>();
		if (jsonStr.length() > 0) {
			JSONArray jsonArray;
			try {
				jsonArray = new JSONArray(jsonStr);
				for (int i = 0; i < jsonArray.length(); i++) {
					String s = jsonArray.getString(i);
					Dictionary dict = Dictionary.fromJsonStr(s);
					if (dict != null) {
						list.add(dict);
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return list;
	}

	public int estimateDictionaryCount()
	{
		int count = 0;

		File[] files = mBookFolder.listFiles();

		if( files == null ){
			return 0;
		}
		for (File folder : files) {
			if (folder.isDirectory()) {
				String name =getDictName(folder);
				File idx = new File(folder, name+".idx");
				if( !idx.exists() ){
					continue;
				}
				File data = new File(folder, name + ".dict");
				if( data.exists() ){
					count++;
					continue;
				}
				data = new File(folder, name + ".dict.dz");
				if( data.exists() ){
					count++;
					continue;
				}
			}
		}
		return count;
	}

	private String getDictName(File folder) {
		// TODO Auto-generated method stub
		String dictName = null;
		File[] files = folder.listFiles();
		for( File f:files){
			if( f.isFile() ){
				String name = f.getName();
				if( name.endsWith(".ifo") ){
					dictName = name.replace(".ifo", "");
					break;
				}
			}
		}
		return dictName;
	}

	private ArrayList<Dictionary> scanCard() {
		ArrayList<Dictionary> dicts = new ArrayList<Dictionary>();

		File[] files = mBookFolder.listFiles();
		if( files == null ){
			return dicts;
		}
		for (File folder : files) {
			if (folder.isDirectory()) {
				Dictionary dict = Dictionary.getFromDictPath(folder);
				if (dict != null) {
					Log.v(TAG, dict.toString());
					dicts.add(dict);
				}
			}
		}
		return dicts;
	}

	public File getRootFolder() {
		return mRootFolder;
	}

	public File getBookFolder(){
		return mBookFolder;
	}

	public File getHtmlFolder(){
		return mHtmlFolder;
	}

	public int getDictId(Dictionary dict) {
		return mActiveDicts.indexOf(dict);
	}

	class TextComparator implements Comparator<String> {
		@Override
		public int compare(String lhs, String rhs) {
			// TODO Auto-generated method stub
			return lhs.compareToIgnoreCase(rhs);
		}

	}

	private void writeAppData(String str) {
		try {

			FileOutputStream fout = mCntx.openFileOutput("dicts.json",
					Context.MODE_PRIVATE);
			fout.write(str.getBytes());
			fout.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String readAppData() {
		String res = "";
		try {
			FileInputStream fin = mCntx.openFileInput("dicts.json");
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = new String(buffer);
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;

	}
	
	private String loadSdcardFile(String file) {
		String res = "";
		try {
			FileInputStream fin = new FileInputStream(file);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = new String(buffer);
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;

	}

	

	// 写数据到SD中的文件
	public void writeHtmlFile(String html) {
		try {
			File file = new File(mHtmlFolder, "index.html");
			FileOutputStream fout = new FileOutputStream(file);
			byte[] bytes = html.getBytes();
			fout.write(bytes);
			fout.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getActiveDictCount() {
		// TODO Auto-generated method stub
		return mActiveDicts.size();
	}
	
	protected Context getContext(){
		return mCntx;
	}
}
