package com.mar.MARPirates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;


public class HomeTab extends Activity implements View.OnClickListener  {
	private static final int METADELAY = 1000;
	public static String MY_PREFS = "MY_PREFS";
	private static final String TAG = "com.mar.MARPirates-HomeTab:";
	private static final int CONFIRM_STREAM_DIALOG = 1;
	private static final int CONFIRM_WIFI_DIALOG = 2;
	private static final int CONFIRM_RECORDING_DIALOG = 3;
	private static final int CONFIRM_DEFAULT_DIALOG = 4;
	private static final int NOTIFY_SDCARD_DIALOG = 5;
	private static final int RETRY_CONNECT_DIALOG = 6;
	private static final int CONFIRM_SETTINGS_DIALOG = 7;


	boolean Playing = false;
	boolean Recording = false;
	Button buttonPlayStart;
	Button buttonPlayStop;
	Button buttonRecStart;
	Button buttonRecStop;
	Button buttonPrefs;
	Button buttonMenu;


	Handler MetaHandler;
	String metaData;
	SharedPreferences sharedPrefs;


	Context context;
	public boolean WifiPrefs;
	public boolean InetAvail;
	public String StreamPrefs;
	public String RecordPrefs;
	public String StoragePrefs;

	public String Storage;
	public String StoragePath;



	boolean recstart,recstop,playstart,playstop,storageok;


	//TODO
	//Get send sms to use my layout!
	//TODO
	//Use lib to update metadata


