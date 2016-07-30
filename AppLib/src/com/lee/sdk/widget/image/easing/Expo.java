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

package com.lee.sdk.widget.image.easing;

public class Expo implements Easing {

    @Override
    public double easeOut(double time, double start, double end, double duration) {
        return (time == duration) ? start + end : end * (-Math.pow(2.0, -10.0 * time / duration) + 1) + start;
    }

    @Override
    public double easeIn(double time, double start, double end, double duration) {
        return (time == 0) ? start : end * Math.pow(2.0, 10.0 * (time / duration - 1.0)) + start;
    }

    @Override
    public double easeInOut(double time, double start, double end, double duration) {
        if (time == 0)
            return start;
        if (time == duration)
            return start + end;
        if ((time /= duration / 2.0) < 1.0)
            return end / 2.0 * Math.pow(2.0, 10.0 * (time - 1.0)) + start;
        return end / 2.0 * (-Math.pow(2.0, -10.0 * --time) + 2.0) + start;
    }

}
