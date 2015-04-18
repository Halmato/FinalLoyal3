package com.loyal3.loyal3;

import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ContactUsActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
	    actionBar.setBackgroundDrawable(new ColorDrawable(0xFF800000));	//Sets AB Color
		setContentView(R.layout.activity_contactus);
		
		
		Button btnSendEmail = (Button) findViewById(R.id.btnSendEmail);
		
		btnSendEmail.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				String refNumber = getSharedPrefs("userDetails", "uuid", "A4192bb-a8kLrE-a635s");
				String scrambledRefNumberFirst = refNumber.substring(4, refNumber.length());
				String scrambledRedNumberLast = refNumber.substring(0,4);
				
				
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","support@loyal3.co.za", null));
				intent.putExtra(Intent.EXTRA_SUBJECT, "Mobile Application Feedback");
				intent.putExtra(Intent.EXTRA_TEXT, "Unique Reference number: \n"+scrambledRefNumberFirst+scrambledRedNumberLast+"s"+"\n"+"\n");
				startActivity(Intent.createChooser(intent, "Choose an Email client :"));
			}
		});
		
		
		Button btnVisitFAQ = (Button) findViewById(R.id.btnVisitFAQ);
		
		btnVisitFAQ.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.loyal3.co.za/faq"));
				startActivity(browserIntent);
			}
		});
		
		
		
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_contact) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private String getSharedPrefs(String folder, String file,
			String defaultValue) {

		SharedPreferences sp = getSharedPreferences(folder, MODE_PRIVATE);
		return sp.getString(file, defaultValue);
	}
	
	
}
