package com.assassin;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

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

	public ArrayList<Location> getRunnerLocations() {
		return getPlayerRelation(true);
	}

	public ArrayList<Location> getChaserLocations() {
		return getPlayerRelation(false);
	}

	private ArrayList<Location> getPlayerRelation(boolean getRunners) {
		try {
			parseObject.fetchIfNeeded();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		ArrayList<Location> result = new ArrayList<Location>();

		try {
			List<ParseObject> relations;

			if (getRunners) {
				relations = parseObject.getRelation("runners").getQuery()
						.find();
			} else {
				relations = parseObject.getRelation("chasers").getQuery()
						.find();
			}

			for (ParseObject relation : relations) {
				ParseGeoPoint runnerLoc = relation.getParseGeoPoint("location");
				if (runnerLoc != null) {
					Location newLoc = new Location("");
					newLoc.setLatitude(runnerLoc.getLatitude());
					newLoc.setLongitude(runnerLoc.getLongitude());
					result.add(newLoc);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean isCaught() {
		return parseObject.getBoolean("caught");
	}

	public static void resetInstance() {
		instance = null;
	}
}
