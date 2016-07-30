package com.lee.sdk.downloads;

/**
 * DownloadService会在启动的时候调用该方法，从而实现一些关于下载服务的初始化操作,DownloadService. java所在的应用的Application应用实现该接口。
 */
public interface DownloadServiceCallback {
    /**
     * DownloadService会在启动的时候调用该方法，从而实现一些关于下载服务的初始化操作。
     */
    void onDownloadServiceCreate();
}
