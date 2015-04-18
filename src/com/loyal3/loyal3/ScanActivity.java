//Last Changes:  Inserted getPremiumAdvertImageIDs, and put downloadMissingAdverts into that (it was just downloadMissingAdverts). Added a deleteImagesIfOld of so iets ook. 

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
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
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

	/*
	 * ##########################################################################
	 * #################### OnResume - Re-populates scrollView if a new shop has
	 * been added
	 * ###############################################################
	 * ################################
	 */
	/*
	 * protected void onResume() { super.onResume(); if(newShop.equals("true"))
	 * { Intent intent = getIntent(); finish(); startActivity(intent);
	 * 
	 * }
	 * 
	 * }
	 */

	/*
	 * ##########################################################################
	 * #################### OnCreate-
	 * ###########################################
	 * ####################################################
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(0xFF800000)); // Sets AB Color
																																		 
		setContentView(R.layout.activity_scan);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		scanActivity = this;

		height = displaymetrics.heightPixels;
		width = displaymetrics.widthPixels;
		buttonsInRow = 4;

		imageCounter = 0;
		
		int onCreateCounter = Integer.parseInt(getSharedPrefs("userDetails", "onCreateCounter", "1"));
		onCreateCounter++;
		setSharedPrefs("userDetails", "onCreateCounter", Integer.toString(onCreateCounter));

		if (networkIsAvailable() &&  (onCreateCounter% 5 == 0)) {
					
			try {

				String url = "https://www.loyal3.co.za/refresh?uuid="+ getSharedPrefs("userDetails", "uuid", "")	+ "&shop=advertshop";
				
				new getPremiumAdvertImageIDs().execute(url);

			} catch (Exception e) {
				getBaseContext().getSharedPreferences("YOUR_PREFS", 0).edit().clear().commit();										//Defuq is this. 
				Toast.makeText(getBaseContext(), "Catch #11:34 NB!",Toast.LENGTH_LONG).show();
				recreate();
			}
		} else {

			downloadPremiumAdvertsIfMissing();
		}

		setScanButton();

		// setHorizontalScrollView(); Incorporated in dowloadPremiumAdvertsIfMissing's onPostExecute methods. Do not think it is neccesary here
	}

	/*
	 * ##########################################################################
	 * ##################### Action-bar settings
	 * ################################
	 * ##############################################################
	 */
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
		if (id == R.id.action_contact) {
			Intent intent = new Intent(ScanActivity.this,
					ContactUsActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_redeem) {
			Intent intent = new Intent(ScanActivity.this,
					My3wardsActivity.class);
			startActivity(intent);
			return true;
		} else if (id == R.id.action_profile) {
			Intent intent = new Intent(ScanActivity.this, ProfileActivity.class);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/*
	 * ##########################################################################
	 * #################### On result of the QR-SCAN, creates URL and starts
	 * HttpAsyncTask with said URL.
	 * #############################################
	 * #################################################
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		
		if (!scanResult.toString().equals("null") && !scanResult.toString().equals(null) && scanResult != null) {

			if (!scanResult.equals("null") || scanResult != null) {

				String scanResultCode ="";
				
				try{
				scanResultCode = scanResult.toString().substring(scanResult.toString().lastIndexOf("code=")+5);
				}catch(Exception e) {
					scanResultCode = "";
				}
							
				
				String uuid = getSharedPrefs("userDetails", "uuid", "");
				String URL = "http://www.loyal3.co.za/scan?uuid=" + uuid + "&code=" + scanResultCode;

				new HttpAsyncTaskScan().execute(URL);
			}

		} else {
			Toast.makeText(getBaseContext(), "Scan Cancelled",
					Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * ##########################################################################
	 * #################### HTTP-Request. Gets&reads JSON. Download new adverts.
	 * Sends to DisplayActivity.
	 * ################################################
	 * ##############################################
	 */
	private class HttpAsyncTaskScan extends AsyncTask<String, Void, String> {

		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(ScanActivity.this);
			pDialog.setMessage("Just a second ....");
			pDialog.show();
		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);
		}

		protected void onPostExecute(String result) {
			try {
				JSONObject json = new JSONObject(result);

				String lScanCount = json.getString("scanCount");
				String lMaxScans = json.getString("maxScans");
				String lShopName = json.getString("shopName").trim();
				String lImageIDsString = json.getString("imageIDs").trim();
				
				String lWebsiteAddress = json.getString("website").trim();
				String lTradingHours = json.getString("hours").trim();
				String lPhysAddress = json.getString("address").trim();
				String lRequired = json.getString("required").trim();
				String l3ward = json.getString("3ward").trim();
		
				addShopToSPListIfNew(lShopName);

				deleteImagesIfMissing(lImageIDsString, lShopName);

				setSharedPrefs(lShopName, "scanCount", lScanCount);
				setSharedPrefs(lShopName, "maxScans", lMaxScans);
				setSharedPrefs(lShopName, "imageIDs", lImageIDsString);
				
				setSharedPrefs(lShopName, "website", lWebsiteAddress);
				setSharedPrefs(lShopName, "hours", lTradingHours);
				setSharedPrefs(lShopName, "address", lPhysAddress);
				setSharedPrefs(lShopName, "required", lRequired);
				setSharedPrefs(lShopName, "3ward", l3ward);
				
				if(Integer.parseInt(lScanCount) < Integer.parseInt(lMaxScans)) {
					setSharedPrefs(lShopName, "localScanCount", lScanCount);
				} else {

					//Adds one to localScanCount Shared Preferences. 
					setSharedPrefs(lShopName, "localScanCount", Integer.toString(Integer.parseInt(getSharedPrefs(lShopName, "localScanCount", "0"))+1));
				}

				Intent intent = new Intent(ScanActivity.this,
						DisplayActivity.class);
				intent.putExtra("shopName", lShopName);
				startActivity(intent);

			} catch (Exception e) {
				
				new AlertDialog.Builder(ScanActivity.this)											
			    .setMessage("Please ensure that the Loyal3 code is valid.")										
			    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {										
			        public void onClick(DialogInterface dialog, int which) { 										
			        //Do Nothing.		
			        }										
			     })																			
			    .setIcon(android.R.drawable.ic_dialog_alert)										
			     .show();										

			}
			pDialog.dismiss();
		}
	}

	/*
	 * ##########################################################################
	 * #################### Part of the ASYNC-TASK function
	 * #####################
	 * #########################################################################
	 */
	public static String GET(String url) {
		InputStream inputStream = null;
		String result = "";

		try {
			
			// create HttpClient
			HttpClient httpClient = new DefaultHttpClient();

			// make GET request to the given URL
			HttpResponse httpResponse = httpClient.execute(new HttpGet(url));

			// receive response as inputStream
			inputStream = httpResponse.getEntity().getContent();

			// convert inputstream to string
			if (inputStream != null) {
				result = convertInputStreamToString(inputStream);
			} else {
				result = "Did not work!";
			}
			
		} catch (Exception e) {
			
		}

		return result;
	}

	/*
	 * ##########################################################################
	 * #################### Part of the ASYNC-TASK function. Builds InputStream
	 * #
	 * #########################################################################
	 * ####################
	 */
	private static String convertInputStreamToString(InputStream inputStream)
			throws IOException {

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null) {
			result += line;
		}

		inputStream.close();
		return result;
	}

	/*
	 * ##########################################################################
	 * #################### Adds shop to SP list if it is new
	 * ###################
	 * #######################################################
	 * ####################
	 */
	private void addShopToSPListIfNew(String newShopName) {

		StringBuilder sb = null;

		String[] los = getListOfShopsArray();
		Set<String> set = new HashSet<String>(Arrays.asList(los));

		if (!(set.contains(newShopName) || set.contains(newShopName + " "))) {

			sb = new StringBuilder();

			for (int i = 0; i < los.length; i++) { // Creates new string from
													// old listOfShops
				sb.append(los[i].trim()).append(",");
			}
			sb.append(newShopName).append(",");

			setSharedPrefs("userDetails", "listOfShops", sb.toString());

			Toast.makeText(getBaseContext(),
					"Added " + newShopName + " to listOfShops",
					Toast.LENGTH_LONG).show();
		}
	}

	/*
	 * ##########################################################################
	 * #################### Download Advert Images
	 * ##############################
	 * #################################################################
	 */
	private class DownloadAdvertImages extends AsyncTask<String, Void, String> {

		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);
		}

		protected void onPostExecute(String result) {

			try {
				Bitmap bmap = decodeBitmapBase64(result);
				saveBitmapToInternalStorage(bmap,
						arrayOfImageIDsToDownload[imageCounter]);

				imageCounter++;

				if (imageCounter < arrayOfImageIDsToDownload.length) {

					String url = "http://www.loyal3.co.za/downloadImage?image="
							+ arrayOfImageIDsToDownload[imageCounter].trim()
							+ "&res=" + screenSize();

					new DownloadAdvertImages().execute(url);

				} else {
					setHorizontalScrollView();
					setAdvertFlipper();
					setAdvertImages();
					pDialog.dismiss();
				}

			} catch (Exception e) {
				Toast.makeText(getBaseContext(), "Catch #09:39",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/*
	 * ##########################################################################
	 * #################### Sets onTouchEvent
	 * ###################################
	 * ###########################################################
	 */
	public boolean onTouchEvent(MotionEvent touchevent) {

		try {
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
					viewFlipperScan.setInAnimation(this,
							R.anim.slide_in_from_left);

					// Current screen goes out from right.
					viewFlipperScan.setOutAnimation(this,
							R.anim.slide_out_to_right);

					// Display next screen.
					viewFlipperScan.showNext();
				}
				// Handling right to left screen swap.
				if (lastXScan > currentX) {
					// If there is a child (to the left), just break.
					if (viewFlipperScan.getDisplayedChild() == 1)
						break;

					// Next screen comes in from right.
					viewFlipperScan.setInAnimation(this,
							R.anim.slide_in_from_right);

					// Current screen goes out from left.
					viewFlipperScan.setOutAnimation(this,
							R.anim.slide_out_to_left);

					// Display previous screen.
					viewFlipperScan.showPrevious();
				}

				break;
			}
		} catch (Exception e) {
		}
		return false;

	}

	/*
	 * ##########################################################################
	 * #################### Sets Flipper speed and self-flipping.
	 * ###############
	 * ###########################################################
	 * ####################
	 */
	private void setAdvertFlipper() {
		// Makes the advert viewFlipper auto flip after 5 seconds
		viewFlipperScan = (ViewFlipper) findViewById(R.id.view_flipper_scan);
		viewFlipperScan.setFlipInterval(5000);
		viewFlipperScan.setInAnimation(this, R.anim.slide_in_from_left);
		viewFlipperScan.setOutAnimation(this, R.anim.slide_out_to_right);
		viewFlipperScan.startFlipping();

	}

	/*
	 * ##########################################################################
	 * ######################## Sets premiumAdvert images
	 * #######################
	 * ###################################################
	 * #########################
	 */
	private void setAdvertImages() {

		viewFlipperScan = (ViewFlipper) findViewById(R.id.view_flipper_scan);

		String[] ids = getImageIDs("advertshop");

		for (int i = 0; i < ids.length; i++) {

			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(LinearLayout.VERTICAL);

			LinearLayout.LayoutParams lpScan = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			ll.setLayoutParams(lpScan);
			ll.setGravity(Gravity.CENTER);

			ImageView iv = new ImageView(this);
			iv.setLayoutParams(lpScan);
			iv.setImageBitmap(imageLoadedFromInternalStorage(ids[i]));

			ll.addView(iv);

			viewFlipperScan.addView(ll);
		}
	}

	/*
	 * ##########################################################################
	 * ############################# Checks if network is available
	 * #############
	 * #############################################################
	 * ############################
	 */
	private boolean networkIsAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null
				&& activeNetworkInfo.isConnectedOrConnecting();
	}

	/*
	 * ##########################################################################
	 * #################### Creates array from CSV-string
	 * #######################
	 * #######################################################################
	 */
	/*
	 * private String[] createArrayFromString (String shopsString) {
	 * 
	 * String[] resultArray = shopsString.trim().split(",");
	 * 
	 * if(resultArray[0].equals("")) { removeElt(resultArray, 0); } return
	 * resultArray; }
	 */

	/*
	 * ##########################################################################
	 * ############################ Checks if all 'advertshops' exists (File
	 * f.exists). If not, downloads the images and saves them.
	 * ##################
	 * ########################################################
	 * ##############################
	 */
	private void downloadPremiumAdvertsIfMissing() {

		arrayOfImageIDsToDownload = getArrayOfMissingImages(getImageIDs("advertshop"));

		if (networkIsAvailable()
				&& arrayOfImageIDsToDownload != null && arrayOfImageIDsToDownload.length != 0 && !(arrayOfImageIDsToDownload.length == 1 && arrayOfImageIDsToDownload[0]
						.equals("")))  { // Changed
																					// from
																					// &&																		// to
																					// ||
			pDialog = new ProgressDialog(ScanActivity.this);
			pDialog.setMessage("Downloading adverts...");
			pDialog.show();

			imageCounter = 0;

			String url = "http://www.loyal3.co.za/downloadImage?image="
					+ arrayOfImageIDsToDownload[imageCounter].trim()
					+ "&res="
					+ screenSize();

			new DownloadAdvertImages().execute(url);

		} else {
			
			setHorizontalScrollView();
			setAdvertFlipper(); // Mag dalk die 2 moet omruil.
			setAdvertImages();
		}
	}

	/*
	 * ##########################################################################
	 * #################### Decodes Base64 encoded Bitmap, and saves it to a new
	 * file name after the instance of imageID
	 * ##################################
	 * ############################################################
	 */
	/*
	 * private void decodeBitmapBase64 (String encodedString, String imageName)
	 * { try {
	 * 
	 * byte[] decodedString = Base64.decode(encodedString.getBytes("UTF-8"),
	 * Base64.DEFAULT); Bitmap decodedImage =
	 * BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
	 * 
	 * saveBitmapToInternalStorage(decodedImage, imageName);
	 * 
	 * } catch (Exception e) {
	 * 
	 * Toast.makeText(getBaseContext(), "Catch # 12:17",
	 * Toast.LENGTH_LONG).show();
	 * 
	 * } }
	 */

	/*
	 * ##########################################################################
	 * #################### Saves a Bitmap to
	 * /data/data/loyal3/app_data/imageDir/<imageName>
	 * ##########################
	 * ####################################################################
	 */
	/*
	 * private String saveBitmapToInternalStorage(Bitmap bitmapImage, String
	 * imageName){
	 * 
	 * ContextWrapper cw = new ContextWrapper(getApplicationContext()); // path
	 * to /data/data/yourapp/app_data/imageDir File directory =
	 * cw.getDir("imageDir", Context.MODE_PRIVATE); // Create imageDir File
	 * mypath=new File(directory,imageName.toLowerCase()+".jpg");
	 * 
	 * FileOutputStream fos = null; try { fos = new FileOutputStream(mypath);
	 * 
	 * bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
	 * 
	 * fos.close();
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } //Returns the DIRECTORY
	 * the image was saved to. Not the directory of the image itself. return
	 * directory.getAbsolutePath(); }
	 */

	/*
	 * ##########################################################################
	 * ##################### Sets scanButton, that starts QR-Scanner. Also sets
	 * spacing above&below scanButton.
	 * ##########################################
	 * ######################################################
	 */
	private void setScanButton() {

		final ImageButton ibScan = (ImageButton) findViewById(R.id.ibScan);
		ibScan.setLayoutParams(new LinearLayout.LayoutParams(
				(int) (width * 0.45), (int) (width * 0.45)));
		ibScan.setScaleType(ScaleType.FIT_XY);

		ibScan.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				
				if(networkIsAvailable())	{
				
					IntentIntegrator integrator = new IntentIntegrator(ScanActivity.this);
					integrator.initiateScan();
				} else {
					Toast.makeText(getBaseContext(), "Please ensure that you have a working internet connection.", Toast.LENGTH_LONG).show();
				}

			}
		});

		View viewTop = (View) findViewById(R.id.viewSpacingTop);
		viewTop.setLayoutParams(new LinearLayout.LayoutParams(
				(int) (height * 0.03), (int) (height * 0.03)));
		View viewBottom = (View) findViewById(R.id.viewSpacingBottom);
		viewBottom.setLayoutParams(new LinearLayout.LayoutParams(
				(int) (height * 0.03), (int) (height * 0.03)));
	}

	/*
	 * ##########################################################################
	 * #################### Sets the scroll view and all its contents
	 * (ImageBtns, onClicks, ImageBtns Images)
	 * ##################################
	 * #############################################################
	 */
	/*
	 * private void setVerticalScrollView() {
	 * 
	 * /* LinearLayout llMain = (LinearLayout) findViewById(R.id.llMain);
	 * ScrollView sv = new ScrollView(this); TableLayout tl = new
	 * TableLayout(this);
	 * 
	 * sv.setLayoutParams(new
	 * LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
	 * ,LinearLayout.LayoutParams.MATCH_PARENT));
	 * 
	 * llMain.addView(sv); sv.addView(tl);
	 * 
	 * 
	 * setListOfShopsArray();
	 * 
	 * try{ for(int i = 0;i <= (listOfShopsArray.length/buttonsInRow); i++) {
	 * //Creates TableRows (minimum 1)
	 * 
	 * TableRow tr = new TableRow(this); tl.addView(tr);
	 * 
	 * for(int j =0; j <buttonsInRow;j++) { //Creates 'buttonsInRow' image
	 * buttons per row and sets onClick
	 * 
	 * if(((i*buttonsInRow)+j) < listOfShopsArray.length - emptyArray ) {
	 * 
	 * 
	 * shopName = listOfShopsArray[(i*buttonsInRow)+j].trim(); final String
	 * shopNameTest = shopName;
	 * 
	 * ImageButton ib = new ImageButton(this); int id =
	 * getResources().getIdentifier("shoplogo_"+shopName.toLowerCase(),
	 * "drawable", "com.loyal3.loyal3"); int id2 =
	 * getResources().getIdentifier("shoplogo_no_image", "drawable",
	 * "com.loyal3.loyal3"); Toast.makeText(getBaseContext(), "|"+shopName+"|" ,
	 * Toast.LENGTH_LONG).show();
	 * 
	 * //Checks if shopLogo Exists and sets image accordingly if(id==0) {
	 * ib.setImageResource(id2); } else { ib.setImageResource(id); }
	 * 
	 * ib.setScaleType(ScaleType.FIT_XY); ib.setPadding(2, 0, 2, 6);
	 * ib.setBackgroundColor(Color.TRANSPARENT);
	 * 
	 * // ##MAGIC HAPPENS HERE TableRow.LayoutParams lp = new
	 * TableRow.LayoutParams(width/buttonsInRow, width/buttonsInRow);
	 * 
	 * ib.setLayoutParams(lp);
	 * 
	 * ib.setOnClickListener(new View.OnClickListener() {
	 * 
	 * public void onClick(View arg0) {
	 * 
	 * Intent intent = new Intent(ScanActivity.this, DisplayActivity.class);
	 * intent.putExtra("shopName", shopNameTest); startActivity(intent); } });
	 * //onClickListener
	 * 
	 * tr.addView(ib);
	 * 
	 * } // if } //for(int j) } //for(int i) }catch(Exception e) {
	 * 
	 * Toast.makeText(getBaseContext(),
	 * "Error at Creating of ScrollView",Toast.LENGTH_LONG).show(); }
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * this.setContentView(llMain); }
	 */

	/*
	 * ##########################################################################
	 * #################### Saves a Bitmap to
	 * /data/data/loyal3/app_data/imageDir/<imageName>
	 * ##########################
	 * ####################################################################
	 */
	private String saveBitmapToInternalStorage(Bitmap bitmapImage,
			String imageName) {

		String formattedImageName = imageName.replaceFirst("/", "");

		ContextWrapper cw = new ContextWrapper(getApplicationContext());
		// path to /data/data/yourapp/app_data/imageDir
		File directory = cw.getDir("imageDir", Context.MODE_PRIVATE); // readable
																		// is
																		// just
																		// for
																		// testing
		// Create imageDir
		File mypath = new File(directory, formattedImageName + ".jpg");

		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(mypath);

			bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);

			fos.close();

		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Couldn't save Image",
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		return directory.getAbsolutePath();
	}

	/*
	 * ##########################################################################
	 * #################### Returns an array of the listOfShops SharedPreference
	 * #
	 * #########################################################################
	 * ###################
	 */
	private String[] getListOfShopsArray() {

		String listOfShopsString = getSharedPrefs("userDetails", "listOfShops",
				"");

		if (!listOfShopsString.endsWith(",")) {
			listOfShopsString = listOfShopsString + ",";
		}

		String[] asdf = listOfShopsString.split(",");

		if (asdf.length == 0 || (asdf[0].equals("") && asdf.length == 1)) {
			String[] empty = new String[0];
			return empty;
		} else {
			return asdf;
		}
	}

	/*
	 * ##########################################################################
	 * ############################## Returns all the logoIDs of an array of
	 * shops, as an array.
	 * ######################################################
	 * ###################################################
	 */
	private String[] getLogoIDs(String[] shopArray) {

		String logoIDsString = "";

		for (int i = 0; i < shopArray.length; i++) {

			String mShopName = shopArray[i].trim();

			String imageIDsString = getSharedPrefs(mShopName, "imageIDs",
					"empty");

			if (!imageIDsString.endsWith(",")) {
				imageIDsString = imageIDsString + ",";
			}

			String[] arrayOfIDs = imageIDsString.split(",");

			if (!(arrayOfIDs[0].equals("") && arrayOfIDs.length == 1)) {

				logoIDsString = logoIDsString + arrayOfIDs[0].trim() + ",";

			}

		}

		String[] allLogoImageIDs = logoIDsString.split(",");

		if (allLogoImageIDs[0].equals("") && allLogoImageIDs.length == 1) {

			String[] empty = new String[0];
			return empty;

		} else {
			return allLogoImageIDs;

		}
	}

	private String getLogoID(String mShopName) {

		String imageIDsString = getSharedPrefs(mShopName.trim(), "imageIDs", "empty");
	

		if (!imageIDsString.endsWith(",")) {
			imageIDsString = imageIDsString + ",";
		}

		String[] arrayOfIDs = imageIDsString.split(",");

		return arrayOfIDs[0].trim();

	}

	/*
	 * ##########################################################################
	 * ###################### Creates array of images that is missing (and needs
	 * to be downloaded)
	 * ########################################################
	 * ##############################
	 */
	private String[] getArrayOfMissingImages(String[] imageNames) {

		String missingImagesString = "";
		
		for (String item : imageNames) {

			String formattedImageName = item.replaceFirst("/", "").trim();

			ContextWrapper cw = new ContextWrapper(getApplicationContext());
			// path to /data/data/yourapp/app_data/imageDir
			File directory = cw.getDir("imageDir", Context.MODE_PRIVATE); // readable
																			// is
																			// just
																			// for
																			// testing
			// Create imageDir
			File mypath = new File(directory, formattedImageName.replaceFirst(
					"/", "") + ".jpg");

			// File f = new File(item);

			if (!mypath.exists()) {

				missingImagesString = missingImagesString + item.trim() + ",";
			}

		}

		if (!missingImagesString.endsWith(",")) {
			missingImagesString = missingImagesString + ",";

		}

		String[] missingImagesArray = missingImagesString.split(",");		

		if (missingImagesArray.length == 0 || (missingImagesArray[0].equals("") && missingImagesArray.length == 1)) {

			String[] empty = new String[0];

			return empty;

		} else {

			return missingImagesArray;

		}

	}

	/*
	 * ##########################################################################
	 * #################### Decodes the Base64 encoded string.
	 * ##################
	 * ########################################################
	 * ####################
	 */
	private Bitmap decodeBitmapBase64(String encodedString) {

		try {
			byte[] decodedString = Base64.decode(
					encodedString.getBytes("UTF-8"), Base64.DEFAULT);

			Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedString,
					0, decodedString.length);

			return decodedImage;

		} catch (Exception e) {

			Toast.makeText(getBaseContext(), "Catch # 12:18", Toast.LENGTH_LONG)
					.show();
			return null;

		}

	}

	/*
	 * ##########################################################################
	 * ################################# Determines ScreenSize
	 * ##################
	 * ########################################################
	 * ##################################
	 */
	private String screenSize() {

		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			return "medium";

		} else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
			return "small";

		} else {
			return "large";
		}
	}

	/*
	 * ##########################################################################
	 * ################## get and set SharedPreferences
	 * #########################
	 * ####################################################################
	 */
	private String getSharedPrefs(String folder, String file,
			String defaultValue) {

		SharedPreferences sp = getSharedPreferences(folder, MODE_PRIVATE);
		return sp.getString(file, defaultValue);
	}

	private void setSharedPrefs(String folder, String file, String value) {
		SharedPreferences sp = getSharedPreferences(folder, MODE_PRIVATE);
		SharedPreferences.Editor spe = sp.edit();
		spe.putString(file, value);
		spe.apply();

	}

	/*
	 * ##########################################################################
	 * #################### sets logo
	 * ###########################################
	 * ##################################################
	 */
	private void setLogo(String imageID, ImageButton imageButton) {

		try {

			if (!imageID.equals("empty")) {

				imageButton
						.setImageBitmap(imageLoadedFromInternalStorage(imageID));

			} else {

				int id = getResources().getIdentifier("shoplogo_no_image",
						"drawable", "com.loyal3.loyal3");
				imageButton.setImageResource(id);
			}

		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Catch #10:55", Toast.LENGTH_LONG)
					.show();
		}
	}

	/*
	 * ##########################################################################
	 * ############################### Sets Horizontal ScrollView with their
	 * logos and onClicks
	 * #######################################################
	 * ########################################################
	 */
	private void setHorizontalScrollView() {

		LinearLayout llMain = (LinearLayout) findViewById(R.id.llMain);
		HorizontalScrollView sv = new HorizontalScrollView(this);

		LinearLayout llSv = new LinearLayout(this);
		llSv.setOrientation(LinearLayout.HORIZONTAL);
		llSv.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));

		LinearLayout llContainsSv = (LinearLayout) findViewById(R.id.llContainsSv);
		llContainsSv.addView(sv);

		sv.addView(llSv);
		
		String[] shops = getListOfShopsArray();

		for (int i = 0; i < shops.length; i++) {

			ImageButton ib = new ImageButton(this);
			ib.setScaleType(ScaleType.FIT_XY);
			ib.setPadding(2, 0, 2, 6);
			ib.setBackgroundColor(Color.TRANSPARENT);

			String sn = shops[i].trim();
			final String shopnameChanging = sn;

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width
					/ buttonsInRow, width / buttonsInRow);
			ib.setLayoutParams(lp);

			ib.setOnClickListener(new View.OnClickListener() {

				public void onClick(View arg0) {

					Intent intent = new Intent(ScanActivity.this,
							DisplayActivity.class);
					intent.putExtra("shopName", shopnameChanging);
					startActivity(intent);
				}
			}); // onClickListener

			llSv.addView(ib);

			try {

				String shopLogo = getLogoID(getListOfShopsArray()[i]).trim();
								
				setLogo(shopLogo, ib);

			} catch (Exception e) {
				int id = getResources().getIdentifier("shoplogo_no_image",
						"drawable", "com.loyal3.loyal3");
				ib.setImageResource(id);
			}
		}
		this.setContentView(llMain);
	}

	/*
	 * ##########################################################################
	 * #################### Returns a Bitmap that was loaded from internal
	 * storage (imageName)
	 * ######################################################
	 * ########################################
	 */
	private Bitmap imageLoadedFromInternalStorage(String imageName) {
		try {
			File f = new File(getApplicationContext().getDir("imageDir",
					MODE_PRIVATE).getPath()
					+ "/", imageName.replaceFirst("/", "") + ".jpg");
			Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
			return b;
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Image could not be loaded",
					Toast.LENGTH_LONG).show();
			return null;
		}

	}

	/*
	 * ##########################################################################
	 * ###################### Returns all the imageIDs of a single shop as an
	 * array
	 * ####################################################################
	 * ###############################
	 */
	private String[] getImageIDs(String shop) {

		imageCounter = 0;

		String allImageIDsString = "";

		allImageIDsString = getSharedPrefs(shop.trim(), "imageIDs", "empty");

		if (!allImageIDsString.equals("empty")) {

			if (!allImageIDsString.endsWith(",")) {
				allImageIDsString = allImageIDsString + ",";
			}

			String[] allImageIDs = allImageIDsString.split(",");

			return allImageIDs;

		} else {

			String[] empty = new String[0];
			Toast.makeText(getBaseContext(), "Images: 'empty'",
					Toast.LENGTH_LONG).show();
			return empty;
		}
	}

	private void deleteImagesIfMissing(String newImageIDs, String shopName) {

		ContextWrapper cw = new ContextWrapper(getApplicationContext());

		File directory = cw.getDir("imageDir", Context.MODE_PRIVATE); // readable
																		// is
																		// just
																		// for
																		// testing

		String[] oldImages = getImageIDs(shopName);

		String newImageIDsString = newImageIDs;

		if (!newImageIDs.endsWith(",")) {
			newImageIDsString = newImageIDsString + ",";
		}

		String[] newImages = newImageIDs.split(",");

		if (!(newImages.length == 1 && newImages[0].equals(""))) {

			Set<String> set = new HashSet<String>(Arrays.asList(newImages));

			for (String ID : oldImages) {

				if (!(set.contains(ID) || set.contains(ID + " "))) {

					File mypath = new File(directory, ID + ".jpg");

					boolean deleted = mypath.delete();

					if (deleted) {
						Toast.makeText(getBaseContext(),
								"Image Deleted: " + ID, Toast.LENGTH_LONG)
								.show();
					}
				}
			}
		}
	}

	/*
	 * ##########################################################################
	 * #################### HTTP-Request. Gets&reads JSON. Download new adverts.
	 * Sends to DisplayActivity.
	 * ################################################
	 * ##############################################
	 */
	private class getPremiumAdvertImageIDs extends
			AsyncTask<String, Void, String> {

		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(ScanActivity.this);
			pDialog.setMessage("Just a second ....");
			pDialog.show();
		}

		protected String doInBackground(String... urls) {
			return GET(urls[0]);
		}

		protected void onPostExecute(String result) {

			try {		
				JSONObject json = new JSONObject(result);

				String lShopName = "advertshop";
				String lImageIDsString = json.getString("imageIDs").trim();

				deleteImagesIfMissing(lImageIDsString, lShopName);

				setSharedPrefs(lShopName, "imageIDs", lImageIDsString);


				pDialog.dismiss();
				downloadPremiumAdvertsIfMissing();

			} catch (Exception e) {
				Toast.makeText(getBaseContext(), "Catch #11:49\n"+e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
				
				pDialog.dismiss();
				downloadPremiumAdvertsIfMissing();
				
			
			}
		}
	}

}
