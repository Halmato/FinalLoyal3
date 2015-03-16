package com.loyal3.loyal3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ScanActivity extends ActionBarActivity {

	private int imageCounter;
	private int buttonsInRow;
	private int height, width;
	
	private String[] arrayOfImageIDsToDownload;

	ProgressDialog pDialog;

	private float lastXScan;
	private ViewFlipper viewFlipperScan;

	Button btnRetrieve;
	
	public static Activity scanActivity;

	/*##############################################################################################
	 * OnResume - Re-populates scrollView if a new shop has been added
	###############################################################################################*/
	/*protected void onResume()	{
	   super.onResume();
	   if(newShop.equals("true"))	{
			Intent intent = getIntent();
			finish();
			startActivity(intent);

		}

	}*/
	
	/*##############################################################################################
	 * OnCreate-
	###############################################################################################*/
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(0xFF800000));	//Sets AB Color
		setContentView(R.layout.activity_scan);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		
		scanActivity = this;
		
		height = displaymetrics.heightPixels;
		width = displaymetrics.widthPixels;
		buttonsInRow = 4;

		imageCounter = 0;
		
		
		
		try{
			downloadPremiumAdvertsIfMissing();
		} catch (Exception e)	{
			getBaseContext().getSharedPreferences("YOUR_PREFS", 0).edit().clear().commit();
			recreate();
		}
		
		
		
		
		
		setScanButton();
		
