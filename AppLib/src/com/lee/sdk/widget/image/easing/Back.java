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

public class Back implements Easing {

    @Override
    public double easeOut(double time, double start, double end, double duration) {
        return easeOut(time, start, end, duration, 0);
    }

    @Override
    public double easeIn(double time, double start, double end, double duration) {
        return easeIn(time, start, end, duration, 0);
    }

    @Override
    public double easeInOut(double time, double start, double end, double duration) {
        return easeInOut(time, start, end, duration, 0.9);
    }

    public double easeIn(double t, double b, double c, double d, double s) {
        if (s == 0)
            s = 1.70158;
        return c * (t /= d) * t * ((s + 1) * t - s) + b;
    }

    public double easeOut(double t, double b, double c, double d, double s) {
        if (s == 0)
            s = 1.70158;
        return c * ((t = t / d - 1) * t * ((s + 1) * t + s) + 1) + b;
    }

    public double easeInOut(double t, double b, double c, double d, double s) {
        if (s == 0)
            s = 1.70158;
        if ((t /= d / 2) < 1)
            return c / 2 * (t * t * (((s *= (1.525)) + 1) * t - s)) + b;
        return c / 2 * ((t -= 2) * t * (((s *= (1.525)) + 1) * t + s) + 2) + b;
    }
}
