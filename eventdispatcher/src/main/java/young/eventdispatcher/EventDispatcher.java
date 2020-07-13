package young.eventdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventDispatcher {

    static EventDispatcher sEventDispatcher;

    private WeakReferenceQueue<Object> mSubscriber = new WeakReferenceQueue<>();
    private GeneratedDispatcherHandleImpl mDispatcherHandle;

    private EventDispatcher() {
        try {
            mDispatcherHandle = new GeneratedDispatcherHandleImpl();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static EventDispatcher instance() {
        if (sEventDispatcher == null) {
            synchronized (EventDispatcher.class) {
                if (sEventDispatcher == null) {
                    sEventDispatcher = new EventDispatcher();
                }
            }
        }
        return sEventDispatcher;
    }

    public void register(Object subscriber) {
        synchronized (this) {
            mSubscriber.add(subscriber);
        }
        mDispatcherHandle.postCache(subscriber);
    }

    public void unregister(Object subscriber) {
        synchronized (this) {
            mSubscriber.remove(subscriber);
        }
    }

    public void post(Object event) {
        post(event, Object.class);
    }

    public void post(Object event, Class flag) {
        if (mDispatcherHandle == null) {
            return;
        }
        Map<Class, List<Object>> subscribers = new HashMap<>();
        synchronized (this) {
            Iterator<?> iterator = mSubscriber.iterator();
            while (iterator.hasNext()) {
                Object subscriber = iterator.next();
                List<Object> objects = subscribers.get(subscriber.getClass());
                if (objects == null) {
                    objects = new ArrayList<>();
                    subscribers.put(subscribers.getClass(), objects);
                }
                objects.add(subscriber);
            }
        }
        mDispatcherHandle.post(subscribers, event, flag);
    }
}
