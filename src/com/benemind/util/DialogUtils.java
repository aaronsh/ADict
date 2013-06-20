package com.benemind.util;

import com.benemind.adict.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

public class DialogUtils {

	public static void cancelLoadingDialog(ProgressDialog dialog) {
		// TODO Auto-generated method stub
	    if ((dialog != null) && (dialog.isShowing()))
	    	dialog.cancel();
	}

	 

	  public static void showCommonMsgDialog(Context cntx, String msg)
	  {
		  AlertDialog.Builder builder = new AlertDialog.Builder(cntx);
		  builder.setTitle(R.string.app_name);
		  builder.setMessage(msg);
		  builder.setPositiveButton(R.string.about_ok, new DialogInterface.OnClickListener() {
			  @Override
			  public void onClick(DialogInterface dialog, int which) {
				  dialog.dismiss();
			  }
		  });
		  builder.create().show();
	  }

}
