package com.woaiqw.postprocessing;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;

import com.woaiqw.postprocessing.model.AppDelegate;
import com.woaiqw.postprocessing.utils.ClassUtils;
import com.woaiqw.postprocessing.utils.WeakHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by haoran on 2018/10/10.
 */
public class AndroidPostProcessing {

    private volatile static Application app;

    private volatile static AndroidPostProcessing instance = null;

    private volatile static LinkedHashMap<String, AppDelegate> map;

    private volatile static ScheduledExecutorService taskPool;

    private static AtomicBoolean initCompleted = new AtomicBoolean(false);

    private static WeakHandler h = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    }, Looper.getMainLooper());


    static {
        int CPU_COUNT = Runtime.getRuntime().availableProcessors();
        int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
        map = new LinkedHashMap<>(32, 0.75f, true);
        taskPool = Executors.newScheduledThreadPool(CORE_POOL_SIZE);
    }


    private AndroidPostProcessing(@NonNull final Application app) {
        initAppDelegateMap(app);
        initCompleted.set(true);
    }

    public static AndroidPostProcessing initialization(@NonNull final Application app) {
        if (null == instance) {
            synchronized (AndroidPostProcessing.class) {
                if (null == instance)
                    instance = new AndroidPostProcessing(app);
            }
        }
        return instance;
    }


    private void initAppDelegateMap(@NonNull final Application app) {

        try {
            List<String> classFileNames = ClassUtils.getFileNameByPackageName(app, "com.woaiqw.generate");
            for (String className : classFileNames) {
                String s = Class.forName(className).newInstance().toString();
                Log.d("111", s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO: processor annotation
        AppDelegate agent = new AppDelegate();
//        agent.setAgent();
//        agent.setName();
//        agent.setPriority();
//        agent.setAsync();
//        agent.setDelayTime();
        map.put("key", agent);
    }

    public void dispatcher() {
        if (app == null)
            throw new RuntimeException(" AndroidPostProcessing must init ");

        for (Map.Entry<String, AppDelegate> entry : map.entrySet()) {
            final AppDelegate value = entry.getValue();
            if (value.isAsync()) {
                taskPool.schedule(new Runnable() {
                    @Override
                    public void run() {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        value.getAgent().dispatcher(app);
                    }
                }, value.getDelayTime(), TimeUnit.MILLISECONDS);
            } else {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        value.getAgent().dispatcher(app);
                    }
                });
            }
        }
    }

    public static void release() {
        if (!initCompleted.get())
            throw new RuntimeException(" must init completed before the fun to release ");
        map.clear();
        taskPool.shutdown();
        h.removeCallbacksAndMessages(null);
    }


}
