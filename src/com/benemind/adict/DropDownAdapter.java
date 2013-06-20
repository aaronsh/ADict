package com.benemind.adict;

import java.util.ArrayList;

import com.benemind.adict.R;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class DropDownAdapter extends BaseAdapter implements Filterable {
	private static final String TAG = "DropDownAdapter";
	ArrayList<String> mWords;
	private LayoutInflater mInflater;
	private Context mCntx;
	
	private PassAllFilter mFilter;
	public DropDownAdapter(Context cntx) {
		// TODO Auto-generated constructor stub
		mWords = null;
		mCntx = cntx;
		mInflater = LayoutInflater.from(cntx);
		
		mFilter = new PassAllFilter();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if( mWords == null ){
			return 0;
		}
		return mWords.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if( mWords == null ){
			return null;
		}
		return mWords.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if( convertView == null ){
			convertView = mInflater.inflate(R.layout.drop_down_list_item, parent, false);
		}
		
		TextView v = (TextView)convertView.findViewById(R.id.item_text);
		v.setText(mWords.get(position));
		
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
		return false;
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
		return true;
	}

	public void setDateSource(ArrayList<String> words) {
		// TODO Auto-generated method stub
		Log.v(TAG, "setDateSource()" + words.size());
		mWords = words;
		this.notifyDataSetChanged();
	}
	

	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		return mFilter;
	}
	
	class PassAllFilter extends Filter{

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// TODO Auto-generated method stub
			FilterResults results = new FilterResults();
			results.values = mWords;
			if( mWords != null ){
				results.count = mWords.size();
			}
			else{
				results.count = 0;
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// TODO Auto-generated method stub
			Log.v(TAG, "publishResults");
			notifyDataSetChanged();
		}
		
	}
}
