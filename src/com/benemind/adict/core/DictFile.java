package com.benemind.adict.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.benemind.util.StringUtils;
import com.benemind.adict.core.kingsoft.KingsoftDictHtmlBuilder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class DictFile {
	private static final String TAG = "DictFile";
	File mFile;
	long mSize;
	boolean mCompressed;
	String mDictType;
	
	public DictFile(File file, String type) {
		// TODO Auto-generated constructor stub
		mFile = file;
		mDictType = type;
		String name = file.getName();
		if( name.endsWith(".dz") ){
			mCompressed = true;
		}
		else{
			mCompressed = false;
		}
	}
	
	public String fetchExplanation(MapNode map)
	{
		Log.v(TAG, "fetchExplanation " + map.toJsonStr());
		if( mCompressed ){
			return fetchExplanationCompressed(map);
		}
		return fetchExplanationPlain(map);
	}

	private String fetchExplanationPlain(MapNode map) {
		// TODO Auto-generated method stub
		byte buf[] = new byte[map.getBytesInDict()];
		int read = 0;
		RandomAccessFile f;
		try {
			f = new RandomAccessFile(mFile, "r");
			try {
				f.seek(map.getOffsetInDict());
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
		if( read != buf.length ){
			return null;
		}
		
		return decodeExplanation(buf);
	}

	private String fetchExplanationCompressed(MapNode map) {
		// TODO Auto-generated method stub
		byte buf[] = new byte[map.getBytesInDict()];
		int read = 0;
		DictZipFile dz = new DictZipFile(mFile);
		dz.seek(map.getOffsetInDict());
		try {
			read = dz.read(buf, buf.length);
			dz.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.v(TAG, "read "+read+"bytes to buf["+buf.length+"]");
		return decodeExplanation(buf);
	}

	private String decodeExplanation(byte[] buf) {
		// TODO Auto-generated method stub
		if( mDictType.contains("y") ){
			return parseDictDataMultiStr(buf);
		}
		if( mDictType.contains("t") ){
			return parseDictDataMultiStr(buf);
		}
		if (mDictType.contains("m") ){
			return parseDictDataPlainText(buf);
		}

		if (mDictType.contains("x") )
		{
			return parseDictDataPlainText(buf);
		}

		if (mDictType.contains("n") )
		{
			return parseDictDataN(buf);
		}

		if (mDictType.contains("k") )
		{
			return  parseDictDataKingsoft(buf);
		}
		
		if (mDictType.contains("h") )
		{
			return  parseDictDataHtml(buf);
		}
		return null;
	}

	private String parseDictDataHtml(byte[] buf) {
		// TODO Auto-generated method stub
		String s = new String(buf);
        String path = mFile.getParent()	+File.separator+"res"+File.separator;

        Document doc = Jsoup.parse(s);
		Elements imgs = doc.getElementsByTag("img");
		Element e;
		for(int i=0; i<imgs.size(); i++){
			e = imgs.get(i);
			String link = e.attr("src").trim().toLowerCase();
			if( link.endsWith(".bmp") ){
				Bitmap bmp = BitmapFactory.decodeFile(path+link);
    			link = path+i+".jpg";
    			if( bmp != null ){
    				saveJPGE(bmp, link);
    			}
    			link = "file://"+link;
			}
			else{
				link =  "file://"+path+link;
			}
			e.attr("src", link);
		}
		
		
		Elements chars = doc.getElementsByTag("charset");
		for(int i=0; i<chars.size(); i++){
			e = chars.get(i);
			String txt = e.text();
			int val = 0;
			for(int j=0; j<txt.length();j++){
				char ch = txt.charAt(j);
				if( ch >='0' && ch<='9' ){
					val = val*16 + ch - '0';
				}
				if( ch >='a' && ch <='f' ){
					val = val*16 + ch - 'a' + 10;
				}
				if( ch >='A' && ch <='F' ){
					val = val*16 + ch - 'A' + 10;
				}
			}
			TextNode node = new TextNode(String.valueOf((char)val), null);
			e.replaceWith(node);
		}
		s=  doc.outerHtml();
		return s;
	}
	
	
	/**  
	* ±£´æÍ¼Æ¬ÎªJPEG  
	*   
	* @param bitmap  
	* @param path  
	*/  
	public static void saveJPGE(Bitmap bitmap, String path) {   
		File file = new File(path);   
		try {   
			FileOutputStream out = new FileOutputStream(file);   
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)) {   
				out.flush();   
				out.close();   
			}   
		} catch (FileNotFoundException e) {   
			e.printStackTrace();   
		} catch (IOException e) {   
			e.printStackTrace();   
		}   
	}   

	private String parseDictDataKingsoft(byte[] buf) {
		// TODO Auto-generated method stub
		String s = new String(buf);
		Log.v(TAG, "decodeExplanation raw:"+s);
		return KingsoftDictHtmlBuilder.buildDictSearchContent(s);
	}
	
	/* WordNet 3.0
	 * Sample: <type>n</type><wordgroup><word>fine</word><word>mulct</word><word>amercement</word></wordgroup><gloss>money extracted as a penalty</gloss>
	 * <type>: "n",Noun; "v",Verb; "a",Adjective; "s",Adjective satellite; "r",Adverb
	 */
	private String parseDictDataN(byte[] buf) {
		// TODO Auto-generated method stub
		String s = new String(buf);
		Log.v(TAG, "decodeExplanation raw:"+s);
		return s;
	}


	private String parseDictDataPlainText(byte[] buf) {
		String s = new String(buf);
		Log.v(TAG, "decodeExplanation raw:"+s);
		return StringUtils.htmEncode(s);
	}

	// Chinese YinBiao or Japanese KANA, replace '\0' with '\n'
	private String parseDictDataMultiStr(byte[] buf) {
		// TODO Auto-generated method stub
		int startAt =0;
		StringBuilder b = new StringBuilder();
		int pos = 0;
		for(pos=0; pos<buf.length; pos++){
			if( buf[pos]== 0 ){
				String s = new String(buf, startAt, (pos-startAt) );
				b.append(s);
				b.append("\n");
				startAt = pos+1;
			}
		}
		if( startAt < pos){
			String s = new String(buf, startAt, (pos-startAt) );
			b.append(s);
		}
		return StringUtils.htmEncode(b.toString());
	}

}
