package young.eventdispatcher.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PosterUtil {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private static Handler sHandler;

    public static void runOnAsyncThread(final Runnable runnable) {
        EXECUTOR_SERVICE.execute(runnable);
    }

    public static void runOnBackgroundThread(final Runnable runnable) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            runnable.run();
            return;
        }
        runOnAsyncThread(runnable);
    }

    public static void runOnUiThread(Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
            return;
        }
        getHandler().post(runnable);
    }

    private static Handler getHandler() {
        synchronized (PosterUtil.class) {
            if (sHandler == null) {
                sHandler = new Handler(Looper.getMainLooper());
            }
            return sHandler;
        }
    }
}
