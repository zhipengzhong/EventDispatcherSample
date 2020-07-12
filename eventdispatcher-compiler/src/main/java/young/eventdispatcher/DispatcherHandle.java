package young.eventdispatcher;

public abstract class DispatcherHandle {

    protected synchronized void registSubscribe(Class clazz, int methodId, ThreadMode mode, Class flag, Class... arg) {
        throw new RuntimeException("stub!");
    }

    public void post(Object subscriber, Object event, Class flag) {
        throw new RuntimeException("stub!");
    }

    protected void dispatch(final int methodId, ThreadMode mode, final Object subscriber, final Object event) {
        throw new RuntimeException("stub!");
    }

    protected abstract void dispatch(int methodId, Object subscriber, Object event);

}
