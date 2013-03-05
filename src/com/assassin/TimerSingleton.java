package com.assassin;

import java.util.Timer;

public class TimerSingleton extends Timer {

	private static TimerSingleton instance;

	private TimerSingleton() {
		// create as daemon thread always
		super(true);
	}

	public static TimerSingleton getInstance() {
		if (instance == null) {
			instance = new TimerSingleton();
		}

		return instance;
	}
}