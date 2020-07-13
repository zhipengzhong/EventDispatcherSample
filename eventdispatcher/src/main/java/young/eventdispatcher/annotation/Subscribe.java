package young.eventdispatcher.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import young.eventdispatcher.EventDispatcher;
import young.eventdispatcher.ThreadMode;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
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
}
