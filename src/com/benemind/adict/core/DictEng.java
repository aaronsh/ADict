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
import android.util.Log;

public class DictEng {
	private static final String TAG = "DictEng";

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
		mHtmlFolder = new File(mRootFolder, "html");
		mActiveDicts = new LinkedList<Dictionary>();
		
		final String PREF_KEY = "installed";
		SharedPreferences pref = cntx.getSharedPreferences("dict", Context.MODE_PRIVATE);
		Boolean installed = pref.getBoolean(PREF_KEY, false);
		if( !installed ){
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
			File pluginsFolder = new File(mHtmlFolder, "plugins");
			if(!pluginsFolder.exists()){
				pluginsFolder.mkdirs();
			}
			String[] files = {"html.xls","iciba.xls", "plugins.xls","dict.js",
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
							"plugins/langdao-ec-gb.js",
							"plugins/qq_online.js",
							"plugins/stardict-kdic-ec-11w-2.4.2.js",
							"plugins/stardict-langdao-ec-gb-2.4.2.js",
							"plugins/stardict-lazyworm-ec-2.4.2.js",
							"plugins/stardict-oxfordjm-ec-2.4.2.js",
							"plugins/stardict-powerword2007_pwdecmc-2.4.2.css",
							"plugins/stardict-powerword2007_pwdecmc-2.4.2.js",
							"plugins/stardict-quick_eng-zh_CN-2.4.2.js",
							"plugins/stardict-stardict1.3-2.4.2.js",
							"plugins/stardict-xdict-ec-gb-2.4.2.js",
							"plugins/youdao_online.js"			
					};
			for(String file:files){
				installFile(file);
			}
			
			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean(PREF_KEY, true);
			editor.commit();
		}

		reloadConfig();
	}

	public void reloadConfig()
	{
		DictHtmlBuilder.loadHtmlSections(mHtmlFolder);
		DictHtmlBuilder.loadHtmlPlugins(mHtmlFolder);
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

	public void loadDicts() throws NotFoundDictException {
		mActiveDicts.clear();
		ArrayList<Dictionary> onCardList = scanCard();
		onCardList.add(new OnlineDictionary("有道在线词典"));
		onCardList.add(new OnlineDictionary("QQ在线词典"));
		if (onCardList.isEmpty()) {
			throw new NotFoundDictException();
		}

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

		Log.v(TAG, "active dicts:" + mActiveDicts.size() + mActiveDicts);
		onCardList.clear();
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

	public String lookupWord(String key) {
		key = key.trim();
		StringBuilder body = new StringBuilder();

		Iterator<Dictionary> it = mActiveDicts.iterator();
		while (it.hasNext()) {
			Dictionary dict = it.next();
			String text = dict.lookupWord(key);
			if (text.length() > 0) {
				body.append(DictHtmlBuilder.buildDictionary(text, dict));
			}
		}
		StringBuilder header = DictHtmlBuilder.buildHeader();
		header.append(DictHtmlBuilder.buildBody(body.toString()));
		StringBuilder html = DictHtmlBuilder.buildHtml(header.toString());
		String htmlText = html.toString();
		writeHtmlFile(htmlText);
		return htmlText;
	}

	public ArrayList<Dictionary> listDicts() {
		ArrayList<Dictionary> list = new ArrayList<Dictionary>();

		ArrayList<Dictionary> onCardList = scanCard();
		//add online dicts
		onCardList.add(new OnlineDictionary("有道在线词典"));
		onCardList.add(new OnlineDictionary("QQ在线词典"));
		
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
