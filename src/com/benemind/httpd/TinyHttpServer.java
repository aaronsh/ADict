package com.benemind.httpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import android.util.Log;

public class TinyHttpServer implements Runnable {
	private static final String TAG = "HttpServer";
	private static File mServerRoot = null;
	private LinkedList<TinyHttpRequestHandler> mReqHandlers;

	/**
	 * Some HTTP response status codes
	 */
	public static final String HTTP_OK = "200 OK",
			HTTP_REDIRECT = "301 Moved Permanently",
			HTTP_FORBIDDEN = "403 Forbidden", HTTP_NOTFOUND = "404 Not Found",
			HTTP_BADREQUEST = "400 Bad Request",
			HTTP_INTERNALERROR = "500 Internal Server Error",
			HTTP_NOTIMPLEMENTED = "501 Not Implemented";

	/**
	 * Common mime types for dynamic content
	 */
	public static final String MIME_PLAINTEXT = "text/plain",
			MIME_HTML = "text/html",
			MIME_DEFAULT_BINARY = "application/octet-stream";
	

	private static String[] MimeTypes = { "htm		text/html ",
			"html		text/html ", "txt		text/plain ", "asc		text/plain ",
			"gif		image/gif ", "jpg		image/jpeg ", "jpeg		image/jpeg ",
			"png		image/png ", "mp3		audio/mpeg ", "m3u		audio/mpeg-url ",
			"pdf		application/pdf ", "doc		application/msword ",
			"ogg		application/x-ogg ", "zip		application/octet-stream ",
			"exe		application/octet-stream ",
			"class		application/octet-stream ", "css		text/css ",
			"js			text/javascript " };

	private Thread mServeThread;
	private boolean mServerRunning;
	private int mPort;

	public TinyHttpServer(int port) {
		mPort = port;
		mServeThread = null;
		mServerRunning = false;

		mServerRoot = new File("/mnt/sdcard/adict/html");
		mReqHandlers = new LinkedList<TinyHttpRequestHandler>();
	}

	public void setServerRoot(File root) {
		mServerRoot = root;
	}
	public static File getServerRoot(){
		return mServerRoot;
	}

	public void addRequestHandler(TinyHttpRequestHandler handler) {
		if (!mReqHandlers.contains(handler)) {
			synchronized(this){
				mReqHandlers.add(handler);
			}
		}
	}

	public void removeRequestHandler(TinyHttpRequestHandler handler) {
		synchronized(this){
			mReqHandlers.remove(handler);
		}
	}

	public void startServer() {
		mServeThread = new Thread(this);
		mServeThread.start();
	}

