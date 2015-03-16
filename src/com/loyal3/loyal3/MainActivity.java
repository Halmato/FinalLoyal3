package com.loyal3.loyal3;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;


import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	// # Checks whether SP has the user registered. Sends to sign-up if not registered.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		String hasRegistered = getSharedPref("userDetails", "hasRegistered", "false");

		if(hasRegistered.equals("false"))	{
			Intent intent = new Intent(this, RegisterActivity.class);
			String uuid = UUID.randomUUID().toString();
			setSharedPreference("userDetails", "uuid", uuid);
			
			startActivity(intent);
			
			finish();
			
		} else {
			Intent intent = new Intent(this, ScanActivity.class);
			startActivity(intent);
			finish();
		}	
	}

	void setSharedPreference(String folder,String file, String value)	{
		SharedPreferences sharedPreferences = getSharedPreferences(folder, MODE_PRIVATE);
		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
		
		sharedPreferencesEditor.putString(file, value);
		sharedPreferencesEditor.apply();
	}
	
	String getSharedPref(String folderName, String fileName, String defaultValue)									{
		String value;								
		SharedPreferences SP = getSharedPreferences(folderName, MODE_PRIVATE);								
		value = SP.getString(fileName, defaultValue);								
		return value;								
	}						
	

}
