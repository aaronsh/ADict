package com.benemind.voa.xdict.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class IdxFile {
	private static final String TAG = "IdxFile";

	private static final String KEY_INDEX_SIZE = "indexSize";
	private static final String KEY_DICT_SIZE = "dictSize";
	private static final String KEY_TAG = "tag";
	private static final String MAP_FILE_HEADER_TAG = "map file v2";
	
	private static final int SCAN_MAP_NODE_BUF_SIZE = 512;

	private static final int WORD_AFTER_CURRENT_POSITION = -1;
	private static final int WORD_BEFORE_CURRENT_POSIITON = -2;
	private static final int WORD_NOT_FOUND = -3;
	
	File mFile;
	long mDictFileSize;
	ArrayList<MapNode> mIndexMap;

	public IdxFile(File file) {
		// TODO Auto-generated constructor stub
		mFile = file;
		mIndexMap = new ArrayList<MapNode>();
	}

	void load() throws IllegalIndexFileException {
		File idx = mFile;
		mIndexMap.clear();
		try {
			long IdxFileSize = idx.length();
			RandomAccessFile f = new RandomAccessFile(idx, "r");
			f.seek(IdxFileSize - 8);
			int offset = f.readInt();
			int block = f.readInt();
			Log.v(TAG, "offset:" + offset + ",block:" + block);
			f.close();

			mDictFileSize = offset + block;

			if( !loadMap() ){
				createMap();
				saveMap();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	boolean loadMap() {
		String name = mFile.getName() + ".map";
		File f = new File(mFile.getParent(), name);
		if (!f.exists()) {
			return false;
		}

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line = reader.readLine();
			if (line != null) {
				line = line.trim();
			}
			if (line != null) {
				try {
					JSONObject jsonObj = new JSONObject(line);
					if (MAP_FILE_HEADER_TAG.equals(jsonObj.getString(KEY_TAG))
							&& jsonObj.getLong(KEY_INDEX_SIZE) == mFile
									.length()
							&& jsonObj.getLong(KEY_DICT_SIZE) == mDictFileSize) {
						while ((line = reader.readLine()) != null) {
							MapNode map = MapNode
									.fromJsonStr(line.trim());
							if (map != null) {
								mIndexMap.add(map);
							}
						}

					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
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

		if (mIndexMap.size() != 33 ) {
			return false;
		}
		MapNode node = mIndexMap.get(mIndexMap.size() - 1);
		if( (node.getOffsetInDict() + node.getBytesInDict()) != mDictFileSize ){
			return false;
		}
		return true;
	}

	void createMap() throws IllegalIndexFileException {
		mIndexMap.clear();
		long len = mFile.length();
		long step = len/32;
		ArrayList<MapNode> list;
		for(int i=0 ; i<32; i++){
			list = scan(i*step);
			if( list.size() < 10 ){
				String msg = String.format("can not find enough map node in idx file. file:%1$s, offset:%2$x",
						mFile.getName(), i*step );
				throw new IllegalIndexFileException(msg);
			}
			if( i == 0 ){
				mIndexMap.add(list.get(0));
			}
			else{
				MapNode node = list.get(1);
				mIndexMap.add(node);
			}
			Log.v(TAG, "next test");
		}
		
		//get last map node
		len = len - SCAN_MAP_NODE_BUF_SIZE*2/3;
		list = scan( len );
		if( list.size() < 10 ){
			String msg = String.format("can not find enough map node in idx file. file:%1$s, offset:%2$x",
					mFile.getName(), len );
			throw new IllegalIndexFileException(msg);
		}
		MapNode node = list.get(list.size()-1);
		mIndexMap.add(node);
	}

	void saveMap() {
		String name = mFile.getName() + ".map";
		File f = new File(mFile.getParent(), name);

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
			try {
				JSONObject param = new JSONObject();
				try {
					param.put(KEY_TAG, MAP_FILE_HEADER_TAG);
					param.put(KEY_INDEX_SIZE, mFile.length());
					param.put(KEY_DICT_SIZE, mDictFileSize);

					fos.write(param.toString().getBytes());
					fos.write("\n".getBytes());

					Iterator<MapNode> it = mIndexMap.iterator();
					while (it.hasNext()) {
						MapNode map = it.next();
						fos.write(map.toJsonStr().getBytes());
						fos.write("\n".getBytes());
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return;
	}

	public boolean check(String fileSize) {
		// TODO Auto-generated method stub
		try {
			long size = Long.valueOf(fileSize);
			if (size == mFile.length() && size > SCAN_MAP_NODE_BUF_SIZE ) {
				return true;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return false;
	}

	
	private ArrayList<MapNode> scan(long offset)
	{
		ArrayList<MapNode> list = new ArrayList<MapNode>();
		
		MapNode node = new MapNode();
		byte[] buf = new byte[SCAN_MAP_NODE_BUF_SIZE];
		int read = 0;
		RandomAccessFile f;
		try {
			f = new RandomAccessFile(mFile, "r");
			try {
				f.seek(offset);
				read = f.read(buf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if( read == -1 ){
			return null;
		}
		
		
		int startAt = 0;
		read = read > 8 ? (read - 8): 0;
		while( startAt<read ){
			MapNode parsed = getMapNode(buf, startAt, SCAN_MAP_NODE_BUF_SIZE, offset, node);
//			Log.v(TAG, "MapNode:"+parsed.toJsonStr());
			startAt = startAt + parsed.getBytesInIndex();
			if( parsed.isInvalid() ){
				continue;
			}
			if( list.isEmpty() ){
				list.add(new MapNode(parsed));
			}
			else{
				list.add(new MapNode(parsed));
				if( !verifyMapNodeList(list) ){
					list.clear();
				}
			}
		}
		Log.v(TAG, "list size:"+list.size());
		return list;
	}
	
	private boolean verifyMapNodeList(ArrayList<MapNode> list) {
		// TODO Auto-generated method stub
		boolean passCheck = true;
		long offset = 0;
		Iterator<MapNode> it = list.iterator();
		while(it.hasNext()){
			MapNode node = it.next();
			if( offset != node.getOffsetInDict() && offset != 0 ){
				passCheck = false;
				break;
			}
			offset = node.getOffsetInDict() + node.getBytesInDict();
		}
		return passCheck;
	}

	private MapNode getMapNode(byte[] buf, int startAt, int size, long offsetInFile, MapNode map){
		int strEnd;
		int len = size - 8;
		int bytes = 0;
		for(strEnd=startAt; strEnd<len; strEnd++){
			if( buf[strEnd] == 0 ){
				break;
			}
		}
		bytes = strEnd - startAt + 1;
		if( strEnd == startAt ){
			map.setBytesInIndex(bytes);
			map.setInvalid(true);
			return map;
		}
		if( strEnd >= len ){
			map.setBytesInIndex(bytes);
			map.setInvalid(true);
			return map;
		}
		long offsetInDict = 0;
		int bytesInDict = 0;
		int n = strEnd+1;
		try{
			for (int i = 0; i < 4; i++, n++) {
				offsetInDict <<= 8;
				offsetInDict |= (buf[n] & 0xff);
			}

			for (int i = 0; i < 4; i++, n++) {
				bytesInDict <<= 8;
				bytesInDict |= (buf[n] & 0xff);
			}
		}catch(java.lang.ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		//checks
		if( offsetInDict < 0 ){
			map.setBytesInIndex(bytes);
			map.setInvalid(true);
			return map;
		}
		if( bytesInDict < 0 ){
			map.setBytesInIndex(bytes);
			map.setInvalid(true);
			return map;
		}
		if( offsetInDict > mDictFileSize ){
			map.setBytesInIndex(bytes);
			map.setInvalid(true);
			return map;
		}
		if( (offsetInDict + bytesInDict) > mDictFileSize ){
			map.setBytesInIndex(bytes);
			map.setInvalid(true);
			return map;
		}
	
		bytes = bytes + 8 ;
		String word = new String(buf, startAt, strEnd-startAt);
		offsetInFile = startAt + offsetInFile;
		map.update(word, offsetInDict, bytesInDict, offsetInFile, offsetInFile+bytes, bytes);
		map.setInvalid(false);
		
		return map;
	}

	public ArrayList<String> listWords(String key) {
		// TODO Auto-generated method stub
		ArrayList<String> words = new ArrayList<String>();
		String a = "a";
		Log.v(TAG, "compare a:b is "+a.compareToIgnoreCase("b"));
		MapNode startRange = null;
		MapNode endRange = null;
		MapNode node;
		int comp;
		int size = mIndexMap.size();
		for(int i=0; i<size; i++){
			node = mIndexMap.get(i);
			comp = node.CompareKey(key);
			if( i == 0){
				startRange = node;
			}
			if( comp < 0 ){
				startRange = node;
			}
			else if( comp == 0 ){
				startRange = node;
				endRange = node;
				break;
			}
			else {
				endRange = node;
				break;
			}
		}

		//check is before the first node
		node = mIndexMap.get(0);
		if( node.CompareKey(key) > 0 ){
			return words;
		}

		//check is after the last node
		node = mIndexMap.get(size -1);
		if( node.CompareKey(key) < 0 ){
			return words;
		}
		
		long offset;
		long offsetMin = SCAN_MAP_NODE_BUF_SIZE/2;
		boolean IncludeFirst = false;
		ArrayList<MapNode> list;
		int tries =0;
		while(true){
			tries++;
			if( (endRange.getOffsetInIndex() - startRange.getOffsetInIndex()) < offsetMin ){
				offset = startRange.getOffsetInIndex() ;
				if( offset > offsetMin ){
					offset = offset - offsetMin;
				}
			}
			else{
				offset = startRange.getOffsetInIndex();
				offset = (offset + endRange.getOffsetInIndex())/2;
			}
			list = scan(offset);
			if( list.isEmpty() ){
				break;
			}
			MapNode first = list.get(0);
			if( first.getOffsetInIndex() == startRange.getOffsetInIndex() || first.getOffsetInIndex() == 0 ){
				IncludeFirst = true;
			}
			else{
				IncludeFirst = false;
				first = list.get(1);
			}
			
			comp = findMatchKey(list, key, IncludeFirst);
			if( comp == WORD_NOT_FOUND ){
				int i = 1;
				if( IncludeFirst ){
					i = 0;
				}
				for(; i<list.size() && words.size() < 20; i++ ){
					node = list.get(i);
					comp = node.CompareKey(key);
					if( comp < 0 ){
						first = node;
					}
					else if( comp > 0  ){
						if( first != null ){
							words.add(first.getWord());
							first = null;
						}
						words.add(node.getWord());
					}
				}
				list.clear();
				break;
			}
			else if( comp  ==  WORD_AFTER_CURRENT_POSITION ){
				startRange = list.get(list.size() -1);
			}
			else if( comp == WORD_BEFORE_CURRENT_POSIITON){
				endRange = first;
			}
			else{
				//find match
				first = list.get(list.size() - 1);
				if( (first.getOffsetInDict() + first.getBytesInDict()) == mDictFileSize ){
					//read EOF
					for(;comp<list.size(); comp++){
						first = list.get(comp);
						words.add(first.getWord());
					}
					break;
				}
				else{
					//normal 
					if( (list.size() - comp)<20 ){
						first = list.get(comp);
						list = scan(first.getOffsetInIndex());
						comp = 0;
					}
					for(int count =0;comp<list.size() && count<20 ; comp++,count++){
						first = list.get(comp);
						words.add(first.getWord());
					}
					break;
				}
			}
		}

		Log.v(TAG, "loopupWord treis:"+tries);
		
		return words;
	}

	private int findMatchKey(ArrayList<MapNode> list, String key, boolean includeFirst) {
		// TODO Auto-generated method stub
		int size = list.size();
		int comp;
		int result = WORD_BEFORE_CURRENT_POSIITON;
		for(int i=0; i<size; i++){
			if( i==0 && includeFirst == false ){
				continue;
			}
			MapNode node = list.get(i);
			comp = node.CompareKey(key);
			if( comp < 0 ){
				result = WORD_AFTER_CURRENT_POSITION;
			}
			else if( comp == 0 ){
				result = i;
				break;
			}
			else if( comp > 0 ){
				if( result == WORD_AFTER_CURRENT_POSITION ){
					result = WORD_NOT_FOUND;
				}
				break;
			}
		}
		return result;
	}

	public LinkedList<MapNode> lookupWord(String key) {
		// TODO Auto-generated method stub
		LinkedList<MapNode> words = new LinkedList<MapNode>();
		
		MapNode startRange = null;
		MapNode endRange = null;
		MapNode node;
		int comp;
		int size = mIndexMap.size();
		for(int i=0; i<size; i++){
			node = mIndexMap.get(i);
			comp = node.CompareKey(key);
			if( i == 0){
				startRange = node;
			}
			if( comp < 0 ){
				startRange = node;
			}
			else if( comp == 0 ){
				startRange = node;
				endRange = node;
				break;
			}
			else {
				endRange = node;
				break;
			}
		}

		//check is before the first node
		node = mIndexMap.get(0);
		if( node.CompareKey(key) > 0 ){
			return words;
		}

		//check is after the last node
		node = mIndexMap.get(size -1);
		if( node.CompareKey(key) < 0 ){
			return words;
		}
		
		long offset;
		long offsetMin = SCAN_MAP_NODE_BUF_SIZE/2;
		boolean IncludeFirst = false;
		ArrayList<MapNode> list;
		int tries = 0;
		while(true){
			tries++;
			if( (endRange.getOffsetInIndex() - startRange.getOffsetInIndex()) < offsetMin ){
				offset = startRange.getOffsetInIndex() ;
				if( offset > offsetMin ){
					offset = offset - offsetMin;
				}
			}
			else{
				offset = startRange.getOffsetInIndex();
				offset = (offset + endRange.getOffsetInIndex())/2;
			}
			list = scan(offset);
			if( list.isEmpty() ){
				break;
			}
			MapNode first = list.get(0);
			if( first.getOffsetInIndex() == startRange.getOffsetInIndex() || first.getOffsetInIndex() == 0 ){
				IncludeFirst = true;
			}
			else{
				IncludeFirst = false;
				first = list.get(1);
			}
			
			comp = findMatchKey(list, key, IncludeFirst);
			if( comp == WORD_NOT_FOUND ){
				break;
			}
			else if( comp  ==  WORD_AFTER_CURRENT_POSITION ){
				startRange = list.get(list.size() -1);
			}
			else if( comp == WORD_BEFORE_CURRENT_POSIITON){
				endRange = first;
			}
			else{
				//find match
				first = list.get(comp);
				offset = first.getOffsetInIndex() ;
				if( offset > offsetMin ){
					offset = offset - offsetMin;
				}
				else{
					offset = 0;
				}
				list = scan(offset);
				first = list.get(0);
				if( first.getOffsetInIndex() == offset ){
					IncludeFirst = true;
				}
				else{
					IncludeFirst = false;
				}
				for(int i=0; i<list.size(); i++ ){
					if( i==0 && IncludeFirst==false ){
						continue;
					}
					first = list.get(i);
					if( first.CompareKey(key) == 0 ){
						words.add(first);
					}
				}
				list.clear();
				break;
			}
		}
		Log.v(TAG, "loopupWord treis:"+tries);
		
		return words;
	}
}
