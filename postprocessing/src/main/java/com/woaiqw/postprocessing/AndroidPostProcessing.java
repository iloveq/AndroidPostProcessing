package com.woaiqw.postprocessing;

import android.app.Application;

import com.woaiqw.postprocessing.model.AppDelegate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by haoran on 2018/10/10.
 */
public class AndroidPostProcessing {

    private volatile static AndroidPostProcessing instance = null;

    private volatile LinkedHashMap<String, AppDelegate> map = new LinkedHashMap<>(32, 0.75f, true);

    private AndroidPostProcessing() {
        processorAnnotationOfApp();
    }

    public static AndroidPostProcessing initialization() {
        if (null == instance) {
            synchronized (AndroidPostProcessing.class) {
                if (null == instance)
                    instance = new AndroidPostProcessing();
            }
        }
        return instance;
    }


    private void processorAnnotationOfApp() {
        //TODO: processor annotation
        AppDelegate agent = new AppDelegate();
        map.put("key", agent);
    }

    public void dispatcher(Application app) {
        for (Map.Entry<String, AppDelegate> entry : map.entrySet()) {
            entry.getValue().getAgent().dispatcher(app);
        }
    }

    public void release(Application app) {
        map.clear();
    }


}
