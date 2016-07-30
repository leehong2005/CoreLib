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

package com.lee.sdk.lock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The lock pattern utility
 * 
 * @author lihong06
 * @since 2014-10-25
 */
public class LockPatternUtils {
    /**
     * TAG
     */
    private static final String TAG = "LockPatternUtils";
    
    /**
     * DEBUG
     */
    private static final boolean DEBUG = true;
    
    /**
     * The minimum number of dots in a valid pattern.
     */
    public static final int MIN_LOCK_PATTERN_SIZE = 4;
    
    /**
     * The minimum number of dots the user must include in a wrong pattern
     * attempt for it to be counted against the counts that affect
     * {@link #FAILED_ATTEMPTS_BEFORE_TIMEOUT} and {@link #FAILED_ATTEMPTS_BEFORE_RESET}
     */
    public static final int MIN_PATTERN_REGISTER_FAIL = MIN_LOCK_PATTERN_SIZE;

    /**
     * The number of incorrect attempts before which we fall back on an alternative
     * method of verifying the user, and resetting their lock pattern.
     */
    public static final int FAILED_ATTEMPTS_BEFORE_RESET = 20;

    /**
     * How long the user is prevented from trying again after entering the
     * wrong pattern too many times.
     */
    public static final long FAILED_ATTEMPT_TIMEOUT_MS = 30000L;

    /**
     * The interval of the countdown for showing progress of the lockout.
     */
    public static final long FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS = 1000L;
    
    /**
     * The maximum number of incorrect attempts before the user is prevented from trying again for
     * {@link #FAILED_ATTEMPT_TIMEOUT_MS}.
     */
    public static final int FAILED_ATTEMPTS_BEFORE_TIMEOUT = 5;
    
    /**
     * Lock pattern file
     */
    private static final String LOCK_PATTERN_FILE = "gesture.key";
    
    /**
     * KEY
     */
    private final static String HAVE_SAVED_PATTERN_KEY = "key_have_saved_pattern";
    
    /**
     * Context
     */
    private Context mContext = null;
    
    
    /**
     * Constructor method
     * 
     * @param context context
     */
    public LockPatternUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Check to see if the user has stored a lock pattern.
     * 
     * @return Whether a saved pattern exists.
     */
    public boolean savedPatternExists() {
        File file = new File(getLockPatternFileName());
        if (file.exists() && file.length() > 0) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Clear any lock pattern or password.
     */
    public void clearLock() {
        saveLockPattern(null);
    }

    /**
     * Save a lock pattern.
     * 
     * @param pattern The new pattern to save.
     */
    public void saveLockPattern(List<LockPatternView.Cell> pattern) {
        // Compute the hash
        final byte[] hash = patternToHash(pattern);
        try {
            writeFile(getLockPatternFileName(), hash);
            if (pattern != null) {
                setBoolean(HAVE_SAVED_PATTERN_KEY, true);
            } else {
                setBoolean(HAVE_SAVED_PATTERN_KEY, false);
            }
        } catch (Exception re) {
            Log.e(TAG, "Couldn't save lock pattern " + re);
        }
    }
    
    /**
     * Check to see if a pattern matches the saved pattern. If no pattern exists, always returns
     * true.
     * 
     * @param pattern The pattern to check.
     * @return Whether the pattern matches the stored one.
     */
    public boolean checkPattern(List<LockPatternView.Cell> pattern) {
        return checkPattern(getLockPatternFileName(), patternToHash(pattern));
    }
    
    /**
     * Check to see if a pattern matches the saved pattern. If no pattern exists, always returns
     * true.
     * 
     * @param name name
     * @param pattern The pattern to check.
     * @return Whether the pattern matches the stored one.
     */
    private static boolean checkPattern(String name, byte[] hash) {
        try {
            // Read all the bytes from the file
            RandomAccessFile raf = new RandomAccessFile(name, "r");
            final byte[] stored = new byte[(int) raf.length()];
            int got = raf.read(stored, 0, stored.length);
            raf.close();
            if (got <= 0) {
                return true;
            }
            // Compare the hash from the file with the entered pattern's hash
            return Arrays.equals(stored, hash);
        } catch (FileNotFoundException fnfe) {
            if (DEBUG) {
                Log.e(TAG, "Cannot read file " + fnfe);
            }
            return true;
        } catch (IOException ioe) {
            if (DEBUG) {
                Log.e(TAG, "Cannot read file " + ioe);
            }
            return true;
        }
    }

    /**
     * Get the lock pattern file name
     * 
     * @return
     */
    private String getLockPatternFileName() {
        File file = new File(mContext.getFilesDir(), LOCK_PATTERN_FILE);
        return file.getAbsolutePath();
    }

    /**
     * Set the boolean value.
     * 
     * @param key key
     * @param value value
     */
    public void setBoolean(String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(key, value).commit();
    }
    
    /**
     * Get the boolean value
     * 
     * @param key key
     * @return value
     */
    public boolean getBoolean(String key) {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(key, false);
    }
    
    /**
     * Set the long value
     * 
     * @param key key
     * @param value value
     */
    public void setLong(String key, long value) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putLong(key, value).commit();
    }
    
