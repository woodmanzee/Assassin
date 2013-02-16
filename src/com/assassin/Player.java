package com.assassin;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

public class Player {

	private static Player instance;
	private ParseObject parseObject;

	// Return instance of Singleton
	public static Player getInstance() {
		if (instance == null) {
			instance = new Player();
		}

		return instance;
	}

	// Singleton constructor
	private Player() {
		parseObject = new ParseObject("Player");
		try {
			parseObject.save();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void saveLocation(Location location) {
		ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(),
				location.getLongitude());
		parseObject.put("location", point);

		try {
			parseObject.save();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void refresh() {
		try {
			parseObject.refresh();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<LatLng> getRunnerLocations() {
		try {
			parseObject.fetchIfNeeded();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		ArrayList<LatLng> result = new ArrayList<LatLng>();

		try {
			List<ParseObject> runners = parseObject.getRelation("chasers")
					.getQuery().find();
			for (ParseObject runner : runners) {
				ParseGeoPoint runnerLoc = runner.getParseGeoPoint("location");
				if (runnerLoc != null) {
					LatLng newLoc = new LatLng(runnerLoc.getLatitude(),
							runnerLoc.getLongitude());
					Log.d("ASSASSINS", "New Loc: " + newLoc.latitude + " "
							+ newLoc.longitude);
					result.add(newLoc);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return result;
	}
}
