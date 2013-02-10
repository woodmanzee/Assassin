package com.assassin;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.RefreshCallback;
import com.parse.SaveCallback;

import android.location.Location;

public class Player {
	
	private ParseObject parsePlayer;
	private static Location curLocation;
	
	public Player(Location location) {
		parsePlayer = new ParseObject("Player");
		parsePlayer.saveInBackground();
		curLocation = location;
	}

	public static void updateLocation(Location location) {
		// gets location from Android devices
		curLocation = location;
	}

	public void refresh() {
		// refreshes object information from Parse
		parsePlayer.refreshInBackground(new RefreshCallback() {
			public void done(ParseObject object, ParseException e) {
			    if (e == null) {
			      parsePlayer = object;
			    } else {
			      // Failure!
			    }
			}
		});
		
	}

	public void updateGeoPoint() {
		// save GeoPoint to parse
		ParseGeoPoint point = new ParseGeoPoint(curLocation.getLatitude(), curLocation.getLongitude());
        parsePlayer.put("location", point);
        saveInBackground();
	}

	public List<LatLng> getChasers() {
		// get locations of all chasers
		final List<LatLng> chasers = new ArrayList<LatLng>();
		ParseRelation relation = parsePlayer.getRelation("chasers");
		
		relation.getQuery().findInBackground(new FindCallback() {
		    public void done(List<ParseObject> results, ParseException e) {
		      if (e != null) {
		        // There was an error
		      } else {
			        for (int i = 0; i < results.size(); i++) {
			        	ParseGeoPoint point = results.get(i).getParseGeoPoint("location");
			        	chasers.add(new LatLng(point.getLatitude(), point.getLongitude()));
			        }
		      }
		    }
		});
		
		
		return chasers;
	}

	public void saveInBackground() {
		// save Parse player to Parse
		parsePlayer.saveInBackground(new SaveCallback() {
			  public void done(ParseException e) {
			      parsePlayer.saveInBackground();
			  }
			  
		});
	}
	
	// * * * * * * * * * * ATTEMPT AT USING CALLS IN CALLBACKS TO AVOID NETWORK ERROR - CURRENTLY FAILS * * * * * * * * * * 
	public List<LatLng> runLoop() {
		final List<LatLng> chasers = new ArrayList<LatLng>();
		parsePlayer.refreshInBackground(new RefreshCallback() {
			public void done(ParseObject object, ParseException e) {
				if (e == null) {
					// Success
					parsePlayer = object;
					
					// find chasers
					ParseRelation relation = parsePlayer.getRelation("chasers");
					
					relation.getQuery().findInBackground(new FindCallback() {
					    public void done(List<ParseObject> results, ParseException e) {
					      if (e != null) {
					        // There was an error
					      } else {
						        for (int i = 0; i < results.size(); i++) {
						        	ParseGeoPoint point = results.get(i).getParseGeoPoint("location");
						        	chasers.add(new LatLng(point.getLatitude(), point.getLongitude()));
						        }
						        
						        ParseGeoPoint myPoint = new ParseGeoPoint(curLocation.getLatitude(), curLocation.getLongitude());
						        parsePlayer.put("location", myPoint);
						        parsePlayer.saveInBackground();
					      }
					    }
					});
				} else {
					// failure
					return;
				}
			}
		});
		return chasers;
	}

}
