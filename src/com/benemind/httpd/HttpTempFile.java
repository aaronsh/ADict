package com.benemind.httpd;

import java.io.File;

public class HttpTempFile {
	String name;
	File file;
	public String ContentType;

	protected void finalize() throws Throwable {  
		super.finalize();  
	}  

}
