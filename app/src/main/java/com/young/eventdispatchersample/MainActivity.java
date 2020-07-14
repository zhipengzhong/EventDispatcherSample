package com.young.eventdispatchersample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import young.eventdispatcher.EventDispatcher;
import young.eventdispatcher.ThreadMode;
import young.eventdispatcher.annotation.Subscribe;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventDispatcher.instance().register(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    Log.d(TAG, "run: send event 1");
                    EventDispatcher.instance().post("event 1");
                } catch (InterruptedException e) {
                }
            }
        }).start();


        Log.d(TAG, "run: send event 2");
        EventDispatcher.instance().post("event 2");

    }

    @Override
    protected void onDestroy() {
        EventDispatcher.instance().unregister(this);
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Subscribe()
    public void testThreadMode1(String test) {
        Log.d(TAG, "testThreadMode1 Thread:" + Thread.currentThread().getName() + "  event:" + test);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void testThreadMode2(String test) {
        Log.d(TAG, "testThreadMode2 Thread:" + Thread.currentThread().getName() + "  event:" + test);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void testThreadMode3(String test) {
        Log.d(TAG, "testThreadMode3 Thread:" + Thread.currentThread().getName() + "  event:" + test);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void testThreadMode4(String test) {
        Log.d(TAG, "testThreadMode4 Thread:" + Thread.currentThread().getName() + "  event:" + test);
    }
}
