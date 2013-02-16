package com.assassin;

import java.util.Timer;
import java.util.TimerTask;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements LocationListener, LocationSource {
  
  private String provider;
	private static final int ADD_MARKER = 0;
	private static final int CLEAR_MAP = 1;
  
  private GoogleMap mMap;
  
  private OnLocationChangedListener mListener;
  private LocationManager locationManager;

  private Location playerLocation = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

    // Handles message passing from background thread to UI thread
	final Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if(msg.what == ADD_MARKER)
			{
				mMap.addMarker(new MarkerOptions().position((LatLng)msg.obj));
			}
			else if(msg.what == CLEAR_MAP)
			{
				mMap.clear();
			}
			super.handleMessage(msg);
		}
	};

		// Spawns new thread that will handle player updating every 15 seconds.
		Timer timer = new Timer(true);
		TimerTask refresher = new TimerTask() {
			public void run() {
				Player player = Player.getInstance();
				if (playerLocation != null)
					player.saveLocation(playerLocation);
				player.refresh();

				Message msg = handler.obtainMessage();
				msg.what = CLEAR_MAP;
				handler.sendMessage(msg);

				for (LatLng runnerLoc : player.getRunnerLocations()) {
					msg = handler.obtainMessage();
					msg.what = ADD_MARKER;
					msg.obj = runnerLoc;
					handler.sendMessage(msg);
				}
			}
		};

	  // first event immediately,  following after 15 seconds each
	  timer.scheduleAtFixedRate(refresher, 0, 15000);

      locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

  	    if(locationManager != null)
  	    {
  	        boolean gpsIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
  	        boolean networkIsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
  	    	
  	    	if(gpsIsEnabled)
  	    	{
  	    		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10F, this);
  	    	}
  	    	else if(networkIsEnabled)
  	    	{
  	    		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 10F, this);
  	    	}
  	    	else
  	    	{
  	    		//Show an error dialog that GPS is disabled...
            }
  	    }
  	    else
  	    {
  	    	//Show some generic error dialog because something must have gone wrong with location manager.
  	    }
      
      setUpMapIfNeeded();
  }

	@Override
	public void onPause()
	{
		if(locationManager != null)
		{
			locationManager.removeUpdates(this);
		}
		
		super.onPause();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		setUpMapIfNeeded();
		
		if(locationManager != null)
		{
			mMap.setMyLocationEnabled(true);
		}
	}
	

  /**
   * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
   * installed) and the map has not already been instantiated.. This will ensure that we only ever
   * call {@link #setUpMap()} once when {@link #mMap} is not null.
   * <p>
   * If it isn't installed {@link SupportMapFragment} (and
   * {@link com.google.android.gms.maps.MapView
   * MapView}) will show a prompt for the user to install/update the Google Play services APK on
   * their device.
   * <p>
   * A user can return to this Activity after following the prompt and correctly
   * installing/updating/enabling the Google Play services. Since the Activity may not have been
   * completely destroyed during this process (it is likely that it would only be stopped or
   * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
   * {@link #onResume()} to guarantee that it will be called.
   */
  private void setUpMapIfNeeded() {
      // Do a null check to confirm that we have not already instantiated the map.
      if (mMap == null) 
      {
          // Try to obtain the map from the SupportMapFragment.
          mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
          // Check if we were successful in obtaining the map.
         
          if (mMap != null) 
          {
              setUpMap();
              
          }

          //This is how you register the LocationSource
          mMap.setLocationSource(this);
          
          Criteria criteria = new Criteria();
          provider = locationManager.getBestProvider(criteria, false);
          
          // gets last known location from 
          Location location = locationManager.getLastKnownLocation(provider);
          LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
          mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
      }
  }
  
  /**
   * This is where we can add markers or lines, add listeners or move the camera.
   * <p>
   * This should only be called once and when we are sure that {@link #mMap} is not null.
   */
  private void setUpMap() 
  {
      mMap.setMyLocationEnabled(true);
  }
  
	@Override
	public void activate(OnLocationChangedListener listener) 
	{
		mListener = listener;
	}
	
	@Override
	public void deactivate() 
	{
		mListener = null;
	}

	@Override
	public void onLocationChanged(Location location) 
	{
	    if( mListener != null )
	    {
	        mListener.onLocationChanged( location );

	        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
	    }

	    playerLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider) 
	{
		// TODO Auto-generated method stub
		Toast.makeText(this, "Provider disabled", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) 
	{
		// TODO Auto-generated method stub
		Toast.makeText(this, "Provider enabled", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{
		// TODO Auto-generated method stub
		Toast.makeText(this, "Location based status has changed", Toast.LENGTH_SHORT).show();
	}
}