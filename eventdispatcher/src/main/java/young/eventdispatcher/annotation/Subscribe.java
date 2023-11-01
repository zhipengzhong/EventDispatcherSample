package young.eventdispatcher.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import young.eventdispatcher.EventDispatcher;
import young.eventdispatcher.ThreadMode;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Subscribe {

    /**
     * Controls which thread the subscriber runs on.
     *
     * @return
     */
    ThreadMode threadMode() default ThreadMode.POSTING;

    /**
     * @return
     * @see EventDispatcher#post(Object, Class)
     */
    Class flag() default Object.class;

    /**
     * If true, events are cached and sent to subscribers as soon as they register.
     *
     * @return
     */
    boolean cache() default false;

    /**
     * The priority value must >= -1.
     * If == -1, The events that the subscriber receives will be unordered;
     * If >= 0, A smaller priority value will receive events faster, even if the subscriber is in a different thread.
     * An ordered subscriber can intercept the event by returning true and terminate the event received by a lower priority subscriber
     *
     * @return
     */
    int priority() default -1;
}
