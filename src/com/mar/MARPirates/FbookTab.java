package com.mar.MARPirates;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.graphics.Bitmap;



public class FbookTab extends Activity {

WebView webview;
String currentURL = "http://m.facebook.com/MARPirates";
final Activity activity = this;

/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    this.getWindow().requestFeature(Window.FEATURE_PROGRESS);

    setContentView(R.layout.webview);

    getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
            Window.PROGRESS_VISIBILITY_ON);

    webview = (WebView) findViewById(R.id.webview01);
    webview.getSettings().setJavaScriptEnabled(true);

    final ProgressDialog progressDialog = new ProgressDialog(activity);
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setMessage("Loading...please wait");
    progressDialog.setCancelable(true);

    webview.setWebViewClient(new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
        
        @Override  
        public void onPageStarted(WebView view, String url, Bitmap favicon) {  
            // TODO Auto-generated method stub  
            super.onPageStarted(view, url, favicon);  
        }  

    });

    webview.loadUrl(currentURL);

    // WebChromeClient give progress etc info
    webview.setWebChromeClient(new WebChromeClient() {

        public void onProgressChanged(WebView view, int progress) {

            progressDialog.show();
            progressDialog.setProgress(0);
            activity.setProgress(progress * 1000);

            progressDialog.incrementProgressBy(progress);

            if (progress == 100 && progressDialog.isShowing())
                progressDialog.dismiss();
        }
    });

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
}
