package com.mar.MARPirates;
/*
 * Credits must go to :
 * From The Code Project
 * ShoutcastStream Class By Dirk Reske | 10 Jun 2007 
 * http://www.codeproject.com/Articles/19125/ShoutcastStream-Class
 * 
 * SHOUTcast Stream Ripper By espitech | 14 Aug 2005
 * http://www.codeproject.com/Articles/11308/SHOUTcast-Stream-Ripper?msg=2160300#xx2160300xx
 * Both written in C#
 * 
 * From http://groups.google.com/group/android-developers/browse_thread/thread/c08b99c0f26fef7d
 * Android Developers Creating Comunal ShoutCasts Lib by ikalbeniz
 * 
 * Many thanks go to above for getting me started on this project.  It is just a block 
 * of code to grab the metadata from a shoutcast stream at the moment.  
 * 
 * With some of your help, (I hope) it will become functional to enable a radio station to 
 * stream to an Android phone.     	
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bugsense.trace.BugSenseHandler;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class RecordService extends Service
{
  
	private static final String TAG = "com.mar.MARPirates-AudioRecorder:";

	private static final String OldMetaDataHeader = null;
	
	URLConnection InputConnection = null;
	URLConnection OutputConnection = null;
	URLConnection StreamUrl = null;
	
	FileOutputStream fos = null;
	BufferedOutputStream writer = null;
	
	File OutputFile = null;
	
	
	
	private InputStream RecordingStream = null;
	
	final int BUFFER = 8192;
	
	byte[] buffer;
	
	private boolean isRunning = true;
          
    @Override
    public IBinder onBind(Intent intent) {
    	
    	return null;
    }
          
    /* (non-Javadoc)    
     * @see android.app.Service#onCreate()
     */
      
    @Override
    public void onCreate() {
    	super.onCreate();
                  
        //android.os.Debug.waitForDebugger();  // this line is key

        Log.d("com.mar.MARPirates-AudioRecorder:", "rec onCreate");
  	}


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	super.onStartCommand(intent, flags, startId);
                  
        //Announcement about starting
        Toast.makeText(this, "Starting the Record Service", Toast.LENGTH_SHORT).show();
                  
        //Start a Background thread
        isRunning = true;
        Thread backgroundThread = new Thread(new BackgroundThread());
        backgroundThread.start();
                  
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
                  
        //Stop the Background thread
        isRunning = false;
                  
        //Announcement about stopping
        Toast.makeText(this, "Stopping the Record Service", Toast.LENGTH_SHORT).show();
    }
          
    private class BackgroundThread implements Runnable{
    	public void run(){
         
    		do_download();
    		            
       }
    }
    
    private void ParseMetaInfo(String metainfo){
    	// fileName = Regex.Match(metadataHeader, 
    	// "(StreamTitle=')(.*)(';StreamUrl)").Groups[2].Value.Trim();
        
    	Pattern pattern = Pattern.compile("(StreamTitle=')(.*)(';StreamUrl)");
    	Matcher matcher = pattern.matcher(metainfo);
    }
    
    
    protected InputStream openUrl(String url) {
        
        InputStream stream = null;
        URLConnection connection = null;
		try {
			
			URL StreamUrl = new URL(url);
			
			connection = StreamUrl.openConnection();
			String serverPath ="/";
			connection.setRequestProperty("GET", serverPath  + " HTTP/1.0");
	        connection.setRequestProperty("User-Agent", "WinampMPEG/5.09");
	        connection.setRequestProperty("Icy-MetaData", "1");
	        connection.connect();
	        
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        try { 
        	int ok = HttpURLConnection.HTTP_OK; 
        	int ans = ((HttpURLConnection) connection).getResponseCode();
			stream = connection.getInputStream();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return stream;
    }

          
    public void do_download() {
    	int MetaInt = 0;
    	String MetaDataHeader;
    	String OldMetaDataHeader = null;
    	FileOutputStream writer =null;
    	BufferedInputStream input = null;
    	
		
        
        
        Log.d(TAG, "url.openStream()");
        InputStream myInput = openUrl(getString(R.string.RecordStream));
        
        
     // Set the output folder on the SDcard
	    File directory = new File("/sdcard/MARPirates/");
	    // Create the folder if it doesn't exist:
	    if (!directory.exists()) 
	    {
	        directory.mkdirs();
	    } 
	    // Set the output file stream up:
	    String Name = setOutputFile("00-mar-MAR-Recording-");
	    OutputStream myOutput = null;
		try {
			
			File file = new File(directory,Name);  //make the file
			myOutput = new FileOutputStream(file);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
        
	    
	 // Transfer bytes from the input file to the output file
	    byte[] buffer = new byte[1024];
	    int length;
	    
	    
		try {
			int total = 0;
			while (((length = myInput.read(buffer))>0) && isRunning){
				    myOutput.write(buffer, 0, length);
				    
				    myOutput.flush();
				    Log.d(TAG, "Streamin() read " + length);
				    total += length;
				    Log.d(TAG, "Streamout() totalwritten " + total);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    // Close and clear the streams
	    try {
			myOutput.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    try {
			myOutput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    try {
			myInput.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
       
		
    }

      
    private String setOutputFile(String Name){
    	String str1 =  Name + mydate() +  ".mp3";
    	String str2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MARPirates/";
    	//str2+str1;
    	return str1;
    }

    
    
    private File MakeFolder(String Name)throws IOException{
    	
    	File sdCard = Environment.getExternalStorageDirectory();
    	File folder = new File(sdCard.getAbsolutePath()+ Name);
        folder.mkdirs();
        
    	return folder;
    }
    
    private File MakeFile(File Folder,String Name) {
    	File file =new File(Folder,Name);
    	return file;
    }
  
    
   	public String mydate(){
   		Date localDate = Calendar.getInstance().getTime();
    	return new SimpleDateFormat("dd-MM-yyyy--hh-mm-ss").format(localDate);
   	}
  }