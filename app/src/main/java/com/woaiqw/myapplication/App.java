package com.woaiqw.myapplication;

import android.app.Application;

import com.woaiqw.postprocessing.AndroidPostProcessing;

import hugo.weaving.DebugLog;

/**
 * Created by haoran on 2018/9/12.
 */
public class App extends Application {

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();

        AndroidPostProcessing.initialization(this).dispatcher();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AndroidPostProcessing.release();
    }
}
