package com.benemind.adict;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
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
import com.actionbarsherlock.view.SubMenu;
import com.benemind.adict.R;
import com.benemind.util.AboutDialog;
import com.benemind.util.DialogUtils;
import com.benemind.adict.core.DictEng;
import com.benemind.adict.core.WordLookupResult;

public class XDictMainActivity extends SherlockActivity implements
		OnItemClickListener, TextWatcher, OnClickListener,
		OnEditorActionListener {
	private static final String TAG = "MainActivity";
	protected static final int MSG_LIST_WORDS = 0;
	protected static final int MSG_REDIRECT_WORD = 1;

	private static final int MENU_ID_DICT_MGR = 1;
	private static final int MENU_ID_SYS = 0;
	
	private static final int MENU_ID_WEBSITE = 11;
	private static final int MENU_ID_HELP = 12;
	private static final int MENU_ID_APP_STORE = 13;
	private static final int MENU_ID_ABOUT = 14;
	private static final int MENU_ID_APP_SHARE = 15;
	private static final int MENU_ID_REFRESH = 21;
	
	
	enum DictAction {
		LIST_WORDS, SEARCH_WORD
	}
	

	AutoCompleteTextView mSearchInput;
	WebView mSearchResult;
	// ArrayAdapter<String> mAdapter;
	DropDownAdapter mAdapter;
	DictEng mDictEng;
	int mDictsOnCard;
	private DictAction mDictAction;

	boolean mListWordTaskRunning;
	String mPenddingListWordTask;
	String mIndexHtmlContent = "";
	
	ArrayList<WordLookupResult> mWordLookupResults;

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
			Log.v(TAG, "onLoadResource " + url);
			super.onLoadResource(view, url);
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
				R.string.menu_dict_mngr);
		refreshItem.setIcon(R.drawable.ic_menu_books);
		refreshItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


		SubMenu subMenu2 = menu.addSubMenu(R.string.menu_more);
		subMenu2.add(1, MENU_ID_WEBSITE, Menu.NONE, R.string.menu_website);
		subMenu2.add(1, MENU_ID_HELP, Menu.NONE, R.string.menu_help);
		subMenu2.add(1, MENU_ID_APP_STORE, Menu.NONE, R.string.menu_app_store);
