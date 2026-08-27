package com.focusforge.app;

import android.app.Application;

public class FocusForgeApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FocusForgeConfig.init(this);
    }
}
