package com.benemind.adict;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListActivity;
import com.benemind.adict.R;
import com.benemind.adict.core.DictEng;
import com.benemind.adict.core.Dictionary;
import com.benemind.adict.core.NotFoundDictException;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

public class DictMgrActivity extends SherlockListActivity  {
    private static final String TAG = "DictMgrActivity";
	private DictListAdapter mAdapter;
    DictEng mDictEng;
    
    private ProgressDialog mWaitingDlg;

    private DragSortListView.DropListener onDrop =
        new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                if (from != to) {
                	mAdapter.drop(from, to);
                    DragSortListView list = getListView();
/*                	
                    String item = adapter.getItem(from);
                    adapter.remove(item);
                    adapter.insert(item, to);
*/                    
                    list.moveCheckState(from, to);
                }
            }
        };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// setTheme(SampleList.THEME); //Used for theme switching in samples

		super.onCreate(savedInstanceState);
        setContentView(R.layout.xdict_dict_mgr);

        mAdapter = new DictListAdapter(this);
        setListAdapter(mAdapter);
        
        DragSortListView list = getListView();
        list.setDropListener(onDrop);
        
        mWaitingDlg = null;

		mDictEng = DictEng.getInstance(this);
		int DictCount = mDictEng.estimateDictionaryCount();
		if( DictCount < 10 ){
	        ArrayList<Dictionary> dicts = mDictEng.listDicts();
	        initList(dicts);
		}
		else{
			listDictsTask loadTask = new listDictsTask();
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
	
	private void initList(ArrayList<Dictionary> dicts) {
		// TODO Auto-generated method stub
		mAdapter.setDateSource(dicts);
        int size = dicts.size();
        DragSortListView list = getListView();
        for(int i=0; i<size; i++){
        	Dictionary d = dicts.get(i);
        	int state = d.getState(); 
        	if( state == Dictionary.DICT_STATE_INSTALLED ){
        		list.setItemChecked(i, true);
        	}
        	else{
        		list.setItemChecked(i, false);
        	}
        }
	}

	@Override
	protected void onDestroy()
	{
        ArrayList<Dictionary> dicts = mAdapter.getDataSource();
        DragSortListView list = getListView();;
        long[] checkedIds = list.getCheckedItemIds();
        Iterator<Dictionary> it = dicts.iterator();
        while(it.hasNext()){
        	Dictionary dict = it.next();
        	int state = dict.getState();
        	if( state == Dictionary.DICT_STATE_INSTALLED || state == Dictionary.DICT_STATE_SKIPPED ){
        		dict.setState(Dictionary.DICT_STATE_SKIPPED);
        		for(int i=0; i<checkedIds.length; i++){
        			if( checkedIds[i] == dict.getDictManagementID() ){
        				dict.setState(Dictionary.DICT_STATE_INSTALLED);
        				break;
        			}
        		}
        	}
        }
        if( dicts!=null ){
        	mDictEng.saveDictList(dicts);
        }
        
        this.setResult(Activity.RESULT_OK, null);
        super.onDestroy();
	}
    @Override
    public DragSortListView getListView() {
        return (DragSortListView) super.getListView();
    }
    
    class listDictsTask extends AsyncTask<Void, Void, ArrayList<Dictionary>> {

		public listDictsTask() {
		}

		@Override
		protected ArrayList<Dictionary> doInBackground(Void... args) {
			return	mDictEng.listDicts();
		}

		@Override
		protected void onPostExecute(ArrayList<Dictionary> list) {
			if (mWaitingDlg != null && mWaitingDlg.isShowing()) {
				mWaitingDlg.cancel();
			}
			mWaitingDlg = null;

			initList(list);
		}
	}

}
