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

public class Bounce implements Easing {

    @Override
    public double easeOut(double t, double b, double c, double d) {
        if ((t /= d) < (1.0 / 2.75)) {
            return c * (7.5625 * t * t) + b;
        } else if (t < (2.0 / 2.75)) {
            return c * (7.5625 * (t -= (1.5 / 2.75)) * t + .75) + b;
        } else if (t < (2.5 / 2.75)) {
            return c * (7.5625 * (t -= (2.25 / 2.75)) * t + .9375) + b;
        } else {
            return c * (7.5625 * (t -= (2.625 / 2.75)) * t + .984375) + b;
        }
    }

    @Override
    public double easeIn(double t, double b, double c, double d) {
        return c - easeOut(d - t, 0, c, d) + b;
    }

    @Override
    public double easeInOut(double t, double b, double c, double d) {
        if (t < d / 2.0)
            return easeIn(t * 2.0, 0, c, d) * .5 + b;
        else
            return easeOut(t * 2.0 - d, 0, c, d) * .5 + c * .5 + b;
    }
}
