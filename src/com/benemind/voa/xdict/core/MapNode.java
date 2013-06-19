package com.benemind.voa.xdict.core;


import org.json.JSONException;
import org.json.JSONObject;

public class MapNode {
	private String Word;
	private long OffsetInDict;
	private int BytesInDict;
	private long OffsetInIndex;
	private long NextOffset;
	private int BytesInIndex;
	private boolean Invalid;
	

	public MapNode(MapNode another) {
		// TODO Auto-generated constructor stub
		Word = another.Word;
		OffsetInDict = another.OffsetInDict;
		BytesInDict  = another.BytesInDict;
		OffsetInIndex  = another.OffsetInIndex;
		NextOffset  = another.NextOffset;
		Invalid  = another.Invalid;
	}

	public MapNode() {
		// TODO Auto-generated constructor stub
	}

	public static MapNode fromJsonStr(String jsonStr) {
		// TODO Auto-generated method stub
		MapNode mapNode = new MapNode();
		
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(jsonStr);
			mapNode.Word = jsonObj.getString("Word");
			mapNode.OffsetInDict = jsonObj.getLong("OffsetInDict");
			mapNode.BytesInDict = jsonObj.getInt("BytesInDict");
			mapNode.OffsetInIndex = jsonObj.getLong("OffsetInIndex");
			mapNode.NextOffset = jsonObj.getInt("NextOffset");
			mapNode.Invalid = false;
			mapNode.BytesInIndex = (int)(mapNode.NextOffset - mapNode.OffsetInIndex);
			
			return mapNode;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String toJsonStr() {
		// TODO Auto-generated method stub
		JSONObject param = new JSONObject();		
		try {
			param.put("Word", Word);
			param.put("OffsetInDict", OffsetInDict);
			param.put("BytesInDict", BytesInDict);
			param.put("OffsetInIndex", OffsetInIndex);
			param.put("NextOffset", NextOffset);

			return param.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String toString(){
		return toJsonStr();
	}
	

	public void setBytesInIndex(int bytes) {
		// TODO Auto-generated method stub
		BytesInIndex = bytes;
	}

	public int getBytesInIndex()
	{
		return BytesInIndex;
	}

	public void setInvalid(boolean flag) {
		// TODO Auto-generated method stub
		Invalid = flag;
	}

	public void update(String word, long offsetInDict, int bytesInDict,
			long offsetInIndex, long nextOffset, int bytes) {
		// TODO Auto-generated method stub
		this.Word = word;
		this.OffsetInDict = offsetInDict;
		this.BytesInDict = bytesInDict;
		this.OffsetInIndex = offsetInIndex;
		this.NextOffset = nextOffset;
		this.BytesInIndex = bytes;
	}

	public boolean isInvalid() {
		// TODO Auto-generated method stub
		return Invalid;
	}

	public long getNextOffset() {
		// TODO Auto-generated method stub
		return NextOffset;
	}

	public long getOffsetInIndex() {
		// TODO Auto-generated method stub
		return OffsetInIndex;
	}

	public int getBytesInDict() {
		// TODO Auto-generated method stub
		return BytesInDict;
	}

	public long getOffsetInDict() {
		// TODO Auto-generated method stub
		return OffsetInDict;
	}

	public int CompareKey(String key) {
		// TODO Auto-generated method stub
		return Word.compareToIgnoreCase(key);
	}

	public String getWord() {
		// TODO Auto-generated method stub
		return Word;
	}

	

}
