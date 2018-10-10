package com.woaiqw.postprocessing.model;

import android.support.annotation.NonNull;

import com.woaiqw.postprocessing.IApp;

/**
 * Created by haoran on 2018/10/10.
 */
public class AppDelegate implements Comparable<AppDelegate> {

    IApp agent;
    String name;
    int priority;
    boolean isAsync;
    long delay;

    public IApp getAgent() {
        return agent;
    }

    public void setAgent(IApp agent) {
        this.agent = agent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }


    public long getDelayTime() {
        return delay;
    }

    public void setDelayTime(long delay) {
        this.delay = delay;
    }

    @Override
    public String toString() {
        return "AppDelegate{" +
                "agent=" + agent +
                ", name='" + name + '\'' +
                ", priority=" + priority +
                ", isAsync=" + isAsync +
                ", delay=" + delay +
                '}';
    }


    @Override
    public int compareTo(@NonNull AppDelegate o) {
        return this.getPriority()-o.getPriority();
    }
}
