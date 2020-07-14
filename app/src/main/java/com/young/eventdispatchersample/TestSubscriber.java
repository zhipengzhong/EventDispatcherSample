package com.young.eventdispatchersample;

import android.util.Log;

import young.eventdispatcher.EventDispatcher;
import young.eventdispatcher.annotation.Subscribe;

public class TestSubscriber {

    private byte[] b = new byte[1024 * 1024 * 1];
    volatile boolean stop = false;

    public TestSubscriber() {
        EventDispatcher.instance().register(this);
//        Log.d("TAG", "this:" + this.toString() + " register");
    }

    @Subscribe()
    public void test(String s) {
        if (stop) {
            Log.d("TAG", "this:" + this.toString() + " test: " + s);
        }
    }

    public void unregister() {
        EventDispatcher.instance().unregister(this);
        stop = true;
//        Log.d("TAG", "this:" + this.toString() + " unregister");
    }
}
