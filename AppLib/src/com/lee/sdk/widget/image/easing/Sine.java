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

public class Sine implements Easing {

    @Override
    public double easeOut(double t, double b, double c, double d) {
        return c * Math.sin(t / d * (Math.PI / 2)) + b;
    }

    @Override
    public double easeIn(double t, double b, double c, double d) {
        return -c * Math.cos(t / d * (Math.PI / 2)) + c + b;
    }

    @Override
    public double easeInOut(double t, double b, double c, double d) {
        return -c / 2 * (Math.cos(Math.PI * t / d) - 1) + b;
    }
}
