package young.eventdispatcher.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import young.eventdispatcher.ThreadMode;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Subscribe {

    ThreadMode threadMode() default ThreadMode.POSTING;

    Class flag() default Object.class;

    boolean cache() default false;

    int priority() default -1;
}
