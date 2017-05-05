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

package com.lee.sdk.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * This class provides the method to operation for bitmap.
 * 
 * @author Li Hong
 * @date 2011/08/10
 */
@SuppressLint("WorldWriteableFiles")
@SuppressWarnings("deprecation")
public class BitmapUtil {
    /**
     * Get rounded corner bitmap from specified Bitmap.
     * 
     * @param bmpSrc The source Bitmap object.
     * @param rx The radius of horizontal direction.
     * @param ry The radius of vertical direction.
     * 
     * @return The rounded corner bitmap object, null will be returned if this method failed.
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bmpSrc, float rx, float ry) {
        if (null == bmpSrc) {
            return null;
        }

        int bmpSrcWidth = bmpSrc.getWidth();
        int bmpSrcHeight = bmpSrc.getHeight();

        try {
            if (bmpSrcWidth > 0 && bmpSrcHeight > 0) {
                Bitmap bmpDest = Bitmap.createBitmap(bmpSrcWidth, bmpSrcHeight, Config.ARGB_8888);
                if (null != bmpDest) {
                    Canvas canvas = new Canvas(bmpDest);
                    final int color = 0xff424242;
                    final Paint paint = new Paint();
                    final Rect rect = new Rect(0, 0, bmpSrcWidth, bmpSrcHeight);
                    final RectF rectF = new RectF(rect);

                    // Setting or clearing the ANTI_ALIAS_FLAG bit AntiAliasing smooth out
                    // the edges of what is being drawn, but is has no impact on the interior of the
                    // shape.
                    paint.setAntiAlias(true);

                    canvas.drawARGB(0, 0, 0, 0);
                    paint.setColor(color);
                    canvas.drawRoundRect(rectF, rx, ry, paint);
                    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
                    canvas.drawBitmap(bmpSrc, rect, rect, paint);
                }

                return bmpDest;
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Duplicate a bitmap by specified source bitmap.
     * 
     * @param bmpSrc The specified bitmap.
     * 
     * @return The duplicate bitmap.
     */
    public static Bitmap duplicateBitmap(Bitmap bmpSrc) {
        if (null == bmpSrc) {
            return null;
        }

        int bmpSrcWidth = bmpSrc.getWidth();
        int bmpSrcHeight = bmpSrc.getHeight();

        if (0 == bmpSrcWidth || 0 == bmpSrcHeight) {
            return null;
        }

        Bitmap bmpDest = null;

        try {
            bmpDest = Bitmap.createBitmap(bmpSrcWidth, bmpSrcHeight, Config.ARGB_8888);
            if (null != bmpDest) {
                Canvas canvas = new Canvas(bmpDest);
                final Rect rect = new Rect(0, 0, bmpSrcWidth, bmpSrcHeight);

                canvas.drawBitmap(bmpSrc, rect, rect, null);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bmpDest;
    }

    /**
     * Convert a picture object to bitmap.s
     * 
     * @param pic The object of picture..
     * 
     * @return The target bitmap.
     */
    public static Bitmap pictureToBitmap(Picture pic) {
        Bitmap bmp = null;

        if (null != pic) {
            int w = pic.getWidth();
            int h = pic.getHeight();

            try {
                bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
                if (null != bmp) {
                    Canvas canvas = new Canvas(bmp);
                    pic.draw(canvas);
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bmp;
    }

    /**
     * Returns an immutable bitmap from the specified subset of the source bitmap.
     * 
     * @param bitmap The source bitmap.
     * @param wScale X axis scale.
     * @param hScale Y axis scale.
     * 
     * @return The target bitmap.
     */
    public static Bitmap getScaleBitmap(Bitmap bitmap, float wScale, float hScale) {
        try {
            Matrix matrix = new Matrix();
            matrix.postScale(wScale, hScale);
            Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bmp;
        } catch (OutOfMemoryError e) {
            // TODO: handle exception
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Create sized bitmap.
     * 
     * @param bitmap The source bitmap.
     * @param width The width of new bitmap.
     * @param height The height of new bitmap.
     * 
     * @return The target bitmap.
     */
    public static Bitmap getSizedBitmap(Bitmap bitmap, int dstWidth, int dstHeight) {
        if (null != bitmap) {
            try {
                if (dstWidth > 0 && dstHeight > 0) {
                    Bitmap result = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
                    return result;
                }
            } catch (OutOfMemoryError e) {
                // TODO: handle exception
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    /**
     * Returns an full screen bitmap from the specified subset of the source bitmap.
     * 
     * @param bitmap The source bitmap.
     * @param wScale X axis scale.
     * @param hScale Y axis scale.
     * 
     * @return The target bitmap.
     */
    public static Bitmap getFullScreenBitmap(Bitmap bitmap, int wScale, int hScale) {
        int dstWidth = bitmap.getWidth() * wScale;
        int dstHeight = bitmap.getHeight() * hScale;
        Bitmap result = null;

        try {
            result = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Convert a byte array to a bitmap.
     * 
     * @param array The byte array that to be convert.
     * 
     * @return The bitmap.
     */
    public static Bitmap byteArrayToBitmap(byte[] array) {
        if (null == array) {
            return null;
        }

        Bitmap bitmap = null;

        try {
            bitmap = BitmapFactory.decodeByteArray(array, 0, array.length);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * Convert a bitmap to a byte array.
     * 
     * @param bitmap The bitmap that to be convert.
     * 
     * @return The byte array.
     */
    public static byte[] bitampToByteArray(Bitmap bitmap) {
        byte[] array = null;
        try {
            if (null != bitmap) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 10, os);
                array = os.toByteArray();
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return array;
    }

    /**
     * Save file to Android file system.
     * 
     * @param context The Android context.
     * @param bmp The Bitmap to be saved.
     * @param name The name of file.
     */
    public static void saveBitmapToFile(Context context, Bitmap bmp, String name) {
        if (null != context && null != bmp && null != name && name.length() > 0) {
            try {
                FileOutputStream fos = context.openFileOutput(name, Context.MODE_WORLD_WRITEABLE);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                fos = null;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save bitmap to the a specified file path.
     * 
     * @param bmp The Bitmap to be saved.
     * @param destFile The file object which bitmap will be save to.
     */
    public static void saveBitmapToFile(Bitmap bmp, File destFile) {
        if (null != bmp && null != destFile && !destFile.isDirectory()) {
            FileOutputStream fos = null;

            try {
                fos = new FileOutputStream(destFile);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                fos = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Compute the sample size as a function of minSideLength and maxNumOfPixels. minSideLength is
     * used to specify that minimal width or height of a bitmap. maxNumOfPixels is used to specify
     * the maximal size in pixels that is tolerable in terms of memory usage.
     * 
     * The function returns a sample size based on the constraints. Both size and minSideLength can
     * be passed in as IImage.UNCONSTRAINED, which indicates no care of the corresponding
     * constraint. The functions prefers returning a sample size that generates a smaller bitmap,
     * unless minSideLength = IImage.UNCONSTRAINED.
     * 
     * Also, the function rounds up the sample size to a power of 2 or multiple of 8 because
     * BitmapFactory only honors sample size this way. For example, BitmapFactory downsamples an
     * image by 2 even though the request is 3. So we round up the sample size to avoid OOM.
     */
    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    /**
     * computeInitialSampleSize
     * 
     * @param options
     * @param minSideLength
     * @param maxNumOfPixels
     * @return
     */
    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        final int UNCONSTRAINED = -1;

        int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math.min(Math.floor(w / minSideLength),
                Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == UNCONSTRAINED) && (minSideLength == UNCONSTRAINED)) {
            return 1;
        } else if (minSideLength == UNCONSTRAINED) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * Get the appropriate sample size from the input stream.
     * 
     * @param is The input stream.
     * @param minSideLength is used to specify that minimal width or height of a bitmap.
     * @param maxNumOfPixels is used to specify the maximal size in pixels that is tolerable in
     *            terms of memory usage.
     * 
     * @return the sample size.
     */
    public static int getAppropriateSampleSize(InputStream is, int minSideLength, int maxNumOfPixels) {
        int sampleSize = 1;

        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, opts);

            if (opts.outHeight > 0 && opts.outWidth > 0) {
                sampleSize = computeSampleSize(opts, minSideLength, maxNumOfPixels);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        android.util.Log.d("leehong2", "    BitmapUtil.getAppropriateSampleSize inSampleSize = " + sampleSize);

        return sampleSize;
    }

    /**
     * Load bitmap from Android file system.
     * 
     * @param context The Android context.
     * @param name The file name.
     * @param minSideLength is used to specify that minimal width or height of a bitmap.
     * @param maxNumOfPixels is used to specify the maximal size in pixels that is tolerable in
     *            terms of memory usage.
     * 
     * @return The bitmap to be loaded.
     */
    public static Bitmap loadBitmapFromFile(Context context, String name, int minSideLength, int maxNumOfPixels) {
        Bitmap bmp = null;
        try {
            if (null != context && null != name && name.length() > 0) {
                FileInputStream fis = context.openFileInput(name);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = getAppropriateSampleSize(fis, minSideLength, maxNumOfPixels);
                fis.close();
                fis = null;

                fis = context.openFileInput(name);
                bmp = BitmapFactory.decodeStream(fis, null, opts);
                fis.close();
                fis = null;
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bmp;
    }

    /**
     * Load bitmap from SD Card.
     * 
     * @param strPath The path of the bitmap.
     * @param minSideLength is used to specify that minimal width or height of a bitmap.
     * @param maxNumOfPixels is used to specify the maximal size in pixels that is tolerable in
     *            terms of memory usage.
     * 
     * @return The object of bitmap.
     */
    public static Bitmap loadBitmapFromSDCard(String strPath, int minSideLength, int maxNumOfPixels) {
        File file = new File(strPath);

        try {
            FileInputStream fis = new FileInputStream(file);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = getAppropriateSampleSize(fis, minSideLength, maxNumOfPixels);
            fis.close();
            fis = null;

            fis = new FileInputStream(file);
            Bitmap bmp = BitmapFactory.decodeStream(fis, null, opts);
            fis.close();
            fis = null;

            return bmp;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Save the bitmap to sd card.
     * 
     * @param bmp The bitmap object to be saved.
     * @param strPath The bitmap file path, contains file extension.
     */
    public static Drawable bitmapToDrawable(Bitmap bmp) {
        if (null == bmp) {
            return null;
        }

        return new BitmapDrawable(bmp);
    }

    /**
     * Load bitmap file from sd card.
     * 
     * @param strPath The bitmap file path.
     * 
     * @return The Bitmap object, the returned value may be null.
     */
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (null == drawable) {
            return null;
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        return drawableToBitmap(drawable, width, height);
    }

    /**
     * Load bitmap file from sd card.
     * 
     * @param strPath The bitmap file path.
     * 
     * @return The Bitmap object, the returned value may be null.
     */
    public static Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
        if (null == drawable || width <= 0 || height <= 0) {
            return null;
        }

        Config config = (drawable.getOpacity() != PixelFormat.OPAQUE) ? Config.ARGB_8888 : Config.RGB_565;

        Bitmap bitmap = null;

        try {
            bitmap = Bitmap.createBitmap(width, height, config);
            if (null != bitmap) {
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, width, height);
                drawable.draw(canvas);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * Save bitmap to SD Card.
     * 
     * @param bmp The bitmap object.
     * @param strPath The path of the bitmap relative to SD Card.
     */
    public static void saveBitmapToSDCard(Bitmap bmp, String strPath) {
        if (null != bmp && null != strPath && !strPath.equalsIgnoreCase("")) {
            File file = new File(strPath);
            saveBitmapToFile(bmp, file);
        }
    }

    /**
     * Get bitmap according to specified url from network.
     * 
     * @param url The URL of the bitmap to download.
     * @param inSample If set to a value > 1, requests the decoder to subsample the original image,
     *            returning a smaller image to save memory.
     * @param username The user name of HTTP BASIC Authentication.
     * @param password The password of HTTP BASIC Authentication.
     * 
     * @return The bitmap object.
     */
    public static Bitmap getBitmapFromNet(String url, int inSample, String username, String password) {
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        InputStream is = null;

        try {
            final String usernameTemp = username;
            final String passwordTemp = password;

            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(usernameTemp, passwordTemp.toCharArray());
                }
            });

            URL imageUrl = new URL(url);
            conn = (HttpURLConnection) (imageUrl.openConnection());
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            // conn.setDoInput(true);
            conn.connect();

            is = conn.getInputStream();
            if (null != is) {
                BitmapFactory.Options ops = new BitmapFactory.Options();
                ops.inSampleSize = inSample;
                bitmap = BitmapFactory.decodeStream(is, null, ops);
            }
        } catch (InterruptedIOException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (null != conn) {
                conn.disconnect();
            }
        }

        return bitmap;
    }

    /**
     * Get bitmap according to specified url from network.
     * 
     * @param url The URL of the bitmap to download.
     * 
     * @return The bitmap object.
     */
    public static Bitmap getBitmapFromNet(String url) {
        return getBitmapFromNet(url, 2, null, null);
    }

    /**
     * Decode bitmap from Uri path.
     * 
     * @param context The context object used to get content resolver.
     * @param strUir The Uri path.
     * 
     * @return
     */
    public static Bitmap getBitmapFromUri(Context context, String strUri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(Uri.parse(strUri));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            Bitmap bmp = BitmapFactory.decodeStream(is, null, options);

            return bmp;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Decode bitmap from Uri path.
     * 
     * @param context The context object used to get content resolver.
     * @param uri The Uri path.
     * 
     * @return The bitmap of the specified Uri.
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            Bitmap bmp = BitmapFactory.decodeStream(is);

            return bmp;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get bitmap thumb from uri path.
     * 
     * @param context The context object used to get content resolver.
     * @param uri The Uri path.
     * 
     * @return The thumbnaim of bitmap.
     */
    public static Bitmap getBitmapThumbFromUri(Context context, Uri uri) {
        try {
            long id = -1;
            // Get the image id from the uri path.
            String strPath = uri.getPath();
            int index = strPath.lastIndexOf("/");
            if (index >= 0 && index < strPath.length()) {
                String strId = strPath.substring(index + 1);
                id = Integer.parseInt(strId);
            }

            if (-1 != id) {
                // Get the image thumb from the content thumb.
                ContentResolver cr = context.getContentResolver();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;

                Bitmap thumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id,
                        MediaStore.Images.Thumbnails.MINI_KIND, options);

                return thumb;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Create the reflected bitmap.
     * 
     * @param srcBitmap
     * 
     * @return
     */
    public static Bitmap createReflectedBitmap(Bitmap srcBitmap) {
        if (null == srcBitmap) {
            return null;
        }

        // The gap between the reflection bitmap and original bitmap.
        final int REFLECTION_GAP = 4;

        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        int reflectionWidth = srcBitmap.getWidth();
        int reflectionHeight = srcBitmap.getHeight() / 2;

        if (0 == srcWidth || srcHeight == 0) {
            return null;
        }

        // The matrix
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        try {
            // The reflection bitmap, width is same with original's, height is half of original's.
            Bitmap reflectionBitmap = Bitmap.createBitmap(srcBitmap, 0, srcHeight / 2, srcWidth, srcHeight / 2, matrix,
                    false);

            if (null == reflectionBitmap) {
                return null;
            }

            // Create the bitmap which contains original and reflection bitmap.
            Bitmap bitmapWithReflection = Bitmap.createBitmap(reflectionWidth, srcHeight + reflectionHeight
                    + REFLECTION_GAP, Config.ARGB_8888);

            if (null == bitmapWithReflection) {
                return null;
            }

            // Prepare the canvas to draw stuff.
            Canvas canvas = new Canvas(bitmapWithReflection);

            // Draw the original bitmap.
            canvas.drawBitmap(srcBitmap, 0, 0, null);

            // Draw the reflection bitmap.
            canvas.drawBitmap(reflectionBitmap, 0, srcHeight + REFLECTION_GAP, null);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            LinearGradient shader = new LinearGradient(0, srcHeight, 0, bitmapWithReflection.getHeight()
                    + REFLECTION_GAP, 0x70FFFFFF, 0x00FFFFFF, TileMode.MIRROR);
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));

            // Draw the linear shader.
            canvas.drawRect(0, srcHeight, srcWidth, bitmapWithReflection.getHeight() + REFLECTION_GAP, paint);

            return bitmapWithReflection;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
