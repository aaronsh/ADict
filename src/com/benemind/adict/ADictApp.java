package com.benemind.adict;

import android.app.Application;
import android.util.Log;

import org.acra.*;
import org.acra.annotation.*;

/*
user this link to view reports: 
    http://www.benemind.com/index.php/en/?option=com_acra&controller=report&task=viewReports&package=order.benemind.com
    or
    http://www.benemind.com/index.php/en/?option=com_acra&controller=report&task=viewReports&package=admin
*/

@ReportsCrashes(formKey = "teswt", // will not be used
formUri = "http://acrahost.duapp.com?do=submit",
formUriBasicAuthLogin = "yourlogin", // optional&
formUriBasicAuthPassword = "y0uRpa$$w0rd", // optional
includeDropBoxSystemTags = true,
mode = ReportingInteractionMode.TOAST,
resToastText = R.string.acra_report)
public class ADictApp extends Application {
	private static final String TAG = "VoaApp";




	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.v(TAG, "onCreate");
		super.onCreate();
		ACRA.init(this);


	}
}
