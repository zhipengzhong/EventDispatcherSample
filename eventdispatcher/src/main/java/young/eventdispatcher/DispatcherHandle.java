package young.eventdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import young.eventdispatcher.util.PosterUtil;

@SuppressWarnings({"deprecation", "unchecked"})
public abstract class DispatcherHandle {

    private Map<Class, SubscriberHelper> mSubscriberHelperMap = new HashMap<>();
    private List<Class> mSubscriberRegisterTypes = new ArrayList<>();
    private WeakReferenceQueue mUnsubscriber;

    protected DispatcherHandle(WeakReferenceQueue unsubscriber) {
        mUnsubscriber = unsubscriber;
    }

    protected synchronized void registerSubscribe(Class clazz, int methodId, ThreadMode mode, Class flag, boolean cache, int priority, Class... arg) {
        SubscriberHelper helper = mSubscriberHelperMap.get(clazz);
        if (helper == null) {
            helper = new SubscriberHelper(clazz);
            mSubscriberHelperMap.put(clazz, helper);
            mSubscriberRegisterTypes.add(clazz);
        }
        helper.putSubscribe(methodId, mode, flag, cache, priority, arg);
    }

    protected void post(Map<Class, List<Object>> subscribers, Object event, Class flag) {
        Set<Map.Entry<Class, SubscriberHelper>> entries = mSubscriberHelperMap.entrySet();
        PendingPostQueue pendingPostQueue = null;
        for (Map.Entry<Class, SubscriberHelper> entry : entries) {
            Class clazz = entry.getKey();
            SubscriberHelper helper = entry.getValue();
            List<SubscriberHelper.SubscribeHelper> subscribeList = helper.getSupportSubscribe(event, flag);
            for (SubscriberHelper.SubscribeHelper subscribeHelper : subscribeList) {
                if (subscribeHelper.mCache) {
                    subscribeHelper.mCacheEvent = event;
                }
                List<Object> objects = subscribers.get(clazz);
                if (objects == null) {
                    continue;
                }
                if (subscribeHelper.mPriority <= -1) {
                    for (Object object : objects) {
                        dispatch(subscribeHelper.mMethodId, subscribeHelper.mMode, object, event, false);
                    }
                } else {
                    if (pendingPostQueue == null) {
                        pendingPostQueue = new PendingPostQueue();
                    }
                    pendingPostQueue.add(new PendingPostQueue.PendingPost(subscribeHelper.mPriority,
                            subscribeHelper.mMethodId, subscribeHelper.mMode, objects));
                }
            }
        }
        if (pendingPostQueue != null) {
            Iterator<? extends PendingPostQueue.PendingPost> iterator = pendingPostQueue.iterator();
            while (iterator.hasNext()) {
                PendingPostQueue.PendingPost next = iterator.next();
                for (Object object : next.mObjects) {
                    if (dispatch(next.mMethodId, next.mMode, object, event, true)) {
                        return;
                    }
                }
            }
        }
    }

    protected void postCache(Object subscriber) {
        for (Class clazz : subscriberTypes()) {
            if (!clazz.isAssignableFrom(subscriber.getClass())) {
                continue;
            }
            SubscriberHelper helper = mSubscriberHelperMap.get(clazz);
            if (helper == null) return;
            List<SubscriberHelper.SubscribeHelper> subscribe = helper.getSubscribe();
            Iterator<SubscriberHelper.SubscribeHelper> iterator = subscribe.iterator();
            while (iterator.hasNext()) {
                SubscriberHelper.SubscribeHelper subscribeHelper = iterator.next();
                if (subscribeHelper.mCache && subscribeHelper.mCacheEvent != null) {
                    dispatch(subscribeHelper.mMethodId, subscribeHelper.mMode, subscriber, subscribeHelper.mCacheEvent, false);
                }
            }
        }
    }

    protected List<Class> subscriberTypes() {
        return mSubscriberRegisterTypes;
    }

    private boolean dispatch(final int methodId, ThreadMode mode, final Object subscriber, final Object event, boolean waitResult) {
        final Object[] result = {null};
        final CountDownLatch[] countDownLatch = {null};
        if (waitResult) {
            countDownLatch[0] = new CountDownLatch(1);
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (mUnsubscriber) {
                        if (mUnsubscriber.contains(subscriber)) return;
                    }
                    result[0] = dispatch(methodId, subscriber, event);
                } finally {
                    if (countDownLatch[0] != null) {
                        countDownLatch[0].countDown();
                    }
                }
            }
        };
        if (mode == ThreadMode.MAIN) {
            PosterUtil.runOnUiThread(runnable);
        } else if (mode == ThreadMode.POSTING) {
            runnable.run();
        } else if (mode == ThreadMode.BACKGROUND) {
            PosterUtil.runOnBackgroundThread(runnable);
        } else if (mode == ThreadMode.ASYNC) {
            PosterUtil.runOnAsyncThread(runnable);
        }
        if (!waitResult) {
            return false;
        }
        try {
            countDownLatch[0].await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        Object o = result[0];
        if (o == null) {
            return false;
        }

        if (Boolean.class.isAssignableFrom(o.getClass())) {
            return ((Boolean) o).booleanValue();
        }

        return false;
    }

    protected abstract Object dispatch(int methodId, Object subscriber, Object event);


    protected <T> T getSubscribe(Object subscriber, Class<T> t) {
        if (t == null) {
            return null;
        }
        if (!t.isAssignableFrom(subscriber.getClass())) {
            return null;
        }
        return (T) subscriber;
    }

    protected <T> T getEvent(Object event, Class<T> t) {
        if (t == null) {
            return null;
        }
        if (!t.isAssignableFrom(event.getClass())) {
            return null;
        }
        return (T) event;
    }

    private static class SubscriberHelper {

        private List<SubscribeHelper> mSubscribeHelpers = new ArrayList<>();
        private Class mTypeClzz;

        private SubscriberHelper(Class clazz) {
            mTypeClzz = clazz;
        }

        private void putSubscribe(int methodId, ThreadMode mode, Class flag, boolean cache, int priority, Class[] arg) {
            mSubscribeHelpers.add(new SubscribeHelper(methodId, mode, flag, cache, priority, arg));
        }

        private List<SubscribeHelper> getSubscribe() {
            return mSubscribeHelpers;
        }

        private List<SubscribeHelper> getSupportSubscribe(Object event, Class flag) {
            List<SubscribeHelper> result = new ArrayList<>();
            Iterator<SubscribeHelper> iterator = mSubscribeHelpers.iterator();
            boolean isInclude;
            while (iterator.hasNext()) {
                SubscribeHelper next = iterator.next();
                if (next.mFlag != flag) {
                    continue;
                }
                isInclude = false;
                for (Class clazz : next.mArg) {
                    if (clazz.isAssignableFrom(event.getClass())) {
                        isInclude = true;
                    }
                }
                if (isInclude) {
                    result.add(next);
                }
            }
            return result;
        }

        private static class SubscribeHelper {

            private final int mMethodId;
            private final ThreadMode mMode;
            private final Class mFlag;
            private boolean mCache;
            private int mPriority;
            private final Class[] mArg;
            public Object mCacheEvent;

            public SubscribeHelper(int methodId, ThreadMode mode, Class flag, boolean cache, int priority, Class[] arg) {
                mMethodId = methodId;
                mMode = mode;
                mFlag = flag;
                mCache = cache;
                mPriority = priority;
                mArg = arg;
            }
        }
    }

}
