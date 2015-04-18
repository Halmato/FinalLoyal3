package com.loyal3.loyal3;

import java.io.File;
import java.io.FileInputStream;
import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class My3wardsActivity extends ActionBarActivity {
	
	private int height, width;

	
	/*###############################################################################################
	 * onCreate-
	 ###############################################################################################*/
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setBackgroundDrawable(new ColorDrawable(0xFF800000));
		setContentView(R.layout.activity_my3wards);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		
		height = displaymetrics.heightPixels;
		width = displaymetrics.widthPixels;
		
		setLogoImageButtonList();
	}

	/*############################################################################################
	 * Action Bar Options Menu Settings
	 #################################################################################################*/
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my3wards, menu);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_contact) {
			Intent intent = new Intent(My3wardsActivity.this, ContactUsActivity.class);
			startActivity(intent);
			return true;
			
		}else if(id == R.id.action_profile)	{
			Intent intent = new Intent(My3wardsActivity.this, ProfileActivity.class);
			startActivity(intent);
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
	
	/*##############################################################################################
	 * Returns an array of the listOfShops SharedPreference
	 #############################################################################################*/
	private String[] getListOfShopsArray()	{

		String listOfShopsString =  getSharedPrefs("userDetails", "listOfShops", "");

		String[] asdf = listOfShopsString.split(",");

		if( asdf.length ==0 || (asdf[0].equals("") && asdf.length == 1))	{
			String[] empty = new String[0];
			Toast.makeText(getBaseContext(), "No Shops Available", Toast.LENGTH_LONG).show();

			return empty;
		} else {
			return asdf;
		}
	}		
		
	/*##############################################################################################
	 * Sets logo
	 #############################################################################################*/
	private void setLogo(String imageID , ImageButton imageButton)	{
		try {

			imageButton.setImageBitmap(imageLoadedFromInternalStorage(imageID));

		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Catch #10:56", Toast.LENGTH_LONG).show();

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
			Toast.makeText(getBaseContext(), "Image could not be loaded", Toast.LENGTH_LONG).show();
			
			
			int lId = getResources().getIdentifier("shoplogo_no_image", "drawable", "com.loyal3.loyal3");
			
			Bitmap noImage = BitmapFactory.decodeResource(getResources(), lId);
			
			return noImage;
		}

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
	 * Sets scroll view filled with image buttons of all the shops that directs user to DisplayActivity.
	 ##################################################################################################*/
	private void setLogoImageButtonList()	{
		
		
		String[] shops = getListOfShopsArray();
		
		// Sets scrollView dynamically with ImageButtons, textViews and Buttons(claim).		
		LinearLayout llMain = (LinearLayout) findViewById(R.id.llMain);
		ScrollView sv = new ScrollView(this);

		llMain.addView(sv);
		
		TableLayout tl = new TableLayout(this);
		sv.addView(tl);

		for(int i = 0; i < shops.length; i++)	{	
			
			TableRow tr = new TableRow(this);

			tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
			tr.setBackgroundResource(R.drawable.row_border);	
			
			tl.addView(tr);
			
			String shopname = shops[i].trim();
			String scancount = getSharedPrefs(shopname, "scanCount", "0");
			String maxscans = getSharedPrefs(shopname, "maxScans", "10");

			
/*			if(Integer.parseInt(scancount) == 0 || Integer.parseInt(scancount) >= Integer.parseInt(maxscans)) {
				scancount = getSharedPrefs(shopname, "localScanCount", "0");
			}
			*/
			
			String lastredeem = getSharedPrefs(shopname, "lastRedeem", "No Redeems");
			
			String shopLogo = getLogoID(shopname);
			
			ImageButton ib = new ImageButton(this);
			
			setLogo(shopLogo, ib);

			TableRow.LayoutParams lp1 = new TableRow.LayoutParams((int)(width/2),(int) (width/2));
			TableRow.LayoutParams lp2 = new TableRow.LayoutParams((int)(width/2),(int) (width/8*3));
			TableRow.LayoutParams lp3 = new TableRow.LayoutParams((int)(width/2),(int) (width/8));


			ib.setLayoutParams(lp1);
			
			ib.setScaleType(ScaleType.FIT_XY);
			ib.setPadding(0, 2, 0, 2);
			
			final String shopNameInstance = shopname;

			ib.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					
					Intent intent = new Intent(My3wardsActivity.this, DisplayActivity.class);
					intent.putExtra("shopName", shopNameInstance);
					startActivity(intent);
					finish();
					}
				});	//End of onClickListener
			
			
						
			LinearLayout llInTR = new LinearLayout(this);
			
			llInTR.setOrientation(LinearLayout.VERTICAL);
			llInTR.setLayoutParams(lp1);
			
		//	TextView tvScanCount = new TextView(this);
		//	TextView tvLastRedeem = new TextView(this);
			
			Button btnScanCount = new Button(this);
			Button btnLastRedeem = new Button(this);
			
			
			btnScanCount.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					
					Intent intent = new Intent(My3wardsActivity.this, DisplayActivity.class);
					intent.putExtra("shopName", shopNameInstance);
					startActivity(intent);
					finish();
					}
				});	//End of onClickListener
			
			btnLastRedeem.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					
					Intent intent = new Intent(My3wardsActivity.this, DisplayActivity.class);
					intent.putExtra("shopName", shopNameInstance);
					startActivity(intent);
					finish();
					}
				});	//End of onClickListener
			
			btnScanCount.setLayoutParams(lp2);
			btnLastRedeem.setLayoutParams(lp3);
			
			btnLastRedeem.setPadding(0, 0, 0, 0);
			btnScanCount.setPadding(0, 0, 0, 0);

			
	//		tvScanCount.setLayoutParams(lp2);
	//		tvLastRedeem.setLayoutParams(lp3);

			
			btnScanCount.setText("SCANS  "+scancount+" / "+maxscans);
			btnScanCount.setGravity(Gravity.CENTER);
			
	//		tvScanCount.setText("SCANS  "+scancount+" / "+maxscans);
	//		tvScanCount.setGravity(Gravity.CENTER);
			
			
			btnLastRedeem.setText("Last Redeem:\n"+lastredeem);
			btnLastRedeem.setGravity(Gravity.CENTER);
			
			btnLastRedeem.setBackgroundColor(Color.TRANSPARENT);
			btnScanCount.setBackgroundColor(Color.TRANSPARENT);
			
	//		tvLastRedeem.setText("Last Redeem: "+lastredeem);
	//		tvLastRedeem.setGravity(Gravity.CENTER);
			
			tr.addView(ib);
			tr.addView(llInTR);
			

			llInTR.addView(btnScanCount);
			llInTR.addView(btnLastRedeem);
							
		//	llInTR.addView(tvScanCount);
		//	llInTR.addView(tvLastRedeem);
							
		}	

			this.setContentView(llMain);
	}
	
}