//		setHorizontalScrollView();		Incorporated in dowloadPremiumAdvertsIfMissing's onPostExecute methods. Do not think it is neccesary here
		
	}

	/*###############################################################################################
	 * Action-bar settings
 	##############################################################################################*/
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan, menu);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(ScanActivity.this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}else if(id == R.id.action_redeem)	{
			Intent intent = new Intent(ScanActivity.this, My3wardsActivity.class);
			startActivity(intent);
			return true;
		}else if(id == R.id.action_profile)	{
			Intent intent = new Intent(ScanActivity.this, ProfileActivity.class);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/*##############################################################################################
	 * 	On result of the QR-SCAN, creates URL and starts HttpAsyncTask with said URL. 
    ##############################################################################################*/
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
			
			Toast.makeText(getBaseContext(), "Result: |"+scanResult+"|", Toast.LENGTH_LONG).show();

			
			
			if(!scanResult.equals("null"))	{

				String uuid = getSharedPrefs("userDetails", "uuid", "");
				String URL = "http://wilcostr.pythonanywhere.com/scan?uuid="+uuid+"&code="+scanResult;
	
				new HttpAsyncTaskScan().execute(URL);	
			}
			
		}else{
			Toast.makeText(getBaseContext(), "DIDNT SCAN!"+scanResult, Toast.LENGTH_LONG).show();
		}
	}

	/*##############################################################################################
	 * 	HTTP-Request. Gets&reads JSON. Download new adverts. Sends to DisplayActivity.
    ##############################################################################################*/
	private class HttpAsyncTaskScan extends AsyncTask<String, Void, String> {								

		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog (ScanActivity.this);
			pDialog.setMessage("Just a second ....");
			pDialog.show();
		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);	}							

		protected void onPostExecute(String result) {							
			try{	
				Toast.makeText(getBaseContext(), "scanResult: "+result, Toast.LENGTH_LONG).show();
				JSONObject json = new JSONObject(result);	

				String lScanCount = json.getString("scanCount");								
				String lMaxScans = json.getString("maxScans");	
				String lShopName = json.getString("shopName").trim();
				String lImageIDsString = json.getString("imageIDs").trim();

				addShopToSPListIfNew(lShopName);
				
				setSharedPrefs(lShopName, "scanCount", lScanCount);
				setSharedPrefs(lShopName, "maxScans", lMaxScans);
				setSharedPrefs(lShopName, "imageIDs", lImageIDsString);

				Intent intent = new Intent(ScanActivity.this,DisplayActivity.class);
				intent.putExtra("shopName", lShopName);
				startActivity(intent);
				

			}catch(Exception e)	{
				Toast.makeText(getBaseContext(), "Failed! Please ensure you have a stable Internet Connection, or that the QR-Code is valid1.", Toast.LENGTH_LONG).show();
			}	
			pDialog.dismiss();
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
	 * Adds shop to SP list if it is new
     ##############################################################################################*/
	private void addShopToSPListIfNew(String newShopName)	{
		
		StringBuilder sb = null;
		
		String[] los = getListOfShopsArray();
		Set<String> set = new HashSet<String>(Arrays.asList(los));
		
		if(!  (set.contains(newShopName) || set.contains(newShopName+" ") ))	{
			
			sb = new StringBuilder();
			
			for (int i = 0; i < los.length; i++) {		//Creates new string from old listOfShops
				sb.append(los[i].trim()).append(",");
			}
			sb.append(newShopName).append(",");

			setSharedPrefs("userDetails", "listOfShops", sb.toString());
			
			Toast.makeText(getBaseContext(), "Added "+newShopName+ " to listOfShops", Toast.LENGTH_LONG).show();

		}
		
		for(int i = 0; i < getListOfShopsArray().length; i++)	{
			Toast.makeText(getBaseContext(), "TEST:\n|"+getListOfShopsArray()[i]+"|", Toast.LENGTH_LONG).show();

		}
		
		
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
					Toast.makeText(getBaseContext(), "Adverts Downloaded at SCAN", Toast.LENGTH_LONG).show();
					
					setHorizontalScrollView();
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
	 * Sets onTouchEvent
     ##############################################################################################*/ 	
	public boolean onTouchEvent(MotionEvent touchevent) {

		try{  	
			switch (touchevent.getAction()) {				
			case MotionEvent.ACTION_DOWN: 				

				lastXScan = touchevent.getX();			
				break;			

			case MotionEvent.ACTION_UP: 				
				float currentX = touchevent.getX();			

				// Handling left to right screen swap.			
				if (lastXScan < currentX) {			
					// If there aren't any other children, just break.		
					if (viewFlipperScan.getDisplayedChild() == 0)		
						break;	
					// Next screen comes in from left.		
					viewFlipperScan.setInAnimation(this, R.anim.slide_in_from_left);		

					// Current screen goes out from right. 		
					viewFlipperScan.setOutAnimation(this, R.anim.slide_out_to_right);		

					// Display next screen.		
					viewFlipperScan.showNext();		
				}			
				// Handling right to left screen swap.			
				if (lastXScan > currentX) {			
					// If there is a child (to the left), just break.		
					if (viewFlipperScan.getDisplayedChild() == 1)		
						break;	

					// Next screen comes in from right.		
					viewFlipperScan.setInAnimation(this, R.anim.slide_in_from_right);		

					// Current screen goes out from left. 		
					viewFlipperScan.setOutAnimation(this, R.anim.slide_out_to_left);		

					// Display previous screen.		
					viewFlipperScan.showPrevious();		
				}			

				break;			
			}	
		}catch(Exception e)	{
		}
		return false;	

	}

	/*##############################################################################################
	 * Sets Flipper speed and self-flipping.
     ##############################################################################################*/ 		
	private void setAdvertFlipper()	{
		// Makes the advert viewFlipper auto flip after 5 seconds	
		viewFlipperScan = (ViewFlipper) findViewById(R.id.view_flipper_scan);
		viewFlipperScan.setFlipInterval(5000);
		viewFlipperScan.setInAnimation(this, R.anim.slide_in_from_left);			
		viewFlipperScan.setOutAnimation(this, R.anim.slide_out_to_right);
		viewFlipperScan.startFlipping();	

	}

    /*##################################################################################################
     * Sets premiumAdvert images
     ###################################################################################################*/
	private void setAdvertImages()	{

		viewFlipperScan = (ViewFlipper) findViewById(R.id.view_flipper_scan);
		
		String[] ids = getImageIDs("advertshop");

		for(int i = 0; i < ids.length; i++)	{

			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(LinearLayout.VERTICAL);

			LinearLayout.LayoutParams lpScan = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
			ll.setLayoutParams(lpScan);
			ll.setGravity(Gravity.CENTER);

			ImageView iv = new ImageView(this);
			iv.setLayoutParams(lpScan);
			iv.setImageBitmap(imageLoadedFromInternalStorage(ids[i]));
			
			ll.addView(iv);
			
			viewFlipperScan.addView(ll);
		} 
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
	 * Creates array from CSV-string
    ##############################################################################################*/	
	/*private String[] createArrayFromString (String shopsString)	{

		String[] resultArray = shopsString.trim().split(",");

		if(resultArray[0].equals(""))	{
			removeElt(resultArray, 0);
		}
		return resultArray;
	}*/

	/*######################################################################################################
	 * Checks if all 'advertshops' exists (File f.exists). If not, downloads the images and saves them.
 	########################################################################################################*/
	private void downloadPremiumAdvertsIfMissing()	{

		arrayOfImageIDsToDownload = getArrayOfMissingImages(getImageIDs("advertshop"));		

		if(networkIsAvailable() && !(arrayOfImageIDsToDownload.length == 1 && arrayOfImageIDsToDownload[0].equals("")) && arrayOfImageIDsToDownload.length != 0 )	{
			pDialog = new ProgressDialog (ScanActivity.this);
			pDialog.setMessage("Downloading adverts...");
			pDialog.show();	

			imageCounter=0;
			
			String url = "http://wilcostr.pythonanywhere.com/downloadImage?image="+arrayOfImageIDsToDownload[imageCounter].trim()+"&res="+screenSize();
			Toast.makeText(getBaseContext(), "URL: "+url, Toast.LENGTH_LONG).show();
		
			
			new DownloadAdvertImages().execute(url);
		
		} else {

			setHorizontalScrollView();
			setAdvertFlipper();	//Mag dalk die 2 moet omruil.
			setAdvertImages();
		}
	}

	/*##############################################################################################
	 * 	Decodes Base64 encoded Bitmap, and saves it to a new file name after the instance of imageID
	##############################################################################################*/
/*	private void decodeBitmapBase64 (String encodedString, String imageName)	{
		try {

			byte[] decodedString = Base64.decode(encodedString.getBytes("UTF-8"), Base64.DEFAULT);
			Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

			saveBitmapToInternalStorage(decodedImage, imageName);

		} catch (Exception e) {

			Toast.makeText(getBaseContext(), "Catch # 12:17", Toast.LENGTH_LONG).show();

		}
	}	*/

	/*##############################################################################################
	 *  Saves a Bitmap to /data/data/loyal3/app_data/imageDir/<imageName>
	##############################################################################################*/
	/*private String saveBitmapToInternalStorage(Bitmap bitmapImage, String imageName){

		ContextWrapper cw = new ContextWrapper(getApplicationContext());
		// path to /data/data/yourapp/app_data/imageDir
		File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
		// Create imageDir
		File mypath=new File(directory,imageName.toLowerCase()+".jpg");

		FileOutputStream fos = null;
		try {          
			fos = new FileOutputStream(mypath);

			bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);

			fos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		//Returns the DIRECTORY the image was saved to. Not the directory of the image itself.
		return directory.getAbsolutePath();
	}*/

	/*###############################################################################################
	 * 	Sets scanButton, that starts QR-Scanner. Also sets spacing above&below scanButton.
    ################################################################################################*/	
	private void setScanButton()	{

		ImageButton ibScan = (ImageButton) findViewById(R.id.ibScan);	
		ibScan.setLayoutParams(new LinearLayout.LayoutParams((int)(width*0.45),(int)(width*0.45)));
		ibScan.setScaleType(ScaleType.FIT_XY);

		ibScan.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				IntentIntegrator integrator = new IntentIntegrator(ScanActivity.this);
				integrator.initiateScan();
			}
		});

		View viewTop = (View) findViewById(R.id.viewSpacingTop);
		viewTop.setLayoutParams(new LinearLayout.LayoutParams((int)(height*0.03),(int)(height*0.03)));
		View viewBottom = (View) findViewById(R.id.viewSpacingBottom);
		viewBottom.setLayoutParams(new LinearLayout.LayoutParams((int)(height*0.03),(int)(height*0.03)));
	}

	/*##############################################################################################
	 * Sets the scroll view and all its contents (ImageBtns, onClicks, ImageBtns Images)
    ###############################################################################################*/	
	/*private void setVerticalScrollView()	{
		
		/*	LinearLayout llMain = (LinearLayout) findViewById(R.id.llMain);
			ScrollView sv = new ScrollView(this);
			TableLayout tl = new TableLayout(this);

			sv.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));

			llMain.addView(sv);
			sv.addView(tl);


			setListOfShopsArray();

			try{
				for(int i = 0;i <= (listOfShopsArray.length/buttonsInRow); i++)	{	//Creates TableRows (minimum 1)

					TableRow tr = new TableRow(this);
					tl.addView(tr);

					for(int j =0; j <buttonsInRow;j++)	{	//Creates 'buttonsInRow' image buttons per row and sets onClick

						if(((i*buttonsInRow)+j) < listOfShopsArray.length - emptyArray   )	{


							shopName = listOfShopsArray[(i*buttonsInRow)+j].trim();
							final String shopNameTest = shopName;

							ImageButton ib = new ImageButton(this);
							int id = getResources().getIdentifier("shoplogo_"+shopName.toLowerCase(), "drawable", "com.loyal3.loyal3");
							int id2 = getResources().getIdentifier("shoplogo_no_image", "drawable", "com.loyal3.loyal3");
							Toast.makeText(getBaseContext(), "|"+shopName+"|" , Toast.LENGTH_LONG).show();

						//Checks if shopLogo Exists	and sets image accordingly
							if(id==0)	{
								ib.setImageResource(id2);
							}	else	{
								ib.setImageResource(id);
							}

							ib.setScaleType(ScaleType.FIT_XY);
							ib.setPadding(2, 0, 2, 6);
							ib.setBackgroundColor(Color.TRANSPARENT);

				// ##MAGIC HAPPENS HERE			
							TableRow.LayoutParams lp = new TableRow.LayoutParams(width/buttonsInRow, width/buttonsInRow);

							ib.setLayoutParams(lp);

							ib.setOnClickListener(new View.OnClickListener() {

								public void onClick(View arg0) {

									Intent intent = new Intent(ScanActivity.this, DisplayActivity.class);
									intent.putExtra("shopName", shopNameTest);
									startActivity(intent);
								}
							});	//onClickListener

							tr.addView(ib); 

						}	// if
					}	//for(int j)
				}	//for(int i)
			}catch(Exception e)	{

				Toast.makeText(getBaseContext(), "Error at Creating of ScrollView",Toast.LENGTH_LONG).show();
			}






				this.setContentView(llMain);	}*/
	
	/*##############################################################################################
	 *  Saves a Bitmap to /data/data/loyal3/app_data/imageDir/<imageName>
	##############################################################################################*/
	private String saveBitmapToInternalStorage(Bitmap bitmapImage, String imageName){

		String formattedImageName = imageName.replaceFirst("/", "");
		
		ContextWrapper cw = new ContextWrapper(getApplicationContext());
		// path to /data/data/yourapp/app_data/imageDir
		File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);	//readable is just for testing
		// Create imageDir
		File mypath=new File(directory, formattedImageName+".jpg");

		FileOutputStream fos = null;

		try {          
			fos = new FileOutputStream(mypath);

			bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);

			fos.close();

		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Couldn't save Bitmap", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		return directory.getAbsolutePath();
	}
	
	/*##############################################################################################
	 * Returns an array of the listOfShops SharedPreference
	 #############################################################################################*/
	private String[] getListOfShopsArray()	{

		String listOfShopsString =  getSharedPrefs("userDetails", "listOfShops", "");

		String[] asdf = listOfShopsString.split(",");

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

			String imageIDsString = getSharedPrefs(mShopName, "imageIDs", "empty");  

			if(!imageIDsString.equals("empty"))	{

				String[] arrayOfIDs = imageIDsString.split(",");

				if( !(arrayOfIDs[0].equals("")&&arrayOfIDs.length ==1) )	{

					logoIDsString = logoIDsString + arrayOfIDs[0] + ",";

				}
			}
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
	 *  Decodes the Base64 encoded string. 
	##############################################################################################*/
	private Bitmap decodeBitmapBase64 (String encodedString)	{
		

		try {
			byte[] decodedString = Base64.decode(encodedString.getBytes("UTF-8"), Base64.DEFAULT);

			Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

			return decodedImage;

		} catch (Exception e) {

			Toast.makeText(getBaseContext(), "Catch # 12:18", Toast.LENGTH_LONG).show();
			return null;

		}

	}		
	
	/*###########################################################################################################
	 * Determines ScreenSize
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
	 *  sets logo
	 #############################################################################################*/
	private void setLogo(String imageID , ImageButton imageButton)	{
		try {

			imageButton.setImageBitmap(imageLoadedFromInternalStorage(imageID));

		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Catch #10:55", Toast.LENGTH_LONG).show();

		}
	}	

	/*#########################################################################################################
	 * Sets Horizontal ScrollView with their logos and onClicks
	 ###############################################################################################################*/
	private void setHorizontalScrollView()	{


		LinearLayout llMain = (LinearLayout) findViewById(R.id.llMain);
		HorizontalScrollView sv = new HorizontalScrollView(this);

		LinearLayout llSv = new LinearLayout(this);
		llSv.setOrientation(LinearLayout.HORIZONTAL);
		llSv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));

		LinearLayout llContainsSv = (LinearLayout) findViewById(R.id.llContainsSv);
		llContainsSv.addView(sv);

		sv.addView(llSv);

		String[] shops = getListOfShopsArray();

		try{		
			for(int i = 0; i < shops.length; i++   )	{

				ImageButton ib = new ImageButton(this);
				ib.setScaleType(ScaleType.FIT_XY);
				ib.setPadding(2, 0, 2, 6);
				ib.setBackgroundColor(Color.TRANSPARENT);


				String sn = shops[i].trim();
				final String shopnameChanging = sn;

				String shopLogo = getLogoIDs(getListOfShopsArray())[i];

				setLogo(shopLogo, ib);

				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width/buttonsInRow, width/buttonsInRow);
				ib.setLayoutParams(lp);		

				ib.setOnClickListener(new View.OnClickListener() {

					public void onClick(View arg0) {

						Intent intent = new Intent(ScanActivity.this, DisplayActivity.class);
						intent.putExtra("shopName", shopnameChanging);
						startActivity(intent);
					}
				});	//onClickListener

				llSv.addView(ib); 			
			}

		} catch(Exception e)	{
			Toast.makeText(getBaseContext(), "Catch #11:08", Toast.LENGTH_LONG).show();
		}	

		this.setContentView(llMain);
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
			Toast.makeText(getBaseContext(), "Image could not be loaded", Toast.LENGTH_LONG).show();
			return null;
		}

	}		
	
	/*################################################################################################
	 * Returns all the imageIDs of a single shop as an array
    ###################################################################################################*/
	private String[] getImageIDs(String shop)	{

		imageCounter = 0;

		String allImageIDsString = "";

		allImageIDsString = getSharedPrefs(shop.trim(),"imageIDs","empty");

		if(!allImageIDsString.equals("empty"))	{
			String[] allImageIDs = allImageIDsString.split(",");

			return allImageIDs;
		} else {
			String[] empty = new String[0];
			Toast.makeText(getBaseContext(), "getImageIDs: 'empty'", Toast.LENGTH_LONG).show();
			return empty;
		}
	}	
		
	
	
	
}