	public void stopServer() {
		mServerRunning = false;
		try {
			mServeThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mServeThread = null;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		mServerRunning = true;
		ServerSocket serversocket = null;
		try {
			while (true) {
				try {
					serversocket = new ServerSocket(mPort);
					serversocket.setReuseAddress(true);
					serversocket.setSoTimeout(200);
					break;
				} catch (java.net.BindException bindException) {
					bindException.printStackTrace();
					mPort++;
				}
			}
			Log.v(TAG, "port:" + mPort);

			while (mServerRunning) {
				Socket connection = null;
				try {
					connection = serversocket.accept();
					Log.v("server", "accept");
					TinyHttpSession session = new TinyHttpSession(this,
							connection);
				} catch (IOException e) {
					// e.printStackTrace();
					continue;
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("httpD terminated");
	}

	/**
	 * Override this to customize the server.
	 * <p>
	 * 
	 * (By default, this delegates to serveFile() and allows directory listing.)
	 * @param _FILES 
	 * 
	 * @parm uri Percent-decoded URI without parameters, for example
	 *       "/index.cgi"
	 * @parm method "GET", "POST" etc.
	 * @parm parms Parsed, percent decoded parameters from URI and, in case of
	 *       POST, data.
	 * @parm header Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 */
	protected TinyHttpResponse serve(String uri, String method,
			Properties header, Properties _GET, Properties _POST, Hashtable<String, HttpTempFile> _FILES) {
//		System.out.println(method + " '" + uri + "' ");
/*
		Enumeration<?> e = header.propertyNames();
		while (e.hasMoreElements()) {
			String value = (String) e.nextElement();
			System.out.println("  HDR: '" + value + "' = '"
					+ header.getProperty(value) + "'");
		}
		e = _GET.propertyNames();
		while (e.hasMoreElements()) {
			String value = (String) e.nextElement();
			System.out.println("  PRM: '" + value + "' = '"
					+ _GET.getProperty(value) + "'");
		}
*/
		TinyHttpResponse response = null;
		TinyHttpRequestHandler CustHandler = null;
		synchronized(this){
			Iterator<TinyHttpRequestHandler> it = mReqHandlers.iterator();
			while (it.hasNext()) {
				TinyHttpRequestHandler handler = it.next();
				if( handler.ready(uri, method, header, _GET, _POST, _FILES) ){
					CustHandler = handler;
					break;
				}
			}
		}
		if( CustHandler != null ){
			response = CustHandler.serve(uri, method, header, _GET, _POST, _FILES);
		}
		if (response != null) {
			return response;
		}

		File file = new File(mServerRoot, uri);
		if (file.isDirectory()) {
			return serveFolder(file, uri, header, true);
		}
		return serveFile(file, header, true);
	}



	public static TinyHttpResponse serveFile(File file, Properties header, boolean b) {
		// TODO Auto-generated method stub
		try {
			// Get MIME type from file name extension, if possible
			String mime = null;
			String ext = getFileExtName(file);
			mime = getMimeType(ext);

			// Support (simple) skipping:
			long startFrom = 0;
			String range = header.getProperty("Range");
			if (range != null) {
				if (range.startsWith("bytes=")) {
					range = range.substring("bytes=".length());
					int minus = range.indexOf('-');
					if (minus > 0)
						range = range.substring(0, minus);
					try {
						startFrom = Long.parseLong(range);
					} catch (NumberFormatException nfe) {
					}
				}
			}

			FileInputStream fis = new FileInputStream(file);
			fis.skip(startFrom);
			TinyHttpResponse r = new TinyHttpResponse(HTTP_OK, mime, fis);
			r.addHeader("Content-length", "" + (file.length() - startFrom));
			r.addHeader("Content-range", "" + startFrom + "-"
					+ (file.length() - 1) + "/" + file.length());
			return r;
		} catch (IOException ioe) {
			return new TinyHttpResponse(HTTP_FORBIDDEN, MIME_PLAINTEXT,
					"FORBIDDEN: Reading file failed.");
		}
	}

	/**
	 * Serves file from homeDir and its' subdirectories (only). Uses only URI,
	 * ignores all headers and HTTP parameters.
	 */
	public TinyHttpResponse serveFolder(File file, String uri,
			Properties header, boolean allowDirectoryListing) {
		// Make sure we won't die of an exception later
		if (allowDirectoryListing) {

			Hashtable styles = new Hashtable();
			Hashtable style;

			style = new Hashtable();
			style.put("background", "#181818");
			style.put("color", "#dddddd");
			style.put("padding-top", "10px");
			styles.put(".localhttpd", style);

			style = new Hashtable();
			style.put("color", "#ff3333");
			styles.put(".localhttpd a", style);

			style = new Hashtable();
			style.put("color", "#ff6666");
			styles.put(".localhttpd a:hover", style);

			style = new Hashtable();
			style.put("margin", "0px 0px 5px 0px");
			style.put("padding", "0px");
			style.put("font-size", "16px");
			styles.put(".localhttpd h1", style);

			String header_block = "<head><style>";
			for (Enumeration e = styles.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				Hashtable item = (Hashtable) styles.get(key);
				header_block += key + "{\n";
				for (Enumeration e2 = item.keys(); e2.hasMoreElements();) {
					String skey = (String) e2.nextElement();
					header_block += skey + ":" + item.get(skey) + ";\n";
				}
				header_block += key + "}\n";
			}
			header_block += "</style></head>";

			String[] files = file.list();
			String msg = "<html>" + header_block
					+ "<body class=\"localhttpd\"><h1>Directory " + uri
					+ "</h1>";

			if (uri.length() > 1) {
				String u = uri.substring(0, uri.length() - 1);
				int slash = u.lastIndexOf('/');
				if (slash >= 0 && slash < u.length())
					msg += "<b><a href=\"" + uri.substring(0, slash + 1)
							+ "\">..</a></b><br/>";
			}

			for (int i = 0; i < files.length; ++i) {
				File curFile = new File(file, files[i]);
				boolean dir = curFile.isDirectory();
				if (dir) {
					msg += "<b>";
					files[i] += "/";
				}

				// Show file size
				String extra = "";
				if (curFile.isFile()) {
					extra = "target='_blank'";
				}

				msg += "<a " + extra + " href=\""
						+ URLEncoder.encode(uri + files[i]) + "\">" + files[i]
						+ "</a>";

				// Show file size
				if (curFile.isFile()) {
					long len = curFile.length();
					msg += " &nbsp;<font size=2>(";
					if (len < 1024)
						msg += curFile.length() + " bytes";
					else if (len < 1024 * 1024)
						msg += curFile.length() / 1024 + "."
								+ (curFile.length() % 1024 / 10 % 100) + " KB";
					else
						msg += curFile.length() / (1024 * 1024) + "."
								+ curFile.length() % (1024 * 1024) / 10 % 100
								+ " MB";

					msg += ")</font>";
				}
				msg += "<br/>";
				if (dir)
					msg += "</b>";
			}
			return new TinyHttpResponse(HTTP_OK, MIME_HTML, msg);

		} else {
			return new TinyHttpResponse(HTTP_FORBIDDEN, MIME_PLAINTEXT,
					"FORBIDDEN: No directory listing.");
		}

	}

	private static String getFileExtName(File file) {
		// TODO Auto-generated method stub
		String name = file.getName();
		int dot = name.lastIndexOf('.');
		if (dot >= 0) {
			name = name.substring(dot + 1).toLowerCase();
			return name;
		}
		return null;
	}

	private static String getMimeType(String extFileName) {
		if (extFileName == null) {
			return MIME_DEFAULT_BINARY;
		}

		for (String s : MimeTypes) {
			if (s.startsWith(extFileName)) {
				s = s.substring(extFileName.length());
				return s.trim();
			}
		}
		return MIME_DEFAULT_BINARY;
	}
}
