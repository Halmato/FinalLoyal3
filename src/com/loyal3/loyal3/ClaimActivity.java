package com.loyal3.loyal3;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;

import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ClaimActivity extends ActionBarActivity {

	private int seconds = 300;	//5 minutes
	private int interval = 1;	//seconds between flashing
	private int idBlack, idBlack2, idBlack3, counter;
	private int width;
	private String shopName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
	    actionBar.setBackgroundDrawable(new ColorDrawable(0xFF800000));	//Sets AB Color
		setContentView(R.layout.activity_claim);
		
		final Calendar cal = Calendar.getInstance();
		
		TextView tvDate = (TextView) findViewById(R.id.tvDate);
		tvDate.setText(currentTime());
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		width = displaymetrics.widthPixels;
		
		Bundle extras = getIntent().getExtras();
		shopName = extras.getString("shopName");
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int)(width*0.6),(int)(width*0.6));
		lp.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
		
		ImageView ivShopLogo = (ImageView) findViewById(R.id.ivShopLogo);
		ivShopLogo.setLayoutParams(lp);		
		
		ivShopLogo = (ImageView) findViewById(R.id.ivShopLogo);
		
		
		setLogo(getLogoID(shopName), ivShopLogo);	
		
		idBlack = getResources().getIdentifier("background_qr_xxhdpi_black_2", "drawable", "com.loyal3.loyal3");
		idBlack2 = getResources().getIdentifier("background_qr_xxhdpi_black2_2", "drawable", "com.loyal3.loyal3");
		idBlack3 = getResources().getIdentifier("background_qr_xxhdpi_black3_2", "drawable", "com.loyal3.loyal3");

		
		new CountDownTimer((seconds * 1000), 1000) {
			public void onTick(long millisUntilFinished) {
	
				RelativeLayout rlMain = (RelativeLayout) findViewById(R.id.rlMain);	
				if((millisUntilFinished/1000) % interval == 0)	{
				
					if(counter%4 == 0){
						rlMain.setBackgroundResource(idBlack);
					} else if(counter%4 == 1){
						rlMain.setBackgroundResource(idBlack2);
					} else if(counter%4 == 2){
						rlMain.setBackgroundResource(idBlack3);
					} else {
						rlMain.setBackgroundResource(idBlack2);

					}
					
					counter++;
				}	
				
				String minutesCorrectFormat = "";
				String secondsCorrectFormat = "";
				
				if(millisUntilFinished/60000 < 10) {
					minutesCorrectFormat = "0"+(millisUntilFinished/60000);
				} else {
					minutesCorrectFormat = "" + millisUntilFinished/60000;
				}
				
				if((millisUntilFinished/1000)%60 < 10) {
					secondsCorrectFormat = "0"+((millisUntilFinished/1000)%60);
				} else {
					secondsCorrectFormat = "" + (millisUntilFinished/1000)%60;
				}
				
				TextView tvTest = (TextView)findViewById(R.id.tvTimer);
				tvTest.setText("Time remaining: " + minutesCorrectFormat +":"+secondsCorrectFormat);}

			     public void onFinish() {
			    	 finish();
			     }
			  }.start();
	}

/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.claim, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/
	
	
	
	
	
	
	
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
	
	
	/*############################################################################################
	 * get and set SharedPreferences
	#############################################################################################*/
	private String getSharedPrefs (String folder, String file, String defaultValue)	{

		SharedPreferences sp = getSharedPreferences(folder, MODE_PRIVATE);
		return sp.getString(file, defaultValue);
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
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        exitByBackKey();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	protected void exitByBackKey() {

	    AlertDialog alertbox = new AlertDialog.Builder(this)
	    .setMessage("You will not be able to return to this screen if you proceed. Are you sure you want to close the screen?")
	    .setPositiveButton("YES", new DialogInterface.OnClickListener() {

	        // do something when the button is clicked
	        public void onClick(DialogInterface arg0, int arg1) {

	            finish();
	            //close();


	        }
	    })
	    .setNegativeButton("NO", new DialogInterface.OnClickListener() {

	        // do something when the button is clicked
	        public void onClick(DialogInterface arg0, int arg1) {
	                       }
	    })
	      .show();

	}
	
	
	
	
	
	
	
	
	
}
