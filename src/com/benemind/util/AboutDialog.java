package com.benemind.util;

import com.benemind.adict.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

public class AboutDialog extends AlertDialog {
	public AboutDialog(Context context) {   
        super(context);   
        final View view = getLayoutInflater().inflate(R.layout.about,   
                null);   
        TextView txt = (TextView)view.findViewById(R.id.about_msg);
        String msg = context.getString(R.string.about_text);
        txt.setText(Html.fromHtml(msg));
        txt.setMovementMethod(LinkMovementMethod.getInstance());
        
        setIcon(R.drawable.ic_launcher);  
        String packageName = context.getPackageName();
        String title = context.getString(R.string.app_name);
        try {
			PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
			title = String.format("%s %s", title, info.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        setTitle(title);   
        setView(view);   
      
        this.setButton(BUTTON_POSITIVE, context.getText(R.string.about_ok), (OnClickListener)null);
    } 
}
