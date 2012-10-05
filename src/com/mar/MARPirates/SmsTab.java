package com.mar.MARPirates;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SmsTab extends Activity
{
  private static final String TAG = "com.mar.MARPirates-SmsTab:";

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    Intent localIntent = new Intent("android.intent.action.VIEW");
    localIntent.setType("vnd.android-dir/mms-sms");
    localIntent.putExtra("address", "07504464678");
    localIntent.putExtra("sms_body", "Message");
    startActivity(localIntent);
  }
}

