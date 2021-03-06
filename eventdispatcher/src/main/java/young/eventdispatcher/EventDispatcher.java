package young.eventdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import young.eventdispatcher.annotation.Subscribe;

public class EventDispatcher {

    static EventDispatcher sEventDispatcher;

    private WeakReferenceQueue<Object> mSubscriber = new WeakReferenceQueue<>();
    private WeakReferenceQueue<Object> mUnsubscriber = new WeakReferenceQueue<>();
    private GeneratedDispatcherHandleImpl mDispatcherHandle;

    private EventDispatcher() {
        try {
            mDispatcherHandle = new GeneratedDispatcherHandleImpl(mUnsubscriber);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the singleton object for {@link EventDispatcher}
     *
     * @return
     */
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

    /**
     * Register the subscriber to accept subscription events. Subscribers can call {@link #unregister(Object)}
     * that no longer receive events.
     * Subscribers need to receive the event content via a method annotated by {@link Subscribe}
     *
     * @param subscriber
     */
    public void register(Object subscriber) {
        synchronized (this) {
            mSubscriber.add(subscriber);
        }
        if (mDispatcherHandle != null) {
            mDispatcherHandle.postCache(subscriber);
        }
    }

    /**
     * Unregister the subscriber ends the receipt of the event.
     *
     * @param subscriber
     */
    public void unregister(Object subscriber) {
        synchronized (this) {
            mSubscriber.remove(subscriber);
        }
        synchronized (mUnsubscriber) {
            mUnsubscriber.add(subscriber);
        }
    }

    /**
     * Posts an event to subscribers, {@link Subscribe#flag()} value is default (Object.class).
     *
     * @param event
     */
    public void post(Object event) {
        post(event, Object.class);
    }

    /**
     * Posts an event to subscribers, {@link Subscribe#flag()} value is {@paramref flag}.
     *
     * @param event
     * @param flag
     */
    public void post(Object event, Class flag) {
        if (mDispatcherHandle == null) {
            return;
        }
        Map<Class, List<Object>> subscribers = new HashMap<>();
        synchronized (this) {
            Iterator<?> iterator = mSubscriber.iterator();
            while (iterator.hasNext()) {
                Object subscriber = iterator.next();
                for (Class clazz : mDispatcherHandle.subscriberTypes()) {
                    if (!clazz.isAssignableFrom(subscriber.getClass())) {
                        continue;
                    }
                    List<Object> objects = subscribers.get(clazz);
                    if (objects == null) {
                        objects = new ArrayList<>();
                        subscribers.put(clazz, objects);
                    }
                    objects.add(subscriber);
                }
            }
        }
        mDispatcherHandle.post(subscribers, event, flag);
    }
}
