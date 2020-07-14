package com.young.eventdispatchersample;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import young.eventdispatcher.EventDispatcher;
import young.eventdispatcher.ThreadMode;
import young.eventdispatcher.annotation.Subscribe;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private long mTimeMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventDispatcher.instance().register(this);


        new Thread(new Runnable() {
            @Override
            public void run() {
                TestSubscriber[] subscribers = new TestSubscriber[5];
                for (int i = 0; i < 100000; i++) {
                    for (int j = 0; j < 5; j++) {
                        subscribers[j] = new TestSubscriber();
                    }
                    for (int j = 0; j < 5; j++) {
                        subscribers[j].unregister();
                    }
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100000; i++) {
                    EventDispatcher.instance().post("event");
                }
            }
        }).start();



        Log.d(TAG, "run: send event 2");
        mTimeMillis = System.currentTimeMillis();
        EventDispatcher.instance().post("event 2");
    }

    @Override
    protected void onDestroy() {
        EventDispatcher.instance().unregister(this);
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

//    @Subscribe(priority = 1)
//    public void testThreadMode1(String test) {
//        Log.d(TAG, "testThreadMode1 Thread:" + Thread.currentThread().getName() + "  event:" + test);
//    }
//
//    @Subscribe(threadMode = ThreadMode.BACKGROUND, priority = 2)
//    public void testThreadMode2(String test) {
//        Log.d(TAG, "testThreadMode2 Thread:" + Thread.currentThread().getName() + "  event:" + test);
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN, priority = 3)
//    public void testThreadMode3(String test) {
//        Log.d(TAG, "testThreadMode3 Thread:" + Thread.currentThread().getName() + "  event:" + test);
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN, priority = 2)
//    public void testThreadMode5(String test) {
//        Log.d(TAG, "testThreadMode5 Thread:" + Thread.currentThread().getName() + "  event:" + test);
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
//    public boolean testThreadMode6(String test) {
//        Log.d(TAG, "testThreadMode6 Thread:" + Thread.currentThread().getName() + "  event:" + test);
//        return true;
//    }
//
//    @Subscribe(threadMode = ThreadMode.BACKGROUND, priority = 0)
//    public Boolean testThreadMode7(String test) {
//        Log.d(TAG, "testThreadMode7: " + (System.currentTimeMillis() - mTimeMillis));
//        Log.d(TAG, "testThreadMode7 Thread:" + Thread.currentThread().getName() + "  event:" + test);
//        return false;
//    }
//
//    @Subscribe(threadMode = ThreadMode.BACKGROUND)
//    public void testThreadMode4(String test) {
//        Log.d(TAG, "testThreadMode4: " + (System.currentTimeMillis() - mTimeMillis));
//        Log.d(TAG, "testThreadMode4 Thread:" + Thread.currentThread().getName() + "  event:" + test);
//    }
}
