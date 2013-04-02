package com.assassin;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

public class Player {

	private static Player instance;
	private ParseObject parseObject;
	private boolean runnerCaughtByOther;
	private boolean runnerCaughtByMe;
	private String targetPoint;

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

		runnerCaughtByOther = false;
		runnerCaughtByMe = false;
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

				// Check for caught runners
				if (getRunners && relation.getBoolean("caught"))
				{
					if (relation.getParseObject("caughtBy").getObjectId().equals(parseObject.getObjectId()))
					{
						runnerCaughtByMe = true;
					}
					else
					{
						runnerCaughtByOther = true;
					}

					// Acknowledge catch by removing from relation
					ParseRelation runners = parseObject.getRelation("runners");
					runners.remove(relation);
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

	public boolean runnerCaughtByPlayer()
	{
		boolean result = runnerCaughtByMe;
		runnerCaughtByMe = false;
		return result;
	}

	public boolean runnerCaughtByOther()
	{
		boolean result = runnerCaughtByOther;
		runnerCaughtByOther = false;
		return result;
	}

	public int getRunnerType() {
		// returns 1 if going to location, 0 if chasing a human
		if (parseObject.getBoolean("chasingHuman") == true) {
			return 0;
		} else {
			return 1;
		}
	}

	public Location getPointLocation() {
		
		try {
			parseObject.fetchIfNeeded();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//ParseGeoPoint point = parseObject.getParseObject("currentTarget").getParseGeoPoint("location");
		
		Location newLoc = new Location("");
		ParseQuery query = new ParseQuery("BYULocation");
		query.getInBackground(parseObject.getParseObject("currentTarget").getObjectId(), new GetCallback() {
		  public void done(ParseObject object, ParseException e) {
		    if (e == null) {
		    	Location newLoc = new Location("");
				newLoc.setLatitude(object.getParseGeoPoint("location").getLatitude());
				newLoc.setLongitude(object.getParseGeoPoint("location").getLongitude());
				targetPoint = object.getString("description");
		    } else {
		      // something went wrong
		    }
		  }
		});

		return newLoc;
	}

	public String getPointDescription() {
		//return parseObject.getParseObject("targetLocation").getString("description");
		return targetPoint;
	}
	
	
}
