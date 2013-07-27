package com.benemind.httpd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;


public class HttpSocketReader {
	private byte[] buffer;
	private InputStream in;
	private int bufferSize;
	private int dataLen;
	private int BodySize;
	private String MultiPartBoundary;

	public static final String BLANK_LINE = "\r\n\r\n";
	public static final String NEW_LINE = "\r\n";
	public static final String SPACE = " ";
	private static final int UNSET_LEN = -1;
	
	HttpSocketReader(Socket skt) throws IOException {
		this.in = skt.getInputStream();
		bufferSize = 102;
		buffer = new byte[bufferSize];
		dataLen = 0;
		BodySize = UNSET_LEN;
	}

	int read(OutputStream out, String endFlag) throws IOException {
		int bytes;
		int BytesToRead;
		IOException ex = null;
		
		if( out instanceof ByteArrayOutputStream){
			((ByteArrayOutputStream)out).reset();
		}
		Charset utf8Coding = Charset.forName("utf-8");
		byte[] endBytes = endFlag.getBytes(utf8Coding);
		int EndFlagPos = IndexOf(endBytes);
		if( EndFlagPos >= 0 ){
			//end flag in buffer
			if( EndFlagPos > 0 ){
				out.write(buffer, 0, EndFlagPos);
			}
			removeFromBuffer(0, EndFlagPos + endBytes.length);
		}
		else{
			//end flag not in buffer
			boolean inLoop = true;
			while(inLoop){
				BytesToRead = bufferSize - dataLen;
				if( BodySize != UNSET_LEN ){
					BytesToRead = BytesToRead < BodySize ? BytesToRead:BodySize;
				}
				bytes = 0;
				try {
					bytes = in.read(buffer, dataLen, BytesToRead);
				} catch (java.net.SocketTimeoutException e) {
					e.printStackTrace();
					ex = e;
					inLoop = false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					ex = e;
					inLoop = false;
				}
				if (bytes == -1) {
					//reaches the end. But it seems that this case never happens because the stream is socket
					bytes = 0;
					inLoop = false;
				}
				if (bytes > 0) {
					dataLen = dataLen + bytes;
					if( BodySize != UNSET_LEN ){
						BodySize = BodySize - bytes;
					}
				}
				if( BodySize == 0 ){
					inLoop = false;
				}
				EndFlagPos = IndexOf(endBytes);
				if (EndFlagPos >= 0) {
					if( EndFlagPos > 0 ){
						out.write(buffer, 0, EndFlagPos);
					}
					removeFromBuffer(0, EndFlagPos + endBytes.length);
					break;
				}
				else{
					if( dataLen > endBytes.length ){
						out.write(buffer, 0, dataLen - endBytes.length);
						removeFromBuffer(0, dataLen - endBytes.length); //clear for next read loop
					}
				}

			}
			if( ex != null ){
				throw ex;
			}
		}

		return 0;
	}

	private void removeFromBuffer(int startPos, int size) {
		// TODO Auto-generated method stub
		for (int i = startPos, j = startPos + size; j < dataLen; i++, j++) {
			buffer[i] = buffer[j];
		}
		dataLen = dataLen - size;
	}

	int IndexOf(byte[] EndBytes) {
		int len = EndBytes.length;
		if( len == 0 ){
			return -1;
		}
		if (dataLen < len ) {
			return -1;
		}
		int endPos = dataLen - len;
		for (int i = 0; i <= endPos; i++) {
			boolean match = true;
			for (int j = i, k = 0; k < len; j++, k++) {
				if (buffer[j] != EndBytes[k]) {
					match = false;
					break;
				}
			}
			if (match) {
				return i;
			}
		}
		return -1;
	}
	
	

	String readRequest() throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		read(out, NEW_LINE);
		System.out.println("readRequest:"+out.toString("UTF-8"));

		return out.toString("UTF-8");
	}
	
	String readHeader() throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		read(out, BLANK_LINE);
		System.out.println("readHeader:"+out.toString("UTF-8"));

		return out.toString("UTF-8");
	}
	
	void setBodySize(int size){
		if( size > dataLen ){
			BodySize = size - dataLen;
		}
		else{
			BodySize = 0;
		}
	}
	
	void setBodyBoundary(String boundary){
		MultiPartBoundary = "--"+boundary;
	}
	
	String readBody() throws HttpSocketReaderException, IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		read(out, "");
		System.out.println("readBody:"+out.toString("UTF-8"));

		return out.toString("UTF-8");
	}
	
	String readTextPart() throws HttpSocketReaderException, IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		read(out, NEW_LINE+MultiPartBoundary);
		System.out.println("readTextPart:"+out.toString("UTF-8"));
		return out.toString("UTF-8");
	}
	
	HttpTempFile saveFilePart(String name) throws HttpSocketReaderException{
		String fileName = String.format("%1d.tmp", System.currentTimeMillis());
		File file = new File( fileName );
		try {
			FileOutputStream out = new FileOutputStream(file);
			read(out, NEW_LINE+MultiPartBoundary);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		HttpTempFile tmpFile = new HttpTempFile();
		tmpFile.file = file;
		tmpFile.name = name;
		return tmpFile;
	}
	
	String readPartTitle() throws HttpSocketReaderException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			//read(out, MultiPartBoundary);
			read(out, BLANK_LINE);
			System.out.println("title:"+out.toString("UTF-8"));
			return out.toString("UTF-8").trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	class HttpSocketReaderException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 4193263691592286323L;

		public HttpSocketReaderException(String msg){
			super(msg);
		}
	}
}
