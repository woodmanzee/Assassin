package com.assassin;

import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements LocationListener,
		LocationSource {

	private String provider;

	// Message IDs
	private static final int ADD_MARKER = 0;
	private static final int CLEAR_MAP = 1;
	protected static final int SET_DISTANCE = 2;
	protected static final int CAUGHT = 3;

	protected static final float FEET_IN_ONE_METER = 3.28084F;

	private static final long REFRESH_RATE = 15000; // time in ms

	private GoogleMap mMap;

	private OnLocationChangedListener mListener;
	private LocationManager locationManager;

	private Location playerLocation = null;

	private TextView distanceText;

	private AlertDialog caughtAlert;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		distanceText = (TextView) findViewById(R.id.distanceText);

		// Instantiate caught alert box.
		if (caughtAlert == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			// Set content
			builder.setTitle(R.string.caught);
			builder.setMessage(R.string.caughtMsg);

			// Add the buttons
			builder.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// User clicked OK button
						}
					});
			// Create the AlertDialog
			caughtAlert = builder.create();
		}

		// Handles message passing from background thread to UI thread
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == ADD_MARKER) {
					mMap.addMarker(new MarkerOptions().position(
							(LatLng) msg.obj).title(msg.arg1 + " feet"));
				} else if (msg.what == CLEAR_MAP) {
					mMap.clear();
				} else if (msg.what == SET_DISTANCE) {
					if (msg.arg1 != Integer.MAX_VALUE)
						distanceText.setText("  Nearest chaser: " + msg.arg1
								+ " feet");
					else
						distanceText.setText("  Nearest chaser: n/a");
				} else if (msg.what == CAUGHT) {
					caughtAlert.show();
				}
				super.handleMessage(msg);
			}
		};

		// Spawns new thread that will handle player updating every 15 seconds.
		TimerSingleton timer = TimerSingleton.getInstance();
		TimerTask refresher = new TimerTask() {
			public void run() {
				Player player = Player.getInstance();
				if (playerLocation != null)
					player.saveLocation(playerLocation);
				player.refresh();

				// Check if caught
				if (player.isCaught()) {
					Message msg = handler.obtainMessage();
					msg.what = CAUGHT;
					handler.sendMessage(msg);
					return;
				}

				Message msg = handler.obtainMessage();
				msg.what = CLEAR_MAP;
				handler.sendMessage(msg);

				int minDistance = Integer.MAX_VALUE;

				// Send messages to add markers for chasers
				for (Location runnerLoc : player.getRunnerLocations()) {
					msg = handler.obtainMessage();
					msg.what = ADD_MARKER;

					LatLng latLng = new LatLng(runnerLoc.getLatitude(),
							runnerLoc.getLongitude());
					msg.obj = latLng;

					int distance = Integer.MAX_VALUE;
					if (playerLocation != null)
						distance = (int) (playerLocation.distanceTo(runnerLoc) * FEET_IN_ONE_METER);

					msg.arg1 = distance;

					if (playerLocation != null)
						handler.sendMessage(msg);
				}

				// Calculate distance for chaser
				for (Location chaserLoc : player.getChaserLocations()) {
					int distance = Integer.MAX_VALUE;
					if (playerLocation != null)
						distance = (int) (playerLocation.distanceTo(chaserLoc) * FEET_IN_ONE_METER);
					if (distance < minDistance)
						minDistance = distance;
				}

				msg = handler.obtainMessage();
				msg.what = SET_DISTANCE;
				msg.arg1 = minDistance;
				if (playerLocation != null)
					handler.sendMessage(msg);
			}
		};

		// first event immediately, following after n seconds each
		timer.scheduleAtFixedRate(refresher, 0, REFRESH_RATE);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		if (locationManager != null) {
			boolean gpsIsEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			boolean networkIsEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (gpsIsEnabled) {
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 5000L, 10F, this);
			} else if (networkIsEnabled) {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 5000L, 10F, this);
			} else {
				// Show an error dialog that GPS is disabled...
			}
		} else {
			// Show some generic error dialog because something must have gone
			// wrong with location manager.
		}

		setUpMapIfNeeded();
	}

	@Override
	public void onPause() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}

		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		setUpMapIfNeeded();

		if (locationManager != null) {
			mMap.setMyLocationEnabled(true);
		}
	}

	/**
	 * Sets up the map if it is possible to do so (i.e., the Google Play
	 * services APK is correctly installed) and the map has not already been
	 * instantiated.. This will ensure that we only ever call
	 * {@link #setUpMap()} once when {@link #mMap} is not null.
	 * <p>
	 * If it isn't installed {@link SupportMapFragment} (and
	 * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt
	 * for the user to install/update the Google Play services APK on their
	 * device.
	 * <p>
	 * A user can return to this Activity after following the prompt and
	 * correctly installing/updating/enabling the Google Play services. Since
	 * the Activity may not have been completely destroyed during this process
	 * (it is likely that it would only be stopped or paused),
	 * {@link #onCreate(Bundle)} may not be called again so we should call this
	 * method in {@link #onResume()} to guarantee that it will be called.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.

			if (mMap != null) {
				setUpMap();

			}

			// This is how you register the LocationSource
			mMap.setLocationSource(this);

			Criteria criteria = new Criteria();
			provider = locationManager.getBestProvider(criteria, false);

			// gets last known location from
			Location location = locationManager.getLastKnownLocation(provider);
			LatLng latlng = new LatLng(location.getLatitude(),
					location.getLongitude());
			mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
					location.getLatitude(), location.getLongitude())));
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
		}
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the
	 * camera.
	 * <p>
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	private void setUpMap() {
		mMap.setMyLocationEnabled(true);
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
	}

	@Override
	public void deactivate() {
		mListener = null;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (mListener != null) {
			mListener.onLocationChanged(location);

			mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
					location.getLatitude(), location.getLongitude())));
		}

		playerLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}
}