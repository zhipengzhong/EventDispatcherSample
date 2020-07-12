package young.eventdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import young.eventdispatcher.util.RunUtil;

public abstract class DispatcherHandle {

    private Map<Class, SubscriberHelper> mSubscriberHelperMap = new HashMap<>();

    protected synchronized void registerSubscribe(Class clazz, int methodId, ThreadMode mode, Class flag, Class... arg) {
        SubscriberHelper helper = mSubscriberHelperMap.get(clazz);
        if (helper == null) {
            helper = new SubscriberHelper(clazz);
            mSubscriberHelperMap.put(clazz, helper);
        }
        helper.putSubscribe(methodId, mode, flag, arg);
    }

    public void post(Object subscriber, Object event, Class flag) {
        SubscriberHelper helper = mSubscriberHelperMap.get(subscriber.getClass());
        List<SubscriberHelper.SubscribeHelper> subscribeList = helper.getSubscribe(event, flag);
        for (SubscriberHelper.SubscribeHelper subscribeHelper : subscribeList) {
            dispatch(subscribeHelper.mMethodId, subscribeHelper.mMode, subscriber, event);
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

        private void putSubscribe(int methodId, ThreadMode mode, Class flag, Class[] arg) {
            mSubscribeHelpers.add(new SubscribeHelper(methodId, mode, flag, arg));
        }

        public List<SubscribeHelper> getSubscribe(Object event, Class flag) {
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
            private final Class[] mArg;

            public SubscribeHelper(int methodId, ThreadMode mode, Class flag, Class[] arg) {
                mMethodId = methodId;
                mMode = mode;
                mFlag = flag;
                mArg = arg;
            }
        }
    }

}
