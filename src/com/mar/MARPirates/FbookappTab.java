package com.mar.MARPirates;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
//import com.facebook.android.*;
//import com.facebook.android.Facebook.*;





public class FbookappTab extends Activity {

WebView webview;
String currentURL = "http://m.facebook.com/MARPirates";
final Activity activity = this;


public String Fb_MyAppId = "418870381474332";


/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
   
      
    
    
    if (!isAppInstalled("com.facebook.katana")) {
 
    	Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setData(Uri.parse("market://details?id=com.facebook.katana"));
    	startActivity(intent);
    }else {
    	
    	
    	Intent intent = new Intent(Intent.ACTION_VIEW);
    	intent.setData(Uri.parse("fb://profile/100000666160763"));
    	
    	// mar 100000666160763
    	// rodger 100000062505155
    	startActivity(intent);

    }
    
      
    
    
}

	


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
        webview.goBack();
        return true;
		}
		else {
	    /*
    	setCurrentTab(4);
    	//((Startup) getParent()).getTabHost().setCurrentTab(0);
			return super.onKeyDown(keyCode, event);
    	finish(); */
		}
    return true;
	}
	
	
       
	private boolean isAppInstalled(String uri) {
		try{
		    ApplicationInfo info = getPackageManager().
		            getApplicationInfo(uri, 0 );
		    return true;
		} catch( PackageManager.NameNotFoundException e ){
		    return false;
		}

	}
	
	


	private boolean validateAppSignatureForIntent(Activity activity2,
			Intent intent) {
		// TODO Auto-generated method stub
		return false;
	}
}
