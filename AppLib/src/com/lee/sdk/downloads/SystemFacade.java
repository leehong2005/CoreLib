
package com.lee.sdk.downloads;

import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * System interface facecade.
 */
interface SystemFacade {
    
    /**
     * System#currentTimeMillis()
     * @return @see System#currentTimeMillis()
     */
    long currentTimeMillis();

    /**
     * Network type (as in ConnectivityManager.TYPE_*) of currently active network, or null
     * if there's no active connection.
     * 
     * @return as the description
     */
    Integer getActiveNetworkType();

    /**
     * android.telephony.TelephonyManager#isNetworkRoaming
     * @see android.telephony.TelephonyManager#isNetworkRoaming
     * @return see the description
     */
    boolean isNetworkRoaming();

    /**
     * maximum size, in bytes, of downloads that may go over a mobile connection; or null if
     * there's no limit
     * @return see description
     */
    Long getMaxBytesOverMobile();

    /**
     * recommended maximum size, in bytes, of downloads that may go over a mobile
     * connection; or null if there's no recommended limit.  The user will have the option to bypass
     * this limit.
     * @return seee decsription
     */
    Long getRecommendedMaxBytesOverMobile();

    /**
     * Send a broadcast intent.
     * @param intent Intent
     */
    void sendBroadcast(Intent intent);

    /**
     * Returns true if the specified UID owns the specified package name.
     * @param uid uid
     * @param pckg package name
     * @return Returns true if the specified UID owns the specified package name.
     * @throws NameNotFoundException NameNotFoundException
     */
    boolean userOwnsPackage(int uid, String pckg) throws NameNotFoundException;

    /**
     * Post a system notification to the NotificationManager.
     * @param id notification id
     * @param notification notification
     */
    void postNotification(long id, Notification notification);

    /**
     * Cancel a system notification.
     * @param id notification id.
     */
    void cancelNotification(long id);

    /**
     * Cancel all system notifications.
     */
    void cancelAllNotifications();

    /**
     * Start a thread.
     * @param thread Thread
     */
    void startThread(Thread thread);
}
