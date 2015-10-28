package com.healzo.doc;

import android.app.Application;

import com.bugsnag.android.Bugsnag;
import com.parse.Parse;
import com.parse.ParseCrashReporting;

public class CabzoDriverApp extends Application {	

	@Override
    public void onCreate() {
        super.onCreate();
        ParseCrashReporting.enable(this);
        Parse.initialize(this, "JXtfT9oCo1yxzIelfh3zwkgL4W24XQIoNvMHNb0P", "tHD2WiXXb2WWrfB1V2zhyWZfJyA3uQRb8hI1tg7K");
        //Bugsnag.register(this, "46b4008dcb284e84383eaf37318fe0ee");
//        Bugsnag.notify(new RuntimeException("Non-fatal"));
    }
}
