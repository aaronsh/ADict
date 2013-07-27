package com.benemind.httpd;

import java.util.Hashtable;
import java.util.Properties;

public interface TinyHttpRequestHandler {
	/**
	 * Override this to customize the server.<p>
	 *
	 * (By default, this delegates to serveFile() and allows directory listing.)
	 * @param _FILES 
	 *
	 * @parm uri	Percent-decoded URI without parameters, for example "/index.cgi"
	 * @parm method	"GET", "POST" etc.
	 * @parm parms	Parsed, percent decoded parameters from URI and, in case of POST, data.
	 * @parm header	Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 */
	public TinyHttpResponse serve( String uri, String method, Properties header, Properties _GET,  Properties _POST, Hashtable<String, HttpTempFile> _FILES);
	public boolean ready( String uri, String method, Properties header, Properties _GET,  Properties _POST, Hashtable<String, HttpTempFile> _FILES);
}
