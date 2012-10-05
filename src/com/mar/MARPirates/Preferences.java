package com.mar.MARPirates;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity
{
  public static final String RECORDPREF = "STEREO";
  public static final String STREAMPREF = "MONO";
  public static final Boolean WIFIPREF_ = Boolean.valueOf(false);

  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    addPreferencesFromResource(R.xml.preferences);
  }
}

