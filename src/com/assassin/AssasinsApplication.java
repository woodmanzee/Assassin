package com.assassin;
import com.parse.Parse;

import android.app.Application;

public class AssasinsApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		
		Parse.initialize(this, "7WuNEiRpNQA9Hh296skBSfDjDT6zeiIaVouMG2ur", "pxYTUrq6usL2f2y2rqWedkncUfKVHeNIenChc77N");
	}
}
