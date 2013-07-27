package com.benemind.adict;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.benemind.adict.R;
import com.benemind.util.DialogUtils;
import com.benemind.util.FileUtils;
import com.benemind.adict.core.DictConsts;
import com.benemind.adict.core.DictEng;
import com.benemind.adict.core.NotFoundDictException;
import com.benemind.httpd.HttpTempFile;
import com.benemind.httpd.TinyHttpRequestHandler;
import com.benemind.httpd.TinyHttpResponse;
import com.benemind.httpd.TinyHttpServer;
//import com.benemind.httpd.HttpServer;

public class XDictMainActivity extends SherlockActivity implements
		OnItemClickListener, TextWatcher, OnClickListener,
		OnEditorActionListener, TinyHttpRequestHandler {
	private static final String TAG = "MainActivity";
	protected static final int MSG_LIST_WORDS = 0;
	protected static final int MSG_REDIRECT_WORD = 1;

	private static final int MENU_ID_DICT_MGR = 0;

	private static final int ACTION_LIST_WORDS = 0;
	private static final int ACTION_SEARCH_WORD = 1;

	enum DictAction {
		LIST_WORDS, SEARCH_WORD
	}

	AutoCompleteTextView mSearchInput;
	WebView mSearchResult;
	// ArrayAdapter<String> mAdapter;
	DropDownAdapter mAdapter;
	DictEng mDictEng;
	private DictAction mDictAction;

	boolean mListWordTaskRunning;
	String mPenddingListWordTask;

	private ProgressDialog mWaitingDlg;
	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LIST_WORDS:
				listWord((String) msg.obj);
				break;
			case MSG_REDIRECT_WORD:
				setSearchInputText((String) msg.obj);
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	};

	private class DictWebViewClient extends WebViewClient {
		private final static String BWORD_URL = "bword://";

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.v(TAG, "shouldOverrideUrlLoading:" + url);
			if (url.startsWith(BWORD_URL)) {
				String word = url.substring(BWORD_URL.length());
				Message msg = mHandler.obtainMessage(MSG_REDIRECT_WORD, word);
				mHandler.sendMessage(msg);
				return true;
			}
			return false;
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			
			if( !url.startsWith("http://192.168") ){
//				downloadWebResource(url);
				try {
					url = "http://192.168.0.113/ajax?"+ URLEncoder.encode(url, "utf-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			Log.v(TAG, "onLoadResource " + url);
			super.onLoadResource(view, url);
		}

		private void downloadWebResource(String url) {
			// TODO Auto-generated method stub
			 /*声明网址字符串*/  
	        String uriAPI = "http://fanyi.youdao.com/openapi.do?keyfrom=N3verL4nd&key=208118276&type=data&doctype=json&version=1.1&q=dog";   
	        /*建立HTTP Get联机*/  
	        HttpGet httpRequest = new HttpGet(uriAPI);   
	        try   
	        {   
	          /*发出HTTP request*/  
	          HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);   
	          /*若状态码为200 ok*/  
	          if(httpResponse.getStatusLine().getStatusCode() == 200)    
	          {   
	            /*取出响应字符串*/  
	            String strResult = EntityUtils.toString(httpResponse.getEntity());  
	            Log.v(TAG, "web return:"+strResult); 
	          }   
	          else   
	          {   
	   
	          }   
	        }   
	        catch (ClientProtocolException e)   
	        {    
	          e.printStackTrace();   
	        }   
	        catch (IOException e)   
	        {    
	          e.printStackTrace();   
	        }   
	        catch (Exception e)   
	        {    
	          e.printStackTrace();    
	        }
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			Log.v(TAG, "onReceivedError " + errorCode + " " + description
					+ " url:" + failingUrl);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.e(TAG, "onPageFinished");
			mSearchInput.dismissDropDown();
		}
	}

	private class ChromeClient extends WebChromeClient {
		@Override
		public boolean onConsoleMessage(ConsoleMessage msg) {
			Log.d(TAG, msg.lineNumber() + " " + msg.message());
			return super.onConsoleMessage(msg);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuItem refreshItem = menu.add(0, MENU_ID_DICT_MGR, Menu.NONE,
				R.string.menu_refresh);
		refreshItem.setIcon(R.drawable.ic_menu_books);
		refreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(TAG,
				"onOptionsItemSelected:" + item.getTitle() + item.getItemId());
		switch (item.getItemId()) {
		case MENU_ID_DICT_MGR:
			Intent intent = new Intent(this, DictMgrActivity.class);
			this.startActivityForResult(intent, 0);
			break;
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// setTheme(SampleList.THEME); //Used for theme switching in samples

		super.onCreate(savedInstanceState);
		setContentView(R.layout.xdict_main);

		mSearchInput = (AutoCompleteTextView) findViewById(R.id.word_for_search);
		mSearchInput.addTextChangedListener(this);
		mSearchInput.setOnItemClickListener(this);
		mAdapter = new DropDownAdapter(this);
		mSearchInput.setAdapter(mAdapter);
		mSearchInput.setOnEditorActionListener(this);

		View v = findViewById(R.id.search_in_dict);
		v.setOnClickListener(this);

		mSearchResult = (WebView) findViewById(R.id.search_result);
		WebSettings webSettings = mSearchResult.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

		WebViewClient webClient = new DictWebViewClient();
		mSearchResult.setWebViewClient(webClient);
		ChromeClient chromeClient = new ChromeClient();
		mSearchResult.setWebChromeClient(chromeClient);
		mSearchResult.reload();

		mListWordTaskRunning = false;
		mPenddingListWordTask = null;

		mWaitingDlg = null;
		mDictAction = DictAction.LIST_WORDS;

		mDictEng = DictEng.getInstance(this);
		mDictEng.reloadConfig();
		loadDicts();
		
//		HttpServer server = new HttpServer(30111);
//		server.startServer();
		
		TinyHttpServer server = new TinyHttpServer(8080);
        server.setServerRoot(mDictEng.getHtmlFolder());
        server.addRequestHandler(this);
        server.startServer();
	}

	private void loadDicts() {
		// TODO Auto-generated method stub
		int DictCount = mDictEng.estimateDictionaryCount();
		if (DictCount < 10) {
			try {
				mDictEng.loadDicts();

				Intent intent = getIntent();
				String word = intent.getStringExtra("word");

				if (word != null) {
					intent.putExtra("word", (String) null);
					setSearchInputText(word);
				}
			} catch (NotFoundDictException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mSearchResult.loadUrl("file:///android_asset/dict_no_dict_"
						+ getLanguageEnv() + ".html");

				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(
						mSearchInput.getWindowToken(),
						InputMethodManager.HIDE_IMPLICIT_ONLY);
			}
		} else {
			loadDictsTask loadTask = new loadDictsTask();
			loadTask.execute();

			// show dilag
			ProgressDialog dlg = new ProgressDialog(this);
			dlg.setTitle("");
			dlg.setMessage(getText(R.string.loading_dictionaries));
			dlg.setIndeterminate(false);
			dlg.setCancelable(true);
			dlg.setCancelable(false);
			dlg.show();
			mWaitingDlg = dlg;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private String getLanguageEnv() {
		Locale l = Locale.getDefault();
		String language = l.getLanguage();
		String country = l.getCountry().toLowerCase();
		if ("zh".equals(language)) {
			if ("cn".equals(country)) {
				language = "zh-CN";
			} else if ("tw".equals(country)) {
				language = "zh-TW";
			} else {
				language = "zh-TW";
			}
		} else if ("pt".equals(language)) {
			if ("br".equals(country)) {
				language = "pt-BR";
			} else if ("pt".equals(country)) {
				language = "pt-PT";
			}
		}
		return language;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Log.v(TAG, "onItemClick " + mAdapter.getItem(arg2));
		if (mHandler.hasMessages(MSG_LIST_WORDS)) { // cancel listWord task
													// because afterTextChanged
													// was called before the
													// calling of onItemClick
			mHandler.removeMessages(MSG_LIST_WORDS);
		}
		Object obj = mAdapter.getItem(arg2);
		String text = obj.toString();
		SearchWordTask task = new SearchWordTask();
		task.execute(text);
		mSearchInput.dismissDropDown();
		mSearchInput.setSelection(text.length());
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		Log.v(TAG, "afterTextChanged " + arg0.toString());
		String text = arg0.toString();
		if (text.length() == 0) {
			mPenddingListWordTask = null;

			// load history
		} else {
			if (mDictAction == DictAction.SEARCH_WORD) {
				Log.v(TAG, "mDictAction is DictAction.SEARCH_WORD");
				SearchWordTask task = new SearchWordTask();
				task.execute(text);
				mDictAction = DictAction.LIST_WORDS;
			} else {
				Log.v(TAG, "mDictAction is DictAction.LIST_WORDS");
				Message msg = mHandler.obtainMessage(MSG_LIST_WORDS, text);
				mHandler.sendMessageDelayed(msg, 100);
			}
		}

	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	private void listWord(String keyWord) {
		if (!mListWordTaskRunning) {
			ListWordsTask task = new ListWordsTask();
			// task.doInBackground(keyWord);
			task.execute(keyWord);
		} else {
			mPenddingListWordTask = keyWord;
		}
		Log.v(TAG, "listWord end");
	}

	private void setSearchInputText(String text) {
		mDictAction = DictAction.SEARCH_WORD;
		mSearchInput.setText(text);
		mSearchInput.setSelection(text.length());
		mSearchInput.dismissDropDown();
	}

	class ListWordsTask extends AsyncTask<String, Void, ArrayList<String>> {

		public ListWordsTask() {
			mListWordTaskRunning = true;
		}

		@Override
		protected ArrayList<String> doInBackground(String... keywords) {
			return mDictEng.listWords(keywords[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<String> strWordsList) {
			Log.v(TAG, "onPostExecute");
			Log.v(TAG, "list size:" + strWordsList.size() + " pendding:"
					+ mPenddingListWordTask + " " + strWordsList);
			mListWordTaskRunning = false;
			if (mPenddingListWordTask != null) {
				listWord(mPenddingListWordTask);
				mPenddingListWordTask = null;
			} else {
				// update UI
				mAdapter.setDateSource(strWordsList);
			}
		}
	}

	class SearchWordTask extends AsyncTask<String, Void, String> {

		public SearchWordTask() {
		}

		@Override
		protected String doInBackground(String... keywords) {
			return mDictEng.lookupWord(keywords[0]);
		}

		@Override
		protected void onPostExecute(String htmlContent) {
			showWordSearchResult(htmlContent);
		}
	}

	class loadDictsTask extends AsyncTask<Void, Void, Void> {

		public loadDictsTask() {
		}

		@Override
		protected Void doInBackground(Void... args) {
			try {
				mDictEng.loadDicts();
			} catch (NotFoundDictException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			if (mWaitingDlg != null && mWaitingDlg.isShowing()) {
				mWaitingDlg.dismiss();
			}
			mWaitingDlg = null;

			if (mDictEng.getActiveDictCount() > 0) {
				Intent intent = getIntent();
				String word = intent.getStringExtra("word");
				if (word != null) {
					intent.putExtra("word", (String) null);
					setSearchInputText(word);
				}
			} else {
				mSearchResult.loadUrl("file:///android_asset/dict_no_dict_"
						+ getLanguageEnv() + ".html");

				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(
						mSearchInput.getWindowToken(),
						InputMethodManager.HIDE_IMPLICIT_ONLY);
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String text = mSearchInput.getEditableText().toString();
		SearchWordTask task = new SearchWordTask();
		task.execute(text);
	}

	private void showWordSearchResult(String htmlContent) {
		// TODO Auto-generated method stub
		DictEng eng = DictEng.getInstance(this);
		File html = eng.getHtmlFolder();
		StringBuilder b = new StringBuilder();
		b.append("file://");
		b.append(html.getPath());
		b.append(File.separator);
		// b.append(html.getName());
		Log.v(TAG, "baseUrl:" + b);
		mSearchResult.loadUrl("http://127.0.0.1:8080/index.html");
//		mSearchResult.loadDataWithBaseURL("http://localhost:30111/", htmlContent,
//				"text/html", "utf-8", null);
		mSearchResult.scrollTo(0, 0);

		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(
				mSearchInput.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	// 写数据到SD中的文件
	public void writeFileSdcardFile(String write_str) {
		try {

			FileOutputStream fout = new FileOutputStream("/mnt/sdcard/log.html");
			byte[] bytes = write_str.getBytes();

			fout.write(bytes);
			fout.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		switch (actionId) {
		case EditorInfo.IME_ACTION_UNSPECIFIED:
		case EditorInfo.IME_ACTION_SEARCH:
			// mSearchInput.dismissDropDown();
			onClick(v);
			return true;
		default:
			break;
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult " + requestCode + " " + resultCode);
		Intent intent = getIntent();
		String word = mSearchInput.getText().toString();
		Log.d(TAG, word);
		if (word.length() > 0) {
			intent.putExtra("word", word);
		}
		loadDicts();
	}

	@Override
	public TinyHttpResponse serve(String uri, String method, Properties header,
			Properties _GET, Properties _POST,
			Hashtable<String, HttpTempFile> _FILES) {
		// TODO Auto-generated method stub
		if( uri.startsWith("/ajax") ){
			URL url;
			TinyHttpResponse resp = null;
			try {
				url = new URL(_GET.getProperty("url"));
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
			    //httpURLConnection.setDoOutput(true);// 打开写入属性  
			    conn.setDoInput(true);// 打开读取属性  
			    conn.setRequestMethod("GET");// 设置提交方法  
			    conn.setConnectTimeout(50000);// 连接超时时间  
			    conn.setReadTimeout(50000);  
			    
//			    httpURLConnection.setRequestProperty(key, value)
			    conn.connect();  
			    if( conn.getResponseCode() == HttpURLConnection.HTTP_OK ){
			    	byte[] buf = new byte[1024];
			    	ByteArrayOutputStream out = new ByteArrayOutputStream();
			    	InputStream in = conn.getInputStream();
			    	int size = conn.getContentLength();
			    	int received = 0;
			    	System.out.println("size:"+conn.getContentLength());
			    	while(received < size || size == -1){
			    		int bytes = in.read(buf);
			    		System.out.println("bytes:"+bytes);
			    		if( bytes == -1 ){
			    			break;
			    		}
			    		out.write(buf, 0, bytes);
			    		if( size == -1 ){
			    			if( bytes == 0 ){
			    				break;
			    			}
			    		}
			    		else{
			    			received = received + bytes;
			    		}
			    	}
			    	in.close();
			    	resp =  new TinyHttpResponse( String.valueOf(conn.getResponseCode()), TinyHttpServer.MIME_PLAINTEXT,
							new ByteArrayInputStream(out.toByteArray()) );
			    	
			    }
			    else{
			    	resp =  new TinyHttpResponse( String.valueOf(conn.getResponseCode()), TinyHttpServer.MIME_PLAINTEXT,
							conn.getInputStream() );
			    }
			    
			    Map<String,List<String>> HostHeader =  conn.getHeaderFields();
			    Set<String> keys = HostHeader.keySet();
			    for(String key:keys){
			    	if( key == null ){
			    		continue;
			    	}
			    	resp.addHeader(key, conn.getHeaderField(key));  
			    }
			    if( conn.getContentEncoding() != null ){
			    	resp.addHeader("Content-Encoding", conn.getContentEncoding()); 
			    }
//			    resp.header.remove("Server");
//			    resp.header.remove("Cache-Control");
//			    resp.header.remove("Set-Cookie");
			    resp.header.remove("Transfer-Encoding");
			    conn.disconnect();//断开连接  
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				resp =  new TinyHttpResponse( TinyHttpServer.HTTP_BADREQUEST, TinyHttpServer.MIME_PLAINTEXT,
						"" );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				resp =  new TinyHttpResponse( TinyHttpServer.HTTP_BADREQUEST, TinyHttpServer.MIME_PLAINTEXT,
						"" );
			}
			
	    	return resp;
		}
		
		if( uri.startsWith("/test") ){
			TinyHttpResponse resp = null;
			resp =  new TinyHttpResponse( TinyHttpServer.HTTP_OK, TinyHttpServer.MIME_PLAINTEXT,
					"i'm ok!" );
			resp.addHeader("Vary","Accept-Encoding");
			resp.addHeader("Transfer-Encoding","chunked");
			return resp;
		}
		
		return null;
	}

	@Override
	public boolean ready(String uri, String method, Properties header,
			Properties _GET, Properties _POST,
			Hashtable<String, HttpTempFile> _FILES) {
		// TODO Auto-generated method stub
		Log.v(TAG, method+ " "+uri+" "+_GET);
		if( uri.startsWith("/ajax") ){
			return true;
		}
		if( uri.startsWith("/test") ){
			return true;
		}
		return false;
	}
	
	
}
