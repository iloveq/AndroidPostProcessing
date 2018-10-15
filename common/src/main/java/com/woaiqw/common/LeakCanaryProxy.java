package com.woaiqw.common;

import android.app.Application;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.woaiqw.postprocessing.IApp;
import com.woaiqw.postprocessingannotation.App;

//import hugo.weaving.DebugLog;


/**
 * Created by haoran on 2018/10/11.
 */
@App(name = "LeakCanary", type = App.DEBUG, priority = 1, delay = 5000)
public class LeakCanaryProxy implements IApp {

    //@DebugLog
    @Override
    public void dispatcher(@NonNull Application application) {

        Toast.makeText(application, "LeakCanary", Toast.LENGTH_SHORT).show();
    }
}
