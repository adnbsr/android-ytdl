package com.andromedalabs.ytdl;

import android.app.Application;
import timber.log.Timber;

/**
 * Created by adnan on 6/26/15.
 */
public class App extends Application {

	@Override public void onCreate() {
		super.onCreate();

		if(!BuildConfig.DEBUG){
			Timber.plant(new Timber.DebugTree());
		}
	}
}
