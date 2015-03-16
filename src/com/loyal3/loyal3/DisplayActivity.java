package com.loyal3.loyal3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class DisplayActivity extends ActionBarActivity {
	
	private int height,width,starCounter;
	private String[] arrayOfImageIDsToDownload;
	private int imageCounter;
	private String shopName, maxScans, scanCount;
	private float lastX;
	
	private ViewFlipper viewFlipper;
	private ImageView logoView;
	private ProgressDialog pDialog;

	/*################################################################################################
	 * onCreate-
	 #################################################################################################*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(0xFF800000));
		setContentView(R.layout.activity_display);

		Bundle extras = getIntent().getExtras();

		shopName = extras.getString("shopName").trim();
		scanCount = getSharedPrefs(shopName, "scanCount", "0");
		maxScans = getSharedPrefs(shopName, "maxScans", "7");

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		height = displaymetrics.heightPixels;
		width = displaymetrics.widthPixels;
		logoView = (ImageView) findViewById(R.id.ivShopLogoDisplay);

		downloadImagesIfMissing();

		setScanStars("starfilled", "starnotfilled", "stargold");
		
		setClaimButton();

	}	//End of onCreate

	/*############################################################################################
	 * Options menus (Actionbar)
	 #############################################################################################*/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(DisplayActivity.this, SettingsActivity.class);
			startActivity(intent);
			return true;

		}else if(id == R.id.action_redeem)	{
			Intent intent = new Intent(DisplayActivity.this, My3wardsActivity.class);
			startActivity(intent);
			finish();
			return true;

		}else if(id == R.id.action_profile)	{
			Intent intent = new Intent(DisplayActivity.this, ProfileActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*############################################################################################
	 * Returns the current Time in the correct format (dd/mm/yyyy hh:mm)
	##############################################################################################*/
	private String currentTime()	{
		Calendar c = Calendar.getInstance(); 

		String seconds;
		if(c.get(Calendar.SECOND) < 10)	{
			seconds = "0"+Integer.toString(c.get(Calendar.SECOND));
		} else {
			seconds = Integer.toString(c.get(Calendar.SECOND));
		}

		String minutes;
		if(c.get(Calendar.MINUTE) < 10)	{
			minutes = "0"+Integer.toString(c.get(Calendar.MINUTE));
		} else {
			minutes = Integer.toString(c.get(Calendar.MINUTE));
		}

		String hours;
		if(c.get(Calendar.HOUR_OF_DAY) < 10)	{
			hours = "0"+Integer.toString(c.get(Calendar.HOUR_OF_DAY));
		} else {
			hours = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
		}

		String day;
		if(c.get(Calendar.DATE) < 10)	{
			day = "0"+Integer.toString(c.get(Calendar.DATE));
		} else {
			day = Integer.toString(c.get(Calendar.DATE));
		}

		String month;
		if((c.get(Calendar.MONTH)+1) < 10)	{
			month = "0"+Integer.toString(c.get(Calendar.MONTH)+1);
		} else {
			month = Integer.toString(c.get(Calendar.MONTH)+1);
		}

		String year;
		if(c.get(Calendar.YEAR) < 10)	{
			year = "0"+Integer.toString(c.get(Calendar.YEAR));
		} else {
			year = Integer.toString(c.get(Calendar.YEAR));
		}

		return day+"/"+month+"/"+year + "  "+hours+":"+minutes+":"+seconds;	
	}

	/*###############################################################################################
	 * Checks if network is available
	 ################################################################################################*/
	private boolean networkIsAvailable() {								
		ConnectivityManager connectivityManager 								
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);								
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();								
		return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();								
	}	

	/*##############################################################################################
	 *  sets logo
	 #############################################################################################*/
	private void setLogo(String imageID , ImageView iv)	{
		try {

			iv.setImageBitmap(imageLoadedFromInternalStorage(imageID));

		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Catch #10:56", Toast.LENGTH_LONG).show();

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

	/*######################################################################################################
	 * Checks if all image of specific shop exists (File f.exists). If not, downloads the images and saves them.
	########################################################################################################*/
	private void downloadImagesIfMissing()	{

		arrayOfImageIDsToDownload = getArrayOfMissingImages(getImageIDs(shopName));		

		if(networkIsAvailable() && !(arrayOfImageIDsToDownload.length == 1 && arrayOfImageIDsToDownload[0].equals("")) && arrayOfImageIDsToDownload.length != 0 )	{

			pDialog = new ProgressDialog (DisplayActivity.this);
			pDialog.setMessage("Downloading "+arrayOfImageIDsToDownload.length+" Images...");
			pDialog.show();	

			imageCounter=0;
			
			String url = "http://wilcostr.pythonanywhere.com/downloadImage?image="+arrayOfImageIDsToDownload[imageCounter].trim()+"&res="+screenSize();

			new DownloadAdvertImages().execute(url);

		} else {

			setLogo(getLogoID(shopName), logoView);
					
			setAdvertFlipper();	//Mag dalk die 2 moet omruil.
			setAdvertImages();
		}
	}		

	/*################################################################################################
	 * Returns all the imageIDs of a single shop as an array
	###################################################################################################*/
	private String[] getImageIDs(String shop)	{

		String[] empty = new String[0];

		imageCounter = 0;

		String allImageIDsString = "";

		allImageIDsString = getSharedPrefs(shop.trim(),"imageIDs","empty");

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

	/*##############################################################################################
	 *   Download Advert Images
	###############################################################################################*/
	private class DownloadAdvertImages extends AsyncTask<String, Void, String> {								

		protected void onPreExecute()	{
			super.onPreExecute();
		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {

			try{	
				Bitmap bmap = decodeBitmapBase64(result);
				saveBitmapToInternalStorage(bmap, arrayOfImageIDsToDownload[imageCounter]);

				imageCounter++;

				if(imageCounter < arrayOfImageIDsToDownload.length)	{

					String url = "http://wilcostr.pythonanywhere.com/downloadImage?image="+arrayOfImageIDsToDownload[imageCounter].trim()+"&res="+screenSize();

					new DownloadAdvertImages().execute(url);

				} else {
					Toast.makeText(getBaseContext(), imageCounter+" images Downloaded at DISPLAY", Toast.LENGTH_LONG).show();

					setLogo(getLogoID(shopName), logoView);
					ScanActivity.scanActivity.recreate();
					setAdvertFlipper();
					setAdvertImages();
					pDialog.dismiss();
				}

			} catch(Exception e)	{   	
				Toast.makeText(getBaseContext(), "Catch #09:39", Toast.LENGTH_LONG).show();
			}
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

			Toast.makeText(getBaseContext(), "Catch # 12:19", Toast.LENGTH_LONG).show();
			return null;

		}

	}				

	/*##########################################################################################
	 * Sets advert flipper
	###########################################################################################*/
	private void setAdvertFlipper()	{
		// Makes the advert viewFlipper auto flip after 5 seconds	
		viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper_display);
		viewFlipper.setFlipInterval(5000);
		viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);			
		viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);
		viewFlipper.startFlipping();	

	}		

	/*##################################################################################################
	 * Sets advert images ( i = 1, because imageList[0] = logo
		 ###################################################################################################*/
	private void setAdvertImages()	{

		viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper_display);

		String[] ids = getImageIDs(shopName);

		if(ids.length == 1)	{		//Just the logo exists
			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(LinearLayout.VERTICAL);

			LinearLayout.LayoutParams lpScan = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
			ll.setLayoutParams(lpScan);
			ll.setGravity(Gravity.CENTER);

			ImageView iv = new ImageView(this);
			iv.setLayoutParams(lpScan);
			iv.setImageBitmap(imageLoadedFromInternalStorage(ids[0]));		//Sets flipper images = logo  (ids[0])

			ll.addView(iv);
			viewFlipper.addView(ll);	
		}


		for(int i = 1; i < ids.length; i++)	{

			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(LinearLayout.VERTICAL);

			LinearLayout.LayoutParams lpScan = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
			ll.setLayoutParams(lpScan);
			ll.setGravity(Gravity.CENTER);

			ImageView iv = new ImageView(this);
			iv.setLayoutParams(lpScan);
			iv.setImageBitmap(imageLoadedFromInternalStorage(ids[i]));

			ll.addView(iv);
			viewFlipper.addView(ll);	
		}
	}

	/*##############################################################################################
	 * 	Returns a Bitmap that was loaded from internal storage (imageName)
	##############################################################################################*/
	private Bitmap imageLoadedFromInternalStorage(String imageName)
	{
		try {
			File f=new File(getApplicationContext().getDir("imageDir", MODE_PRIVATE).getPath()+"/", imageName.replaceFirst("/", "") +".jpg");
			Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
			return b;
		} 
		catch (Exception e) 
		{
			try{
				File f=new File(getApplicationContext().getDir("imageDir", MODE_PRIVATE).getPath()+"/", getLogoID(shopName).replaceFirst("/", "") +".jpg");
				Bitmap c = BitmapFactory.decodeStream(new FileInputStream(f));
				return c;
				
			} catch (Exception e2)	{
				
				int idLogo = getResources().getIdentifier("ic_launcher", "drawable", "com.loyal3.loyal3");
				Bitmap d = BitmapFactory.decodeResource(getResources(), idLogo);
				Toast.makeText(getBaseContext(), "Image could not be loaded", Toast.LENGTH_LONG).show();
				return d;
			}
			
		}

	}			

	/*######################################################################################################
	 * Part of Async function
	#######################################################################################################*/
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

	/*###############################################################################################
	 * Sets onTouch event (Lets ViewFlipper slide images)
	################################################################################################*/
	public boolean onTouchEvent(MotionEvent touchevent) {						
		switch (touchevent.getAction()) {				
		case MotionEvent.ACTION_DOWN: 				

			lastX = touchevent.getX();			
			break;			

		case MotionEvent.ACTION_UP: 				
			float currentX = touchevent.getX();			

			// Handling left to right screen swap.			
			if (lastX < currentX) {			
				// If there aren't any other children, just break.		
				if (viewFlipper.getDisplayedChild() == 0)		
					break;	
				// Next screen comes in from left.		
				viewFlipper.setInAnimation(this, R.anim.slide_in_from_left);		

				// Current screen goes out from right. 		
				viewFlipper.setOutAnimation(this, R.anim.slide_out_to_right);		

				// Display next screen.		
				viewFlipper.showNext();		
			}			
			// Handling right to left screen swap.			
			if (lastX > currentX) {			
				// If there is a child (to the left), just break.		
				if (viewFlipper.getDisplayedChild() == 1)		
					break;	

				// Next screen comes in from right.		
				viewFlipper.setInAnimation(this, R.anim.slide_in_from_right);		

				// Current screen goes out from left. 		
				viewFlipper.setOutAnimation(this, R.anim.slide_out_to_left);		

				// Display previous screen.		
				viewFlipper.showPrevious();		
			}			
			break;			
		}				
		return false;				
	}		

	/*###################################################################################################
	 * Sets The Claim Button. Triggers yes/no-Dialog. On yes, sends to ClaimActivity.
	 *###################################################################################################*/
	private void setClaimButton()	{
		
		if(Integer.parseInt(scanCount) >= Integer.parseInt(maxScans))	{

			/*	Image*/Button ib = (/*Image*/Button) findViewById(R.id.ibClaim);
			ib.setVisibility(View.VISIBLE);
			/*	ib.setPadding(0, 0, 0, 0);*/

			ib.setOnClickListener(new View.OnClickListener() {

				public void onClick(View arg0) {

					if(getSharedPrefs("userDetails", "allInfo", "").equals("true"))	{
						
					AlertDialog.Builder adb = new AlertDialog.Builder(DisplayActivity.this);
					adb.setMessage("Once you click 'Yes, I am', you will have 5 minutes to claim your item from the cashier.\nAre you by the cashier?");
					
					adb.setPositiveButton("Yes, I am", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							String uuid = getSharedPrefs("userDetails", "uuid", "");
							
							String url = "http://wilcostr.pythonanywhere.com/redeem?uuid="+uuid+"&shop="+shopName;
						
							new ValidateCounts().execute(url);
						}
					});	

					//Neg Button does nothing.
					adb.setNegativeButton("No, I am not", new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							
						}
					});
					
					adb.show();	
					
					} else {
						Toast.makeText(getBaseContext(), "Please update your information in your Profile Page in order to claim your free item", Toast.LENGTH_LONG).show();
					}
				}
			});
		}	
	}

	/*###################################################################################################
	 * Sets Stars based on ScanCount & MaxScans in two rows.  (params: name of images)
	 *###################################################################################################*/		
	private void setScanStars(String filled, String notFilled, String filledGold)	{
		
		int idFilled = getResources().getIdentifier(filled, "drawable", "com.loyal3.loyal3");
		int idNotFilled = getResources().getIdentifier(notFilled, "drawable", "com.loyal3.loyal3");
		int idFilledGold = getResources().getIdentifier(filledGold, "drawable", "com.loyal3.loyal3");
		
		//Top Row 
		LinearLayout llStarsTopRow = (LinearLayout) findViewById(R.id.llStars1);
		LinearLayout llStarsBottomRow = (LinearLayout) findViewById(R.id.llStars2);

		for(int i = 0; i < (Integer.parseInt(maxScans)-(Integer.parseInt(maxScans)/2));i++)	{

			starCounter = i;
			ImageView iv = new ImageView(this);


			if(Integer.parseInt(scanCount) == Integer.parseInt(maxScans))	{
				iv.setImageResource(idFilledGold);
			}
			else if( i < Integer.parseInt(scanCount))	{
				iv.setImageResource(idFilled);
			}	
			else	{
				iv.setImageResource(idNotFilled);
			}

			iv.setLayoutParams(new LinearLayout.LayoutParams((int)(width/(Integer.parseInt(maxScans)-(Integer.parseInt(maxScans)/2))),(int)(height*(0.08))));
			iv.setBackgroundColor(Color.TRANSPARENT);

			llStarsTopRow.addView(iv);
		}

		//Bottom Row
		for(int i = starCounter+1; i < (Integer.parseInt(maxScans));i++)	{
			ImageView iv = new ImageView(this);


			if(Integer.parseInt(scanCount) == Integer.parseInt(maxScans))	{
				iv.setImageResource(idFilledGold);
			}
			else if( i < Integer.parseInt(scanCount))	{
				iv.setImageResource(idFilled);
			}	
			else	{
				iv.setImageResource(idNotFilled);
			}

			iv.setLayoutParams(new LinearLayout.LayoutParams((int)(width/(Integer.parseInt(maxScans)/2)),(int)(height*(0.08))));
			iv.setBackgroundColor(Color.TRANSPARENT);

			llStarsBottomRow.addView(iv);

		}

		LinearLayout llMainDisplay = (LinearLayout) findViewById(R.id.llMainDisplay);

		this.setContentView(llMainDisplay);	
	}
	
	/*########################################################################################################
	 * Returns all the logoIDs of an array of shops, as an array.
    #########################################################################################################*/
	private String getLogoID(String shop)	{

		String logoIDsString = "";

		String imageIDsString = getSharedPrefs(shop, "imageIDs", "empty");  

		if(!imageIDsString.equals("empty"))	{

			String[] arrayOfIDs = imageIDsString.split(",");

			if( !(arrayOfIDs[0].equals("")&&arrayOfIDs.length ==1) )	{

				logoIDsString = arrayOfIDs[0].trim();

			}
		}

		return logoIDsString;
	}  
	
	/*################################################################################################
	 * Creates array of images that is missing (and needs to be downloaded)
    ######################################################################################*/
	private String[] getArrayOfMissingImages (String[] imageNames)	{

		String missingImagesString ="";

		for(String item : imageNames)	{
			
			String formattedImageName = item.replaceFirst("/", "").trim();

			ContextWrapper cw = new ContextWrapper(getApplicationContext());
			// path to /data/data/yourapp/app_data/imageDir
			File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);	//readable is just for testing
			// Create imageDir
			File mypath=new File(directory, formattedImageName.replaceFirst("/", "")+".jpg");

//			File f = new File(item);
			
			

			if(!mypath.exists())	{

				missingImagesString = missingImagesString + item + ",";
			}

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
	 * 	Called by SignUp. User /refresh to get "advertshop"s imageIDs. Calls DownloadAdverts_refresh. (Params: shop, uuid)
     ##############################################################################################*/
	private class ValidateCounts extends AsyncTask<String, Void, String> {								

		protected void onPreExecute() {
			super.onPreExecute();
			
			pDialog = new ProgressDialog(DisplayActivity.this);
			pDialog.setMessage("Validating...");
			pDialog.show();
		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {	
				
			if(result.equals("*success*"))	{
				int lScanCountOld = Integer.parseInt(getSharedPrefs(shopName, "scanCount", "5"));
				int lMaxScansOld = Integer.parseInt(getSharedPrefs(shopName, "maxScans", "5"));
				String lScanCountNew = Integer.toString(lScanCountOld - lMaxScansOld);
				
				Intent intent = new Intent(DisplayActivity.this, ClaimActivity.class);
				intent.putExtra("shopName", shopName);
				startActivity(intent);
				
				setSharedPrefs(shopName, "lastRedeem", currentTime());
				setSharedPrefs(shopName, "scanCount", lScanCountNew);
				finish();

			} else {
				try{	
					Toast.makeText(getBaseContext(), "Error with mApp side scanCount. Real Scan count returned.", Toast.LENGTH_LONG).show();
					
					JSONObject json = new JSONObject(result);	
	
					String lScanCount = json.getString("scanCount");								
					String lMaxScans = json.getString("maxScans");	

					setSharedPrefs(shopName, "scanCount", lScanCount);
					setSharedPrefs(shopName, "maxScans", lMaxScans);

					finish();
				
				}catch(Exception e)	{
					Toast.makeText(getBaseContext(), "Catch #09:26", Toast.LENGTH_LONG).show();
				}	
				pDialog.dismiss();
			}
			
			
		
		}	
	}
	
	
}
