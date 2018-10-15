package com.woaiqw.postprocessing;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;

import com.woaiqw.postprocessing.model.AppDelegate;
import com.woaiqw.postprocessing.utils.ClassUtils;
import com.woaiqw.postprocessing.utils.PackageUtils;
import com.woaiqw.postprocessing.utils.WeakHandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by haoran on 2018/10/10.
 */
public class AndroidPostProcessing {

    private static final String parsePackageName = "com.woaiqw.generate";

    private static final String TAG = "AndroidPostProcessing";


    private volatile static Application app;

    private volatile static AndroidPostProcessing instance = null;

    private volatile static List<AppDelegate> agents = new ArrayList<>();

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
        taskPool = Executors.newScheduledThreadPool(CORE_POOL_SIZE);
    }


    private AndroidPostProcessing(@NonNull final Application app) {
        long start = System.currentTimeMillis();
        initAppDelegateMap(app);
        Log.e(TAG, "init map time   " + String.valueOf(System.currentTimeMillis() - start) + "ms");
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


    private void initAppDelegateMap(@NonNull final Application application) {

        SharedPreferences sp = application.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        String code = sp.getString("versionCode", "0");
        String name = sp.getString("versionName", "0.0.0");

        String versionCode = PackageUtils.getVersionCode(application);
        String versionName = PackageUtils.getVersionName(application);
        edit.putString("versionCode", versionCode).apply();
        edit.putString("versionName", versionName).apply();

        boolean versionChanged = !code.equals(versionCode) || !name.equals(versionName);

        app = application;

        try {
            Set<String> set;
            if (versionChanged) {
                set = ClassUtils.getFileNameByPackageName(application, parsePackageName);
                edit.putStringSet(TAG, set).apply();
            } else {
                set = sp.getStringSet(TAG, new HashSet<String>());
            }

            parseSet2List(set);

            if (agents != null && agents.size() > 0) {
                Collections.sort(agents);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parseSet2List(Set<String> set) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        for (String classPath : set) {
            Class clazz = Class.forName(classPath);
            Field[] fields = clazz.getFields();

            if (fields != null && fields.length != 0) {
                IApp app = null;
                String name = "Main";
                boolean type = false;
                int priority = 0;
                boolean async = false;
                long delay = 0;
                for (Field field : fields) {
                    String fieldName = field.getName();
                    Object o = field.get(fieldName);
                    switch (fieldName) {
                        case "path":
                            app = (IApp) Class.forName((String) o).newInstance();
                            break;
                        case "name":
                            name = (String) o;
                            break;
                        case "debug":
                            type = (boolean) o;
                            break;
                        case "priority":
                            priority = (int) o;
                            break;
                        case "async":
                            async = (boolean) o;
                            break;
                        case "delay":
                            delay = (long) o;
                            break;
                    }
                }

                AppDelegate agent = new AppDelegate();
                agent.setAgent(app);
                agent.setName(name);
                agent.setType(type);
                agent.setPriority(priority);
                agent.setAsync(async);
                agent.setDelayTime(delay);
                agents.add(agent);
            }
        }
    }

    public void dispatcher() {

        if (app == null)
            throw new RuntimeException(" AndroidPostProcessing must init ");

        if (agents != null && agents.size() > 0) {
            for (final AppDelegate agent : agents) {
                if (agent.getType()) {
                    continue;
                }
                if (agent.isAsync()) {
                    taskPool.schedule(new Runnable() {
                        @Override
                        public void run() {
                            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                            agent.getAgent().dispatcher(app);
                        }
                    }, agent.getDelayTime(), TimeUnit.MILLISECONDS);
                } else {
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            agent.getAgent().dispatcher(app);
                        }
                    }, agent.getDelayTime());
                }
            }
        }

    }

    public static void release() {

        if (!initCompleted.get())
            throw new RuntimeException(" must init completed before the fun to release ");
        agents.clear();
        taskPool.shutdown();
        h.removeCallbacksAndMessages(null);

    }


}
