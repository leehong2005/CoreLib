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

import java.io.UnsupportedEncodingException;

/**
 * CharacterUtils
 * 
 * @author lihong06
 * @since 2013年12月12日
 */
public final class CharacterUtils {
    /**
     * Private constructor to prohibit nonsense instance creation.
     */
    private CharacterUtils() {
    }

    /**
     * 简单粗暴的判断指定的字符串的编码格式
     * 
     * @param chars 指定的字符串
     * @return 编码格式
     */
    public static String guessCharacterSet(CharSequence chars) {
        // Very crude at the moment
        for (int i = 0; i < chars.length(); i++) {
            if (chars.charAt(i) > 0xFF) { // SUPPRESS CHECKSTYLE: 简单粗暴的判断字符串的编码格式
                return "UTF-8";
            }
        }
        return null;
    }

    /**
     * 根据字节流推测最有可能的字符串编码，返回java字符串
     * 
     * @param bytes 字节流
     * @return 字符串
     */
    // CHECKSTYLE:OFF
    public static String getEncodedString(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        /** 判断汉字编码 */
        final String gbk = "GBK";
        final String utf8 = "UTF-8";
        String result = null;

        try {
            // NOTE: GBK is not guaranteed on all platforms
            String gbkText = new String(bytes, gbk);
            String utf8Text = new String(bytes, utf8);

            byte[] gbkBytes = gbkText.getBytes(gbk);
            /** 记录GBK乱码字符个数 */
            int invalidGBKcount = 0;
            for (int c, i = 0; i < gbkBytes.length; i++) {
                c = gbkBytes[i] & 0xFF;
                if (c > 0xA0) {
                    if (c > 0xD7) {
                        invalidGBKcount++;
                    }
                    i++;
                } else {
                    if (c < 0x20 || c == 0x3f) {
                        invalidGBKcount++;
                    }
                }
            }

            byte[] utf8Bytes = utf8Text.getBytes(gbk);
            /** 记录UTF-8乱码字符个数 */
            int invalidUTFcount = 0;
            for (int c, i = 0; i < utf8Bytes.length; i++) {
                c = utf8Bytes[i] & 0xFF;
                if (c > 0xA0) {
                    if (c > 0xD7) {
                        invalidUTFcount++;
                    }
                    i++;
                } else {
                    if (c < 0x20 || c == 0x3f) {
                        invalidUTFcount++;
                    }
                }
            }

            /** 取乱码较少的汉字编码 */
            result = invalidGBKcount >= invalidUTFcount ? utf8Text : gbkText;
            result = result.trim();
            result = result.replace("\r", "");
        } catch (UnsupportedEncodingException e) {
            // system does not support GBK encoding
            e.printStackTrace();
        } catch (Exception e) {
            // keep result empty
            e.printStackTrace();
        }

        /** 若无法识别，使用系统默认编码 */
        return result == null ? new String(bytes) : result;
    }
    // CHECKSTYLE:ON
}
