package young.eventdispatcher.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.core.util.Pair;

public class RunUtil {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private static final int MSG_RUN_ON_UITHREAD = 0x01;
    private static Handler sHandler;

    public static void runOnAsyncThread(final Runnable runnable) {
        runOnAsyncThread(runnable, false);
    }

    public static void runOnAsyncThread(final Runnable runnable, boolean waitUtilDown) {
        CountDownLatch countDownLatch = null;
        if (waitUtilDown) {
            countDownLatch = new CountDownLatch(1);
        }
        final CountDownLatch finalCountDownLatch = countDownLatch;
        EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                runnable.run();
                if (finalCountDownLatch != null) {
                    finalCountDownLatch.countDown();
                }
            }
        });
        if (waitUtilDown) {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void runOnBackgroundThread(final Runnable runnable) {
        runOnBackgroundThread(runnable, false);
    }

    public static void runOnBackgroundThread(final Runnable runnable, boolean waitUtilDown) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            runnable.run();
            return;
        }
        runOnAsyncThread(runnable, waitUtilDown);
    }

    public static void runOnUiThread(Runnable runnable) {
        runOnUiThread(runnable, false);
    }

    public static void runOnUiThread(Runnable runnable, boolean waitUtilDown) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
            return;
        }
        CountDownLatch countDownLatch = null;
        if (waitUtilDown) {
            countDownLatch = new CountDownLatch(1);
        }
        Pair<Runnable, CountDownLatch> pair = new Pair<>(runnable, countDownLatch);
        Message msg = getHandler().obtainMessage(MSG_RUN_ON_UITHREAD, pair);
        getHandler().sendMessageAtFrontOfQueue(msg);
        if (waitUtilDown) {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Handler getHandler() {
        synchronized (RunUtil.class) {
            if (sHandler == null) {
                sHandler = new InternalHandler();
            }
            return sHandler;
        }
    }

    private static class InternalHandler extends Handler {
        public InternalHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_RUN_ON_UITHREAD) {
                Pair<Runnable, CountDownLatch> pair = (Pair<Runnable, CountDownLatch>) msg.obj;
                try {
                    Runnable runnable = pair.first;
                    runnable.run();
                } finally {
                    if (pair.second != null) {
                        pair.second.countDown();
                    }
                }
            }
        }
    }
}
