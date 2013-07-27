package com.benemind.adict.core;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class OnlineDictionary extends Dictionary {

	public OnlineDictionary(String bookName) {
		// TODO Auto-generated constructor stub
		super(bookName);
		setState(DICT_STATE_INSTALLED);
	}
	
	
	public Dictionary checkDict(Dictionary saved)
	{
		String bookName = getBookName();
		if( bookName == null ){
			return null;
		}
		if( !bookName.equalsIgnoreCase(saved.getBookName()) ){
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

	
	public ArrayList<String> listWords(String key) {
		// TODO Auto-generated method stub
		return null;
	}



	public String lookupWord(String key) {
		// TODO Auto-generated method stub
		StringBuilder html = new StringBuilder();
		String text = "无可用网络，在线词典暂时不可用！";
		if( checkNetworkConnection() ){
			text = "<word style=\"display:none;\">"+key+"</word>"+"正在查询...";
		}
		if( DictState == DICT_STATE_INSTALLED && key.length() > 0){
			html = DictHtmlBuilder.buildBookname(getBookName());
			html.append(DictHtmlBuilder.buildWordList(text));
				return html.toString();
		}
		return "";
	}
	
	public JSONObject toJsonObj() {
		JSONObject param = new JSONObject();
		try {
			param.put("OnlineDict", true);
			param.put("BookName", getBookName());
			param.put("DictState", DictState);
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
	
	public static boolean isOnlineDictionary(String jsonStr){
		return jsonStr.contains("\"OnlineDict\":true");
	}
	public static boolean isOnlineDictionary(JSONObject jsonObj){
		try{
			return jsonObj.getBoolean("OnlineDict");
		}catch (JSONException e) {
			return false;
		}
	}
	public static OnlineDictionary fromJsonObj(JSONObject jsonObj)
	{
		try {
			String bookName = jsonObj.getString("BookName");
			OnlineDictionary dict = new OnlineDictionary(bookName);
			dict.setState(jsonObj.getInt("DictState"));
			dict.mLoaded = false;
			return dict;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static OnlineDictionary fromJsonStr(String jsonStr) {
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			return fromJsonObj(jsonObj);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean isMatch(String bookName, String wordCount, String indexFileSize) {
		// TODO Auto-generated method stub
		String name = getBookName();
		if( !bookName.equalsIgnoreCase(name) ){
			return false;
		}
		if( !"-".equals(wordCount) ){
			return false;
		}
		if( !"-".equals(indexFileSize) ){
			return false;
		}		
		return true;
	}
	
	private boolean checkNetworkConnection(){
		DictEng engine = DictEng.getInstance(null);
		Context cntx = engine.getContext();
		ConnectivityManager connectivity = (ConnectivityManager) cntx.getSystemService(Context.CONNECTIVITY_SERVICE);  
		if (connectivity != null)  
		{  
			NetworkInfo[] infos = connectivity.getAllNetworkInfo();
			if( infos != null ){
				for(NetworkInfo info:infos){
					if( info.getState() == NetworkInfo.State.CONNECTED ){
						return true;
					}
				}
			}  
		}  
		return false;  
	}
}
