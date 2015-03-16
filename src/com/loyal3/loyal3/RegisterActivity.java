package com.loyal3.loyal3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import android.support.v7.app.ActionBarActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class RegisterActivity extends ActionBarActivity {

	private String test;
	
	
	private int imageCounter;
	private String[] arrayOfImageIDsToDownload;
	private String username,password,uuid;
	private ProgressDialog pDialog;
	private LinearLayout llSignUp,llHaveAccount;
	private EditText etUsername,etPassword1,etPassword2,etUsernameSignIn,etPasswordSignIn;
	private int buttonCounterSignUp, buttonCounterSignIn;
	private int height,width;

	/*#################################################################################################
	 * onCreate-
	 ###################################################################################################*/
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_register);

		DisplayMetrics displaymetrics = new DisplayMetrics();			
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);			
		height = displaymetrics.heightPixels;			
		width = displaymetrics.widthPixels;			

		imageCounter = 0;
		uuid = getSharedPrefs("userDetails", "uuid", "");

		etUsername = (EditText) findViewById(R.id.username);
		etPassword1 = (EditText) findViewById(R.id.password1);
		etPassword2 = (EditText) findViewById(R.id.password2);
		etUsernameSignIn = (EditText) findViewById(R.id.etUsernameSignIn);
		etPasswordSignIn = (EditText) findViewById(R.id.etPasswordSignIn);
		/*######################################################################################
		 * Sets the little icons to the correct size. 		
 		#######################################################################################*/
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)(width*0.08),(int)(height*0.05));

		ImageView ivUsernameSignUp = (ImageView) findViewById(R.id.ivUsername_signUp);
		ivUsernameSignUp.setLayoutParams(lp);

		ImageView ivPasswordSignUp = (ImageView) findViewById(R.id.ivPassword_signUp);
		ivPasswordSignUp.setLayoutParams(lp);

		ImageView ivPasswordSignUp2 = (ImageView) findViewById(R.id.ivPassword_signUp2);
		ivPasswordSignUp2.setLayoutParams(lp);

		ImageView ivUsernameSignIn = (ImageView) findViewById(R.id.ivUsername_signIn);
		ivUsernameSignIn.setLayoutParams(lp);

		ImageView ivPasswordSignIn = (ImageView) findViewById(R.id.ivPassword_signIn);
		ivPasswordSignIn.setLayoutParams(lp);

		//End of setting icons.

		llSignUp = (LinearLayout) findViewById(R.id.llSignUp);
		llHaveAccount = (LinearLayout) findViewById(R.id.llHaveAccount);


		/*##########################################################################################################
		 * 	Sets Sign-Up / Sign-In UI  (appear/disappear)	
        ##########################################################################################################*/
		Button btnSignUpMain = (Button) findViewById(R.id.btnSignUp);
		btnSignUpMain.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {

				llHaveAccount.setVisibility(View.GONE);
				buttonCounterSignIn = 0;

				if(buttonCounterSignUp%2 == 0){
					llSignUp.setVisibility(View.VISIBLE);
					etUsername.requestFocus();
					buttonCounterSignUp++;

				}	else	{
					llSignUp.setVisibility(View.GONE);
					buttonCounterSignUp++;
				}


			}
		});

		Button btnSignInMain = (Button) findViewById(R.id.btnSingInMain);
		btnSignInMain.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {

				llSignUp.setVisibility(View.GONE);
				buttonCounterSignUp=0;

				if(buttonCounterSignIn%2 == 0){
					llHaveAccount.setVisibility(View.VISIBLE);
					buttonCounterSignIn++;
					etUsernameSignIn.requestFocus();

				}	else	{
					llHaveAccount.setVisibility(View.GONE);
					buttonCounterSignIn++;

				}
			}
		});

		/*##########################################################################################################
		 * 	Sign Up Button - starts SignUpAsyncTask.  Sign-in button  -  Starts SignInAsyncTask	
        ##########################################################################################################*/
		Button btnSignUp = (Button) findViewById(R.id.btnRegister);
		btnSignUp.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				if(networkIsAvailable()&&validDetailsSignUp())	{

					new SignUpAsyncTask().execute();

				} else if (!networkIsAvailable())	{
					Toast.makeText(getBaseContext(), "Please ensure that you have a stable connection to the Internet,  and try again", Toast.LENGTH_LONG).show();
				}
			}
		});

		Button btnSignIn = (Button) findViewById(R.id.btnSignIn);
		btnSignIn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(networkIsAvailable()&&validDetailsSignIn())	{

					imageCounter = -1;
					
					new SignIn().execute();

				} else if (!networkIsAvailable())	{
					Toast.makeText(getBaseContext(), "Please ensure that you have a stable connection to the Internet,  and try again", Toast.LENGTH_LONG).show();
				}
			}
		});	
	}	//END OF onCreate

	/*######################################################################################################
	 * Checks if network is available
	 ########################################################################################################*/
	private boolean networkIsAvailable() {								
		ConnectivityManager connectivityManager 								
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);								
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();								
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();								
	}	

	/*########################################################################################################
	 * Checks if SignUp details are valid, and sets username and password. 
 	##########################################################################################################*/
	private boolean validDetailsSignUp()	{

		String mUsername = etUsername.getText().toString();
		String mPassword = etPassword1.getText().toString();

		if(mUsername.isEmpty()||mPassword.isEmpty())	{
			Toast.makeText(getBaseContext(),"Please fill in all the fields",Toast.LENGTH_LONG).show();
			return false;
		}else if(!etPassword2.getText().toString().equals(mPassword))	{
			Toast.makeText(getBaseContext(),"Your passwords do not match",Toast.LENGTH_LONG).show();
			return false;
		}else if(mPassword.length()< 5)	{
			Toast.makeText(getBaseContext(),"Your password needs to consist of at least 5 characters",Toast.LENGTH_LONG).show();
			return false;
		}else if(mUsername.length() < 5)	{
			Toast.makeText(getBaseContext(),"Your username needs to consist of at least 5 characters",Toast.LENGTH_LONG).show();
			return false;
		}else{
			username = mUsername;
			password = mPassword;
			return true;
		}
	}

	/*###########################################################################################################
	 * 	HTTP-POST-REQUEST  '/signIn'. Sends username, password, uuid. Receives list of shopNames. "" if empty
 	############################################################################################################*/
	/*private class SignInAsyncTask extends AsyncTask<String, String, String> {

		protected void onPreExecute() {

			super.onPreExecute();

			pDialog = new ProgressDialog (RegisterActivity.this);
			pDialog.setMessage("Attempting to Sign In and Retrieve Data.\nThis may take a minute...");
			pDialog.show();
		}

		protected String doInBackground(String... arg0) {

			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://wilcostr.pythonanywhere.com/signIn");
			String result="";

			try {

				List<NameValuePair> nameValuePairsSignIn = new ArrayList<NameValuePair>(3);

				nameValuePairsSignIn.add(new BasicNameValuePair("username", username));
				nameValuePairsSignIn.add(new BasicNameValuePair("password", password));
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
				return "**"; 

			}	    
		}	//End of doInBackground

		protected void onPostExecute(String result) {

			if(result.equals("*invalid*"))	{
				Toast.makeText(getBaseContext(), "Invalid Username and Password", Toast.LENGTH_LONG).show();
				pDialog.dismiss();


			}else if(result.equals("**"))	{
				Toast.makeText(getBaseContext(), "Sign In Error. Please Try Again", Toast.LENGTH_LONG).show();
				pDialog.dismiss();
				Intent intent = getIntent();
				finish();
				startActivity(intent);

			} else {

				try{

					setSharedPrefs("userDetails", "listOfShops", result.trim());

					shopsListArray = createArrayFromString(result);
					shopsListArrayLength = shopsListArray.length;

					if(shopsListArrayLength > 0 && (!shopsListArray[0].equals("")) )	{

						String urlRefresh = "http://wilcostr.pythonanywhere.com/refresh?uuid="+uuid+"&shop="+shopsListArray[counter1].trim();

						new HttpAsyncTaskRefresh().execute(urlRefresh);

					}else{Toast.makeText(getBaseContext(), "No Shops Available", Toast.LENGTH_LONG).show();Intent intent = new Intent(RegisterActivity.this, ScanActivity.class);setSharedPrefs("userDetails", "hasRegistered", "true");pDialog.dismiss();startActivity(intent);finish();}
					//if no shops available, starts ScanActivity. 

				}catch(Exception e)	{
					Toast.makeText(getBaseContext(), "Error #22.  Please try again.", Toast.LENGTH_LONG).show();
					Intent intent = getIntent();
					finish();
					startActivity(intent);
					pDialog.dismiss();

				}
			}	
		}	//End of onPostExecute
	}*/

	/*##############################################################################################
	 *   HTTP-GET-REQUEST to /refresh scanCount,maxScans and imageIDs for respective shopName.
	###############################################################################################*/
	/*private class HttpAsyncTaskRefresh extends AsyncTask<String, Void, String> {

		protected void onPreExecute()	{
			super.onPreExecute();
		}

		protected String doInBackground(String... urls) {

			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {	

			try{	

				JSONObject json = new JSONObject(result);
				String imageIDsString = json.getString("imageIDs").trim();	

				if(counter1 <shopsListArrayLength)	{

					String scanCountFromLoop = json.getString("scanCount");	
					String maxScansFromLoop = json.getString("maxScans");	

					setSharedPreference(shopsListArray[counter1].trim(), "scanCount", scanCountFromLoop);
					setSharedPreference(shopsListArray[counter1].trim(), "maxScans", maxScansFromLoop);
					setSharedPreference(shopsListArray[counter1].trim(), "imageIDs", imageIDsString);

					counter1++;


					if(counter1 < shopsListArrayLength)	{

						String urlRefreshAgain = "http://wilcostr.pythonanywhere.com/refresh?uuid="+uuid+"&shop="+shopsListArray[counter1].trim();
						new HttpAsyncTaskRefresh().execute(urlRefreshAgain);		//Calls himself if there are still stuff left

					} else {

						String urlRefreshAgain = "http://wilcostr.pythonanywhere.com/refresh?uuid="+uuid+"&shop=advertshop";
						new HttpAsyncTaskRefresh().execute(urlRefreshAgain);		//Calls the 1st image of Advertshop. 
					}


				} else {

					setSharedPreference("advertshop", "imageIDs", imageIDsString);
					amountOfAdverts = createArrayFromString(imageIDsString).length;





					if(!signUp.equals("true"))	{
						//Start downloading images
						String imagesOfShopString = getSharedPref(shopsListArray[counter2].trim(), "imageIDs", "");
						String[] imagesOfShopsArray = createArrayFromString(imagesOfShopString);
						String imageLogoOfShop = imagesOfShopsArray[0].trim();

						String urlImage = "http://wilcostr.pythonanywhere.com/downloadImage?shop="+shopsListArray[counter2].trim()+"&image="+imageLogoOfShop+"&res="+screenSize;

						new HttpAsyncTaskDownloadImage().execute(urlImage);

					} else {
						downloadAdvertImages();	
					}

				}

			}catch(Exception e)	{
				Toast.makeText(getBaseContext(), "Catch #201", Toast.LENGTH_LONG).show();

			}
		}
	 */

	/*##############################################################################################
	 *   HTTP-GET-REQUEST to download Images. advert0_shopname = shopLogo. Die res is adverts.
	###############################################################################################*/
	/*private class HttpAsyncTaskDownloadImage extends AsyncTask<String, Void, String> {								

		protected String doInBackground(String... urls) {
			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {

			try {	

				String imagesOfShopString = getSharedPref(shopsListArray[counter2].trim(), "imageIDs", "");
				String[] imagesOfShopsArray = createArrayFromString(imagesOfShopString);
				String imageLogoOfShop = imagesOfShopsArray[0].trim();

				decodeBitmapBase64(result, imageLogoOfShop);

				counter2++;

				if(counter2 < shopsListArrayLength)	{
					imagesOfShopString = getSharedPref(shopsListArray[counter2].trim(), "imageIDs", "");
					imagesOfShopsArray = createArrayFromString(imagesOfShopString);
					imageLogoOfShop = imagesOfShopsArray[0].trim();

					String urlImage = "http://wilcostr.pythonanywhere.com/downloadImage?shop="+shopsListArray[counter2].trim()+"&image="+imageLogoOfShop+"&res="+screenSize();

					new HttpAsyncTaskDownloadImage().execute(urlImage);
				} else {
					downloadAdvertImages();
				}


			} catch(Exception e)	{   	
				Log.v("JSONObject-Creation", e.getLocalizedMessage());
				Toast.makeText(getBaseContext(), "catch5", Toast.LENGTH_LONG).show();

			}
		}	
	}*/

	/*##############################################################################################
	 *   HTTP-GET-REQUEST to /RETRIEVE Adverts.
	 ###############################################################################################*/
	/* private class HttpAsyncTaskRetrieveAdverts extends AsyncTask<String, Void, String> {

	 	protected void onPreExecute()	{
	 		super.onPreExecute();

	 	}

	 	protected String doInBackground(String... urls) {

	 		return GET(urls[0]);	}							

	 	protected void onPostExecute(String result) {	

	 		try{	

	 		    JSONObject json = new JSONObject(result);
	 		   String imageIDsString = json.getString("imageIDs");	


	 		    if(counter2 < 0)	{			//counter2 starts at -1

	 		    }



	 		    setSharedPreference("advertshop", "imageIDs", imageIDsString);

	 			Intent intent = new Intent(RegisterActivity.this, ScanActivity.class);
	 			setSharedPreference("userDetails", "hasRegistered", "true");
	 			pDialog.dismiss();
	 			startActivity(intent);
	 			finish();


	 		} catch(Exception e)	{Toast.makeText(getBaseContext(), "Couldnt set advertString", Toast.LENGTH_LONG).show();pDialog.dismiss();}


	 	}
	}	
	 */

	/*##############################################################################################
	 *  Starts the downloading of Padverts. Sends to HttpAsyncTaskDownloadAdverts.
	###############################################################################################*/
	/*private void downloadAdvertImages(){

		String advertImagesString = getSharedPref("advertshop", "imageIDs", "");
		String[] advertImagesArray = createArrayFromString(advertImagesString);
		String firstAdvertID = advertImagesArray[counter3].trim();

		String urlAdvertImageToDownload = "http://wilcostr.pythonanywhere.com/downloadImage?shop=advertshop&image="+firstAdvertID+"&res="+screenSize();

		new HttpAsyncTaskDownloadAdverts().execute(urlAdvertImageToDownload);
	}*/

	/*##############################################################################################
	 *  Downloads Premium adverts. If all Padverts are downloaded, starts ScanActivity. 
	###############################################################################################*/
	/*private class HttpAsyncTaskDownloadAdverts extends AsyncTask<String, Void, String> {								

		protected String doInBackground(String... urls) {
			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {		

			String advertImagesString = getSharedPref("advertshop", "imageIDs", "");
			String[] advertImagesArray = createArrayFromString(advertImagesString);
			String imageID = advertImagesArray[counter3].trim();

			decodeBitmapBase64(result, imageID);	// decodes and saves Bitmap to internal storage.

			counter3++;

			if(counter3 < amountOfAdverts)	{

				String imageID2 = advertImagesArray[counter3].trim();
				String urlAdvertImageToDownload = "http://wilcostr.pythonanywhere.com/downloadImage?shop=advertshop&image="+imageID2+"&res="+screenSize();
				new HttpAsyncTaskDownloadAdverts().execute(urlAdvertImageToDownload);

			} else {
				Intent intent = new Intent(RegisterActivity.this, ScanActivity.class);
				setSharedPrefs("userDetails", "hasRegistered", "true");
				startActivity(intent);
				finish();
			}
		}
	}*/

	/*##############################################################################################
	 * 	Returns a Bitmap that was loaded from internal storage (imageName)
	##############################################################################################*/
	/*private Bitmap imageLoadedFromInternalStorage(String imageName)
	{

		try {
			File f=new File("/data/data/loyal3/app_data/imageDir/", imageName+".jpg");
			Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
			return b;
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			return null;
		}

	}*/

	/*##############################################################################################
	 * 	Decodes Base64 encoded Bitmap, and saves it to a new file name after the instance of imageID
	##############################################################################################*/
	/*private void decodeBitmapBase64 (String encodedString, String imageName)	{
		try {

			byte[] decodedString = Base64.decode(encodedString.getBytes("UTF-8"), Base64.DEFAULT);
			Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

			saveBitmapToInternalStorage(decodedImage, imageName);

		} catch (Exception e) {

			Toast.makeText(getBaseContext(), "Catch # 12:17", Toast.LENGTH_LONG).show();

		}
	}	*/

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

	/*##############################################################################################
	 * Creates array from CSV-string
	##############################################################################################*/	
	/*public String[] createArrayFromString (String shopsString)	{

		String[] resultArray = shopsString.trim().split(",");

		if(resultArray[0].equals(""))	{
			removeElt(resultArray, 0);
		}
		return resultArray;
	}*/

	/*##############################################################################################
	 * Removes [0] from array  if it is empty. Last [] is null then.
	##############################################################################################*/       
	/*public static void removeElt( String [] arr, int remIndex )
	{
		for ( int i = remIndex ; i < arr.length - 1 ; i++ )
		{
			arr[ i ] = arr[ i + 1 ] ; 
		}
	}*/

	/*##############################################################################################
	 * Checks if details given at sign-in is valid
	##############################################################################################*/  
	private boolean validDetailsSignIn()	{

		username = etUsernameSignIn.getText().toString();
		password = etPasswordSignIn.getText().toString();

		if(username.isEmpty()||password.isEmpty())	{
			Toast.makeText(getBaseContext(),"Please fill in all the fields",Toast.LENGTH_LONG).show();
			return false;
		}else{
			return true;
		}
	}

	/*##############################################################################################
	 * 	Part of the ASYNC-TASK function (GET)
	 ##############################################################################################*/
	public static String GET(String url)	{

		InputStream inputStream = null;							
		String result = "";							
		try	{						
			// create HttpClient						
			DefaultHttpClient httpClient = new DefaultHttpClient();						
			HttpGet httpGet = new HttpGet(url);						


			// make GET request to the given URL						
			HttpResponse httpResponse = httpClient.execute(httpGet);						
			result = "here";	


			// receive response as inputStream						
			inputStream = httpResponse.getEntity().getContent();								

			// convert inputstream to string								
			if(inputStream != null)			{					
				result = convertInputStreamToString(inputStream);

			} else	{						
				//    	             result = "*error1*";								
			}									
		}catch (Exception e)	{	
			result = ""+e.getLocalizedMessage().toString();
			Log.d("InputStream!!!!!!!!!!!!!!!!!!!!!!!", e.getLocalizedMessage());						
		}					

		return result;							
	}					

	/*###########################################################################################################
	 * 	Called by Sign In click. Gets list of shops. Calls RefreshAllShopInfo[0]. (Params: username,password,uuid)
     ############################################################################################################*/
	private class SignIn extends AsyncTask<String, String, String> {

		protected void onPreExecute() {

			super.onPreExecute();
			
			pDialog = new ProgressDialog (RegisterActivity.this);
			pDialog.setMessage("Attempting to Sign In and Retrieve Data.\nThis may take a minute...");
			pDialog.show();
		}

		protected String doInBackground(String... arg0) {

			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://wilcostr.pythonanywhere.com/signIn");

			String result="";

			try {
				List<NameValuePair> nameValuePairsSignIn = new ArrayList<NameValuePair>(3);

				String passwordEncoded = URLEncoder.encode(password, "UTF-8");
				String usernameEncoded = URLEncoder.encode(username, "UTF-8");
				
				nameValuePairsSignIn.add(new BasicNameValuePair("username", usernameEncoded));
				nameValuePairsSignIn.add(new BasicNameValuePair("password", passwordEncoded));
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

			}else if(result.equals("***"))	{
				Toast.makeText(getBaseContext(), "Sign In Error. Please Try Again", Toast.LENGTH_LONG).show();
				pDialog.dismiss();
				Intent intent = getIntent();
				finish();
				startActivity(intent);

			} else {

				try{
					String formattedResult = result + ",";
					setSharedPrefs("userDetails", "listOfShops", formattedResult.trim());

					if(! (getListOfShopsArray(formattedResult).length == 1 && getListOfShopsArray(formattedResult)[0].equals("")) && getListOfShopsArray(formattedResult).length != 0)	{

						String url = "http://wilcostr.pythonanywhere.com/refresh?shop=advertshop&uuid="+uuid;
						
						new RefreshAllShopInfo().execute(url);

					} else {
						Toast.makeText(getBaseContext(), "No Shops Available", Toast.LENGTH_LONG).show();
						
						imageCounter = 0;
						
						String url = "http://wilcostr.pythonanywhere.com/refresh?uuid="+uuid+"&shop=advertshop";
						
						new RefreshAllShopInfo_signUp().execute(url);
						
					}
					//if no shops available, starts ScanActivity. 

				}catch(Exception e)	{
					//Restarts the app. 
					Toast.makeText(getBaseContext(), "Error #22.  Please try again.", Toast.LENGTH_LONG).show();}
					/*Intent intent = getIntent();
					finish();
					startActivity(intent);*/
					/*pDialog.dismiss();

				}*/
			}	
		}	//End of onPostExecute
	}

	/*#################################################################################################
	 * Gets triggered by clicking "Sign Up" button. 
	 ###################################################################################################*/
	private class SignUpAsyncTask extends AsyncTask<String, String, String> {

		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog (RegisterActivity.this);
			pDialog.setMessage("Registering ....");
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {

			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://wilcostr.pythonanywhere.com/addUser");
			String result="";

			try {

				String passwordEncoded = URLEncoder.encode(password, "UTF-8");
				String usernameEncoded = URLEncoder.encode(username, "UTF-8");
				
				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("uuid", uuid));
				nameValuePairs.add(new BasicNameValuePair("username", usernameEncoded));
				nameValuePairs.add(new BasicNameValuePair("password", passwordEncoded));
				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = client.execute(post);
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";

				while ((line = rd.readLine()) != null) {
					result +=line;
				}

				return result;

			} catch (IOException e) {
				e.printStackTrace();
				return "Catch #09:01\nOops! Something went wrong. Please try again.";
			}

		}

		protected void onPostExecute(String result) {

			if(result.equals("*success*"))	{
				
				imageCounter = 0;
				
				String url = "http://wilcostr.pythonanywhere.com/refresh?uuid="+uuid+"&shop=advertshop";
				
				new RefreshAllShopInfo_signUp().execute(url);

			} else if(result.equals("*duplicate username*"))	{
				Toast.makeText(getBaseContext(), "That username is taken. Please choose a new username.", Toast.LENGTH_LONG).show();
				pDialog.dismiss();
				EditText et = (EditText)findViewById(R.id.username);
				et.requestFocus();
				et.setText("");
				
			} else {
				Toast.makeText(getBaseContext(), "Error #101\nPlease Try Again."+result, Toast.LENGTH_LONG).show();

				recreate();
			}
			super.onPostExecute(result);	//weet nie waarvoor dit is nie
		}

	}	
	
	/*##############################################################################################
	 * 	Called by SignIn. Loops through /refresh (scanCounts etc). Calls DownloadLogos_refresh. (Params: shop, uuid)
     ##############################################################################################*/
	private class RefreshAllShopInfo extends AsyncTask<String, Void, String> {								

		protected void onPreExecute() {
			super.onPreExecute();

		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {	
			

			try{	
				String shopName;
				JSONObject json = new JSONObject(result);	

				
				if(imageCounter == -1)	{
					shopName = "advertshop";
				} else	{
					shopName = getListOfShopsArray()[imageCounter].trim();
				}
				
					
				String scanCount = json.getString("scanCount");								
				String maxScans = json.getString("maxScans");	
				String imageIDsString = json.getString("imageIDs").trim();

				setSharedPrefs(shopName, "scanCount", scanCount);
				setSharedPrefs(shopName, "maxScans", maxScans);
				setSharedPrefs(shopName, "imageIDs", imageIDsString);

				imageCounter++;
				
				if(imageCounter < getListOfShopsArray().length)	{

					pDialog = new ProgressDialog(RegisterActivity.this);
					pDialog.setMessage("Calibrating articulate splines");
					pDialog.show();

					String url = "http://wilcostr.pythonanywhere.com/refresh?shop="+getListOfShopsArray()[imageCounter].trim()+"&uuid="+uuid;

					new RefreshAllShopInfo().execute(url);

				} else {

					imageCounter = 0;
					arrayOfImageIDsToDownload = getArrayOfMissingImages(getLogoIDs(getListOfShopsArray()));
					
					String url = "http://wilcostr.pythonanywhere.com/downloadImage?image="+arrayOfImageIDsToDownload[imageCounter].trim()+"&res="+screenSize();

					Toast.makeText(getBaseContext(), "TEST:"+url, Toast.LENGTH_LONG).show();

					new DownloadLogos_refresh().execute(url);
				}

			}catch(Exception e)	{
				Toast.makeText(getBaseContext(), "Catch #21:05.", Toast.LENGTH_LONG).show();
			}	
			pDialog.dismiss();
		}	
	}

	/*##############################################################################################
	 * 	Called by SignUp. User /refresh to get "advertshop"s imageIDs. Calls DownloadAdverts_refresh. (Params: shop, uuid)
     ##############################################################################################*/
	private class RefreshAllShopInfo_signUp extends AsyncTask<String, Void, String> {								

		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {							
			try{	
				JSONObject json = new JSONObject(result);	
			
				String shopName = "advertshop";
				String imageIDsString = json.getString("imageIDs").trim();

				setSharedPrefs(shopName, "imageIDs", imageIDsString);
				
				imageCounter = 0;
				arrayOfImageIDsToDownload = getArrayOfMissingImages(getImageIDs(shopName));

				String url = "http://wilcostr.pythonanywhere.com/downloadImage?image="+arrayOfImageIDsToDownload[imageCounter]+"&res="+screenSize();

				pDialog = new ProgressDialog(RegisterActivity.this);
				pDialog.setMessage("Calibrating articulate splines");
				pDialog.show();

				new DownloadAdverts_refresh().execute(url);
				
			}catch(Exception e)	{
				Toast.makeText(getBaseContext(), "Catch #21:06\n"+"Failed! Please ensure that you have a stable Internet Connection, or that the QR-Code is valid.", Toast.LENGTH_LONG).show();
			}	
			pDialog.dismiss();
		}	
	}
	
	/*##############################################################################################
	 * 	Called by SignUp. User /refresh to get "advertshop"s imageIDs. Calls DownloadAdverts_refresh. (Params: shop, uuid)
     ##############################################################################################*/
	/*private class RefreshAllShopInfo_signIn extends AsyncTask<String, Void, String> {								

		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {							
			try{	
				JSONObject json = new JSONObject(result);	
			
				String shopName = "advertshop";
				String imageIDsString = json.getString("imageIDs").trim();

				setSharedPrefs(shopName, "imageIDs", imageIDsString);
				
				imageCounter = 0;
				arrayOfImageIDsToDownload = getArrayOfMissingImages(getImageIDs(shopName));

				String url = "http://wilcostr.pythonanywhere.com/downloadImage?image="+arrayOfImageIDsToDownload[imageCounter]+"&res="+screenSize();

				pDialog = new ProgressDialog(RegisterActivity.this);
				pDialog.setMessage("Calibrating articulate splines");
				pDialog.show();

				new DownloadAdverts_refresh().execute(url);
				
			}catch(Exception e)	{
				Toast.makeText(getBaseContext(), "Catch #21:06\n"+"Failed! Please ensure that you have a stable Internet Connection, or that the QR-Code is valid.", Toast.LENGTH_LONG).show();
			}	
			pDialog.dismiss();
		}	
	}	*/
	
	/*##############################################################################################
	 *   Called by RefreshAllShopInfo. Loops through /downloadImages (logos). Calls DownloadAdverts_refresh. (Params: image, res) 
 	###############################################################################################*/
	private class DownloadLogos_refresh extends AsyncTask<String, Void, String> {								

		protected void onPreExecute()	{
			super.onPreExecute();
		}

		protected String doInBackground(String... urls) {

			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {							
			try{	
				Bitmap bmap = decodeBitmapBase64(result);
				saveBitmapToInternalStorage(bmap, arrayOfImageIDsToDownload[imageCounter].trim());

			} catch(Exception ignore)	{   	
				Toast.makeText(getBaseContext(), "catch #21:30", Toast.LENGTH_LONG).show();
			}

			imageCounter++;

			
			if(imageCounter < arrayOfImageIDsToDownload.length)	{

				String url = "http://wilcostr.pythonanywhere.com/downloadImage?image="+arrayOfImageIDsToDownload[imageCounter].trim()+"&res="+screenSize();
				new DownloadLogos_refresh().execute(url);

			} else {

				imageCounter = 0; 

				arrayOfImageIDsToDownload = getArrayOfMissingImages(getImageIDs("advertshop"));

				String url = "http://wilcostr.pythonanywhere.com/downloadImage?image="+arrayOfImageIDsToDownload[imageCounter].trim()+"&res="+screenSize();

				new DownloadAdverts_refresh().execute(url);

			}
		}	
	}

	/*##############################################################################################
	 *  Called by DownloadLogos_refresh. Loops through /downloadImages (adverts). Starts ScanActivity. (Params: image,res)
 	###############################################################################################*/
	private class DownloadAdverts_refresh extends AsyncTask<String, Void, String> {								

		protected void onPreExecute()	{
			super.onPreExecute();

		}

		protected String doInBackground(String... urls) {

			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {	

			try{	
				Bitmap bmap = decodeBitmapBase64(result);
				saveBitmapToInternalStorage(bmap, arrayOfImageIDsToDownload[imageCounter].trim());

			} catch(Exception ignore)	{   	
				Toast.makeText(getBaseContext(), "catch #21:30", Toast.LENGTH_LONG).show();
			}

			imageCounter++;
			
			if(imageCounter < arrayOfImageIDsToDownload.length)	{

				String url = "http://wilcostr.pythonanywhere.com/downloadImage?image="+arrayOfImageIDsToDownload[imageCounter].trim()+"&res="+screenSize();
				
				new DownloadAdverts_refresh().execute(url);

			} else {
				imageCounter = 0; 

				setSharedPrefs("userDetails", "hasRegistered", "true");
				
				pDialog.dismiss();

				Intent intent = new Intent (RegisterActivity.this, ScanActivity.class);
				startActivity(intent);
				finish();
			}
		}	
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
	
	/*################################################################################################
	 * Returns all the imageIDs of a single shop as an array
    ###################################################################################################*/
	private String[] getImageIDs(String shop)	{
		
		String[] empty = new String[0];

		imageCounter = 0;

		String allImageIDsString = "";

		allImageIDsString = getSharedPrefs(shop.trim(),"imageIDs","empty");
		String test = getSharedPrefs("advertshop", "imageIDs", "EmPtY");
		Toast.makeText(getBaseContext(), ""+allImageIDsString, Toast.LENGTH_LONG).show();
		Toast.makeText(getBaseContext(), ""+test, Toast.LENGTH_LONG).show();



		if(!allImageIDsString.equals("empty"))	{
			
			if(!allImageIDsString.endsWith(","))	{
				allImageIDsString = allImageIDsString.trim()+",";
			}
			
			String[] allImageIDs = allImageIDsString.split(",");
			
			if(!(allImageIDs.length == 1 && allImageIDs[0].equals("")))	{
				return allImageIDs;
			} else {
				return empty;
			}

			
		} else {
			
			Toast.makeText(getBaseContext(), "getImageIDs: 'empty'", Toast.LENGTH_LONG).show();
			return empty;
		}
	}

	/*########################################################################################################
	 * Returns all the imageIDs of an array of shops as an array
    #########################################################################################################*/
/*	private String[] getImageIDs(String[] shopArray)	{

		imageCounter = 0;

		String allImageIDsString = "";

		for(int i = 0; i < shopArray.length; i++)	{

			String mShopName = shopArray[i].trim();

			String imageIDsString = getSharedPrefs(mShopName, "imageIDs", "empty");  


			if(!imageIDsString.equals("empty"))	{

				allImageIDsString = allImageIDsString + imageIDsString + ",";

			}
		}

		try {

			allImageIDsString = allImageIDsString.substring(0, allImageIDsString.length()-1);		//-1 deletes the comma at end.

		} catch (Exception ignore)	{
			//catch OutOfBounds exception incase array is empty
		}

		String[] allImageIDs = allImageIDsString.split(",");

		return allImageIDs;
	}  */

	/*##############################################################################################
	 * Returns an array of the listOfShops SharedPreference
	 #############################################################################################*/
	private String[] getListOfShopsArray()	{


		String listOfShopsString =  getSharedPrefs("userDetails", "listOfShops", "");

		if(!listOfShopsString.endsWith(","))	{
			
			listOfShopsString = listOfShopsString.trim() + ",";
		}

		String[] asdf = listOfShopsString.split(",");

		if(asdf[0].equals("") && asdf.length == 1)	{
			String[] empty = new String[0];
			return empty;
		} else {

			return asdf;
		}
	}	

	/*##############################################################################################
	 * Returns an array of the listOfShops from String (eg. /signIn
	 #############################################################################################*/
	private String[] getListOfShopsArray(String listOfShops)	{
		
		String[] asdf = listOfShops.trim().split(",");

		if(asdf[0].equals("") && asdf.length == 1)	{
			String[] empty = new String[0];
			return empty;
		} else {
			return asdf;
		}
	}	

	/*########################################################################################################
	 * Returns all the logoIDs of an array of shops, as an array.
    #########################################################################################################*/
	private String[] getLogoIDs(String[] shopArray)	{

		String logoIDsString = "";

		for(int i = 0; i < shopArray.length; i++)	{

			String mShopName = shopArray[i].trim();
			
			Toast.makeText(getBaseContext(), "TEST: "+mShopName+"|", Toast.LENGTH_LONG).show();

			String imageIDsString = getSharedPrefs(mShopName, "imageIDs", "empty");  

			Toast.makeText(getBaseContext(), "TEST: "+imageIDsString, Toast.LENGTH_LONG).show();

			
			if(!imageIDsString.equals("empty"))	{

				String[] arrayOfIDs = imageIDsString.split(",");

				if( !(arrayOfIDs[0].equals("")&&arrayOfIDs.length ==1) )	{

					logoIDsString = logoIDsString + arrayOfIDs[0] + ",";

				}
			}
		}

		
		if(!logoIDsString.endsWith(","))	{
			logoIDsString = logoIDsString + ",";
			Toast.makeText(getBaseContext(), "TEST: added comma at end", Toast.LENGTH_LONG).show();

		}
		
		String[] allLogoImageIDs = logoIDsString.split(",");

		if(allLogoImageIDs[0].equals("") && allLogoImageIDs.length == 1)	{

			String[] empty = new String[0];
			return empty;

		} else {
			return allLogoImageIDs;

		}
	}  

	/*################################################################################################
	 * Creates array of images that is missing (and needs to be downloaded)
    ######################################################################################*/
	private String[] getArrayOfMissingImages (String[] imageNames)	{

		String missingImagesString ="";

		for(String item : imageNames)	{

			File f = new File(item.trim());

			if(!f.exists())	{
				missingImagesString = missingImagesString + item + ",";
			}
		}

		try {
			missingImagesString = missingImagesString.substring(0, missingImagesString.length()-1);
		} catch (Exception ignore)	{

		}

		String[] missingImagesArray = missingImagesString.split(",");

		if(missingImagesArray[0].equals("") && missingImagesArray.length == 1)	{

			String[] empty = new String[0];
			return empty;

		} else {

			return missingImagesArray;
		}
	}

	/*##############################################################################################
	 * 	Decodes Base64 encoded Bitmap, and saves it to a new file name after the instance of imageID
	##############################################################################################*/
	private Bitmap decodeBitmapBase64 (String encodedString)	{
		try {

			byte[] decodedString = Base64.decode(encodedString.getBytes("UTF-8"), Base64.DEFAULT);
			Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

			return decodedImage;

		} catch (Exception e) {

			Toast.makeText(getBaseContext(), "Catch # 12:17", Toast.LENGTH_LONG).show();
			return null;

		}

	}		

	/*###########################################################################################################
	 * 	 Determines ScreenSize
	############################################################################################################*/
	private String screenSize()	{

		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			return "medium";

		} else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
			return "small";

		} else {
			return "large";
		}
	} 

	/*##############################################################################################
	 *  Saves a Bitmap to /data/data/loyal3/app_data/imageDir/<imageName>
	##############################################################################################*/
	private String saveBitmapToInternalStorage(Bitmap bitmapImage, String imageName){

		ContextWrapper cw = new ContextWrapper(getApplicationContext());
		// path to /data/data/yourapp/app_data/imageDir
		File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);	//readable is just for testing
		// Create imageDir
		File mypath=new File(directory, imageName.replaceFirst("/", "")+".jpg");

		FileOutputStream fos = null;

		try {          
			fos = new FileOutputStream(mypath);
			
			bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);

			fos.close();

		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Catch #12:49", Toast.LENGTH_LONG).show();
		}

		return directory.getAbsolutePath();
	}

}
