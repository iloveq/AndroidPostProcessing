package com.woaiqw.hotfix;

import android.app.Application;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.woaiqw.postprocessing.IApp;
import com.woaiqw.postprocessingannotation.App;

import hugo.weaving.DebugLog;

/**
 * Created by haoran on 2018/10/10.
 */
@App(name = "Hotfix", priority = 3)
public class HotfixProxy implements IApp {

    @DebugLog
    @Override
    public void dispatcher(@NonNull Application application) {

        Toast.makeText(application, "Hotfix", Toast.LENGTH_SHORT).show();

    }
}
