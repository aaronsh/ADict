package com.benemind.httpd;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;


/**
 * HTTP response.
 * Return one of these from serve().
 */
public class TinyHttpResponse
{
	private static final String CHARSET_PREFIX = "charset=";
	/**
	 * Default constructor: response = HTTP_OK, data = mime = 'null'
	 */
	public TinyHttpResponse()
	{
		this.status = TinyHttpServer.HTTP_OK;
	}

	/**
	 * Basic constructor.
	 */
	public TinyHttpResponse( String status, String mimeType, InputStream data )
	{
		this.status = status;
		if( mimeType == null){
			mimeType = TinyHttpServer.MIME_PLAINTEXT;
		}
		if( !mimeType.contains(CHARSET_PREFIX) ){
			mimeType = mimeType + "; charset=utf-8";
		}
		addHeader("Content-Type", mimeType);
		this.data = data;
	}

	/**
	 * Convenience method that makes an InputStream out of
	 * given text.
	 */
	public TinyHttpResponse( String status, String mimeType, String txt )
	{
		this.status = status;
		
		if( mimeType == null){
			mimeType = TinyHttpServer.MIME_PLAINTEXT;
		}
		if( !mimeType.contains(CHARSET_PREFIX) ){
			mimeType = mimeType + "; charset=utf-8";
		}
		addHeader("Content-Type", mimeType);
		
		String charset = "UTF-8";
		if( mimeType.contains(CHARSET_PREFIX) ){
			charset = mimeType.substring( mimeType.indexOf(CHARSET_PREFIX) + CHARSET_PREFIX.length() ).trim();
		}
		this.data = new ByteArrayInputStream( txt.getBytes(Charset.forName(charset)));
	}

	/**
	 * Adds given line to the header.
	 */
	public void addHeader( String name, String value )
	{
		header.put( name, value );
	}

	/**
	 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
	 */
	public String status;

	/**
	 * MIME type of content, e.g. "text/html"
	 */
//	public String mimeType;

	/**
	 * Data of the response, may be null.
	 */
	public InputStream data;

	/**
	 * Headers for the HTTP response. Use addHeader()
	 * to add lines.
	 */
	public Properties header = new Properties();
}
