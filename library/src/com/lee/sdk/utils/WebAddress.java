/*
 * Copyright (C) 2006 The Android Open Source Project
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.ParseException;

/**
 * {@hide}
 *
 * Web Address Parser
 *
 * This is called WebAddress, rather than URL or URI, because it
 * attempts to parse the stuff that a user will actually type into a
 * browser address widget.
 *
 * Unlike java.net.uri, this parser will not choke on URIs missing
 * schemes.  It will only throw a ParseException if the input is
 * really hosed.
 *
 * If given an https scheme but no port, fills in port
 *
 */
public class WebAddress {

    /**Log tag.*/
    @SuppressWarnings("unused")
    private static final String LOGTAG = "http";

    /**方案。*/
    public String mScheme;
    /**主机。*/
    public String mHost;
    /**端口。*/
    public int mPort;
    /**路径。*/
    public String mPath;
    /**授权。*/
    public String mAuthInfo;

    /**Https port.*/
    static final int HTTPS_PORT = 443;
    /**Http port.*/
    static final int HTTP_PORT = 80;
    
    /**匹配方案。*/
    static final int MATCH_GROUP_SCHEME = 1;
    /**匹配授权。*/
    static final int MATCH_GROUP_AUTHORITY = 2;
    /**匹配主机。*/
    static final int MATCH_GROUP_HOST = 3;
    /**匹配端口。*/
    static final int MATCH_GROUP_PORT = 4;
    /**匹配路径。*/
    static final int MATCH_GROUP_PATH = 5;
    
    /**
     * Good characters for Internationalized Resource Identifiers (IRI).
     * This comprises most common used Unicode characters allowed in IRI
     * as detailed in RFC 3987.
     * Specifically, those two byte Unicode characters are not included.
     */
    public static final String GOOD_IRI_CHAR =
        "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";

    /**地址表达式。*/
    static Pattern sAddressPattern = Pattern.compile(
            /* scheme    */ "(?:(http|https|file)\\:\\/\\/)?" 
            /* authority */ + "(?:([-A-Za-z0-9$_.+!*'(),;?&=]+(?:\\:[-A-Za-z0-9$_.+!*'(),;?&=]+)?)@)?" 
            /* host      */ + "([-" + GOOD_IRI_CHAR + "%_]+(?:\\.[-" 
                            + GOOD_IRI_CHAR + "%_]+)*|\\[[0-9a-fA-F:\\.]+\\])?" 
            /* port      */ + "(?:\\:([0-9]*))?" 
            /* path      */ + "(\\/?[^#]*)?" 
            /* anchor    */ + ".*", Pattern.CASE_INSENSITIVE);

    /** 
     * parses given uriString. 
     * @param address Address
     * */
    public WebAddress(String address) {
        if (address == null) {
            throw new NullPointerException();
        }

        // android.util.Log.d(LOGTAG, "WebAddress: " + address);

        mScheme = "";
        mHost = "";
        mPort = -1;
        mPath = "/";
        mAuthInfo = "";

        Matcher m = sAddressPattern.matcher(address);
        String t;
        if (m.matches()) {
            t = m.group(MATCH_GROUP_SCHEME);
            if (t != null) {
                mScheme = t.toLowerCase();
            }
            t = m.group(MATCH_GROUP_AUTHORITY);
            if (t != null) {
                mAuthInfo = t;
            }
            t = m.group(MATCH_GROUP_HOST);
            if (t != null) {
                mHost = t;
            }
            t = m.group(MATCH_GROUP_PORT);
            if (t != null && t.length() > 0) {
                // The ':' character is not returned by the regex.
                try {
                    mPort = Integer.parseInt(t);
                } catch (NumberFormatException ex) {
                    throw new ParseException("Bad port");
                }
            }
            t = m.group(MATCH_GROUP_PATH);
            if (t != null && t.length() > 0) {
                /* handle busted myspace frontpage redirect with
                   missing initial "/" */
                if (t.charAt(0) == '/') {
                    mPath = t;
                } else {
                    mPath = "/" + t;
                }
            }

        } else {
            // nothing found... outa here
            throw new ParseException("Bad address");
        }

        /* Get port from scheme or scheme from port, if necessary and
           possible */
        if (mPort == HTTPS_PORT && mScheme.equals("")) {
            mScheme = "https";
        } else if (mPort == -1) {
            if (mScheme.equals("https")) {
                mPort = HTTPS_PORT;
            } else {
                mPort = HTTP_PORT; // default
            }
        }
        if (mScheme.equals("")) {
            mScheme = "http";
        }
    }

    /**
     * Get host address url, example: "http://m.baidu.com/app", "http://m.baidu.com".
     * @return Host address url.
     */
    public String getHostAddress() {
        String port = "";
        if ((mPort != HTTPS_PORT && mScheme.equals("https")) 
                || (mPort != HTTP_PORT && mScheme.equals("http"))) {
            port = ":" + Integer.toString(mPort);
        }
        String authInfo = "";
        if (mAuthInfo.length() > 0) {
            authInfo = mAuthInfo + "@";
        }
        
        return mScheme + "://" + authInfo + mHost + port; 
    }
    
    @Override
    public String toString() {
        return getHostAddress() + mPath;
    }
}
