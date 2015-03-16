package com.loyal3.loyal3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends ActionBarActivity {

	private String name,gender,uuid,dob;
	private int width;
	private Button pPickDate;
	private int pYear, pMonth, pDay;
	static final int DATE_DIALOG_ID = 0;
	
	EditText etName;
	TextView tvDOB;
	RadioButton rbMale;
	RadioButton rbFemale;
	RadioGroup rgGender;
		
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
	    actionBar.setBackgroundDrawable(new ColorDrawable(0xFF800000));
		setContentView(R.layout.activity_profile);
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		
		width = displaymetrics.widthPixels;
		
		etName = (EditText) findViewById(R.id.etName);
		tvDOB = (TextView) findViewById(R.id.tvDOB);
		rbMale = (RadioButton) findViewById(R.id.rbMale);
		rbFemale = (RadioButton) findViewById(R.id.rbFemale);
		rgGender = (RadioGroup) findViewById(R.id.rgGender);
		
		name = getSharedPrefs("userDetails", "name", " ");
		gender = getSharedPrefs("userDetails","gender","");
		dob = getSharedPrefs("userDetails", "dob", "");
		uuid = getSharedPrefs("userDetails", "uuid", "");
		
		if(!name.equals(""))	{
			etName.setText(name.trim());
		}
		
		if(!dob.equals(""))	{
			tvDOB.setText(dob);
		}
		
		if(gender.equals("male"))	{
			rgGender.check(rbMale.getId());
		} else if(gender.equals("female"))	{
			rgGender.check(rbFemale.getId());
		} else {
			Toast.makeText(getBaseContext(), "gender: |"+gender+"|", Toast.LENGTH_LONG).show();

		}
	
		
		LinearLayout.LayoutParams lp1= new LinearLayout.LayoutParams(width/8,width/8);
		
		ImageView ivDOB = (ImageView) findViewById(R.id.ivDOB);
		ivDOB.setLayoutParams(lp1);
		
		ImageView ivName = (ImageView) findViewById(R.id.ivName);
		ivName.setLayoutParams(lp1);
		
		ImageView ivMale = (ImageView) findViewById(R.id.ivMale);
		ivMale.setLayoutParams(lp1);
		
		ImageView ivFemale = (ImageView) findViewById(R.id.ivFemale);
		ivFemale.setLayoutParams(lp1);
		
		Button btnSubmitProfile = (Button) findViewById(R.id.btn_submit);
		btnSubmitProfile.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				int correct = 1;
				
				name = etName.getText().toString();
				dob = tvDOB.getText().toString();
				
				if(rbMale.isChecked())	{
					gender = "male";
				} else if(rbFemale.isChecked())	{
					gender = "female";
				} else {
					Toast.makeText(getBaseContext(), "Please select a gender", Toast.LENGTH_LONG).show();
					correct = 0;
				}
				
				if(name.equals(""))	{
					name="empty";
				}
				
				if(dob.length()<2)	{
					correct = 0;
					Toast.makeText(getBaseContext(), "Please select a Date of Birth", Toast.LENGTH_LONG).show();
				}
				
				if(correct == 1)	{
					if(networkIsAvailable())	{
						
						String nameEncoded ="";
						try {
							nameEncoded = URLEncoder.encode(name.trim(), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							Toast.makeText(getBaseContext(), "Catch #15:32", Toast.LENGTH_LONG).show();

						}
						
						String url = "http://wilcostr.pythonanywhere.com/updateUser?uuid="+uuid+"&year="+pYear+"&month="+pMonth+"&day="+pDay+"&gender="+gender+"&name="+nameEncoded ;
						Toast.makeText(getBaseContext(), "URL: "+url, Toast.LENGTH_LONG).show();
						System.out.print(url);
						
						new updateUserInfo().execute(url);	
					} else {
						Toast.makeText(getBaseContext(), "Please ensure that you are connected to the internet", Toast.LENGTH_LONG).show();
					}
				} 
			}
		});

		pPickDate = (Button) findViewById(R.id.btnSetAge);

		/** Listener for click event of the button */
		pPickDate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDialog(DATE_DIALOG_ID);
			}
		});

		final Calendar cal = Calendar.getInstance();
		pYear = 1990; //cal.get(Calendar.YEAR);
		pMonth = 3;  //.get(Calendar.MONTH);
		pDay = 27;    //cal.get(Calendar.DAY_OF_MONTH);
		
	}

	

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, 
					pDateSetListener,
					pYear, pMonth, pDay);
		}
		return null;

	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/*############################################################################################
	 * get and set SharedPreferences
    #############################################################################################*/
	private String getSharedPrefs (String folder, String file, String defaultValue)	{
	  
		SharedPreferences sp = getSharedPreferences(folder, MODE_PRIVATE);
		return sp.getString(file, defaultValue);
	}
	private void setSharedPrefs (String folder, String file, String value)	{
		SharedPreferences sp = getSharedPreferences(folder, MODE_PRIVATE);
		SharedPreferences.Editor spe = sp.edit();
		spe.putString(file, value);
		spe.apply();

	}	
		
	private DatePickerDialog.OnDateSetListener pDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			pYear = year;
			pMonth = monthOfYear;
			pDay = dayOfMonth;
						
			updateDisplay();		
		}
	};

	private void updateDisplay() {
		String formattedMonth ="";
		if(pMonth+1 < 10)	{
			formattedMonth = "0"+(pMonth+1);
		} else {
			formattedMonth = ""+(pMonth + 1);
		}
		tvDOB.setText(pDay+" / "+formattedMonth+" / "+pYear);	
	}
	
	
	/*##############################################################################################
	 *   Update User Info /updateUser
    ###############################################################################################*/
	private class updateUserInfo extends AsyncTask<String, Void, String> {								

		protected void onPreExecute()	{
			super.onPreExecute();

		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {
			Toast.makeText(getBaseContext(), "Result: "+result, Toast.LENGTH_LONG).show();

			try{	
				if(result.equals("*success*"))	{
					Toast.makeText(getBaseContext(), "Your details has successfully been updated", Toast.LENGTH_LONG).show();
					
					setSharedPrefs("userDetails", "gender", gender);

					setSharedPrefs("userDetails", "name", name);
					setSharedPrefs("userDetails", "dob", dob);
					setSharedPrefs("userDetails", "allInfo", "true");
					
					
				} else {

				}
				
			

			} catch(Exception e)	{   	
				Toast.makeText(getBaseContext(), "Catch #08:28", Toast.LENGTH_LONG).show();
			}
		}	
	}	
	
	
	/*##############################################################################################
	 * 	Part of the ASYNC-TASK function
    ##############################################################################################*/
	public static String GET(String url)	{				
		InputStream inputStream = null;							
		String result = "";							
		try	{						
			// create HttpClient						
			HttpClient httpClient = new DefaultHttpClient();						

			// make GET request to the given URL						
			HttpResponse httpResponse = httpClient.execute(new HttpGet(url));						

			// receive response as inputStream						
			inputStream = httpResponse.getEntity().getContent();								

			// convert inputstream to string								
			if(inputStream != null)			{					
				result = convertInputStreamToString(inputStream);								
			}	else	{						
				result = "Did not work!";								
			}									
		}catch (Exception e)	{					
			Log.d("InputStream", e.getLocalizedMessage());						
		}							

		return result;							
	}					

	
	/*#######################################################################################################
	 *  Checks if network is available
	 ######################################################################################################*/
	private boolean networkIsAvailable() {								
		ConnectivityManager connectivityManager 								
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);								
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();								
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();								
	}		
	
	/*##############################################################################################
	 * 	Part of the ASYNC-TASK function. Builds InputStream
	##############################################################################################*/
	private static String convertInputStreamToString(InputStream inputStream) throws IOException{									

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));									
		String line = "";									
		String result = "";									
		while((line = bufferedReader.readLine()) != null)	{								
			result += line;								
		}									

		inputStream.close();									
		return result;									
	}	
	
	
	
	
	
	
	
	
}