//		subMenu2.add(1, MENU_ID_APP_SHARE, Menu.NONE, R.string.menu_share);
//		subMenu2.add(1, MENU_ID_ABOUT, Menu.NONE, R.string.menu_about);

		MenuItem subMenu2Item = subMenu2.getItem();
		subMenu2Item.setIcon(R.drawable.ic_menu_more);
		subMenu2Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
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
		case MENU_ID_WEBSITE:
			openWebsite();
			break;
		case MENU_ID_HELP:
			openHelp();
			break;
		case MENU_ID_APP_STORE:
			gotoStore();
			break;
		case MENU_ID_ABOUT:
			new AboutDialog(this).show();
			break;
		case MENU_ID_APP_SHARE:
			
			break;
		case MENU_ID_REFRESH:
			
			break;
		}
		return true;
	}
	private void shareWithFriend() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(Intent.ACTION_SEND);

		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_title));
		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content));
		startActivity(Intent.createChooser(intent, getTitle()));
	}
	private void gotoStore(){
		try{
			startActivity(new Intent("android.intent.action.VIEW",
					Uri.parse("market://details?id=com.benemind.adict")));
		}
		catch(ActivityNotFoundException e){
			e.printStackTrace();
			DialogUtils.showNoMarketApp(this);
		}
	}
	private void openHelp() {
		// TODO Auto-generated method stub
		mSearchResult.loadUrl("file:///android_asset/website/help.html");
		//loadUrl(buildUrl("help.html"));
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(
				mSearchInput.getWindowToken(),
				InputMethodManager.HIDE_IMPLICIT_ONLY);		
	}

	private void openWebsite() {
		// TODO Auto-generated method stub
		Intent intent= new Intent();        
		intent.setAction("android.intent.action.VIEW");    
		Uri content_url = Uri.parse("http://aaronsh.github.io/ADict/");   
		intent.setData(content_url);  
		startActivity(intent);
	}

	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// setTheme(SampleList.THEME); //Used for theme switching in samples
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
		mWordLookupResults = null;
			
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
		mSearchResult.addJavascriptInterface(this, "adict");
		//mSearchResult.reload();
		

		mSearchResult.loadUrl("file:///android_asset/website/index.html");
		
		
		mListWordTaskRunning = false;
		mPenddingListWordTask = null;

		mWaitingDlg = null;
		mDictAction = DictAction.LIST_WORDS;

		mDictEng = DictEng.getInstance(this);
		mDictEng.reloadConfig();
		loadDicts();
		mIndexHtmlContent = "";

	}
	
	//idStr = "setting_document_title"
	public String loadResourceString(String idStr){
		Resources res = getResources();
		try{
			String pkg = this.getPackageName();
			int id = res.getIdentifier(idStr, "string", pkg);
			String s = res.getString(id);
			return s;
		}
		catch(android.content.res.Resources.NotFoundException e){
			e.printStackTrace();
		}
		return "";
	}
	public String getDictionaries(){
		JSONArray a = new JSONArray();
		if( mWordLookupResults != null ){
			try{
				for(int i=0; i<mWordLookupResults.size(); i++){
					WordLookupResult r = mWordLookupResults.get(i);
					JSONObject j  = new JSONObject();
					j.put("book", r.book);
					a.put(j);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String t = a.toString();
		writeFileSdcardFile("/mnt/sdcard/adict/dictionaries.json", t);
		return t;
	}
	public String getDictCss(String dictName, int dictIndex){
		if( mWordLookupResults != null ){
			WordLookupResult r = mWordLookupResults.get(dictIndex);
			writeFileSdcardFile("/mnt/sdcard/adict/"+r.book+".css", r.css);
			return r.css;
		}
		return "";
	}
	public String getDictHtml(String dictName, int dictIndex){
		if( mWordLookupResults != null ){
			WordLookupResult r = mWordLookupResults.get(dictIndex);
			writeFileSdcardFile("/mnt/sdcard/adict/"+r.book+".html", r.html);
			return r.html;
		}
		return "";
	}
	public String getDictJs(String dictName, int dictIndex){
		if( mWordLookupResults != null ){
			WordLookupResult r = mWordLookupResults.get(dictIndex);
			writeFileSdcardFile("/mnt/sdcard/adict/"+r.book+".js", r.js);
			return r.js;
		}
		return "";
	}

	
	public String readFileSdcardFile(String fileName){   
		String res="";   
		try{   
			FileInputStream fin = new FileInputStream(fileName);   

			int length = fin.available();   

			byte [] buffer = new byte[length];   
			fin.read(buffer);       

			res = EncodingUtils.getString(buffer, "UTF-8");   

			fin.close();       
		}   

		catch(Exception e){   
			e.printStackTrace();   
		}   
		return res;   
	}  

	private void loadDicts() {
		// TODO Auto-generated method stub
		int DictCount = mDictEng.estimateDictionaryCount();
		if (DictCount < 10) {
			if(DictEng.LOAD_DICTS_SUCC == mDictEng.loadDicts()){

				Intent intent = getIntent();
				String word = intent.getStringExtra("word");

				if (word != null) {
					intent.putExtra("word", (String) null);
					setSearchInputText(word);
				}
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
	
	protected void onDestroy(){
		Log.v(TAG, "onDestroy");
		super.onDestroy();
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
			mWordLookupResults = mDictEng.lookupWord(keywords[0]); 
			return "";
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
			mDictEng.loadDicts();
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
				//mSearchResult.loadUrl(buildUrl("help.html"));

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
		mIndexHtmlContent = htmlContent;
		mSearchResult.loadUrl("file:///android_asset/index.html");
		mSearchResult.scrollTo(0, 0);

		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(
				mSearchInput.getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	// 写数据到SD中的文件
	public void writeFileSdcardFile(String file, String text) {
		try {

			FileOutputStream fout = new FileOutputStream(file);
			byte[] bytes = text.getBytes();

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

	private String readAssetFile(String fileName){
		String res="";   
		try{   

			//得到资源中的asset数据流  
			InputStream in = getResources().getAssets().open(fileName);   

			int length = in.available();           
			byte [] buffer = new byte[length];          

			in.read(buffer);              
			in.close();  
			res = EncodingUtils.getString(buffer, "UTF-8");       

		}catch(Exception e){   

			e.printStackTrace();           

		}
		return res;
	}
}
