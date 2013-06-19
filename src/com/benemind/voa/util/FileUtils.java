package com.benemind.voa.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {
	private static final String APP_PATH= "voa";
	private static File mAppDir = null;
	private static File mDictDir = null;
	
	public static File getAppDir(){
		if( mAppDir != null  ){
			return mAppDir;
		}
		
		String status = Environment.getExternalStorageState();
		if (!status.equals(Environment.MEDIA_MOUNTED)) {// ≈–∂œ «∑Ò”–SDø®
			mAppDir = null;
			return null;
		}

		File dir = new File(
				Environment.getExternalStorageDirectory(), APP_PATH);
		if( !dir.exists() ){
			dir.mkdirs();
		}
		if( !dir.isDirectory() ){
			dir.delete();
			dir.mkdirs();
		}
		mAppDir = dir;
		return mAppDir;
	}
	
	/*
	 * name: do not include App Path 
	 */
	public static File getAppFile(String name)
	{
		getAppDir();
		if( mAppDir == null ){
			return null;
		}
		
		File f = new File(mAppDir, name);
		return f;
	}


	public static File createAppFile(String name) throws IOException {
		File f = getAppFile(name);
		if( f != null ){
			f.createNewFile();
		}
		return f;
	}

	public static boolean isFileExist(String name) {
		File f = getAppFile(name);
		if( f == null ){
			return false;
		}
		return f.exists();
	}

	public static File getDictDir()
	{
		if( mDictDir != null ){
			return mDictDir;
		}
		
		File appDir = getAppDir();
		if( appDir == null ){
			return null;
		}
		File dictDir = new File(appDir, "dict");
		if( !dictDir.exists() ){
			dictDir.mkdirs();
		}
		if( !dictDir.isDirectory() ){
			dictDir.delete();
			dictDir.mkdirs();
		}

		mDictDir = dictDir;
		return dictDir;
	}

	public static File getDictFile(String name)
	{
		getDictDir();
		if( mDictDir == null ){
			return null;
		}
		
		File f = new File(mDictDir, name);
		return f;
	}
	
	public static long getFileSize(String fileName){
		File f = getAppFile(fileName);
		if( f == null ){
			return 0;
		}
		return f.length();
	}

	public static void writeToLogFile(String tag, String log){
		File logFile = getAppFile("log.txt"); 
		if( logFile == null )
			return;
		if( !logFile.exists() ){
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(logFile, true);
			fos.write(getLogDate(System.currentTimeMillis()).getBytes());
			fos.write("\t:".getBytes());
			fos.write(tag.getBytes());
			fos.write("\t:".getBytes());
			fos.write(log.getBytes());
			fos.write("\r\n".getBytes());
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private static String getLogDate(long timestamp) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-d HH:mm:ss");
		Date d = new Date();
		d.setTime(timestamp);
		String ts = sdf.format(d);
		return ts;
	}
}
