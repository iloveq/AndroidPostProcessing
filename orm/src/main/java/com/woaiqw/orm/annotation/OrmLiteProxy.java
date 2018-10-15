package com.woaiqw.orm.annotation;

import android.app.Application;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.woaiqw.postprocessing.IApp;
import com.woaiqw.postprocessingannotation.App;

/**
 * Created by haoran on 2018/10/15.
 */
@App(name = "OrmLite", priority = 2)
public class OrmLiteProxy implements IApp {
    @Override
    public void dispatcher(@NonNull Application application) {
        Toast.makeText(application, "OrmLite", Toast.LENGTH_SHORT).show();
    }
}
