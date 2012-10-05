package com.mar.MARPirates;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TabHost;
 
public class EmailTab extends Activity {
 
	Button buttonSend;
	String textTo;
	String textSubject;
	String textMessage;
 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		String[] recipients = new String[]{"mar@rock.com.com"};

		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,recipients );

		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "<droid>");

		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "message");

		emailIntent.setType("text/plain");

		startActivity(emailIntent);
		
		
		
		//finish();
	  
			
	}
}