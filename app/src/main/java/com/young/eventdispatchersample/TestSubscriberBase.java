package com.young.eventdispatchersample;

import android.util.Log;

import young.eventdispatcher.EventDispatcher;
import young.eventdispatcher.annotation.Subscribe;

public class TestSubscriberBase {

    public TestSubscriberBase() {
        EventDispatcher.instance().register(this);
        Log.d("TAG", "this:" + this.toString() + " register");
    }

    @Subscribe
    public void test(String s) {
        Log.d("TAG", "this:" + this.toString() + " test: " + s);
    }

    public void unregister() {
        EventDispatcher.instance().unregister(this);
        Log.d("TAG", "this:" + this.toString() + " unregister");
    }
}
