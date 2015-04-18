package com.loyal3.loyal3;

import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailsActivity extends ActionBarActivity {
	
	
	private String shopName, title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(0xFF800000));
		
		Bundle extras = getIntent().getExtras();
		shopName = extras.getString("shopName").trim();

		if(shopName.length() > 0)  {
			title = shopName.substring(0, 1).toUpperCase() + shopName.substring(1);

		} else {
			title =shopName;
		}
		
		actionBar.setTitle(title + " Details");
		setContentView(R.layout.activity_details);
		
		LinearLayout llDetailsMain = (LinearLayout) findViewById(R.id.llDetailsMain);
	
		TextView tvWebsite = new TextView(this);
		TextView tvHours = new TextView(this);
		TextView tvAddress = new TextView(this);
		TextView tvRequired = new TextView(this);
		TextView tv3ward = new TextView(this);
		
		
		
		if(getSharedPrefs(shopName, "website", "None").equals("None")) {
			tvWebsite.setVisibility(View.GONE);
		}
		
		if(getSharedPrefs(shopName, "hours", "None").equals("None")) {
			tvHours.setVisibility(View.GONE);
		}
		
		if(getSharedPrefs(shopName, "address", "None").equals("None")) {
			tvAddress.setVisibility(View.GONE);
		}
		
		if(getSharedPrefs(shopName, "required", "None").equals("None")) {
			tvRequired.setVisibility(View.GONE);
		}
		
		if(getSharedPrefs(shopName, "3ward", "None").equals("None")) {
			tv3ward.setVisibility(View.GONE);
		}
		
		
		
		tvWebsite.setText("Website Address: " + getSharedPrefs(shopName, "website", "None"));
		tvHours.setText("Trading Hours: "+getSharedPrefs(shopName, "hours", "Not Available"));
		tvAddress.setText("Located At: "+getSharedPrefs(shopName, "address", "Not Available"));
		tvRequired.setText("Required to get a Code: "+getSharedPrefs(shopName, "required", "Not Available"));
		tv3ward.setText("Given as 3ward: " + getSharedPrefs(shopName, "3ward", "Not Available"));
		
		
		llDetailsMain.addView(tvWebsite);
		llDetailsMain.addView(tvHours);
		llDetailsMain.addView(tvRequired);
		llDetailsMain.addView(tvAddress);
		llDetailsMain.addView(tv3ward);

		
		
		this.setContentView(llDetailsMain);
		
		
		
		
		
		
		
		
		
	}

	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.retrieve, menu);
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
