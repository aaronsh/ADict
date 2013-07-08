package com.benemind.adict;

import com.benemind.adict.R;
import com.benemind.adict.core.Dictionary;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private static final int CHECKABLE_CHILD_INDEX = 1;
	private static final String TAG = "CheckableLinearLayout";
    private Checkable child;
    private Dictionary mDict;

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        child = (Checkable) findViewById(R.id.dict_actived);//getChildAt(CHECKABLE_CHILD_INDEX);
    }

    @Override
    public boolean isChecked() {
    	TextView v = (TextView)findViewById(R.id.dict_name);
    	Log.v(TAG, "isChecked "+child.isChecked() + " "+v.getText());
        return child.isChecked(); 
    }

    @Override
    public void setChecked(boolean checked) {
    	TextView v = (TextView)findViewById(R.id.dict_name);
//    	Log.v(TAG, "setChecked "+checked + " "+v.getText());
        child.setChecked(checked);
        
        ImageView img = (ImageView)findViewById(R.id.dict_icon);
		int imgRes = (Integer)img.getTag();
        if( checked ){
        	if( imgRes != R.drawable.book_open ){
        		img.setImageResource(R.drawable.book_open);
        		img.setTag(R.drawable.book_open);
        	}
        }
        else{
        	if( imgRes == R.drawable.book_open ){
        		img.setImageResource(R.drawable.book_ok);
        		img.setTag(R.drawable.book_ok);
        	}
        }
/*    	
    	int state = mDict.getState(); 
    	if( state == Dictionary.DICT_STATE_SKIPPED ){
    		mDict.setState(Dictionary.DICT_STATE_INSTALLED);
    	}
    	else if( state == Dictionary.DICT_STATE_INSTALLED ){
    		mDict.setState(Dictionary.DICT_STATE_SKIPPED);
    		child.setChecked(checked);
    	}
*/        
    }

    @Override
    public void toggle() {
    	TextView v = (TextView)findViewById(R.id.dict_name);
    	Log.v(TAG, "toggle "+v.getText());
    	int state = mDict.getState(); 
    	if( state == Dictionary.DICT_STATE_SKIPPED ){
    		mDict.setState(Dictionary.DICT_STATE_INSTALLED);
    		child.toggle();
    	}
    	else if( state == Dictionary.DICT_STATE_INSTALLED ){
    		mDict.setState(Dictionary.DICT_STATE_SKIPPED);
    		child.toggle();
    	}
        
    }

	public void linkWithDict(Dictionary dict) {
		// TODO Auto-generated method stub
		mDict = dict;
		
		TextView txt = (TextView)findViewById(R.id.dict_name);
		txt.setText(dict.getBookName());
		
		if( dict.getState() == Dictionary.DICT_STATE_INSTALLED ){
			child.setChecked(true);
		}
		else{
			child.setChecked(false);
		}
		
		ImageView img = (ImageView)findViewById(R.id.dict_icon);
		int imgRes;
		switch(dict.getState()){
		case Dictionary.DICT_STATE_INSTALLED:
			imgRes = R.drawable.book_open;
			break;
		case Dictionary.DICT_STATE_SKIPPED:
			imgRes = R.drawable.book_ok;
			break;
		case Dictionary.DICT_STATE_DESTROYED:
			imgRes = R.drawable.book_err;
			break;
		default:
			imgRes = R.drawable.book_err;
			break;
		}
		
		Log.v(TAG, "dict:"+dict.getBookName() + "  state:"+dict.getState()+ " img:"+imgRes);
		img.setImageResource(imgRes);
		img.setTag(imgRes);
	}
    
}
