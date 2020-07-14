package young.eventdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import young.eventdispatcher.util.RunUtil;

public abstract class DispatcherHandle {

    private Map<Class, SubscriberHelper> mSubscriberHelperMap = new HashMap<>();

    protected synchronized void registerSubscribe(Class clazz, int methodId, ThreadMode mode, Class flag, boolean cache, Class... arg) {
        SubscriberHelper helper = mSubscriberHelperMap.get(clazz);
        if (helper == null) {
            helper = new SubscriberHelper(clazz);
            mSubscriberHelperMap.put(clazz, helper);
        }
        helper.putSubscribe(methodId, mode, flag, cache, arg);
    }

    public void post(Map<Class, List<Object>> subscribers, Object event, Class flag) {
        Set<Map.Entry<Class, SubscriberHelper>> entries = mSubscriberHelperMap.entrySet();
        for (Map.Entry<Class, SubscriberHelper> entry : entries) {
            Class clazz = entry.getKey();
            SubscriberHelper helper = entry.getValue();
            List<SubscriberHelper.SubscribeHelper> subscribeList = helper.getSupportSubscribe(event, flag);
            for (SubscriberHelper.SubscribeHelper subscribeHelper : subscribeList) {
                if (subscribeHelper.mCache) {
                    subscribeHelper.mCacheEvent = event;
                }
                List<Object> objects = subscribers.get(clazz);
                if (objects != null) {
                    for (Object object : objects) {
                        dispatch(subscribeHelper.mMethodId, subscribeHelper.mMode, object, event);
                    }
                }
            }
        }
    }

    public void postCache(Object subscriber) {
        SubscriberHelper helper = mSubscriberHelperMap.get(subscriber.getClass());
        List<SubscriberHelper.SubscribeHelper> subscribe = helper.getSubscribe();
        Iterator<SubscriberHelper.SubscribeHelper> iterator = subscribe.iterator();
        while (iterator.hasNext()) {
            SubscriberHelper.SubscribeHelper subscribeHelper = iterator.next();
            if (subscribeHelper.mCache && subscribeHelper.mCacheEvent != null) {
                dispatch(subscribeHelper.mMethodId, subscribeHelper.mMode, subscriber, subscribeHelper.mCacheEvent);
            }
        }
    }

    protected void dispatch(final int methodId, ThreadMode mode, final Object subscriber, final Object event) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    dispatch(methodId, subscriber, event);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        if (mode == ThreadMode.MAIN) {
            RunUtil.runOnUiThread(runnable);
        } else if (mode == ThreadMode.POSTING) {
            runnable.run();
        } else if (mode == ThreadMode.BACKGROUND) {
            RunUtil.runOnBackgroundThread(runnable);
        } else if (mode == ThreadMode.ASYNC) {
            RunUtil.runOnAsyncThread(runnable);
        }
    }

    protected abstract void dispatch(int methodId, Object subscriber, Object event);


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

        private void putSubscribe(int methodId, ThreadMode mode, Class flag, boolean cache, Class[] arg) {
            mSubscribeHelpers.add(new SubscribeHelper(methodId, mode, flag, cache, arg));
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
            private final Class[] mArg;
            public Object mCacheEvent;

            public SubscribeHelper(int methodId, ThreadMode mode, Class flag, boolean cache, Class[] arg) {
                mMethodId = methodId;
                mMode = mode;
                mFlag = flag;
                mCache = cache;
                mArg = arg;
            }
        }
    }

}
