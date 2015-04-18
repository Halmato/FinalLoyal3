package com.loyal3.loyal3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends ActionBarActivity {

	private String name,gender,uuid,dob,oldPassword,newPassword,newPassword2,email;
	private int width,height;
	private Button pPickDate, btnRecoverDetails;
	private int pYear, pMonth, pDay;
	static final int DATE_DIALOG_ID = 0;
	private ProgressDialog pDialog;

	EditText etNewPassword,etNewPassword2, etOldPassword;
	EditText etName,etEmailProfile;
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
		height = displaymetrics.heightPixels;
		
		etName = (EditText) findViewById(R.id.etName);
		etEmailProfile = (EditText) findViewById(R.id.etEmailProfile);
		etOldPassword = (EditText) findViewById(R.id.etOldPassword);
		etNewPassword = (EditText) findViewById(R.id.etNewPassword);
		etNewPassword2 = (EditText) findViewById(R.id.etNewPassword2);
		
		
		tvDOB = (TextView) findViewById(R.id.tvDOB);
		rbMale = (RadioButton) findViewById(R.id.rbMale);
		rbFemale = (RadioButton) findViewById(R.id.rbFemale);
		rgGender = (RadioGroup) findViewById(R.id.rgGender);
		
		name = getSharedPrefs("userDetails", "name", "");
		gender = getSharedPrefs("userDetails","gender","");
		dob = getSharedPrefs("userDetails", "dob", "");
		uuid = getSharedPrefs("userDetails", "uuid", "");
		email = getSharedPrefs("userDetails", "recoveryEmail", "");
		
		if(!name.equals(""))	{
			etName.setText(name.trim());
		} else {
			etName.setHint("Name (Optional)");
		}
		
		if(!dob.equals(""))	{
			tvDOB.setText(dob);
		}
		
		if(gender.equals("male"))	{
			rgGender.check(rbMale.getId());
		} else if(gender.equals("female"))	{
			rgGender.check(rbFemale.getId());
		} 
	
		if(!etEmailProfile.equals("")) {
			etEmailProfile.setText(email);

		} else {
			etEmailProfile.setHint("Email Address (Optional)");
		}
	
		
		
		btnRecoverDetails = (Button) findViewById(R.id.btnEmailRecoveryProfile);
		btnRecoverDetails.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
				
				
				if(!email.isEmpty()) {
					builder.setTitle("Send account details to \n"+email+"?");
				
					builder.setPositiveButton("Send", new DialogInterface.OnClickListener() { 
						public void onClick(DialogInterface dialog, int which) {
				    
							String urlRecover = "http://www.loyal3.co.za/recover?email="+email;
				    	
							new RecoverAsyncTask().execute(urlRecover);
				    
						}
					});
				} else {
					builder.setTitle("Please update your email in order to recover your account details");
					
				}
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int which) {
				        dialog.cancel();
				    }
				});

				builder.show();
				
			}
		});
		
		
		
		
		
		
		
		
		
		
		
		
		LinearLayout llCake = (LinearLayout) findViewById(R.id.llCake);
		
		
		LinearLayout.LayoutParams lp1= new LinearLayout.LayoutParams(width/8,width/8);
		LinearLayout.LayoutParams lpll= new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);

		
		ImageView ivDOB = (ImageView) findViewById(R.id.ivDOB);
		ivDOB.setLayoutParams(lp1);
		tvDOB.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		Button btnSetAgeSizing = (Button) findViewById(R.id.btnSetAge);
		btnSetAgeSizing.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		llCake.setLayoutParams(lpll);

		
		ImageView ivName = (ImageView) findViewById(R.id.ivName);
		ivName.setLayoutParams(lp1);
		
		ImageView ivMale = (ImageView) findViewById(R.id.ivMale);
		ivMale.setLayoutParams(lp1);
		
		ImageView ivFemale = (ImageView) findViewById(R.id.ivFemale);
		ivFemale.setLayoutParams(lp1);
		
		ImageView ivEmailProfile = (ImageView) findViewById(R.id.ivEmailProfile);
		ivEmailProfile.setLayoutParams(lp1);
		
		Button btnSubmitProfile = (Button) findViewById(R.id.btn_submit);
		btnSubmitProfile.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				int correct = 1;
				
				name = etName.getText().toString();
				dob = tvDOB.getText().toString();
				email = etEmailProfile.getText().toString();
				
				if(rbMale.isChecked())	{
					gender = "male";
				} else if(rbFemale.isChecked())	{
					gender = "female";
				} else {
					Toast.makeText(getBaseContext(), "Please select a gender", Toast.LENGTH_LONG).show();
					correct = 0;
				}
				
		//		if(name.equals(""))	{
		//			name="empty";
		//		}
				
				if(dob.length()<2)	{
					correct = 0;
					Toast.makeText(getBaseContext(), "Please select a Date of Birth", Toast.LENGTH_LONG).show();
				}
				
				if(correct == 1)	{
					if(networkIsAvailable())	{
						
						String nameEncoded ="None";
						try {
							nameEncoded = URLEncoder.encode(name.trim(), "UTF-8");
						} catch (UnsupportedEncodingException e) {
							Toast.makeText(getBaseContext(), "Unsupported character used. Please use standard characters.", Toast.LENGTH_LONG).show();
						}
						
						String url = "http://www.loyal3.co.za/updateUser?uuid="+uuid+"&year="+pYear+"&month="+pMonth+"&day="+pDay+"&gender="+gender+"&name="+nameEncoded+"&email="+email ;
						
						new updateUserInfo().execute(url);	
						
						
					} else {
						Toast.makeText(getBaseContext(), "Please ensure that you are connected to the internet.", Toast.LENGTH_LONG).show();
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
		pYear = Integer.parseInt(getSharedPrefs("userDetails", "year", "1996")); //cal.get(Calendar.YEAR);
		pMonth = Integer.parseInt(getSharedPrefs("userDetails", "month", "3"));  //.get(Calendar.MONTH);
		pDay = Integer.parseInt(getSharedPrefs("userDetails", "day", "27"));;    //cal.get(Calendar.DAY_OF_MONTH);
		
		
		
		
		
		
		
		LinearLayout.LayoutParams lpIcon = new LinearLayout.LayoutParams((int)(width*0.08),(int)(height*0.05));
		
		ImageView ivOldPassword = (ImageView) findViewById(R.id.ivOldPassword);
		ImageView ivNewPassword = (ImageView) findViewById(R.id.ivNewPassword);
		ImageView ivNewPassword2 = (ImageView) findViewById(R.id.ivNewPassword2);
		
		ivOldPassword.setLayoutParams(lpIcon);
		ivNewPassword.setLayoutParams(lpIcon);
		ivNewPassword2.setLayoutParams(lpIcon);



		
		final LinearLayout llChangePassword = (LinearLayout) findViewById(R.id.llChangePassword);
		
		Button btnChangePasswordToggle = (Button) findViewById(R.id.btnChangePasswordToggle);
		
		btnChangePasswordToggle.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				if(llChangePassword.isShown()) {
					llChangePassword.setVisibility(View.GONE);
				} else {
					llChangePassword.setVisibility(View.VISIBLE);
				}
				
			}
		});
		
		
		
		
		
		Button btnSubmitNewPassword = (Button) findViewById(R.id.btnChangePassword);
		btnSubmitNewPassword.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				
				oldPassword = etOldPassword.getText().toString();
				newPassword = etNewPassword.getText().toString();
				newPassword2 = etNewPassword2.getText().toString();
				
				if(!newPassword.isEmpty() && !oldPassword.isEmpty() && !newPassword2.isEmpty()) {
					
					if(newPassword.equals(newPassword2)) {
						
						new ChangePasswordAsync().execute();
								
					} else {
						Toast.makeText(getBaseContext(), "Your new password and re-typed new password do not match", Toast.LENGTH_LONG).show();
					}				
				} else {
					Toast.makeText(getBaseContext(), "Please fill in all three fields", Toast.LENGTH_LONG).show();
				}
				
		
				
			}
		});
		

		
	}


	//end of onCreate :/   sies tiaan. jou code lyk so lelik!
	
	
	

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
		if (id == R.id.action_contact) {
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
			
			setSharedPrefs("userDetails", "year", Integer.toString(year));
			setSharedPrefs("userDetails", "month", Integer.toString(monthOfYear));
			setSharedPrefs("userDetails", "day", Integer.toString(dayOfMonth));
			
			updateDisplay();		
		}
	};

	private void updateDisplay() {
		String formattedMonth ="";
		String formattedDay = "";
		if(pMonth+1 < 10)	{
			formattedMonth = "0"+(pMonth+1);
		} else {
			formattedMonth = ""+(pMonth + 1);
		}
		
		if(pDay < 10)	{
			formattedDay = "0"+pDay;
		} else {
			formattedDay = ""+pDay;
		}
		
		tvDOB.setText(formattedDay+" / "+formattedMonth+" / "+pYear);	
	}
	
	
	/*##############################################################################################
	 *   Update User Info /updateUser
    ###############################################################################################*/
	private class updateUserInfo extends AsyncTask<String, Void, String> {								

		protected void onPreExecute()	{
			super.onPreExecute();
			
			pDialog = new ProgressDialog (ProfileActivity.this);
			pDialog.setMessage("Updating Information....");
			pDialog.show();
		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {

			try{	
				if(result.equals("*success*"))	{
					pDialog.dismiss();
					Toast.makeText(getBaseContext(), "Your details have successfully been updated", Toast.LENGTH_LONG).show();
					
					setSharedPrefs("userDetails", "gender", gender);
					setSharedPrefs("userDetails", "recoveryEmail", email);

					setSharedPrefs("userDetails", "name", name);
					setSharedPrefs("userDetails", "dob", dob);
					setSharedPrefs("userDetails", "allInfo", "true");
					finish();
					
				} else if(result.equals("DB Update Error: Users")) {
					Toast.makeText(getBaseContext(), "Did you change anything?", Toast.LENGTH_LONG).show();
					pDialog.dismiss();
					
				} else {

					Toast.makeText(getBaseContext(), "An error has occurred. Ensure that you aren't using special characters, and try again. ", Toast.LENGTH_LONG).show();
					pDialog.dismiss();
				}
				
			

			} catch(Exception e)	{   	
				Toast.makeText(getBaseContext(), "Catch #08:28", Toast.LENGTH_LONG).show();
				pDialog.dismiss();

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
	
	
	
	/*###########################################################################################################
	 * 	Called by Sign In click. Gets list of shops. Calls RefreshAllShopInfo[0]. (Params: username,password,uuid)
     ############################################################################################################*/
	private class ChangePasswordAsync extends AsyncTask<String, String, String> {

		protected void onPreExecute() {

			super.onPreExecute();
			
			pDialog = new ProgressDialog (ProfileActivity.this);
			pDialog.setMessage("Attempting to update password...");
			pDialog.show();
		}

		protected String doInBackground(String... arg0) {

			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://www.loyal3.co.za/changePassword");

			String result="";

			try {
				List<NameValuePair> nameValuePairsSignIn = new ArrayList<NameValuePair>(3);

				String newPasswordEncoded = URLEncoder.encode(newPassword, "UTF-8");
				String oldPasswordEncoded = URLEncoder.encode(oldPassword, "UTF-8");
				String uuid = getSharedPrefs("userDetails", "uuid", "0");
				
				nameValuePairsSignIn.add(new BasicNameValuePair("oldPassword", oldPasswordEncoded));
				nameValuePairsSignIn.add(new BasicNameValuePair("newPassword", newPasswordEncoded));
				nameValuePairsSignIn.add(new BasicNameValuePair("uuid", uuid));

				post.setEntity(new UrlEncodedFormEntity(nameValuePairsSignIn));

				HttpResponse response = client.execute(post);
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";

				while ((line = rd.readLine()) != null) {
					result +=line;
				}

				return result;

			} catch (IOException e) {
				e.printStackTrace();
				return "***"; 

			}	    
		}	//End of doInBackground

		protected void onPostExecute(String result) {
			
			if(result.equals("*invalid*"))	{
				Toast.makeText(getBaseContext(), "Invalid Username and Password", Toast.LENGTH_LONG).show();
				pDialog.dismiss();

			} else if(result.equals("***"))	{
				Toast.makeText(getBaseContext(), "Update Error.\nPlease only use standard characters, and try again.", Toast.LENGTH_LONG).show();
				pDialog.dismiss();

			} else if(result.equals("*success*")) {
				
				Toast.makeText(getBaseContext(), "Your password has been updated.\nA notification has been sent to your email (if provided)", Toast.LENGTH_LONG).show();
				finish();
				
			} else if(result.equals("*failed*")) {
				Toast.makeText(getBaseContext(), "Update Error. Please try again", Toast.LENGTH_LONG).show();
				pDialog.dismiss();
			} else {
				finish();
			}
		}	//End of onPostExecute
	}
	
	
	
	
	/*##############################################################################################
	 * 
     ##############################################################################################*/
	private class RecoverAsyncTask extends AsyncTask<String, Void, String> {								

		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog (ProfileActivity.this);
			pDialog.setMessage("Checking email validity....");
			pDialog.show();

		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {	
			
			try{	
			
				if(result.equals("*success*")) {
					
					Toast.makeText(getBaseContext(), "An email has been sent to \n"+email, Toast.LENGTH_LONG).show();
					
				} else if(result.equals("*invalid*")) {
					
					Toast.makeText(getBaseContext(), "The email is not valid\nFor further assistance, send an email to \nsupport@loyal3.co.za", Toast.LENGTH_LONG).show();

				} else {
					Toast.makeText(getBaseContext(), "An error has occured, please try again.\nFor further assistance, send an email to \nsupport@loyal3.co.za", Toast.LENGTH_LONG).show();

				}

			}catch(Exception e)	{
				Toast.makeText(getBaseContext(), "An error has occured, please try again.\nFor further assistance, send an email to \nsupport@loyal3.co.za", Toast.LENGTH_LONG).show();
			}	
			pDialog.dismiss();
		}	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
