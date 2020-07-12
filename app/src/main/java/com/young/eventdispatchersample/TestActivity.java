package com.young.eventdispatchersample;

import androidx.appcompat.app.AppCompatActivity;
import young.eventdispatcher.EventDispatcher;
import young.eventdispatcher.ThreadMode;
import young.eventdispatcher.annotation.Subscribe;

import android.os.Bundle;
import android.util.Log;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        EventDispatcher.instance().register(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    Log.d(TAG, "run: send event 1");
                    EventDispatcher.instance().post("event 1");

                    Thread.sleep(3000);
                    Log.d(TAG, "run: send event 2");
                    EventDispatcher.instance().post("event 2", MainActivity.class);

                    Thread.sleep(3000);
                    Log.d(TAG, "run: send event 3");
                    EventDispatcher.instance().post("event 3", MainActivity.class);

                    Thread.sleep(3000);
                    Log.d(TAG, "run: send event 4");
                    EventDispatcher.instance().post(1, MainActivity.class);

                    Thread.sleep(30000);
                    Log.d(TAG, "run: send event 4");
                    EventDispatcher.instance().post(1, MainActivity.class);
                } catch (InterruptedException e) {
                }
            }
        }).start();
    }



    @Subscribe()
    public void test1(String test) {
        Log.d(TAG, "test1 Thread:" + Thread.currentThread().getName() + "  event:" + test);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void test2(String test) {
        Log.d(TAG, "test2 Thread:" + Thread.currentThread().getName() + "  event:" + test);
    }

    @Subscribe(flag = MainActivity.class, threadMode = ThreadMode.MAIN)
    public void test3(String test, Integer i) {
        Log.d(TAG, "test3 Thread:" + Thread.currentThread().getName() + "  event:" + test + "  " + i);
    }
}
