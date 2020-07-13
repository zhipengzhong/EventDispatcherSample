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
                    Log.d(TAG, "run: send event 5");
                    EventDispatcher.instance().post(1, MainActivity.class);
                } catch (InterruptedException e) {
                }
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        EventDispatcher.instance().unregister(this);
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
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

    public void gotoTest(View view) {
        startActivity(new Intent(this, TestActivity.class));
        finish();
    }
}
