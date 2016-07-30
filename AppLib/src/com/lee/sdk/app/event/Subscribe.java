/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.app.event;

//CHECKSTYLE:OFF

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Subscribe annotation
 * 
 * @author lihong06
 * @since 2014-11-20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {

}
