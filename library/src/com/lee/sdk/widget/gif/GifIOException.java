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

//CHECKSTYLE:OFF
package com.lee.sdk.widget.gif;

import java.io.IOException;

/**
 * Exception encapsulating {@link GifError}s.
 * 
 * @author koral--
 */
public class GifIOException extends IOException {

    private static final long serialVersionUID = 13038402904505L;
    /**
     * Reason which caused an exception
     */
    public final GifError reason;

    GifIOException(GifError reason) {
        super(reason.getFormattedDescription());
        this.reason = reason;
    }

    GifIOException(int errorCode) {
        this(GifError.fromCode(errorCode));
    }
}
//CHECKSTYLE:ON
