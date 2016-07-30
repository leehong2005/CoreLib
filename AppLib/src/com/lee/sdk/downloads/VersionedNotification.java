package com.lee.sdk.downloads;

import com.lee.sdk.res.R;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

/**
 * 由于SDK4.0以上与SDK4.0通知的接口不同
 * 由于notification.builder设置进度的api是在api14以上才有，
 * 所以SDK4.0（包括）以上的提供@VersionNotification14，SDK4.0以下的提供@VersionNotification13
 */
public abstract class VersionedNotification {
    /**
     * 获取Notification对象，针对不同的SDK，返回不同的对象。
     * 
     * @param context context
     * @return 返回结果
     */
    public static VersionedNotification getInstance(Context context) {
        VersionedNotification notification = null;
        final int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            notification = new VersionNotification13(context);
        } else {
            notification = new VersionNotification14(context);
        }
        return notification;
    }
    
    /**
     * Combine all of the options that have been set and return a new Notification object.
     * @return notification
     */
    public abstract Notification getNotification();
    
    /**
     * Setting this flag will make it so the notification is automatically canceled 
     * when the user clicks it in the panel.
     * @param autoCancel autoCancel
     */
    public abstract void setAutoCancel(boolean autoCancel);
    
    /**
     * Supply a custom RemoteViews to use instead of the standard one.
     * @param views views
     */
    public abstract void setContent(RemoteViews views);
    
    /**
     * Set the large text at the right-hand side of the notification.
     * @param info info
     */
    public abstract void setContentInfo(CharSequence info);
    
    /**
     * Supply a PendingIntent to send when the notification is clicked. 
     * @param intent intent
     */
    public abstract void setContentIntent(PendingIntent intent);
    
    /**
     * Set the text (second row) of the notification, in a standard notification.
     * @param text text
     */
    public abstract void setContentText(CharSequence text);
    
    /**
     * Set the title (first row) of the notification, in a standard notification.
     * @param title title
     */
    public abstract void setContentTitle(CharSequence title);
    
    /**
     * Set the default notification options that will be used.
     * @param defaults defaults
     */
    public abstract void setDefaults(int defaults);
    
    /**
     * Supply a PendingIntent to send when the notification is cleared by 
     * the user directly from the notification panel. 
     * @param intent intent
     */
    public abstract void setDeleteIntent(PendingIntent intent);
    
    /**
     * An intent to launch instead of posting the notification to the status bar.
     * @param intent intent
     * @param highPriority highPriority
     */
    public abstract void setFullScreenIntent(PendingIntent intent, boolean highPriority);
    
    /**
     * Set the large icon that is shown in the ticker and notification.
     * @param icon icon
     */
    public abstract void setLargeIcon(Bitmap icon);
    
    /**
     * Set the argb value that you would like the LED on the device to blnk, as well as the rate. 
     * @param argb argb
     * @param onMs onMs
     * @param offMs offMs
     */
    public abstract void setLights(int argb, int onMs, int offMs);
    
    /**
     * Set the large number at the right-hand side of the notification. 
     * @param number number
     */
    public abstract void setNumber(int number);
    
    /**
     * Set whether this is an ongoing notification.
     * @param ongoing ongoing
     */
    public abstract void setOngoing(boolean ongoing);
    
    /**
     * Set this flag if you would only like the sound, vibrate and ticker 
     * to be played if the notification is not already showing.
     * @param onlyAlertOnce onlyAlertOnce
     */
    public abstract void setOnlyAlertOnce(boolean onlyAlertOnce);
    
    /**
     * Set the progress this notification represents, which may be represented as a ProgressBar.
     * @param max max
     * @param progress progress
     * @param indeterminate indeterminate
     */
    public abstract void setProgress(int max, int progress, boolean indeterminate);
    
    /**
     * A variant of setSmallIcon(int) that takes an additional level parameter for when the icon is a LevelListDrawable.
     * @param icon icon
     * @param level level
     */
    public abstract void setSmallIcon(int icon, int level);
    
    /**
     * Set the small icon to use in the notification layouts.
     * @param icon icon
     */
    public abstract void setSmallIcon(int icon);
    
    /**
     * Set the sound to play. It will play on the default stream.
     * @param sound sound
     */
    public abstract void setSound(Uri sound);
    
    /**
     * Set the sound to play. It will play on the stream you supply.
     * @param sound sound
     * @param streamType streamType
     */
    public abstract void setSound(Uri sound, int streamType);
    
    /**
     * Set the text that is displayed in the status bar when the notification first arrives, 
     * and also a RemoteViews object that may be displayed instead on some devices.
     * @param tickerText tickerText
     * @param views views
     */
    public abstract void setTicker(CharSequence tickerText, RemoteViews views);
    
    /** 
     * Set the text that is displayed in the status bar when the notification first arrives.
     * @param tickerText tickerText
     */
    public abstract void setTicker(CharSequence tickerText);
    
    /**
     * Set the vibration pattern to use.
     * @param pattern pattern
     */
    public abstract void setVibrate(long[] pattern);
    
    /**
     * Set the time that the event occurred. Notifications in the panel are sorted by this time.
     * @param when when
     */
    public abstract void setWhen(long when);
    
    /**
     * SDK4.0以下提供通知
     * @author liubaowen
     *
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    static final class VersionNotification13 extends VersionedNotification {
        /** context*/
        private Context mContext;
        /** when*/
        private long mWhen;
        /** small icon*/
        private int mSmallIcon;
        /** small icon level*/
        private int mSmallIconLevel;
        /** number*/
        private int mNumber;
        /** title*/
        private CharSequence mContentTitle;
        /** text*/
        private CharSequence mContentText;
        /** info*/
        private CharSequence mContentInfo;
        /** click intent*/
        private PendingIntent mContentIntent;
        /** content view*/
        private RemoteViews mContentView;
        /** delete intent*/
        private PendingIntent mDeleteIntent;
        /** fullscreen intent*/
        private PendingIntent mFullScreenIntent;
        /** tickerText*/
        private CharSequence mTickerText;
        /** TickerView*/
        private RemoteViews mTickerView;
        /** large icon*/
        private Bitmap mLargeIcon;
        /** sound*/
        private Uri mSound;
        /** audio stream type*/
        private int mAudioStreamType;
        /** vibrate*/
        private long[] mVibrate;
        /** led argb*/
        private int mLedArgb;
        /** ledon ms*/
        private int mLedOnMs;
        /** ledoff ms*/
        private int mLedOffMs;
        /** defaults*/
        private int mDefaults;
        /** falgs*/
        private int mFlags;
        /** progress max*/
        private int mProgressMax;
        /** current progress*/
        private int mProgress;
        /** Progress Indeterminate*/
        private boolean mProgressIndeterminate;

        /**
         * 构造函数
         * @param context context
         */
        public VersionNotification13(Context context) {
            mContext = context;
        }
        
        @Override
        public Notification getNotification() {
            Notification n = new Notification();
            
            n.when = mWhen;
            n.icon = mSmallIcon;
            n.iconLevel = mSmallIconLevel;
            n.number = mNumber;
            n.contentView = makeContentView();
            n.contentIntent = mContentIntent;
            n.deleteIntent = mDeleteIntent;
            n.tickerText = mTickerText;
            //API9的变量
//            n.fullScreenIntent = mFullScreenIntent;

            //2.2以前没有下面两个变量，由于暂没用到，所以先不考虑
//            n.tickerView = makeTickerView();
//            n.largeIcon = mLargeIcon;
            n.sound = mSound;
            n.audioStreamType = mAudioStreamType;
            n.vibrate = mVibrate;
            n.ledARGB = mLedArgb;
            n.ledOnMS = mLedOnMs;
            n.ledOffMS = mLedOffMs;
            n.defaults = mDefaults;
            n.flags = mFlags;
            if (mLedOnMs != 0 && mLedOffMs != 0) {
                n.flags |= Notification.FLAG_SHOW_LIGHTS;
            }
            if ((mDefaults & Notification.DEFAULT_LIGHTS) != 0) {
                n.flags |= Notification.FLAG_SHOW_LIGHTS;
            }
            return n;
        }

        @Override
        public void setAutoCancel(boolean autoCancel) {
            setFlag(Notification.FLAG_AUTO_CANCEL, autoCancel);
        }

        @Override
        public void setContent(RemoteViews views) {
            if (views != null) {
                mContentView = views;
            } else {
                mContentView = makeRemoteViews(R.layout.status_bar_ongoing_event_progress_bar);
            }
        }

        @Override
        public void setContentInfo(CharSequence info) {
            mContentInfo = info;
        }

        @Override
        public void setContentIntent(PendingIntent intent) {
            mContentIntent = intent;
        }

        @Override
        public void setContentText(CharSequence text) {
            mContentText = text;
        }

        @Override
        public void setContentTitle(CharSequence title) {
            mContentTitle = title;
        }

        @Override
        public void setDefaults(int defaults) {
            mDefaults = defaults;
        }

        @Override
        public void setDeleteIntent(PendingIntent intent) {
            mDeleteIntent = intent;
        }

        @Override
        public void setFullScreenIntent(PendingIntent intent,
                boolean highPriority) {
            mFullScreenIntent = intent;
            setFlag(Notification.FLAG_HIGH_PRIORITY, highPriority);
        }

        @Override
        public void setLargeIcon(Bitmap icon) {
            mLargeIcon = icon;
        }

        @Override
        public void setLights(int argb, int onMs, int offMs) {
            mLedArgb = argb;
            mLedOnMs = onMs;
            mLedOffMs = offMs;
        }

        @Override
        public void setNumber(int number) {
            mNumber = number;
        }

        @Override
        public void setOngoing(boolean ongoing) {
            setFlag(Notification.FLAG_ONGOING_EVENT, ongoing);
        }

        @Override
        public void setOnlyAlertOnce(boolean onlyAlertOnce) {
            setFlag(Notification.FLAG_ONLY_ALERT_ONCE, onlyAlertOnce);
        }

        @Override
        public void setProgress(int max, int progress, boolean indeterminate) {
            mProgressMax = max;
            mProgress = progress;
            mProgressIndeterminate = indeterminate;
        }

        @Override
        public void setSmallIcon(int icon, int level) {
            mSmallIcon = icon;
            mSmallIconLevel = level;
        }

        @Override
        public void setSmallIcon(int icon) {
            mSmallIcon = icon;
        }

        @Override
        public void setSound(Uri sound) {
            mSound = sound;
            mAudioStreamType = Notification.STREAM_DEFAULT;
        }

        @Override
        public void setSound(Uri sound, int streamType) {
            mSound = sound;
            mAudioStreamType = streamType;
        }

        @Override
        public void setTicker(CharSequence tickerText, RemoteViews views) {
            mTickerText = tickerText;
            mTickerView = views;
        }

        @Override
        public void setTicker(CharSequence tickerText) {
            mTickerText = tickerText;
        }

        @Override
        public void setVibrate(long[] pattern) {
            mVibrate = pattern;
        }

        @Override
        public void setWhen(long when) {
            mWhen = when;
        }
        
        /**
         * 设置flag 
         * @param mask mask
         * @param value value
         */
        private void setFlag(int mask, boolean value) {
            if (value) {
                mFlags |= mask;
            } else {
                mFlags &= ~mask;
            }
        }
        
        /**
         * 生成view
         * @return view
         */
        private RemoteViews makeContentView() {
            if (mContentView != null) {
                return mContentView;
            } else {
                return makeRemoteViews(R.layout.status_bar_ongoing_event_progress_bar);
            }
        }
        
        /**
         * 生成view
         * @param resId resource id
         * @return view
         */
        private RemoteViews makeRemoteViews(int resId) {
            RemoteViews contentView = new RemoteViews(mContext.getPackageName(), resId);
            if (mSmallIcon != 0) {
                contentView.setImageViewResource(R.id.appIcon, mSmallIcon);
                contentView.setViewVisibility(R.id.appIcon, View.VISIBLE);
            } else {
                contentView.setViewVisibility(R.id.appIcon, View.GONE);
            }
            if (mContentTitle != null) {
                contentView.setTextViewText(R.id.title, mContentTitle);
            }
            if (mContentText != null) {
                contentView.setTextViewText(R.id.description, mContentText);
            }
            
            if (mProgressMax != 0 || mProgressIndeterminate) {
                contentView.setProgressBar(
                        R.id.progress_bar, mProgressMax, mProgress, mProgressIndeterminate);
                //ProgressBar不不支持setViewVisibility
//                contentView.setViewVisibility(R.id.progress_bar, View.VISIBLE);
                
                contentView.setTextViewText(R.id.progress_text, 
                        getDownloadingText(mProgressMax, mProgress));
                contentView.setViewVisibility(R.id.progress_text, View.VISIBLE);
            } else {
                contentView.setViewVisibility(R.id.progress_bar, View.GONE);
                contentView.setViewVisibility(R.id.progress_text, View.GONE);
            }
            /*
            if (mWhen != 0) {
                contentView.setLong(R.id.time, "setTime", mWhen);
            }
            */
            return contentView;
        }
        
        /**
         * Helper function to build the downloading text.
         * @param totalBytes totalBytes
         * @param currentBytes currentBytes
         * @return the string 
         */
        private String getDownloadingText(long totalBytes, long currentBytes) {
            if (totalBytes <= 0) {
                return "";
            }
            long progress = currentBytes * 100 / totalBytes; // SUPPRESS CHECKSTYLE
            StringBuilder sb = new StringBuilder();
            sb.append(progress);
            sb.append('%');
            return sb.toString();
        }
    }
    
    /**
     * SDK4.0及以上提供通知
     * @author liubaowen
     *
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    static final class VersionNotification14 extends VersionedNotification {
        /** notification builder*/
        private final Notification.Builder builder;
        
        /**
         * 构造函数
         * @param context context
         */
        public VersionNotification14(Context context) {
            builder = new Notification.Builder(context);
        }
        
        @Override
        public Notification getNotification() {
            return builder.getNotification();
        }

        @Override
        public void setAutoCancel(boolean autoCancel) {
            builder.setAutoCancel(autoCancel);
        }

        @Override
        public void setContent(RemoteViews views) {
            builder.setContent(views);
        }

        @Override
        public void setContentInfo(CharSequence info) {
            builder.setContentInfo(info);
        }

        @Override
        public void setContentIntent(PendingIntent intent) {
            builder.setContentIntent(intent);
        }

        @Override
        public void setContentText(CharSequence text) {
            builder.setContentText(text);
        }

        @Override
        public void setContentTitle(CharSequence title) {
            builder.setContentTitle(title);
        }

        @Override
        public void setDefaults(int defaults) {
            builder.setDefaults(defaults);
        }

        @Override
        public void setDeleteIntent(PendingIntent intent) {
            builder.setDeleteIntent(intent);
        }

        @Override
        public void setFullScreenIntent(PendingIntent intent,
                boolean highPriority) {
            builder.setFullScreenIntent(intent, highPriority);
        }

        @Override
        public void setLargeIcon(Bitmap icon) {
            builder.setLargeIcon(icon);
        }

        @Override
        public void setLights(int argb, int onMs, int offMs) {
            builder.setLights(argb, onMs, offMs);
        }

        @Override
        public void setNumber(int number) {
            builder.setNumber(number);
        }

        @Override
        public void setOngoing(boolean ongoing) {
            builder.setOngoing(ongoing);
        }

        @Override
        public void setOnlyAlertOnce(boolean onlyAlertOnce) {
            builder.setOnlyAlertOnce(onlyAlertOnce);
        }

        @Override
        public void setProgress(int max, int progress, boolean indeterminate) {
            builder.setProgress(max, progress, indeterminate);
        }

        @Override
        public void setSmallIcon(int icon, int level) {
            builder.setSmallIcon(icon, level);
        }

        @Override
        public void setSmallIcon(int icon) {
            builder.setSmallIcon(icon);
        }

        @Override
        public void setSound(Uri sound) {
            builder.setSound(sound);
        }

        @Override
        public void setSound(Uri sound, int streamType) {
            builder.setSound(sound, streamType);
        }

        @Override
        public void setTicker(CharSequence tickerText, RemoteViews views) {
            builder.setTicker(tickerText, views);
        }

        @Override
        public void setTicker(CharSequence tickerText) {
            builder.setTicker(tickerText);
        }

        @Override
        public void setVibrate(long[] pattern) {
            builder.setVibrate(pattern);
        }

        @Override
        public void setWhen(long when) {
            builder.setWhen(when);
        }
        
    }
}
