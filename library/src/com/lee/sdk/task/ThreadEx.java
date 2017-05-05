/*
 * Copyright (C) 2013 Lee Hong (http://blog.csdn.net/leehong2005)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lee.sdk.task;

/**
 * This class extends the {@link Thread} class, it is can be restart, pause, etc.
 * 
 * @author Li Hong
 * 
 * @date 2011/10/17
 */
public class ThreadEx extends Thread {
    /**
     * Indicate the thread has been stopped or not.
     */
    private boolean m_isStop = false;

    /**
     * The constructor method.
     * 
     * @param threadName The name of the thread.
     */
    public ThreadEx(String threadName) {
        super(threadName);
    }

    /**
     * Constructs a new {@code Thread} with a {@code Runnable} object and a newly generated name.
     * The new {@code Thread} will belong to the {@code ThreadGroup} passed as parameter.
     * 
     * @param group {@code ThreadGroup} to which the new {@code Thread} will belong
     * @param runnable a {@code Runnable} whose method <code>run</code> will be executed by the new
     *            {@code Thread}
     * @throws SecurityException if <code>group.checkAccess()</code> fails with a SecurityException
     * @throws IllegalThreadStateException if <code>group.destroy()</code> has already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public ThreadEx(ThreadGroup group, Runnable runnable) {
        super(group, runnable);
    }

    /**
     * Constructs a new {@code Thread} with a {@code Runnable} object, the given name and belonging
     * to the {@code ThreadGroup} passed as parameter.
     * 
     * @param group ThreadGroup to which the new {@code Thread} will belong
     * @param runnable a {@code Runnable} whose method <code>run</code> will be executed by the new
     *            {@code Thread}
     * @param threadName the name for the {@code Thread} being created
     * @throws SecurityException if <code>group.checkAccess()</code> fails with a SecurityException
     * @throws IllegalThreadStateException if <code>group.destroy()</code> has already been done
     * @see java.lang.ThreadGroup
     * @see java.lang.Runnable
     * @see java.lang.SecurityException
     * @see java.lang.SecurityManager
     */
    public ThreadEx(ThreadGroup group, Runnable runnable, String threadName) {
        super(group, runnable, threadName);
    }

    /**
     * Check the thread stops or not.
     * 
     * @return true if it is stopping, otherwise false.
     */
    public boolean isStop() {
        return m_isStop;
    }

    /**
     * Call this method to restart the thread.
     */
    public synchronized void restart() {
        if (Thread.State.WAITING == getState()) {
            synchronized (this) {
                // notify anybody waiting on "this"
                notify();
            }
        }
    }

    /**
     * Call this method to stop thread.
     */
    public void destroy() {
        if (Thread.State.WAITING == this.getState()) {
            restart();
        }

        m_isStop = true;
    }

    /**
     * Call this method to pause this thread.
     */
    public void pause() {
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * The thread working method.
     */
    public void run() {
        if (isStop()) {
            return;
        }
    }
}
