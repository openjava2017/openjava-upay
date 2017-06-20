package org.openjava.upay.proxy.util;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CallableComponent
{
    String id() default "";

    String[] methods() default {};
}