	public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);


		boolean firstrun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstrun", true);
		sharedPrefs = PreferenceManager.
				getDefaultSharedPreferences(this);
		sharedPrefs.registerOnSharedPreferenceChangeListener(prefListener);
		setContentView(R.layout.hometab);
		BugSenseHandler.setup(this, "3212611c");

		if (firstrun){
			SaveDefaultShared();  // SaveDefaultShared so we have them to get!!

			getSharedPreferences("PREFERENCE", MODE_PRIVATE)
			    .edit()
			    .putBoolean("firstrun",true)
			    .commit();
		}


		final ConnectivityManager connectManager =
				   (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);


		buttonPlayStart = ((Button)findViewById(R.id.buttonStartPlay));
		buttonPlayStart.setOnClickListener(this);

		buttonPlayStop = ((Button)findViewById(R.id.buttonStopPlay));
		buttonPlayStop.setOnClickListener(this);

		buttonRecStart = ((Button)findViewById(R.id.buttonStartRecord));
		buttonRecStart.setOnClickListener(this);

		buttonRecStop = ((Button)findViewById(R.id.buttonStopRecord));
		buttonRecStop.setOnClickListener(this);

		buttonMenu = ((Button)findViewById(R.id.buttonMenu));
		buttonMenu.setOnClickListener(this);
		buttonMenu.setEnabled(true);


		if (savedInstanceState != null) {
			onRestoreInstanceState(savedInstanceState);
			Toast.makeText(this, "Restored instance state", 1).show();

		}else {
			RestoreShared();
			Toast.makeText(this, "Restored shared", 1).show();
		}



		AudioManager mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);

		OnAudioFocusChangeListener audioListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int reason) {

	        switch (reason) {
	            case AudioManager.AUDIOFOCUS_LOSS:
	                // Lost focus, pause playback
	            	stopService(new Intent(HomeTab.this, PlayService.class));
	            	break;

	            case AudioManager.AUDIOFOCUS_GAIN:
	                // Gained focus, start playback
	            	startService(new Intent(HomeTab.this, PlayService.class));
	                break;
	        }
		}};

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	  //Handle the back button
	  if (keyCode == KeyEvent.KEYCODE_BACK) {
	    //Ask the user if they want to quit
	    new AlertDialog.Builder(this)
	      .setIcon(android.R.drawable.ic_dialog_alert)
	      .setTitle("Exit")
	      .setMessage("Are you sure you want to leave?  All recordings and Music will stop.  You could press Home!")
	      .setNegativeButton(android.R.string.cancel, null)
	      .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which){
	          // Exit the activity
	          if (Recording) {

	        	  stopRecording();

	          }
	          if (Playing) {
	        	  stopPlaying();
	          }
	          SaveShared();  //just in case?
	          finish();
	        }
	      })
	      .show();

	    // Say that we've consumed the event
	    return true;
	  }

	  return super.onKeyDown(keyCode, event);
	}


	public final boolean InternetOn() {
		ConnectivityManager connec =  (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		// ARE WE CONNECTED TO THE NET
		if ( connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
			connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING ||
			connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING ||
			connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED ) {
				// MESSAGE TO SCREEN FOR TESTING (IF REQ)
				//Toast.makeText(this, connectionType + ” connected”, Toast.LENGTH_SHORT).show();
				return true;
		} else if (
			connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||
			connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED  ) {
				//System.out.println(“Not Connected”);
			return false;
		}
		return false;
	}

	/***************************************************************************************************************
	 * oh boy
	*/




	public void SaveDefaultShared() {

    	final SharedPreferences.Editor editor = sharedPrefs.edit();

    	editor.putBoolean("Playing", false);
    	editor.putBoolean("Recording",false);
       	storageok = sharedPrefs.getBoolean("storageOk",false);

    	if (SdPresent()) {
			storageok=true;
	   		editor.putBoolean("storageOk",storageok);
       		editor.commit();
	 		
       	}else {

    		storageok=false;
    		editor.putBoolean("storageOk",storageok);
    		editor.commit();
    	}

		if (UsingWiFi()) {
    		editor.putBoolean("wifiPrefs", true);
    	}else {
    		editor.putBoolean("wifiPrefs", false);
    	}

    	editor.putString("streamPrefs", "32kbs");
    	editor.putString("recordPrefs","64kbs");
    	editor.commit();
    }

    public String getStoragePath(String StoragePrefs) {
    	if (StoragePrefs.equalsIgnoreCase("Sdcard")) {
    		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/MARPirates/";
    	}else {
    		return "/data/data/com.mar.MARPirates/MARPirates/";
    	}
    }

    public void RestoreShared() {
    	final SharedPreferences.Editor editor = sharedPrefs.edit();

    	Playing = sharedPrefs.getBoolean("Playing",false);
		if (Playing) {

			playstart = false;
			playstop = true;
		} else {

			playstart = true;
			playstop = false;
		}

		Recording = sharedPrefs.getBoolean("Recording",false);
		if (Recording){

			recstart = false;
			recstop = true;
		} else {
			recstart = true;
			recstop = false;
		}

		StreamPrefs = sharedPrefs.getString("streamPrefs","32kbs");
		RecordPrefs = sharedPrefs.getString("recordPrefs","64kbs");

		if (SdPresent()) {
			storageok=true;
    		editor.putBoolean("storageOk",storageok);
    		editor.commit();

    	}else {
			Toast.makeText(this, "Yikes No storage Avilible", 1).show();
    		storageok=false;
    		editor.putBoolean("storageOk",storageok);
    		editor.commit();
    	}

    	// check wifi status, it may have changed whilst we were away
    	if (UsingWiFi()) {
    		editor.putBoolean("wifiPrefs", true);
    	}else {
    		editor.putBoolean("wifiPrefs", false);
    	}
    	editor.commit();

    	if (StreamPrefs.equals("32kbs")) {

		//	((TextView)HomeTab.this.findViewById(R.id.CurrentStream)).setText("http://173.192.22.204:8024");
		    ((TextView)HomeTab.this.findViewById(R.id.CurrentStreamType)).setText(StreamPrefs);
		}else {
			//((TextView)HomeTab.this.findViewById(R.id.CurrentStream)).setText("http://shoutcast2.tidyhosts.com:9032");
			((TextView)HomeTab.this.findViewById(R.id.CurrentStreamType)).setText(StreamPrefs);
		}

    	if (RecordPrefs.equals("32kbs")) {

    		//((TextView)HomeTab.this.findViewById(R.id.RecordStream)).setText("http://173.192.22.204:8024");
			((TextView)HomeTab.this.findViewById(R.id.RecordStreamType)).setText(RecordPrefs);
		}
		else {
			//((TextView)HomeTab.this.findViewById(R.id.RecordStream)).setText("http://shoutcast2.tidyhosts.com:9032");
			((TextView)HomeTab.this.findViewById(R.id.RecordStreamType)).setText(RecordPrefs);
		}

		/*(((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((((
			lok at
		*/

    	buttonRecStart.setEnabled(recstart);
		buttonRecStop.setEnabled(recstop);

		buttonPlayStart.setEnabled(playstart);
		buttonPlayStop.setEnabled(playstop);
    }

    public void SaveShared() {

    	final SharedPreferences.Editor editor = sharedPrefs.edit();

    	editor.putBoolean("Playing", Playing);
    	editor.putBoolean("Recording",Recording);
    	editor.putBoolean("storageOk",storageok);
    	editor.putBoolean("wifiPrefs", WifiPrefs);
    	editor.putString("streamPrefs", StreamPrefs);
    	editor.putString("recordPrefs",RecordPrefs);
    	editor.commit();
    }

    void stopRecording(){
    	stopService(new Intent(HomeTab.this, RecordService.class));
		Recording=(!Recording);
		Recording = !Recording;
  	  	recstart = true;
  	  	recstop =false;
  	  	buttonRecStart.setEnabled(recstart);
  	  	buttonRecStop.setEnabled(recstop);
		((TextView)HomeTab.this.findViewById(R.id.Recording)).setText("Not Recording");
    }


	@Override
	public void onRestoreInstanceState(Bundle SaveInstanceState) {
		super.onRestoreInstanceState(SaveInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.

		RestoreShared();
		Toast.makeText(this, "RestoreSaveInstanceState", 1).show();
		Log.i("onRestoreInstanceState", "onRestoreInstanceState()");

	}

	@Override
    public void onSaveInstanceState(Bundle SaveInstanceState) {
    // TODO Auto-generated method stub
		SaveShared();
		Toast.makeText(this, "onSaveInstanceState state saved", 1).show();
		Log.i("onSaveInstanceState", "onSaveInstanceState()");
		super.onSaveInstanceState(SaveInstanceState);

    }




	@Override
    protected void onStart() {

		//RestoreShared();
		Toast.makeText(this, "onStart", 1).show();
		super.onStart();

    }


	@Override
	protected void onResume(){
		Toast.makeText(this, "onResume", 1).show();
		RestoreShared();
		super.onResume();
	}

	@Override
	protected void onRestart(){

		Toast.makeText(this, "onResart", 1).show();
		RestoreShared();
		super.onRestart();
	}

	@Override
	protected void onPause(){
		SaveShared();
		Toast.makeText(this, "onPause", 1).show();
		super.onPause();

    }

	@Override
	protected void onStop(){
		SaveShared();
		Toast.makeText(this, "onStop", 1).show();
		super.onStop();

    }

	public static boolean SdPresent() {

    	return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

    }

	public boolean UsingWiFi() {
	    ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

	    NetworkInfo wifiInfo = connectivity
	            .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

	    if (wifiInfo.getState() == NetworkInfo.State.CONNECTED
	            || wifiInfo.getState() == NetworkInfo.State.CONNECTING) {
	        return true;
	    }

	    return false;
	}

	public boolean UsingMobile() {
	    ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

	    NetworkInfo mobileInfo = connectivity
	            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

	    if (mobileInfo.getState() == NetworkInfo.State.CONNECTED
	            || mobileInfo.getState() == NetworkInfo.State.CONNECTING) {
	        return true;
	    }

	    return false;
	}


	





    void stopPlaying(){
    	stopService(new Intent(HomeTab.this, PlayService.class));
		MetaStop();
		Playing=(!Playing);
		playstart = true;
  	  	playstop =false;
		buttonPlayStart.setEnabled(playstart);
		buttonPlayStop.setEnabled(playstop);
		((TextView)HomeTab.this.findViewById(R.id.SongView)).setText("Not Playing");
    }

    private void showErrorDialog(String errorString){
        String okButtonString = getString(R.string.wifi_message);
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(getString(R.string.wifi_title));
        ad.setMessage(errorString);
        ad.setPositiveButton(okButtonString,new OnClickListener() {
                  public void onClick(DialogInterface dialog, int arg1) {
                       reload();
                  } } );
        ad.show();
        return;
   }

    private SharedPreferences.OnSharedPreferenceChangeListener

    prefListener=new SharedPreferences.OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
			final SharedPreferences.Editor prefEditor = sharedPrefs.edit();

			StoragePrefs = sharedPrefs.getString("storagePref", "Sdcard");
			if (key.equals("storgePref")) {

				//do the change stream pref bit
				if (StoragePrefs.equals("Sdcard")) {
					StoragePath = getStoragePath(StoragePrefs);
		    //		((TextView)HomeTab.this.findViewById(R.id.Storage)).setText(StoragePrefs);
					((TextView)HomeTab.this.findViewById(R.id.StoragePath)).setText(StoragePath);
				}
				if (StoragePrefs.equals("Internal")) {
					if (Recording){
						stopRecording();
					}
					StoragePath = getStoragePath(StoragePrefs);
		    	//	((TextView)HomeTab.this.findViewById(R.id.Storage)).setText(StoragePrefs);
					((TextView)HomeTab.this.findViewById(R.id.StoragePath)).setText(StoragePath);

				}


			}

			if (key.equals("wifiPref")) {

				WifiPrefs = sharedPrefs.getBoolean("wifiPref", false);

				if (WifiPrefs) {
					// turned on
					//Do something here
					if (Playing) {
						stopPlaying();
					}
					if (Recording){
						stopRecording();
					}
					WifiManager wifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
					wifiManager.setWifiEnabled(true);
				}


				if (!WifiPrefs) {
					// turned off
					if (Playing) {
						stopPlaying();
					}
					if (Recording){
						stopRecording();
					}
					WifiManager wifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
					wifiManager.setWifiEnabled(false);
				}

			}

			if (key.equals("streamPref")) {
				StreamPrefs = sharedPrefs.getString("streamPref", "32kbs");

				if (Playing) {
					stopPlaying();
				}
				if (Recording){
					stopRecording();
				}

				//do the change stream pref bit
				if (StreamPrefs.equals("32kbs")) {

					//((TextView)HomeTab.this.findViewById(R.id.CurrentStream)).setText("http://173.192.22.204:8024");
				    ((TextView)HomeTab.this.findViewById(R.id.CurrentStreamType)).setText(StreamPrefs);
				}
				if (StreamPrefs.equals("64kbs")) {
				//	((TextView)HomeTab.this.findViewById(R.id.CurrentStream)).setText("http://shoutcast2.tidyhosts.com:9032");
					((TextView)HomeTab.this.findViewById(R.id.CurrentStreamType)).setText(StreamPrefs);
				}

			}

			if (key.equals("recordPref")) {
				RecordPrefs =sharedPrefs.getString("recordPref", "64kbs");
				if (Playing) {
					stopPlaying();
				}
				if (Recording){
					stopRecording();
				}

				//do the change recording pref bit

				if (RecordPrefs.equals("32kbs")) {

				  // ((TextView)HomeTab.this.findViewById(R.id.RecordStream)).setText("http://173.192.22.204:8024");
				   ((TextView)HomeTab.this.findViewById(R.id.RecordStreamType)).setText(RecordPrefs);
				}
				else {
					//((TextView)HomeTab.this.findViewById(R.id.RecordStream)).setText("http://shoutcast2.tidyhosts.com:9032");
					((TextView)HomeTab.this.findViewById(R.id.RecordStreamType)).setText(RecordPrefs);
				}


			}
		}};

		@Override
	    protected Dialog onCreateDialog(int id) {
			AlertDialog dialog;
			AlertDialog.Builder builder;
			DialogInterface.OnClickListener listener;

			switch (id) {

			case CONFIRM_SETTINGS_DIALOG:
				builder = new AlertDialog.Builder(this);
				builder.setCancelable(false);
				builder.setTitle(String.format(getString(R.string.settings_title)));
				builder.setMessage(String.format(getString(R.string.settings_message)));
				//TODO why the heck does is shutup!
				listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								((TextView)findViewById(R.id.SongView)).setText("Not Playing");
								playstart =true;
								playstop = false;
								buttonPlayStart.setEnabled(playstart);
								buttonPlayStop.setEnabled(playstop);

								((TextView)findViewById(R.id.Recording)).setText("Not Recording");
								recstart = true;
								recstop = false;
								buttonRecStart.setEnabled(recstart);
								buttonRecStop.setEnabled(recstop);

								startActivity(new Intent(HomeTab.this, Preferences.class));
								// Dismiss the dialog to ensure OnDismissListeners are notified.
								dialog.dismiss();
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								// Cancel the dialog to ensure OnCancelListeners are notified.
								dialog.cancel();
								break;
						}
						// Remove the dialog so it is re-created next time it is required.
						removeDialog(CONFIRM_SETTINGS_DIALOG);
					}};
				builder.setPositiveButton(android.R.string.yes, listener);
				builder.setNegativeButton(android.R.string.no, listener);
				dialog = builder.create();
				break;


			case CONFIRM_WIFI_DIALOG:
				builder = new AlertDialog.Builder(this);
				builder.setCancelable(false);
				builder.setTitle(String.format(getString(R.string.default_title)));
				builder.setMessage(String.format(getString(R.string.default_message)));

				listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								SaveDefaultShared();
								// Dismiss the dialog to ensure OnDismissListeners are notified.
								dialog.dismiss();
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								// Cancel the dialog to ensure OnCancelListeners are notified.
								dialog.cancel();
								break;
						}
						// Remove the dialog so it is re-created next time it is required.
						removeDialog(CONFIRM_WIFI_DIALOG);
					}};
				builder.setPositiveButton(android.R.string.yes, listener);
				builder.setNegativeButton(android.R.string.no, listener);
				dialog = builder.create();
				break;


			case CONFIRM_STREAM_DIALOG:
				builder = new AlertDialog.Builder(this);
				builder.setCancelable(false);
				builder.setTitle(String.format(getString(R.string.stream_title)));
				builder.setMessage(String.format(getString(R.string.stream_message)));

				listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								SaveDefaultShared();
								// Dismiss the dialog to ensure OnDismissListeners are notified.
								dialog.dismiss();
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								// Cancel the dialog to ensure OnCancelListeners are notified.
								dialog.cancel();
								break;
						}
						// Remove the dialog so it is re-created next time it is required.
						removeDialog(CONFIRM_STREAM_DIALOG);
					}};
				builder.setPositiveButton(android.R.string.yes, listener);
				builder.setNegativeButton(android.R.string.no, listener);
				dialog = builder.create();
				break;

			case CONFIRM_RECORDING_DIALOG:
				builder = new AlertDialog.Builder(this);
				builder.setCancelable(false);
				builder.setTitle(String.format(getString(R.string.recording_title)));
				builder.setMessage(String.format(getString(R.string.recording_message)));

				listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								SaveDefaultShared();

								// Dismiss the dialog to ensure OnDismissListeners are notified.
								dialog.dismiss();
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								// Cancel the dialog to ensure OnCancelListeners are notified.
								dialog.cancel();
								break;
						}
						// Remove the dialog so it is re-created next time it is required.
						removeDialog(CONFIRM_RECORDING_DIALOG);
					}};
				builder.setPositiveButton(android.R.string.yes, listener);
				builder.setNegativeButton(android.R.string.no, listener);
				dialog = builder.create();
				break;

			case NOTIFY_SDCARD_DIALOG:
					builder = new AlertDialog.Builder(this);
					builder.setCancelable(false);
					builder.setTitle(String.format(getString(R.string.sdcard_title)));
					builder.setMessage(String.format(getString(R.string.sdcard_message)));

					listener = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								case DialogInterface.BUTTON_POSITIVE:
									//stop recording
									// Dismiss the dialog to ensure OnDismissListeners are notified.
									dialog.dismiss();
									break;

							}
							// Remove the dialog so it is re-created next time it is required.
							removeDialog(NOTIFY_SDCARD_DIALOG);
						}};
					builder.setPositiveButton(android.R.string.yes, listener);
					dialog = builder.create();
					break;


				case CONFIRM_DEFAULT_DIALOG:
					builder = new AlertDialog.Builder(this);
					builder.setCancelable(false);
					builder.setTitle(String.format(getString(R.string.default_title)));
					builder.setMessage(String.format(getString(R.string.default_message)));

					listener = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								case DialogInterface.BUTTON_POSITIVE:
									SaveDefaultShared();
									// Dismiss the dialog to ensure OnDismissListeners are notified.
									dialog.dismiss();
									break;
								case DialogInterface.BUTTON_NEGATIVE:
									// Cancel the dialog to ensure OnCancelListeners are notified.
									dialog.cancel();
									break;
							}
							// Remove the dialog so it is re-created next time it is required.
							removeDialog(CONFIRM_DEFAULT_DIALOG);
						}};
					builder.setPositiveButton(android.R.string.yes, listener);
					builder.setNegativeButton(android.R.string.no, listener);
					dialog = builder.create();
					break;


				case RETRY_CONNECT_DIALOG:
					builder = new AlertDialog.Builder(this);
					builder.setCancelable(false);
					builder.setTitle(String.format(getString(R.string.sdcard_title)));
					builder.setMessage(String.format(getString(R.string.sdcard_message)));

					listener = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								case DialogInterface.BUTTON_POSITIVE:
									//stop recording
									// Dismiss the dialog to ensure OnDismissListeners are notified.
									dialog.dismiss();
									break;

							}
							// Remove the dialog so it is re-created next time it is required.
							removeDialog(RETRY_CONNECT_DIALOG);
						}};
					builder.setPositiveButton(android.R.string.yes, listener);

					dialog = builder.create();
					break;



				default:
					dialog= null;

			}
			return dialog;

		}

	private void reload(){
	      startActivity(getIntent());
	      finish();
	}



	public void onClick(View v) {


		if (v == buttonMenu) {
			Timer timing = new Timer();
		    timing.schedule(new TimerTask() {

		    /**
		     * {@inheritDoc}
		     */
		     @Override
		     public void run() {
		    	 runOnUiThread(new Runnable() {
		    		 public void run() {
		    			 SaveShared();
		                 openOptionsMenu();
		                 RestoreShared();
		             }
		         });

		    }
		    }, 500);

		}

		if (v == buttonPlayStart) {

            if (!Playing){

            	((TextView)findViewById(R.id.SongView)).setText("Buffering...");
            	String stream = getString(R.string.CurrentStream);
            	startService(new Intent(this, PlayService.class));
            	playstart =false;
        		playstop = true;
        		Playing=(!Playing);
				buttonPlayStart.setEnabled(playstart);
				buttonPlayStop.setEnabled(playstop);


			    ((TextView)findViewById(R.id.SongView)).setText("Searching...");
			    Log.d("metadata", "start metadata");
			    MetaStart();

			}
		}
		if (v == buttonPlayStop) {

			if (Playing){
				stopService(new Intent(this, PlayService.class));
				playstart =true;
				playstop = false;
				Playing=(!Playing);

				buttonPlayStart.setEnabled(playstart);
				buttonPlayStop.setEnabled(playstop);
				Log.d("metadata", "stop metadata");
				MetaStop();
				((TextView)findViewById(R.id.SongView)).setText("Not Playing");
			}
		}

		if (v == buttonRecStart) {

            if (!Recording){
            	Recording=(!Recording);
            	recstart = false;
        		recstop = true;
        		startService(new Intent(this, RecordService.class));

            	buttonRecStart.setEnabled(recstart);
				buttonRecStop.setEnabled(recstop);

			    ((TextView)findViewById(R.id.Recording)).setText("Recording...");
			}
		}


		if (v == buttonRecStop) {

			if (Recording){

				Recording=(!Recording);
				recstart = true;
				recstop = false;
				stopService(new Intent(this, RecordService.class));
				buttonRecStart.setEnabled(recstart);
				buttonRecStop.setEnabled(recstop);
				((TextView)findViewById(R.id.Recording)).setText("Not Recording");
			}
		}

    }

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////

	private Runnable MetaTask = new Runnable() {
		public void run() {

			//This method runs in the same thread as the UI.

			//Do something to the UI thread here
			final TextView songview = (TextView) findViewById(R.id.SongView);



			Thread myThread = new Thread(){  //to prevent networking on ui thread ex
	            @Override
	            public void run() {
	            	metaData = GetMeta();
	            }
	        };

	        myThread.start();
			String ans = metaData;


			songview.setText(ans);

			MetaHandler.postDelayed(MetaTask, METADELAY);
		}

	};


	public void MetaStop (){
		if(MetaTask!=null){
			Log.d(TAG, "MetaTask stopped");
			MetaHandler.removeCallbacks(MetaTask);

		}
	}

	public void MetaStart () {
		Log.d(TAG, "MetaTask started");
		MetaHandler = new Handler();
		MetaHandler.postDelayed(MetaTask, METADELAY);

	}

	public String GetMeta() {

		final String META_STREAM = "http://173.192.22.204:8024/7.html";
		int pos;
		int x,count;
		String result;
		String page;

		BufferedReader in = null;


		HttpClient client = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();

		HttpGet request = new HttpGet(META_STREAM);
		request.setHeader("user-agent", "Mozilla/5.0");
		HttpResponse response = null;
		try {
			response = client.execute(request, localContext);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		result = "";

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(
			   		new InputStreamReader(
			        response.getEntity().getContent()));
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		String line = null;
	    try {
			while ((line = reader.readLine()) != null){
			  result += line + "\n";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    page = result;

		Spanned sresult = Html.fromHtml(page);

		page = sresult.toString();
		x = page.indexOf(",");
		count = 1;

		for (pos = x; pos < page.length(); pos++) {

		   	if ((byte)page.charAt(pos) == 44){
		   		count++;
		   		if (count == 7){
		   			break;
		   		}
		  	}
		}

		result = page.substring(pos+1);

		return result;
	}


	public boolean setCurrentTab(int i) {
	    if (getParent() instanceof MARPiratesActivity) {
	        ((MARPiratesActivity) getParent()).getTabHost().setCurrentTab(i);
	        return true;
	    }
	    return false;
	}


	public boolean onOptionsItemSelected(MenuItem item) {


		if 	(item.getItemId() ==R.id.About) {
			Toast.makeText(this, "You have chosen the " + "About" + " menu option",
                        Toast.LENGTH_SHORT).show();
			new AlertDialog.Builder(this).setTitle("About MarPirates").setMessage("We like to play all sorts of music and we look out for new bands and singers to air on our station.\n\nStation Boss\nBert Williams\n\nStation Manager\nJackie Frost\n\nRegular Djs\nDave Rogers\nEric Monaghan\nHammie Hamster\nJackie Frost\nJohn Freeman\nLee Morrison\nNick Catford\nPaul Jay\nRodger Dee\n\n\nGuest DJs\nDave Simpson\nGary Phillips\nMike Stand\nJohn Dwyer\nSteve Martin\nTony Hayes").setPositiveButton("Done",
			new DialogInterface.OnClickListener(){
			        public void onClick(DialogInterface paramDialogInterface, int paramInt)
			        {
			          Log.d("AlertDialog", "Done");
			        }
			      }).show();
            return true;
		}else if  (item.getItemId() ==   R.id.Settings){
			showDialog(CONFIRM_SETTINGS_DIALOG);
			return true;
        }else if (item.getItemId() == R.id.Save){
			Toast.makeText(this, "You have chosen the " + "Save" + " menu option",
                 Toast.LENGTH_SHORT).show();
				SaveShared();
			Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
			return true;
        }else if   (item.getItemId() == R.id.Default){
			Toast.makeText(this, "You have chosen the " + "Default" + " menu option",
                 Toast.LENGTH_SHORT).show();
				//put a warning in here
				showDialog(CONFIRM_DEFAULT_DIALOG);
				SaveDefaultShared();
				RestoreShared();

			return true;

        }else{
        	return super.onOptionsItemSelected(item);
		}
	}





	public boolean onCreateOptionsMenu(Menu menu) {
	      MenuInflater inflater = getMenuInflater();
	      inflater.inflate(R.menu.menu, menu);
	      return true;
	    }
};






