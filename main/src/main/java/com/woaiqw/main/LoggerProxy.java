package com.woaiqw.main;

import android.app.Application;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.woaiqw.postprocessing.IApp;
import com.woaiqw.postprocessingannotation.App;

/**
 * Created by haoran on 2018/10/15.
 */
//@App(name = "Logger",priority = 1)
public class LoggerProxy implements IApp {

    @Override
    public void dispatcher(@NonNull Application application) {
        Toast.makeText(application, "Logger", Toast.LENGTH_SHORT).show();
    }
}
