package young.eventdispatcher;

import java.util.Iterator;

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
        synchronized (this) {
            Iterator<?> iterator = mSubscriber.iterator();
            while (iterator.hasNext()) {
                Object subscriber = iterator.next();
                mDispatcherHandle.post(subscriber, event, flag);
            }
        }
    }
}
