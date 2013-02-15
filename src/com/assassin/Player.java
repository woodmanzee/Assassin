package com.assassin;

import android.location.Location;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

public class Player {
	
	private static Player instance;
	private ParseObject parseObject;
	
	// Return instance of Singleton
	public static Player getInstance()
	{
		if (instance == null)
		{
			instance = new Player();
		}
		
		return instance;
	}

	// Singleton constructor
	private Player()
	{
		parseObject = new ParseObject("Player");
	}
	
	public void saveLocation(Location location)
	{
		ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
		parseObject.put("location", point);
		parseObject.put("caught", true);
		parseObject.saveInBackground();
	}
}
