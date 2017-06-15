package org.openjava.upay.trade.support;

import org.openjava.upay.Constants;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CallableComponent
{
    String id();

    String[] methods() default {Constants.DEFAULT_ENDPOINT_ID};
}
