/*
 * Copyright (C) 2014 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.app.event;

import com.lee.sdk.Configuration;

import android.util.Log;

/**
 * 设计该类的目的是为了屏蔽引入的开源库代码，对上层作一个隔离。
 * 
 * <p>
 * <b>重要说明：</b>
 * <li>订阅实现的类不能被混淆。
 * </p>
 * <br>
 * 
 * <p>
 * 要监听的类中的方法，必须以 <b>onEvent</b> 命名为前缀，并且必须带有{@link Subscribe}注解。
 * 
 * <pre class="prettyprint">
 *  // 1. Define event
 *  public class MessageEvent {
 *      public void onEvent(Boolean event) {
 *          
 *      }
    }
 * 
 *  // 2. Reigster
 *  EventBusWrapper.register(new MessageEvent());
 * 
 *  // 3. Post
 *  EventBusWrapper.post(true);
 *  
 *  // 4. Unregister
 *  EventBusWrapper.unregister(object);
 * </pre>
 * </p>
 * 
 * @author lihong06
 * @since 2014-11-20
 */
public final class EventBusWrapper {
    /** DEBUG flag */
    public static final boolean DEBUG = Configuration.DEBUG;
    /** tag */
    private static final String TAG = "EventBusWrapper";
    /**
     * Constructor method
     */
    private EventBusWrapper() {
        
    }
    
    /**
     * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Object)} once they
     * are no longer interested in receiving events.
     * <p/>
     * Subscribers have event handling methods that are identified by their name, typically called "onEvent". Event
     * handling methods must have exactly one parameter, the event. If the event handling method is to be called in a
     * specific thread, a modifier is appended to the method name. Valid modifiers match one of the {@link ThreadMode}
     * enums. For example, if a method is to be called in the UI/main thread by EventBus, it would be called
     * "onEventMainThread".
     * 
     * @param subscriber subscriber
     */
    public static void register(Object subscriber) {
        EventBus eventBus = EventBus.getDefault();
        // 避免重复注册抛异常
        if (eventBus.isRegistered(subscriber)) {
            Log.w(TAG, "register: Subscriber class " + subscriber.getClass()
                    + " already registered to event bus " + eventBus);
            return;
        }
        
        eventBus.register(subscriber);
        
    }
    
    /** 
     * Unregisters the given subscriber from all event classes.
     * 
     * @param subscriber subscriber
     */
    public static synchronized void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }
    
    /** 
     * Posts the given event to the event bus. 
     * 
     * @param event event
     */
    public static void post(Object event) {
        EventBus.getDefault().post(event);
    }
}
