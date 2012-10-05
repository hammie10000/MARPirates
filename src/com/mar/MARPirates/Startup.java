package com.mar.MARPirates;





import android.app.TabActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class Startup extends TabActivity
{
	public static final String PREFERENCE_FILENAME = "MARPiratesAppPrefs";
	private static final String TAG = "com.mar.MARPirates-TabActivity:";
	public TabHost mTabHost;
	
	private static Startup theInstance;

    public static Startup getInstance() {
        return Startup.theInstance;
    }

    public void Startup() {
        Startup.theInstance = this;
    }
    



	public void onCreate(Bundle paramBundle)  { 
		super.onCreate(paramBundle);
		setContentView(R.layout.tab);
    
		Drawable HomeDrawable = getResources().getDrawable(R.drawable.home);
		Drawable SmsDrawable = getResources().getDrawable(R.drawable.sms);
		Drawable EmailDrawable = getResources().getDrawable(R.drawable.email);
		Drawable FbookDrawable = getResources().getDrawable(R.drawable.fbook);
		Drawable InternetDrawable = getResources().getDrawable(R.drawable.internet);
    
		mTabHost = getTabHost();
    
    
		TabHost.TabSpec HomeTabSpec = mTabHost.newTabSpec("tid1");
		TabHost.TabSpec SmsTabSpec = mTabHost.newTabSpec("tid1");
		TabHost.TabSpec EmailTabSpec = mTabHost.newTabSpec("tid1");
		TabHost.TabSpec FbookTabSpec = mTabHost.newTabSpec("tid1");
		TabHost.TabSpec InternetTabSpec = mTabHost.newTabSpec("tid1");
    
		HomeTabSpec.setIndicator("Home", HomeDrawable).setContent(new Intent(this, HomeTab.class));
		SmsTabSpec.setIndicator("Sms", SmsDrawable).setContent(new Intent(this, SmsTab.class));
		EmailTabSpec.setIndicator("Email", EmailDrawable).setContent(new Intent(this, EmailTab.class));
		FbookTabSpec.setIndicator("Fbook", FbookDrawable).setContent(new Intent(this, FbookTab.class));
		InternetTabSpec.setIndicator("Internet", InternetDrawable).setContent(new Intent(this, InternetTab.class));
   
		mTabHost.addTab(HomeTabSpec);
		mTabHost.addTab(SmsTabSpec);
		mTabHost.addTab(EmailTabSpec);
		mTabHost.addTab(FbookTabSpec);
		mTabHost.addTab(InternetTabSpec);
		mTabHost.setCurrentTab(0);
	}
  
	
}

