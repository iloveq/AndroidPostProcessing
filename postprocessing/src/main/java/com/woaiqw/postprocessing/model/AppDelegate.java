package com.woaiqw.postprocessing.model;

import com.woaiqw.postprocessing.IApp;

/**
 * Created by haoran on 2018/10/10.
 */
public class AppDelegate {

    IApp agent;
    String name;
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
}
