package com.lee.sdk.downloads;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.text.TextUtils;
import android.util.Log;

/**
 * 用来验证下载文件的合法性。
 */
public final class FileVerifier {
    
    /** private constructor. */
    private FileVerifier() { }
    
    /** log 开关 */
    private static final boolean DEBUG = Constants.LOGV;
    /** log tag. */
    private static final String TAG = FileVerifier.class.getSimpleName();
    
    /**
     * 通过解析APk文件包，获取AndroidManifest.xml，来判断是否是正常的APK文件。如果找到则认为是正常的，否则认为是错误的。
     * 
     * @param filename
     *            文件名字
     * @return true表示正常,false 表示不正常。
     */
    public static boolean isAPK(String filename) {
    	FileInputStream fi = null;
    	ZipInputStream zin = null;
        try {
            if (!TextUtils.isEmpty(filename) && (new File(filename).exists())) {
                fi = new FileInputStream(filename);
            } else {
                if (DEBUG) {
                    Log.e(TAG, "apk文件找不到");
                }
                return false;
            }
            zin = new ZipInputStream(fi);
            ZipEntry entry = null;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.isDirectory() || entry.getName().equals("AndroidManifest.xml")) {
                    if (DEBUG) {
                        Log.e(TAG, "解析APK文件成功");
                    }
                    return true;
                }
            }
            if (DEBUG) {
                Log.e(TAG, "不是APK文件，找不到AndroidManifest.xml");
            }
            return false;
        } catch (IOException e) {
            if (DEBUG) {
                Log.e(TAG, "解析APK出错:" + e.getMessage());
            }
            return false;
        } finally {
			if (fi != null) {
				try {
					fi.close();
				} catch (IOException e) {
					if (DEBUG) {
						e.printStackTrace();
					}
				}
			}
			if (zin != null) {
				try {
					zin.close();
				} catch (IOException e) {
					if (DEBUG) {
						e.printStackTrace();
					}
				}
			}
        }
    }
}