    /**
     * Get the long value.
     * 
     * @param key key
     * @return value
     */
    public long getLong(String key) {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getLong(key, 0L);
    }
    
    /**
     * Deserialize a pattern.
     * 
     * @param string The pattern serialized with {@link #patternToString}
     * @return The pattern.
     */
    public static List<LockPatternView.Cell> stringToPattern(String string) {
        List<LockPatternView.Cell> result = newArrayList();

        final byte[] bytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            result.add(LockPatternView.Cell.of(b / 3, b % 3));
        }
        return result;
    }

    /**
     * Serialize a pattern.
     * 
     * @param pattern The pattern.
     * @return The pattern in string form.
     */
    public static String patternToString(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        return new String(res);
    }

    /**
     * Write the file
     * 
     * @param name name
     * @param hash hash
     */
    private static void writeFile(String name, byte[] hash) {
        try {
            // Write the hash to file
            RandomAccessFile raf = new RandomAccessFile(name, "rw");
            // Truncate the file if pattern is null, to clear the lock
            if (hash == null || hash.length == 0) {
                raf.setLength(0);
            } else {
                raf.write(hash, 0, hash.length);
            }
            raf.close();
        } catch (IOException ioe) {
            if (DEBUG) {
                Log.e(TAG, "Error writing to file " + ioe);
            }
        }
    }
    
    /**
     * Generate an SHA-1 hash for the pattern. Not the most secure, but it is at least a second
     * level of protection. First level is that the file is in a location only readable by the
     * system process.
     * 
     * @param pattern the gesture pattern.
     * 
     * @return the hash of the pattern in a byte array.
     */
    public static byte[] patternToHash(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return null;
        }

        final int patternSize = pattern.size();
        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(res);
            return hash;
        } catch (NoSuchAlgorithmException nsa) {
            return res;
        }
    }

    /**
     * Creates a resizable {@code ArrayList} instance containing the given elements.
     * 
     * <p>
     * <b>Note:</b> due to a bug in javac 1.5.0_06, we cannot support the following:
     * 
     * <p>
     * {@code List<Base> list = Lists.newArrayList(sub1, sub2);}
     * 
     * <p>
     * where {@code sub1} and {@code sub2} are references to subtypes of {@code Base}, not of
     * {@code Base} itself. To get around this, you must use:
     * 
     * <p>
     * {@code List<Base> list = Lists.<Base>newArrayList(sub1, sub2);}
     * 
     * @param elements the elements that the list should contain, in order
     * @return a newly-created {@code ArrayList} containing those elements
     */
    public static <E> ArrayList<E> newArrayList(E... elements) {
        int capacity = (elements.length * 110) / 100 + 5;
        ArrayList<E> list = new ArrayList<E>(capacity);
        Collections.addAll(list, elements);
        return list;
    }
}
