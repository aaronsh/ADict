package com.benemind.voa.xdict;

import java.util.ArrayList;

import com.benemind.adict.R;
import com.benemind.voa.xdict.core.Dictionary;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class DictListAdapter extends BaseAdapter implements OnClickListener, OnCheckedChangeListener {
	private static final String TAG = "DictListAdapter";
	ArrayList<Dictionary> mDictionaries;
	private LayoutInflater mInflater;
	private Context mCntx;

	public DictListAdapter(Context cntx) {
		// TODO Auto-generated constructor stub
		mDictionaries = null;
		mCntx = cntx;
		mInflater = LayoutInflater.from(cntx);
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if( mDictionaries == null ){
			return 0;
		}
		Log.v(TAG, "getCount "+mDictionaries.size());
		return mDictionaries.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		Dictionary dict = mDictionaries.get(position);
		return dict.getDictManagementID();
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.v(TAG, "getView("+position+","+convertView+")");
		Dictionary dict = mDictionaries.get(position);
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.xdict_dict_mgr_item, null);
		}
		CheckableRelativeLayout itemView = (CheckableRelativeLayout)convertView;
		itemView.linkWithDict(dict);
		/*		
		Button btn = (Button)convertView.findViewById(R.id.btn);
		CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.checkBox);
		switch(dict.getState()){
		case Dictionary.DICT_STATE_UNINSTALLED:
			btn.setText(R.string.install);
			checkBox.setVisibility(View.GONE);
			break;
		case Dictionary.DICT_STATE_INSTALLED:
			btn.setVisibility(View.GONE);
			checkBox.setChecked(true);
			checkBox.setVisibility(View.VISIBLE);
			break;
		case Dictionary.DICT_STATE_DOWNLOADING:
			break;
		case Dictionary.DICT_STATE_SKIPPED:
			btn.setVisibility(View.GONE);
			checkBox.setChecked(false);
			checkBox.setVisibility(View.VISIBLE);
			break;
		case Dictionary.DICT_STATE_DESTROYED:
			btn.setText(R.string.add_footer);
			checkBox.setVisibility(View.GONE);
			break;
		default:
			break;
		}
		btn.setTag(dict);
		btn.setOnClickListener(this);
		checkBox.setTag(dict);
		checkBox.setOnCheckedChangeListener(this);
*/
		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		Dictionary dict = mDictionaries.get(position);
		int state = dict.getState();
		if( state == Dictionary.DICT_STATE_INSTALLED || state == Dictionary.DICT_STATE_SKIPPED ){
			return true;
		}
		return false;
	}

	public void setDateSource(ArrayList<Dictionary> dictionaries) {
		// TODO Auto-generated method stub
		Log.v(TAG, "setDateSource()" + dictionaries.size());
		mDictionaries = dictionaries;
		this.notifyDataSetChanged();
	}


	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		Object obj = view.getTag();
		if( obj instanceof Dictionary ){
			Dictionary dict = (Dictionary)obj;
			if( mDictionaries.contains(dict) ){
				//download dict now
				Log.v(TAG, "download dict");
			}
		}		
	}


	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		// TODO Auto-generated method stub
		Object obj = view.getTag();
		if( obj instanceof Dictionary ){
			Dictionary dict = (Dictionary)obj;
			if( mDictionaries.contains(dict) ){
				if( isChecked ){
					dict.setState(Dictionary.DICT_STATE_INSTALLED);
				}
				else{
					dict.setState(Dictionary.DICT_STATE_SKIPPED);
				}				
			}
		}
	}


	public void drop(int from, int to) {
		// TODO Auto-generated method stub
		Dictionary dict = mDictionaries.get(from);
		mDictionaries.remove(from);
		mDictionaries.add(to, dict);
		notifyDataSetChanged();
	}


	public ArrayList<Dictionary> getDataSource() {
		// TODO Auto-generated method stub
		return mDictionaries;
	}

}
