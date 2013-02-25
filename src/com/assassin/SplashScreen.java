package com.assassin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreen extends Activity {

	private static String TAG = SplashScreen.class.getName();
	private static long SLEEP_TIME = 3;
	
	@Override
	protected void onCreate(Bundle SavedInstances){
		super.onCreate(SavedInstances);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.splash);
		
		IntentLauncher it = new IntentLauncher();
		
		it.start();
		
		
	}
	
	private class IntentLauncher extends Thread{
		
		public void run(){
			try{
				Thread.sleep(SLEEP_TIME * 1000);
			}catch(Exception e){
				Log.e(TAG, e.getMessage());
			}
		
		Intent intent = new Intent(SplashScreen.this, MainActivity.class);
		
		SplashScreen.this.startActivity(intent);
		SplashScreen.this.finish();
		}
	}

}

