package org.urbanstew.SoundCloudDroid;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

/**
 * SoundCloudDroid is the main SoundCloud Droid activity.
 * <p>
 * It shows
 * whether SoundCloud Droid has been authorized to access a user
 * account, can initiate the authorization process, and can upload
 * a file to SoundCloud.
 * 
 * @author      Stjepan Rajko
 */
public class SoundCloudDroid extends Activity
{

    /**
     * The method called when the Activity is created.
     * <p>
     * Initializes the user interface.
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.main);
        
        mAuthorized = (CheckBox) this.findViewById(R.id.access_token_status);

        ((Button) this.findViewById(R.id.authorize_button))
        	.setOnClickListener(new OnClickListener()
	        {
				public void onClick(View arg0)
				{
					Intent authorizeIntent = new Intent(SoundCloudDroid.this, ObtainAccessToken.class);
					startActivity(authorizeIntent);
				}
	        });
        
        ((Button) this.findViewById(R.id.upload_button))
        	.setOnClickListener(new OnClickListener()
        	{
				public void onClick(View arg0)
				{
	        		uploadFile();					
				}
        	});

        ((Button) this.findViewById(R.id.me_button))
    	.setOnClickListener(new OnClickListener()
    	{
			public void onClick(View arg0)
			{
        		retreiveMe();					
			}
    	});}
        
    /**
     * The method called when the Activity is resumed.
     * <p>
     * Updates the UI to reflect whether SoundCloud Droid has been
     * authorized to access a user account.
     */
    public void onResume()
    {
    	super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAuthorized.setChecked(preferences.contains("oauth_access_token") && preferences.contains("oauth_access_token_secret"));
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        
        mView = menu.add("View reported defects and feature requests").setIcon(android.R.drawable.ic_dialog_info);
        mReport = menu.add("Report defect of feature request").setIcon(android.R.drawable.ic_dialog_alert);
        mJoinGroup = menu.add("Join discussion group").setIcon(android.R.drawable.ic_dialog_email);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	if(item == mView)
    	    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://code.google.com/p/soundclouddroid/issues/list")));    		
    	else if(item == mReport)
    	    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://code.google.com/p/soundclouddroid/issues/entry")));
    	else if(item == mJoinGroup)
    		startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://groups.google.com/group/soundcloud-droid/subscribe")));
    	else
    		return false;
    	return true;
    }
    
    /**
     * The method called when the upload button is pressed.
     * <p>
     * Invokes OIFileManager to select the file to be uplaoded, or
     * if OIFileManager is not installed it starts the browser
     * to download it. 
     */
    public void uploadFile()
    {
    	Intent intent = new Intent("org.openintents.action.PICK_FILE");
    	intent.setData(Uri.parse("file:///sdcard/"));
    	intent.putExtra("org.openintents.extra.TITLE", "Please select a file");
    	try
    	{
    		startActivityForResult(intent, 1);
    	} catch (ActivityNotFoundException e)
    	{
    		Intent downloadOIFM = new Intent("android.intent.action.VIEW");
    		downloadOIFM.setData(Uri.parse("http://openintents.googlecode.com/files/FileManager-1.0.0.apk"));
    		startActivity(downloadOIFM);
    	}
    }
    
    void retreiveMe()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

    	String consumerKey = getResources().getString(R.string.consumer_key);
        String consumerSecret = getResources().getString(R.string.s5rmEGv9Rw7iulickCZl);

    	SoundCloudRequest request = new SoundCloudRequest
    	(
    		consumerKey,
    		consumerSecret,
    		preferences.getString("oauth_access_token", ""),
    		preferences.getString("oauth_access_token_secret", "")
    	);
    	
    	String response = request.retreiveMe();
    	
    	Log.d(getClass().toString(), "Me complete, response=" + response);

		try {

    			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

    			Document dom = db.parse(new ByteArrayInputStream(response.getBytes("UTF-8")));
    			
    			String username = dom.getElementsByTagName("username").item(0).getFirstChild().getNodeValue();
    			Toast.makeText(this, "Your username is: " + username, Toast.LENGTH_LONG).show();
		}catch(Exception e) {
			e.printStackTrace();
		}

    }
    /**
     * The method called when the file to be uploaded is selected.
     */
    protected void onActivityResult(int requestCode,
            int resultCode, Intent data)
    {
    	if(data == null)
    		return;
    	
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	Log.d(this.getClass().toString(), "Uploading file:" + data.getData());
    	
        // WARNING: the following resources are not a part of the repository for security reasons
        // to build and test this app, you should register your build of the app with SoundCloud:
        //  http://soundcloud.com/settings/applications/new
        // and add your Consumer Key and Consumer Secret as string resources to the project.
        // (with names "consumer_key" and "s5rmEGv9Rw7iulickCZl", respectively)
    	String consumerKey = getResources().getString(R.string.consumer_key);
        String consumerSecret = getResources().getString(R.string.s5rmEGv9Rw7iulickCZl);

    	SoundCloudRequest request = new SoundCloudRequest
    	(
    		consumerKey,
    		consumerSecret,
    		preferences.getString("oauth_access_token", ""),
    		preferences.getString("oauth_access_token_secret", "")
    	);
    	
    	request.uploadFile(data.getData());
    }
    
    // checkbox indicating whether SoundCloud Droid has been authorized
    // to access a user account
    CheckBox mAuthorized;
    
    MenuItem mView, mReport, mJoinGroup;
}


