package com.woaiqw.cache;

import android.app.Application;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.woaiqw.postprocessing.IApp;
import com.woaiqw.postprocessingannotation.App;

/**
 * Created by haoran on 2018/10/10.
 */
@App(name = "Cache", priority = 2, async = true, delay = 2000)
public class CacheProxy implements IApp {

    @Override
    public void dispatcher(@NonNull Application application) {

        Looper.prepare();
        Toast.makeText(application, "cache", Toast.LENGTH_SHORT).show();
        Looper.loop();

    }
}
