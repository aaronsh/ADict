package com.benemind.adict;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
import com.benemind.adict.core.DictConsts;
import com.benemind.adict.core.DictEng;
import com.benemind.adict.core.NotFoundDictException;

public class XDictMainActivity extends SherlockActivity implements
		OnItemClickListener, TextWatcher, OnClickListener,
		OnEditorActionListener {
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
			Log.v(TAG, "url:" + url);
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
		mSearchResult.loadDataWithBaseURL(b.toString(), htmlContent,
				"text/html", "utf-8", null);
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
}
