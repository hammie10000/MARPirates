package com.mar.MARPirates;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.spoledge.aacdecoder.MultiPlayer;
import com.spoledge.aacdecoder.PlayerCallback;
import com.bugsense.trace.BugSenseHandler;



public class PlayService extends Service implements PlayerCallback{
	  
	private static final String TAG = "com.mar.MARPirates-PlayService:";
	private MultiPlayer multiPlayer;
	
	private ProgressBar progress;
    private Handler uiHandler;
	
	////////////////////////////////////////////////////////////////////////////
	// PlayerCallback
	////////////////////////////////////////////////////////////////////////////

	private boolean playerStarted;
	
	public void playerStarted() {
       playerStarted = true;
    }
	
	/**
     * This method is called periodically by PCMFeed.
     *
     * @param isPlaying false means that the PCM data are being buffered,
     *          but the audio is not playing yet
     *
     * @param audioBufferSizeMs the buffered audio data expressed in milliseconds of playing
     * @param audioBufferCapacityMs the total capacity of audio buffer expressed in milliseconds of playing
     */
    public void playerPCMFeedBuffer( final boolean isPlaying,
                                     final int audioBufferSizeMs, final int audioBufferCapacityMs ) {
    	//for the test, no fancy graphics!!!
    }
    
    public void playerStopped( final int perf ) {
        playerStarted = false;
    }
    
    public void playerException( final Throwable t) {
        if (playerStarted) 
        	playerStopped( 0 );
    }
       
    public void playerMetadata( final String key, final String value ) {
        TextView tv = null;
/*
        if ("StreamTitle".equals( key ) || "icy-name".equals( key ) || "icy-description".equals( key )) {
            TextView txtMetaTitle = null;
			tv = txtMetaTitle;
        }
        else if ("StreamUrl".equals( key ) || "icy-url".equals( key )) {
            TextView txtMetaUrl = null;
			tv = txtMetaUrl;
        }
        else if ("icy-genre".equals( key )) {
            TextView txtMetaGenre = null;
			tv = txtMetaGenre;
        }
        else return;

        final TextView ftv = tv;

        uiHandler.post( new Runnable() {
            public void run() {
                ftv.setText( value );
            }
        }); 
        */
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Private
    ////////////////////////////////////////////////////////////////////////////

    private void start() {
    	stop();
/*
    	// we cannot do it in playerStarted() - it is too late:
    	txtMetaTitle.setText("");
    	txtMetaGenre.setText("");
    	txtMetaUrl.setText("");
*/
    	multiPlayer = new MultiPlayer( this, 1500, 700);
    	
    	multiPlayer.playAsync( getString(R.string.CurrentStream));
    }


    private void stop() {
    	if (multiPlayer != null) {
    		multiPlayer.stop();
    		multiPlayer = null;
    	}
    }
	
    public IBinder onBind(Intent paramIntent){
		return null;
	}

	public void onCreate(){
		
		Log.d("com.mar.MARPirates-PlayService:", "onCreate");
    
	}

	public void onDestroy(){
		Toast.makeText(this, "Play Service Stopped", 1).show();
		Log.d("com.mar.MARPirates-PlayService:", "onDestroy");
		super.onDestroy();
		stop();
	}

	public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2){
		super.onStartCommand(paramIntent, paramInt1, paramInt2);
		Toast.makeText(this, "Play Service onStart", 1).show();
		Log.d("com.mar.MARPirates-PlayService:", "onStart");
		start();
		return START_STICKY;

	}
}

