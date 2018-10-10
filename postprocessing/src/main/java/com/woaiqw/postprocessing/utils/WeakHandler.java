package com.woaiqw.postprocessing.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by haoran on 2018/10/10.
 */
public class WeakHandler extends Handler {

    private WeakReference<Callback> mWeakReference;

    public WeakHandler(Callback callback) {
        mWeakReference = new WeakReference<>(callback);
    }

    public WeakHandler(Callback callback, Looper looper) {
        super(looper);
        mWeakReference = new WeakReference<>(callback);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mWeakReference != null && mWeakReference.get() != null) {
            Callback callback = mWeakReference.get();
            callback.handleMessage(msg);
        }
    }

}

