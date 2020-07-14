package young.eventdispatcher;

import java.util.List;
import java.util.Map;

public abstract class DispatcherHandle {

    protected synchronized void registSubscribe(Class clazz, int methodId, ThreadMode mode, Class flag, Class... arg) {
        throw new RuntimeException("stub!");
    }

    public void post(Map<Class, List<Object>> subscribers, Object event, Class flag) {
        throw new RuntimeException("stub!");
    }

    protected void dispatch(final int methodId, ThreadMode mode, final Object subscriber, final Object event) {
        throw new RuntimeException("stub!");
    }

    protected abstract Object dispatch(int methodId, Object subscriber, Object event);

}
