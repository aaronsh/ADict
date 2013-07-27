package com.benemind.httpd;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import com.benemind.httpd.HttpSocketReader.HttpSocketReaderException;

import android.util.Log;

/**
 * Handles one session, i.e. parses the HTTP request and returns the response.
 */
class TinyHttpSession implements Runnable {
	private static final String TAG = "HttpSession";

	private Socket mySocket;
	private TinyHttpServer mServer;
	private java.text.SimpleDateFormat gmtFrmt;

	public TinyHttpSession(TinyHttpServer server, Socket s) {
		mServer = server;
		mySocket = s;

		gmtFrmt = new java.text.SimpleDateFormat(
				"E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));

		Thread t = new Thread(this);
		t.setName("httpSession");
		t.start();
	}

	public void run() {
		try {
			mySocket.setSoTimeout(500); //one minute
			InputStream inputStream = mySocket.getInputStream();

			HttpSocketReader reader = new HttpSocketReader(mySocket);

			String request = "";//new String(buf, 0, len, Charset.forName("utf-8"));
			request = reader.readRequest();
			// handle request line
			if (request.length() == 0) {
				throw new IOException("empty request");
			}

			// System.out.println( "web req:" + request);
			String requestParts[] = request.split(" ");

			if (requestParts.length != 3) {
				throw new IOException("bad request");
			}
			String method = requestParts[0];
			String uri = requestParts[1];

			// Decode parameters from the URI
			Properties _GET = new Properties();

			int qmi = uri.indexOf('?');
			if (qmi >= 0) {
				decodeParms(uri.substring(qmi + 1), _GET);
				uri = URLDecoder.decode(uri.substring(0, qmi), "utf-8");
			} else {
				uri = URLDecoder.decode(uri, "utf-8");
			}

			// read header

			String sHeader = reader.readHeader();
			String[] headerLines = sHeader.split(HttpSocketReader.NEW_LINE);
			Properties header = new Properties();
			for (String line : headerLines) {
				if (line.trim().length() > 0) {
					int p = line.indexOf(':');
					header.put(line.substring(0, p).trim().toLowerCase(), line
							.substring(p + 1).trim());
				}
			}

			Properties _POST = new Properties();
			Hashtable<String, HttpTempFile> _FILES = new Hashtable<String, HttpTempFile>();

			if (method.equalsIgnoreCase("POST")) {
				String contentType = header.getProperty("content-type");
				String bodySize = header.getProperty("content-length");
				if( bodySize == null ){
					throw new IOException("lack Content-Length in header");
				}
				try{
					int size = Integer.valueOf(bodySize);
					reader.setBodySize(size);
				}
				catch(NumberFormatException e){
					throw new IOException("value of Content-Length written in wrong format");
				}
				
				if (contentType.contains("multipart")) {
					String[] parts = contentType.split("boundary=");
					reader.setBodyBoundary(parts[1]);
					while(true){
						String title = reader.readPartTitle();
						if( title == null || title.length() == 0){
							break;
						}
						//parse title
						contentType = null;
						parts = title.split(HttpSocketReader.NEW_LINE);
						if( parts.length > 1 ){
							for(String line:parts){
								if( line.startsWith("Content-Type:") ){
									contentType = line.substring("Content-Type:".length()).trim();
								}
							}
						}
						String name = parsePartTitleField(title, "name=\"");
						if( title.contains("filename=\"") ){
							String file = parsePartTitleField(title, "filename=\"");
							HttpTempFile tmpFile = reader.saveFilePart(file);
							tmpFile.ContentType = contentType;
							_FILES.put(name, tmpFile);
						}
						else{
							String val = reader.readTextPart();
							_POST.setProperty(URLDecoder.decode(name, "utf-8"), val);
						}
					}
				} else {
					String s = reader.readBody();
					decodeParms(s, _POST);
				}
				
			}

			// Ok, now do the serve()
			TinyHttpResponse r = mServer
					.serve(uri, method, header, _GET, _POST, _FILES);
			if (r == null)
				sendError(TinyHttpServer.HTTP_INTERNALERROR,
						"SERVER INTERNAL ERROR: Serve() returned a null response.");
			else
				sendResponse(r);

			inputStream.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			try {
				sendError(
						TinyHttpServer.HTTP_INTERNALERROR,
						"SERVER INTERNAL ERROR: IOException: "
								+ ioe.getMessage());
			} catch (Throwable t) {
			}
		} catch (HttpSocketReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				sendError(
						TinyHttpServer.HTTP_INTERNALERROR,
						"SERVER INTERNAL ERROR: IOException: "
								+ e.getMessage());
			} catch (Throwable t) {
			}
		}
		finally{
			
		}

		try {
			mySocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String parsePartTitleField(String title, String tag) {
		// TODO Auto-generated method stub
		int pos = title.indexOf(tag);
		if( pos < 0 ){
			return null;
		}
		String s = title.substring(pos + tag.length());
		return s.substring(0, s.indexOf('"'));
	}

	private void decodePostParameters(InputStream is, Properties header,
			Properties PostParam) {
		// TODO Auto-generated method stub
		int size = 0;
		String contentLength = header.getProperty("content-length");
		if (contentLength != null) {
			try {
				size = Integer.parseInt(contentLength);

				String ContentType = header.getProperty("content-type");
				if (ContentType != null) {
					if (ContentType.contains("multipart/form-data;")) {
						Log.e(TAG, "do NOT implement yet!");
						/*
						 * String boundary =ContentType.split("=")[1]; String s=
						 * in.readLine();
						 */
					} else {
						try {
							DataInputStream input = new DataInputStream(is);
							Log.v(TAG, "read line");
							System.out.println("available:" + is.available());
							byte[] buf = new byte[size];

							int bytes = input.read(buf);
							// int bytesRead = is.read(buf);
							String s = new String(buf);
							Log.v(TAG, "read: " + s);
							Log.v(TAG, "bytes:" + bytes);
							// s = in.readLine();
							// Log.v(TAG, s);
							// decodeParms( s.trim(), PostParam );
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Decodes parameters in percent-encoded URI-format ( e.g.
	 * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
	 * Properties.
	 */

	private void decodeParms(String parms, Properties p) {
		if (parms == null)
			return;

		String[] parts = parms.split("&");
		for (String s : parts) {
			if (s.contains("=")) {
				String[] kv = s.split("=");
				if (kv.length == 2) {
					try {
						String key = URLDecoder.decode(kv[0], "utf-8");
						String val = URLDecoder.decode(kv[1], "utf-8");
						p.put(key.trim(), val);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Returns an error message as a HTTP response and throws
	 * InterruptedException to stop furhter request processing.
	 */

	private void sendError(String status, String msg)
			throws InterruptedException {
		TinyHttpResponse resp =  new TinyHttpResponse( status, TinyHttpServer.MIME_PLAINTEXT,
				msg );
		
		sendResponse(resp);
		throw new InterruptedException();
	}

	/**
	 * Sends given response to the socket.
	 */
	private void sendResponse(TinyHttpResponse resp) {
		try {
			if (resp.status == null)
				throw new Error("sendResponse(): Status can't be null.");

			OutputStream out = mySocket.getOutputStream();
			PrintWriter pw = new PrintWriter(out);
			pw.print("HTTP/1.0 " + resp.status + " \r\n");

			Properties header = resp.header;

			InputStream data = resp.data;
			try{
			int len = data.available();
			String s = String.valueOf(len);
			header.setProperty("Content-Length", s);
			}
			catch(IOException e){
				e.printStackTrace();
			}
			Log.v(TAG, header.toString());
			
			if (header.getProperty("Date") == null)
				pw.print("Date: " + gmtFrmt.format(new Date()) + "\r\n");

			if (header != null) {
				Enumeration e = header.keys();
				while (e.hasMoreElements()) {
					String key = (String) e.nextElement();
					String value = header.getProperty(key);
					pw.print(key + ": " + value + "\r\n");
				}
			}

			pw.print("\r\n");
			pw.flush();


			if (data != null) {
				byte[] buff = new byte[2048];
				while (true) {
					int read = data.read(buff, 0, 2048);
					if (read < 0)
						break;
					out.write(buff, 0, read);
				}
			}
			out.flush();
			out.close();
			if (data != null)
				data.close();
		} catch (IOException ioe) {
			// Couldn't write? No can do.
			try {
				mySocket.close();
			} catch (Throwable t) {
			}
		}
	}


}
