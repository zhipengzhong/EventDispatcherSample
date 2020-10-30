package young.eventdispatcher;

import java.util.List;
import java.util.Map;

/**
 * Create a Stub class to fool the compiler to avoid the performance penalty of using reflection to invoke code
 */
public class GeneratedDispatcherHandleImpl {

    protected GeneratedDispatcherHandleImpl(WeakReferenceQueue unsubscriber) {
        throw new RuntimeException("stub!");
    }

    protected void post(Map<Class, List<Object>> subscribers, Object event, Class flag) {
        throw new RuntimeException("stub!");
    }

    protected void postCache(Object subscriber) {
        throw new RuntimeException("stub!");
    }

    protected List<Class> subscriberTypes() {
        throw new RuntimeException("stub!");
    }
}
