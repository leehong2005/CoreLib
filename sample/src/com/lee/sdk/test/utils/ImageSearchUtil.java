package com.lee.sdk.test.utils;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.lee.sdk.utils.BitmapUtilEx;

public class ImageSearchUtil {
    private int m_thumbWidth = 100;
    private int m_thumbHeight = 100;
    private Context m_context = null;

    private ContentResolver m_contentResolver = null;
    private BitmapFactory.Options m_imgThumbOptions = new BitmapFactory.Options();

    public ImageSearchUtil(Context context) {
        m_imgThumbOptions.inDither = false;
        this.m_context = context;
    }

    public void setThumbnailSize(int width, int height) {
        m_thumbWidth = width;
        m_thumbHeight = height;
    }

    public ArrayList<MediaInfo> getImagesFromSDCard(boolean getThumb) {
        ArrayList<MediaInfo> dataSet = new ArrayList<MediaInfo>();

        if (null == m_context || !haveSDCard()) {
            return dataSet;
        }

        m_contentResolver = getContentResolver();

        String[] proj = { MediaStore.MediaColumns._ID, // INTEGER
                MediaStore.MediaColumns.DISPLAY_NAME, // TEXT
                MediaStore.MediaColumns.TITLE, // TEXT
                MediaStore.MediaColumns.DATA, // TEXT
                MediaStore.MediaColumns.SIZE, // LONG
                MediaStore.MediaColumns.DATE_ADDED, // INTEGER
                MediaStore.MediaColumns.DATE_MODIFIED, // INTEGER
                MediaStore.MediaColumns.MIME_TYPE, // TEXT

                MediaStore.Images.ImageColumns.DESCRIPTION, // TEXT
                MediaStore.Images.ImageColumns.IS_PRIVATE, // INTEGER
                MediaStore.Images.ImageColumns.LATITUDE, // DOUBLE
                MediaStore.Images.ImageColumns.LONGITUDE, // DOUBLE
                MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC, // INTEGER
                MediaStore.Images.ImageColumns.BUCKET_ID, // TEXT
                MediaStore.Images.ImageColumns.ORIENTATION, // TEXT
                MediaStore.Images.ImageColumns.DATE_TAKEN, // INTEGER
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, // TEXT
                MediaStore.Images.ImageColumns.PICASA_ID, // TEXT
        };

        Uri baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor c = m_contentResolver.query(baseUri, proj, null, null, null);
        final int MAX_COUNT = 1000;

        if (null != c) {
            int i = 0;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                MediaInfo mediaInfo = new MediaInfo();
                fillImageInfoData(mediaInfo, baseUri, c);

                mediaInfo.setBytes(new byte[1024 * 6]);

                if (getThumb) {
                    getImageThumbnail(mediaInfo);
                }

                dataSet.add(mediaInfo);

                if (i++ > MAX_COUNT) {
                    break;
                }
            }

            c.close();
        }

        return dataSet;
    }

    private void fillImageInfoData(MediaInfo mediaInfo, Uri baseUri, Cursor c) {
        mediaInfo.setId(c.getLong(0));
        mediaInfo.setDisplayName(c.getString(1));
        mediaInfo.setTitle(c.getString(2));
        mediaInfo.setFullPath(c.getString(3));
        mediaInfo.setSize(c.getLong(4));
        mediaInfo.setDateAdded(c.getLong(5));
        mediaInfo.setDateModified(c.getLong(6));
        mediaInfo.setMimeType(c.getString(7));
        mediaInfo.setDescription(c.getString(8));
        mediaInfo.setIsPrivate(c.getInt(9));
        mediaInfo.setLatitude(c.getDouble(10));
        mediaInfo.setLongGitude(c.getDouble(11));
        mediaInfo.setMiniThumbMagic(c.getInt(12));
        mediaInfo.setBucketId(c.getString(13));
        mediaInfo.setOrientation(c.getInt(14));
        mediaInfo.setDateTaken(c.getLong(15));
        mediaInfo.setBucketDisplayName(c.getString(16));
        mediaInfo.setPicasaId(c.getString(17));
        mediaInfo.setImageUri(Uri.withAppendedPath(baseUri, String.valueOf(mediaInfo.getId())));
    }

    public Bitmap getMediaThumbnail(long id) {
        m_imgThumbOptions.inSampleSize = BitmapUtilEx.computeSampleSize(m_imgThumbOptions, getThumbMinSize(),
                getThumbMaxNumOfPixels());

        Bitmap thumb = MediaStore.Images.Thumbnails.getThumbnail(m_contentResolver, id,
                MediaStore.Images.Thumbnails.MINI_KIND, m_imgThumbOptions);

        return thumb;
    }

    public boolean getImageThumbnail(MediaInfo outImageInfo) {
        if (null != outImageInfo && null != m_contentResolver) {
            try {
                m_imgThumbOptions.inSampleSize = BitmapUtilEx.computeSampleSize(m_imgThumbOptions, getThumbMinSize(),
                        getThumbMaxNumOfPixels());

                Bitmap thumb = MediaStore.Images.Thumbnails.getThumbnail(m_contentResolver, outImageInfo.getId(),
                        MediaStore.Images.Thumbnails.MINI_KIND, m_imgThumbOptions);

                if (null == thumb) {
                    thumb = BitmapUtilEx.makeBitmap(getThumbMinSize(), getThumbMaxNumOfPixels(),
                            outImageInfo.getImageUri(), m_contentResolver, false);
                }

                if (null == thumb) {
                } else {
                    outImageInfo.setImageThumb(thumb);
                }

                return (null != thumb);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public Bitmap getImageThumbnail2(MediaInfo outImageInfo) {
        Bitmap thumb = null;

        if (null == outImageInfo || null == m_contentResolver) {
            return thumb;
        }

        try {
            m_imgThumbOptions.inSampleSize = BitmapUtilEx.computeSampleSize(m_imgThumbOptions, getThumbMinSize(),
                    getThumbMaxNumOfPixels());

            thumb = MediaStore.Images.Thumbnails.getThumbnail(m_contentResolver, outImageInfo.getId(),
                    MediaStore.Images.Thumbnails.MINI_KIND, m_imgThumbOptions);

            if (null == thumb) {
                thumb = BitmapUtilEx.makeBitmap(getThumbMinSize(), getThumbMaxNumOfPixels(),
                        outImageInfo.getImageUri(), m_contentResolver, false);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return thumb;
    }

    public boolean haveSDCard() {
        boolean retVal = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        return retVal;
    }

    protected ContentResolver getContentResolver() {
        if (null != m_context && null == m_contentResolver) {
            return m_context.getContentResolver();
        }

        return m_contentResolver;
    }

    protected int getThumbMinSize() {
        return Math.min(m_thumbWidth, m_thumbHeight);
    }

    protected int getThumbMaxNumOfPixels() {
        return 200 * 200;// (m_thumbWidth * m_thumbHeight);
    }
}
