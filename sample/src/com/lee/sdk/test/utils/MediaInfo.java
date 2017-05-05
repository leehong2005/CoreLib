package com.lee.sdk.test.utils;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class MediaInfo {
    long mId = 0;
    public String mUrl = null;
    ImageInfo mImageInfo = null;

    public MediaInfo() {
        mImageInfo = new ImageInfo();
    }

    public MediaInfo(int id) {
        mId = id;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public int getOrientation() {
        return mImageInfo.orientation;
    }

    public void setOrientation(int orientation) {
        this.mImageInfo.orientation = orientation;
    }

    public Drawable getErrorDrawable() {
        return mImageInfo.errorDrawable;
    }

    public Bitmap getImageThumb() {
        return mImageInfo.imageThumb;
    }

    public void setImageThumb(Bitmap imageThumb) {
        if (this.mImageInfo.imageThumb != imageThumb) {
            this.mImageInfo.imageThumb = imageThumb;
        }
    }

    public void setErrorBitmap(Bitmap errorBitmap) {
        if (this.mImageInfo.errorBitmap != errorBitmap) {
            this.mImageInfo.errorBitmap = errorBitmap;
        }
    }

    public Bitmap getOriImage() {
        return mImageInfo.oriImage;
    }

    public void setOriImage(Bitmap image) {
        this.mImageInfo.oriImage = image;
    }

    public Uri getImageUri() {
        return mImageInfo.imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.mImageInfo.imageUri = imageUri;
    }

    public String getDisplayName() {
        return mImageInfo.displayName;
    }

    public void setDisplayName(String displayName) {
        this.mImageInfo.displayName = displayName;
    }

    public String getFullPath() {
        return mImageInfo.fullPath;
    }

    public void setFullPath(String fullPath) {
        this.mImageInfo.fullPath = fullPath;
    }

    public String getFileName() {
        if (null == mImageInfo.fullPath) {
            return null;
        }

        mImageInfo.fileName = new File(mImageInfo.fullPath).getName();

        return mImageInfo.fileName;
    }

    public void setTitle(String title) {
        mImageInfo.title = title;
    }

    public void setSize(long size) {
        mImageInfo.size = size;
    }

    public long getSize() {
        return mImageInfo.size;
    }

    public void setDateAdded(long dateAdded) {
        mImageInfo.dateAdded = dateAdded;
    }

    public void setDateModified(long dateModified) {
        mImageInfo.dateModified = dateModified;
    }

    public void setMimeType(String mimeType) {
        mImageInfo.mimeType = mimeType;
    }

    public void setDescription(String desc) {
        mImageInfo.desc = desc;
    }

    public void setIsPrivate(int isPrivate) {
        mImageInfo.isPrivate = isPrivate;
    }

    public void setLatitude(double latitude) {
        mImageInfo.latitude = latitude;
    }

    public void setLongGitude(double longitude) {
        mImageInfo.longitude = longitude;
    }

    public void setMiniThumbMagic(int miniThumbMagic) {
        mImageInfo.miniThumbMagic = miniThumbMagic;
    }

    public void setBucketId(String bucketId) {
        mImageInfo.bucketId = bucketId;
    }

    public void setOrientation(String orientation) {
    }

    public void setDateTaken(long dateTaken) {
        mImageInfo.dateTaken = dateTaken;
    }

    public void setBucketDisplayName(String displayName) {
        mImageInfo.displayName = displayName;
    }

    public void setPicasaId(String picasaId) {
        mImageInfo.picasaId = picasaId;
    }

    public void setBytes(byte[] bytes) {
        mImageInfo.bytes = bytes;
    }

    @SuppressWarnings("unused")
    private static class ImageInfo {
        private int orientation = 0;
        private boolean canRecycle = true;
        private Bitmap imageThumb = null;
        private Bitmap errorBitmap = null;
        private Bitmap oriImage = null;
        private Drawable errorDrawable = null;
        private Uri imageUri = null;
        private String displayName = null;
        private String fullPath = null;
        private String fileName = null;
        private String title;
        private long size;
        private long dateAdded;
        private long dateModified;
        private String mimeType;
        private String desc;
        private int isPrivate;
        private double latitude;
        private double longitude;
        private int miniThumbMagic;
        private String bucketId;
        private long dateTaken;
        private String bucketDisplayName;
        private String picasaId;
        private byte[] bytes;
    }
}
