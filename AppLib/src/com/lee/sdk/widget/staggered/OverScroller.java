/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.lee.sdk.widget.staggered;
//CHECKSTYLE:OFF
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.FloatMath;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/**
 * This class encapsulates scrolling with the ability to overshoot the bounds
 * of a scrolling operation. This class is a drop-in replacement for
 * {@link android.widget.Scroller} in most cases.
 */
public class OverScroller {
    private int mMode;

    private MagneticOverScroller mScrollerX;
    private MagneticOverScroller mScrollerY;

    private final Interpolator mInterpolator;

    private static final int DEFAULT_DURATION = 250;
    private static final int SCROLL_MODE = 0;
    private static final int FLING_MODE = 1;

    /**
     * Creates an OverScroller with a viscous fluid scroll interpolator.
     * @param context
     */
    public OverScroller(Context context) {
        this(context, null);
    }

    /**
     * Creates an OverScroller with default edge bounce coefficients.
     * @param context The context of this application.
     * @param interpolator The scroll interpolator. If null, a default (viscous) interpolator will
     * be used.
     */
    public OverScroller(Context context, Interpolator interpolator) {
        this(context, interpolator, MagneticOverScroller.DEFAULT_BOUNCE_COEFFICIENT,
                MagneticOverScroller.DEFAULT_BOUNCE_COEFFICIENT);
    }

    /**
     * Creates an OverScroller.
     * @param context The context of this application.
     * @param interpolator The scroll interpolator. If null, a default (viscous) interpolator will
     * be used.
     * @param bounceCoefficientX A value between 0 and 1 that will determine the proportion of the
     * velocity which is preserved in the bounce when the horizontal edge is reached. A null value
     * means no bounce.
     * @param bounceCoefficientY Same as bounceCoefficientX but for the vertical direction.
     */
    public OverScroller(Context context, Interpolator interpolator,
            float bounceCoefficientX, float bounceCoefficientY) {
        mInterpolator = interpolator;
        mScrollerX = new MagneticOverScroller();
        mScrollerY = new MagneticOverScroller();
        MagneticOverScroller.initializeFromContext(context);

        mScrollerX.setBounceCoefficient(bounceCoefficientX);
        mScrollerY.setBounceCoefficient(bounceCoefficientY);
    }

    /**
     *
     * Returns whether the scroller has finished scrolling.
     *
     * @return True if the scroller has finished scrolling, false otherwise.
     */
    public final boolean isFinished() {
        return mScrollerX.mFinished && mScrollerY.mFinished;
    }

    /**
     * Force the finished field to a particular value. Contrary to
     * {@link #abortAnimation()}, forcing the animation to finished
     * does NOT cause the scroller to move to the final x and y
     * position.
     *
     * @param finished The new finished value.
     */
    public final void forceFinished(boolean finished) {
        mScrollerX.mFinished = mScrollerY.mFinished = finished;
    }

    /**
     * Returns the current X offset in the scroll.
     *
     * @return The new X offset as an absolute distance from the origin.
     */
    public final int getCurrX() {
        return mScrollerX.mCurrentPosition;
    }

    /**
     * Returns the current Y offset in the scroll.
     *
     * @return The new Y offset as an absolute distance from the origin.
     */
    public final int getCurrY() {
        return mScrollerY.mCurrentPosition;
    }

    /**
     * @hide
     * Returns the current velocity.
     *
     * @return The original velocity less the deceleration, norm of the X and Y velocity vector.
     */
    public float getCurrVelocity() {
        float squaredNorm = mScrollerX.mCurrVelocity * mScrollerX.mCurrVelocity;
        squaredNorm += mScrollerY.mCurrVelocity * mScrollerY.mCurrVelocity;
        return FloatMath.sqrt(squaredNorm);
    }

    /**
     * Returns the start X offset in the scroll.
     *
     * @return The start X offset as an absolute distance from the origin.
     */
    public final int getStartX() {
        return mScrollerX.mStart;
    }

    /**
     * Returns the start Y offset in the scroll.
     *
     * @return The start Y offset as an absolute distance from the origin.
     */
    public final int getStartY() {
        return mScrollerY.mStart;
    }

    /**
     * Returns where the scroll will end. Valid only for "fling" scrolls.
     *
     * @return The final X offset as an absolute distance from the origin.
     */
    public final int getFinalX() {
        return mScrollerX.mFinal;
    }

    /**
     * Returns where the scroll will end. Valid only for "fling" scrolls.
     *
     * @return The final Y offset as an absolute distance from the origin.
     */
    public final int getFinalY() {
        return mScrollerY.mFinal;
    }

    /**
     * Returns how long the scroll event will take, in milliseconds.
     *
     * @return The duration of the scroll in milliseconds.
     *
     * @hide Pending removal once nothing depends on it
     * @deprecated OverScrollers don't necessarily have a fixed duration.
     *             This function will lie to the best of its ability.
     */
    public final int getDuration() {
        return Math.max(mScrollerX.mDuration, mScrollerY.mDuration);
    }

    /**
     * Extend the scroll animation. This allows a running animation to scroll
     * further and longer, when used with {@link #setFinalX(int)} or {@link #setFinalY(int)}.
     *
     * @param extend Additional time to scroll in milliseconds.
     * @see #setFinalX(int)
     * @see #setFinalY(int)
     *
     * @hide Pending removal once nothing depends on it
     * @deprecated OverScrollers don't necessarily have a fixed duration.
     *             Instead of setting a new final position and extending
     *             the duration of an existing scroll, use startScroll
     *             to begin a new animation.
     */
    public void extendDuration(int extend) {
        mScrollerX.extendDuration(extend);
        mScrollerY.extendDuration(extend);
    }

    /**
     * Sets the final position (X) for this scroller.
     *
     * @param newX The new X offset as an absolute distance from the origin.
     * @see #extendDuration(int)
     * @see #setFinalY(int)
     *
     * @hide Pending removal once nothing depends on it
     * @deprecated OverScroller's final position may change during an animation.
     *             Instead of setting a new final position and extending
     *             the duration of an existing scroll, use startScroll
     *             to begin a new animation.
     */
    public void setFinalX(int newX) {
        mScrollerX.setFinalPosition(newX);
    }

    /**
     * Sets the final position (Y) for this scroller.
     *
     * @param newY The new Y offset as an absolute distance from the origin.
     * @see #extendDuration(int)
     * @see #setFinalX(int)
     *
     * @hide Pending removal once nothing depends on it
     * @deprecated OverScroller's final position may change during an animation.
     *             Instead of setting a new final position and extending
     *             the duration of an existing scroll, use startScroll
     *             to begin a new animation.
     */
    public void setFinalY(int newY) {
        mScrollerY.setFinalPosition(newY);
    }

    /**
     * Call this when you want to know the new location. If it returns true, the
     * animation is not yet finished.
     */
    public boolean computeScrollOffset() {
        if (isFinished()) {
            return false;
        }

        switch (mMode) {
            case SCROLL_MODE:
                long time = AnimationUtils.currentAnimationTimeMillis();
                // Any scroller can be used for time, since they were started
                // together in scroll mode. We use X here.
                final long elapsedTime = time - mScrollerX.mStartTime;

                final int duration = mScrollerX.mDuration;
                if (elapsedTime < duration) {
                    float q = (float) (elapsedTime) / duration;

                    if (mInterpolator == null)
                        q = Scroller.viscousFluid(q);
                    else
                        q = mInterpolator.getInterpolation(q);

                    mScrollerX.updateScroll(q);
                    mScrollerY.updateScroll(q);
                } else {
                    abortAnimation();
                }
                break;

            case FLING_MODE:
                if (!mScrollerX.mFinished) {
                    if (!mScrollerX.update()) {
                        if (!mScrollerX.continueWhenFinished()) {
                            mScrollerX.finish();
                        }
                    }
                }

                if (!mScrollerY.mFinished) {
                    if (!mScrollerY.update()) {
                        if (!mScrollerY.continueWhenFinished()) {
                            mScrollerY.finish();
                        }
                    }
                }

                break;
        }

        return true;
    }

    /**
     * Start scrolling by providing a starting point and the distance to travel.
     * The scroll will use the default value of 250 milliseconds for the
     * duration.
     *
     * @param startX Starting horizontal scroll offset in pixels. Positive
     *        numbers will scroll the content to the left.
     * @param startY Starting vertical scroll offset in pixels. Positive numbers
     *        will scroll the content up.
     * @param dx Horizontal distance to travel. Positive numbers will scroll the
     *        content to the left.
     * @param dy Vertical distance to travel. Positive numbers will scroll the
     *        content up.
     */
    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }

    /**
     * Start scrolling by providing a starting point and the distance to travel.
     *
     * @param startX Starting horizontal scroll offset in pixels. Positive
     *        numbers will scroll the content to the left.
     * @param startY Starting vertical scroll offset in pixels. Positive numbers
     *        will scroll the content up.
     * @param dx Horizontal distance to travel. Positive numbers will scroll the
     *        content to the left.
     * @param dy Vertical distance to travel. Positive numbers will scroll the
     *        content up.
     * @param duration Duration of the scroll in milliseconds.
     */
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mScrollerX.startScroll(startX, dx, duration);
        mScrollerY.startScroll(startY, dy, duration);
    }

    /**
     * Call this when you want to 'spring back' into a valid coordinate range.
     *
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param minX Minimum valid X value
     * @param maxX Maximum valid X value
     * @param minY Minimum valid Y value
     * @param maxY Minimum valid Y value
     * @return true if a springback was initiated, false if startX and startY were
     *          already within the valid range.
     */
    public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        mMode = FLING_MODE;

        // Make sure both methods are called.
        final boolean spingbackX = mScrollerX.springback(startX, minX, maxX);
        final boolean spingbackY = mScrollerY.springback(startY, minY, maxY);
        return spingbackX || spingbackY;
    }

    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY) {
        fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
    }

    /**
     * Start scrolling based on a fling gesture. The distance traveled will
     * depend on the initial velocity of the fling.
     *
     * @param startX Starting point of the scroll (X)
     * @param startY Starting point of the scroll (Y)
     * @param velocityX Initial velocity of the fling (X) measured in pixels per
     *            second.
     * @param velocityY Initial velocity of the fling (Y) measured in pixels per
     *            second
     * @param minX Minimum X value. The scroller will not scroll past this point
     *            unless overX > 0. If overfling is allowed, it will use minX as
     *            a springback boundary.
     * @param maxX Maximum X value. The scroller will not scroll past this point
     *            unless overX > 0. If overfling is allowed, it will use maxX as
     *            a springback boundary.
     * @param minY Minimum Y value. The scroller will not scroll past this point
     *            unless overY > 0. If overfling is allowed, it will use minY as
     *            a springback boundary.
     * @param maxY Maximum Y value. The scroller will not scroll past this point
     *            unless overY > 0. If overfling is allowed, it will use maxY as
     *            a springback boundary.
     * @param overX Overfling range. If > 0, horizontal overfling in either
     *            direction will be possible.
     * @param overY Overfling range. If > 0, vertical overfling in either
     *            direction will be possible.
     */
    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY, int overX, int overY) {
        mMode = FLING_MODE;
        mScrollerX.fling(startX, velocityX, minX, maxX, overX);
        mScrollerY.fling(startY, velocityY, minY, maxY, overY);
    }

    /**
     * Notify the scroller that we've reached a horizontal boundary.
     * Normally the information to handle this will already be known
     * when the animation is started, such as in a call to one of the
     * fling functions. However there are cases where this cannot be known
     * in advance. This function will transition the current motion and
     * animate from startX to finalX as appropriate.
     *
     * @param startX Starting/current X position
     * @param finalX Desired final X position
     * @param overX Magnitude of overscroll allowed. This should be the maximum
     *              desired distance from finalX. Absolute value - must be positive.
     */
    public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
        mScrollerX.notifyEdgeReached(startX, finalX, overX);
    }

    /**
     * Notify the scroller that we've reached a vertical boundary.
     * Normally the information to handle this will already be known
     * when the animation is started, such as in a call to one of the
     * fling functions. However there are cases where this cannot be known
     * in advance. This function will animate a parabolic motion from
     * startY to finalY.
     *
     * @param startY Starting/current Y position
     * @param finalY Desired final Y position
     * @param overY Magnitude of overscroll allowed. This should be the maximum
     *              desired distance from finalY.
     */
    public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
        mScrollerY.notifyEdgeReached(startY, finalY, overY);
    }

    /**
     * Returns whether the current Scroller is currently returning to a valid position.
     * Valid bounds were provided by the
     * {@link #fling(int, int, int, int, int, int, int, int, int, int)} method.
     *
     * One should check this value before calling
     * {@link #startScroll(int, int, int, int)} as the interpolation currently in progress
     * to restore a valid position will then be stopped. The caller has to take into account
     * the fact that the started scroll will start from an overscrolled position.
     *
     * @return true when the current position is overscrolled and in the process of
     *         interpolating back to a valid value.
     */
    public boolean isOverScrolled() {
        return ((!mScrollerX.mFinished &&
                mScrollerX.mState != MagneticOverScroller.TO_EDGE) ||
                (!mScrollerY.mFinished &&
                        mScrollerY.mState != MagneticOverScroller.TO_EDGE));
    }

    /**
     * Stops the animation. Contrary to {@link #forceFinished(boolean)},
     * aborting the animating causes the scroller to move to the final x and y
     * positions.
     *
     * @see #forceFinished(boolean)
     */
    public void abortAnimation() {
        mScrollerX.finish();
        mScrollerY.finish();
    }

    /**
     * Returns the time elapsed since the beginning of the scrolling.
     *
     * @return The elapsed time in milliseconds.
     *
     * @hide
     */
    public int timePassed() {
        final long time = AnimationUtils.currentAnimationTimeMillis();
        final long startTime = Math.min(mScrollerX.mStartTime, mScrollerY.mStartTime);
        return (int) (time - startTime);
    }

    static class MagneticOverScroller {
        // Initial position
        int mStart;

        // Current position
        int mCurrentPosition;

        // Final position
        int mFinal;

        // Initial velocity
        int mVelocity;

        // Current velocity
        float mCurrVelocity;

        // Constant current deceleration
        float mDeceleration;

        // Animation starting time, in system milliseconds
        long mStartTime;

        // Animation duration, in milliseconds
        int mDuration;

        // Whether the animation is currently in progress
        boolean mFinished;

        // Constant gravity value, used to scale deceleration
        static float GRAVITY;

        static void initializeFromContext(Context context) {
            final float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
            GRAVITY = SensorManager.GRAVITY_EARTH // g (m/s^2)
                    * 39.37f // inch/meter
                    * ppi // pixels per inch
                    * ViewConfiguration.getScrollFriction();
        }

        private static final int TO_EDGE = 0;
        private static final int TO_BOUNDARY = 1;
        private static final int TO_BOUNCE = 2;

        private int mState = TO_EDGE;

        // The allowed overshot distance before boundary is reached.
        private int mOver;

        // Duration in milliseconds to go back from edge to edge. Springback is half of it.
        private static final int OVERSCROLL_SPRINGBACK_DURATION = 200;

        // Oscillation period
        private static final float TIME_COEF =
            1000.0f * (float) Math.PI / OVERSCROLL_SPRINGBACK_DURATION;

        // If the velocity is smaller than this value, no bounce is triggered
        // when the edge limits are reached (would result in a zero pixels
        // displacement anyway).
        private static final float MINIMUM_VELOCITY_FOR_BOUNCE = Float.MAX_VALUE;//140.0f;

        // Proportion of the velocity that is preserved when the edge is reached.
        private static final float DEFAULT_BOUNCE_COEFFICIENT = 0.16f;

        private float mBounceCoefficient = DEFAULT_BOUNCE_COEFFICIENT;

        MagneticOverScroller() {
            mFinished = true;
        }

        void updateScroll(float q) {
            mCurrentPosition = mStart + Math.round(q * (mFinal - mStart));
        }

        /*
         * Get a signed deceleration that will reduce the velocity.
         */
        static float getDeceleration(int velocity) {
            return velocity > 0 ? -GRAVITY : GRAVITY;
        }

        /*
         * Returns the time (in milliseconds) it will take to go from start to end.
         */
        static int computeDuration(int start, int end, float initialVelocity, float deceleration) {
            final int distance = start - end;
            final float discriminant = initialVelocity * initialVelocity - 2.0f * deceleration
                    * distance;
            if (discriminant >= 0.0f) {
                float delta = (float) Math.sqrt(discriminant);
                if (deceleration < 0.0f) {
                    delta = -delta;
                }
                return (int) (1000.0f * (-initialVelocity - delta) / deceleration);
            }

            // End position can not be reached
            return 0;
        }

        void startScroll(int start, int distance, int duration) {
            mFinished = false;

            mStart = start;
            mFinal = start + distance;

            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = duration;

            // Unused
            mDeceleration = 0.0f;
            mVelocity = 0;
        }

        void fling(int start, int velocity, int min, int max) {
            mFinished = false;

            mStart = start;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();

            mVelocity = velocity;

            mDeceleration = getDeceleration(velocity);

            // A start from an invalid position immediately brings back to a valid position
            if (mStart < min) {
                mDuration = 0;
                mFinal = min;
                return;
            }

            if (mStart > max) {
                mDuration = 0;
                mFinal = max;
                return;
            }

            // Duration are expressed in milliseconds
            mDuration = (int) (-1000.0f * velocity / mDeceleration);

            mFinal = start - Math.round((velocity * velocity) / (2.0f * mDeceleration));

            // Clamp to a valid final position
            if (mFinal < min) {
                mFinal = min;
                mDuration = computeDuration(mStart, min, mVelocity, mDeceleration);
            }

            if (mFinal > max) {
                mFinal = max;
                mDuration = computeDuration(mStart, max, mVelocity, mDeceleration);
            }
        }

        void finish() {
            mCurrentPosition = mFinal;
            // Not reset since WebView relies on this value for fast fling.
            // mCurrVelocity = 0.0f;
            mFinished = true;
        }

        void setFinalPosition(int position) {
            mFinal = position;
            mFinished = false;
        }

        void extendDuration(int extend) {
            final long time = AnimationUtils.currentAnimationTimeMillis();
            final int elapsedTime = (int) (time - mStartTime);
            mDuration = elapsedTime + extend;
            mFinished = false;
        }

        void setBounceCoefficient(float coefficient) {
            mBounceCoefficient = coefficient;
        }

        boolean springback(int start, int min, int max) {
            mFinished = true;

            mStart = start;
            mVelocity = 0;

            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mDuration = 0;

            if (start < min) {
                startSpringback(start, min, false);
            } else if (start > max) {
                startSpringback(start, max, true);
            }

            return !mFinished;
        }

        private void startSpringback(int start, int end, boolean positive) {
            mFinished = false;
            mState = TO_BOUNCE;
            mStart = mFinal = end;
            mDuration = OVERSCROLL_SPRINGBACK_DURATION;
            mStartTime -= OVERSCROLL_SPRINGBACK_DURATION / 2;
            mVelocity = (int) (Math.abs(end - start) * TIME_COEF * (positive ? 1.0 : -1.0f));
        }

        void fling(int start, int velocity, int min, int max, int over) {
            mState = TO_EDGE;
            mOver = over;

            mFinished = false;

            mStart = start;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();

            mVelocity = velocity;

            mDeceleration = getDeceleration(velocity);

            // Duration are expressed in milliseconds
            mDuration = (int) (-1000.0f * velocity / mDeceleration);

            mFinal = start - Math.round((velocity * velocity) / (2.0f * mDeceleration));

            // Clamp to a valid final position
            if (mFinal < min) {
                mFinal = min;
                mDuration = computeDuration(mStart, min, mVelocity, mDeceleration);
            }

            if (mFinal > max) {
                mFinal = max;
                mDuration = computeDuration(mStart, max, mVelocity, mDeceleration);
            }

            if (start > max) {
                if (start >= max + over) {
                    springback(max + over, min, max);
                } else {
                    if (velocity <= 0) {
                        springback(start, min, max);
                    } else {
                        long time = AnimationUtils.currentAnimationTimeMillis();
                        final double durationSinceEdge =
                            Math.atan((start-max) * TIME_COEF / velocity) / TIME_COEF;
                        mStartTime = (int) (time - 1000.0f * durationSinceEdge);

                        // Simulate a bounce that started from edge
                        mStart = max;

                        mVelocity = (int) (velocity / Math.cos(durationSinceEdge * TIME_COEF));

                        onEdgeReached();
                    }
                }
            } else {
                if (start < min) {
                    if (start <= min - over) {
                        springback(min - over, min, max);
                    } else {
                        if (velocity >= 0) {
                            springback(start, min, max);
                        } else {
                            long time = AnimationUtils.currentAnimationTimeMillis();
                            final double durationSinceEdge =
                                Math.atan((start-min) * TIME_COEF / velocity) / TIME_COEF;
                            mStartTime = (int) (time - 1000.0f * durationSinceEdge);

                            // Simulate a bounce that started from edge
                            mStart = min;

                            mVelocity = (int) (velocity / Math.cos(durationSinceEdge * TIME_COEF));

                            onEdgeReached();
                        }

                    }
                }
            }
        }

        void notifyEdgeReached(int start, int end, int over) {
            mDeceleration = getDeceleration(mVelocity);

            // Local time, used to compute edge crossing time.
            float timeCurrent = mCurrVelocity / mDeceleration;
            final int distance = end - start;
            float timeEdge = -(float) Math.sqrt((2.0f * distance / mDeceleration)
                    + (timeCurrent * timeCurrent));

            mVelocity = (int) (mDeceleration * timeEdge);

            // Simulate a symmetric bounce that started from edge
            mStart = end;

            mOver = over;

            long time = AnimationUtils.currentAnimationTimeMillis();
            mStartTime = (int) (time - 1000.0f * (timeCurrent - timeEdge));

            onEdgeReached();
        }

        private void onEdgeReached() {
            // mStart, mVelocity and mStartTime were adjusted to their values when edge was reached.
            final float distance = mVelocity / TIME_COEF;

            if (Math.abs(distance) < mOver) {
                // Spring force will bring us back to final position
                mState = TO_BOUNCE;
                mFinal = mStart;
                mDuration = OVERSCROLL_SPRINGBACK_DURATION;
            } else {
                // Velocity is too high, we will hit the boundary limit
                mState = TO_BOUNDARY;
                int over = mVelocity > 0 ? mOver : -mOver;
                mFinal = mStart + over;
                mDuration = (int) (1000.0f * Math.asin(over / distance) / TIME_COEF);
            }
        }

        boolean continueWhenFinished() {
            switch (mState) {
                case TO_EDGE:
                    // Duration from start to null velocity
                    int duration = (int) (-1000.0f * mVelocity / mDeceleration);
                    if (mDuration < duration) {
                        // If the animation was clamped, we reached the edge
                        mStart = mFinal;
                        // Speed when edge was reached
                        mVelocity = (int) (mVelocity + mDeceleration * mDuration / 1000.0f);
                        mStartTime += mDuration;
                        onEdgeReached();
                    } else {
                        // Normal stop, no need to continue
                        return false;
                    }
                    break;
                case TO_BOUNDARY:
                    mStartTime += mDuration;
                    startSpringback(mFinal, mFinal - (mVelocity > 0 ? mOver:-mOver), mVelocity > 0);
                    break;
                case TO_BOUNCE:
                    //mVelocity = (int) (mVelocity * BOUNCE_COEFFICIENT);
                    mVelocity = (int) (mVelocity * mBounceCoefficient);
                    if (Math.abs(mVelocity) < MINIMUM_VELOCITY_FOR_BOUNCE) {
                        return false;
                    }
                    mStartTime += mDuration;
                    break;
            }

            update();
            return true;
        }

        /*
         * Update the current position and velocity for current time. Returns
         * true if update has been done and false if animation duration has been
         * reached.
         */
        boolean update() {
            final long time = AnimationUtils.currentAnimationTimeMillis();
            final long duration = time - mStartTime;

            if (duration > mDuration) {
                return false;
            }

            double distance;
            final float t = duration / 1000.0f;
            if (mState == TO_EDGE) {
                mCurrVelocity = mVelocity + mDeceleration * t;
                distance = mVelocity * t + mDeceleration * t * t / 2.0f;
            } else {
                final float d = t * TIME_COEF;
                mCurrVelocity = mVelocity * (float)Math.cos(d);
                distance = mVelocity / TIME_COEF * Math.sin(d);
            }

            mCurrentPosition = mStart + (int) distance;
            return true;
        }
    }
    
    /**
     * This class encapsulates scrolling.  The duration of the scroll
     * can be passed in the constructor and specifies the maximum time that
     * the scrolling animation should take.  Past this time, the scrolling is 
     * automatically moved to its final stage and computeScrollOffset()
     * will always return false to indicate that scrolling is over.
     */
    public static class Scroller  {
        private int mMode;

        private int mStartX;
        private int mStartY;
        private int mFinalX;
        private int mFinalY;

        private int mMinX;
        private int mMaxX;
        private int mMinY;
        private int mMaxY;

        private int mCurrX;
        private int mCurrY;
        private long mStartTime;
        private int mDuration;
        private float mDurationReciprocal;
        private float mDeltaX;
        private float mDeltaY;
        private boolean mFinished;
        private Interpolator mInterpolator;
        private boolean mFlywheel;

        private float mVelocity;

        private static final int DEFAULT_DURATION = 250;
        private static final int SCROLL_MODE = 0;
        private static final int FLING_MODE = 1;

        private static float DECELERATION_RATE = (float) (Math.log(0.75) / Math.log(0.9));
        private static float ALPHA = 800; // pixels / seconds
        private static float START_TENSION = 0.4f; // Tension at start: (0.4 * total T, 1.0 * Distance)
        private static float END_TENSION = 1.0f - START_TENSION;
        private static final int NB_SAMPLES = 100;
        private static final float[] SPLINE = new float[NB_SAMPLES + 1];

        private float mDeceleration;
        private final float mPpi;

        static {
            float x_min = 0.0f;
            for (int i = 0; i <= NB_SAMPLES; i++) {
                final float t = (float) i / NB_SAMPLES;
                float x_max = 1.0f;
                float x, tx, coef;
                while (true) {
                    x = x_min + (x_max - x_min) / 2.0f;
                    coef = 3.0f * x * (1.0f - x);
                    tx = coef * ((1.0f - x) * START_TENSION + x * END_TENSION) + x * x * x;
                    if (Math.abs(tx - t) < 1E-5) break;
                    if (tx > t) x_max = x;
                    else x_min = x;
                }
                final float d = coef + x * x * x;
                SPLINE[i] = d;
            }
            SPLINE[NB_SAMPLES] = 1.0f;

            // This controls the viscous fluid effect (how much of it)
            sViscousFluidScale = 8.0f;
            // must be set to 1.0 (used in viscousFluid())
            sViscousFluidNormalize = 1.0f;
            sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
        }

        private static float sViscousFluidScale;
        private static float sViscousFluidNormalize;

        /**
         * Create a Scroller with the default duration and interpolator.
         */
        public Scroller(Context context) {
            this(context, null);
        }

        /**
         * Create a Scroller with the specified interpolator. If the interpolator is
         * null, the default (viscous) interpolator will be used. "Flywheel" behavior will
         * be in effect for apps targeting Honeycomb or newer.
         */
        public Scroller(Context context, Interpolator interpolator) {
            this(context, interpolator,
                    context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.HONEYCOMB);
        }

        /**
         * Create a Scroller with the specified interpolator. If the interpolator is
         * null, the default (viscous) interpolator will be used. Specify whether or
         * not to support progressive "flywheel" behavior in flinging.
         */
        public Scroller(Context context, Interpolator interpolator, boolean flywheel) {
            mFinished = true;
            mInterpolator = interpolator;
            mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
            mDeceleration = computeDeceleration(ViewConfiguration.getScrollFriction());
            mFlywheel = flywheel;
        }

        /**
         * The amount of friction applied to flings. The default value
         * is {@link ViewConfiguration#getScrollFriction}.
         * 
         * @param friction A scalar dimension-less value representing the coefficient of
         *         friction.
         */
        public final void setFriction(float friction) {
            mDeceleration = computeDeceleration(friction);
        }
        
        private float computeDeceleration(float friction) {
            return SensorManager.GRAVITY_EARTH   // g (m/s^2)
                          * 39.37f               // inch/meter
                          * mPpi                 // pixels per inch
                          * friction;
        }

        /**
         * 
         * Returns whether the scroller has finished scrolling.
         * 
         * @return True if the scroller has finished scrolling, false otherwise.
         */
        public final boolean isFinished() {
            return mFinished;
        }
        
        /**
         * Force the finished field to a particular value.
         *  
         * @param finished The new finished value.
         */
        public final void forceFinished(boolean finished) {
            mFinished = finished;
        }
        
        /**
         * Returns how long the scroll event will take, in milliseconds.
         * 
         * @return The duration of the scroll in milliseconds.
         */
        public final int getDuration() {
            return mDuration;
        }
        
        /**
         * Returns the current X offset in the scroll. 
         * 
         * @return The new X offset as an absolute distance from the origin.
         */
        public final int getCurrX() {
            return mCurrX;
        }
        
        /**
         * Returns the current Y offset in the scroll. 
         * 
         * @return The new Y offset as an absolute distance from the origin.
         */
        public final int getCurrY() {
            return mCurrY;
        }
        
        /**
         * Returns the current velocity.
         *
         * @return The original velocity less the deceleration. Result may be
         * negative.
         */
        public float getCurrVelocity() {
            return mVelocity - mDeceleration * timePassed() / 2000.0f;
        }

        /**
         * Returns the start X offset in the scroll. 
         * 
         * @return The start X offset as an absolute distance from the origin.
         */
        public final int getStartX() {
            return mStartX;
        }
        
        /**
         * Returns the start Y offset in the scroll. 
         * 
         * @return The start Y offset as an absolute distance from the origin.
         */
        public final int getStartY() {
            return mStartY;
        }
        
        /**
         * Returns where the scroll will end. Valid only for "fling" scrolls.
         * 
         * @return The final X offset as an absolute distance from the origin.
         */
        public final int getFinalX() {
            return mFinalX;
        }
        
        /**
         * Returns where the scroll will end. Valid only for "fling" scrolls.
         * 
         * @return The final Y offset as an absolute distance from the origin.
         */
        public final int getFinalY() {
            return mFinalY;
        }

        /**
         * Call this when you want to know the new location.  If it returns true,
         * the animation is not yet finished.  loc will be altered to provide the
         * new location.
         */ 
        public boolean computeScrollOffset() {
            if (mFinished) {
                return false;
            }

            int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
        
            if (timePassed < mDuration) {
                switch (mMode) {
                case SCROLL_MODE:
                    float x = timePassed * mDurationReciprocal;
        
                    if (mInterpolator == null)
                        x = viscousFluid(x); 
                    else
                        x = mInterpolator.getInterpolation(x);
        
                    mCurrX = mStartX + Math.round(x * mDeltaX);
                    mCurrY = mStartY + Math.round(x * mDeltaY);
                    break;
                case FLING_MODE:
                    final float t = (float) timePassed / mDuration;
                    final int index = (int) (NB_SAMPLES * t);
                    final float t_inf = (float) index / NB_SAMPLES;
                    final float t_sup = (float) (index + 1) / NB_SAMPLES;
                    final float d_inf = SPLINE[index];
                    final float d_sup = SPLINE[index + 1];
                    final float distanceCoef = d_inf + (t - t_inf) / (t_sup - t_inf) * (d_sup - d_inf);
                    
                    mCurrX = mStartX + Math.round(distanceCoef * (mFinalX - mStartX));
                    // Pin to mMinX <= mCurrX <= mMaxX
                    mCurrX = Math.min(mCurrX, mMaxX);
                    mCurrX = Math.max(mCurrX, mMinX);
                    
                    mCurrY = mStartY + Math.round(distanceCoef * (mFinalY - mStartY));
                    // Pin to mMinY <= mCurrY <= mMaxY
                    mCurrY = Math.min(mCurrY, mMaxY);
                    mCurrY = Math.max(mCurrY, mMinY);

                    if (mCurrX == mFinalX && mCurrY == mFinalY) {
                        mFinished = true;
                    }

                    break;
                }
            }
            else {
                mCurrX = mFinalX;
                mCurrY = mFinalY;
                mFinished = true;
            }
            return true;
        }
        
        /**
         * Start scrolling by providing a starting point and the distance to travel.
         * The scroll will use the default value of 250 milliseconds for the
         * duration.
         * 
         * @param startX Starting horizontal scroll offset in pixels. Positive
         *        numbers will scroll the content to the left.
         * @param startY Starting vertical scroll offset in pixels. Positive numbers
         *        will scroll the content up.
         * @param dx Horizontal distance to travel. Positive numbers will scroll the
         *        content to the left.
         * @param dy Vertical distance to travel. Positive numbers will scroll the
         *        content up.
         */
        public void startScroll(int startX, int startY, int dx, int dy) {
            startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
        }

        /**
         * Start scrolling by providing a starting point and the distance to travel.
         * 
         * @param startX Starting horizontal scroll offset in pixels. Positive
         *        numbers will scroll the content to the left.
         * @param startY Starting vertical scroll offset in pixels. Positive numbers
         *        will scroll the content up.
         * @param dx Horizontal distance to travel. Positive numbers will scroll the
         *        content to the left.
         * @param dy Vertical distance to travel. Positive numbers will scroll the
         *        content up.
         * @param duration Duration of the scroll in milliseconds.
         */
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            mMode = SCROLL_MODE;
            mFinished = false;
            mDuration = duration;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mStartX = startX;
            mStartY = startY;
            mFinalX = startX + dx;
            mFinalY = startY + dy;
            mDeltaX = dx;
            mDeltaY = dy;
            mDurationReciprocal = 1.0f / (float) mDuration;
        }

        /**
         * Start scrolling based on a fling gesture. The distance travelled will
         * depend on the initial velocity of the fling.
         * 
         * @param startX Starting point of the scroll (X)
         * @param startY Starting point of the scroll (Y)
         * @param velocityX Initial velocity of the fling (X) measured in pixels per
         *        second.
         * @param velocityY Initial velocity of the fling (Y) measured in pixels per
         *        second
         * @param minX Minimum X value. The scroller will not scroll past this
         *        point.
         * @param maxX Maximum X value. The scroller will not scroll past this
         *        point.
         * @param minY Minimum Y value. The scroller will not scroll past this
         *        point.
         * @param maxY Maximum Y value. The scroller will not scroll past this
         *        point.
         */
        public void fling(int startX, int startY, int velocityX, int velocityY,
                int minX, int maxX, int minY, int maxY) {
            // Continue a scroll or fling in progress
            if (mFlywheel && !mFinished) {
                float oldVel = getCurrVelocity();

                float dx = (float) (mFinalX - mStartX);
                float dy = (float) (mFinalY - mStartY);
                float hyp = FloatMath.sqrt(dx * dx + dy * dy);

                float ndx = dx / hyp;
                float ndy = dy / hyp;

                float oldVelocityX = ndx * oldVel;
                float oldVelocityY = ndy * oldVel;
                if (Math.signum(velocityX) == Math.signum(oldVelocityX) &&
                        Math.signum(velocityY) == Math.signum(oldVelocityY)) {
                    velocityX += oldVelocityX;
                    velocityY += oldVelocityY;
                }
            }

            mMode = FLING_MODE;
            mFinished = false;

            float velocity = FloatMath.sqrt(velocityX * velocityX + velocityY * velocityY);
         
            mVelocity = velocity;
            final double l = Math.log(START_TENSION * velocity / ALPHA);
            mDuration = (int) (1000.0 * Math.exp(l / (DECELERATION_RATE - 1.0)));
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mStartX = startX;
            mStartY = startY;

            float coeffX = velocity == 0 ? 1.0f : velocityX / velocity;
            float coeffY = velocity == 0 ? 1.0f : velocityY / velocity;

            int totalDistance =
                    (int) (ALPHA * Math.exp(DECELERATION_RATE / (DECELERATION_RATE - 1.0) * l));
            
            mMinX = minX;
            mMaxX = maxX;
            mMinY = minY;
            mMaxY = maxY;

            mFinalX = startX + Math.round(totalDistance * coeffX);
            // Pin to mMinX <= mFinalX <= mMaxX
            mFinalX = Math.min(mFinalX, mMaxX);
            mFinalX = Math.max(mFinalX, mMinX);
            
            mFinalY = startY + Math.round(totalDistance * coeffY);
            // Pin to mMinY <= mFinalY <= mMaxY
            mFinalY = Math.min(mFinalY, mMaxY);
            mFinalY = Math.max(mFinalY, mMinY);
        }
        
        static float viscousFluid(float x)
        {
            x *= sViscousFluidScale;
            if (x < 1.0f) {
                x -= (1.0f - (float)Math.exp(-x));
            } else {
                float start = 0.36787944117f;   // 1/e == exp(-1)
                x = 1.0f - (float)Math.exp(1.0f - x);
                x = start + x * (1.0f - start);
            }
            x *= sViscousFluidNormalize;
            return x;
        }
        
        /**
         * Stops the animation. Contrary to {@link #forceFinished(boolean)},
         * aborting the animating cause the scroller to move to the final x and y
         * position
         *
         * @see #forceFinished(boolean)
         */
        public void abortAnimation() {
            mCurrX = mFinalX;
            mCurrY = mFinalY;
            mFinished = true;
        }
        
        /**
         * Extend the scroll animation. This allows a running animation to scroll
         * further and longer, when used with {@link #setFinalX(int)} or {@link #setFinalY(int)}.
         *
         * @param extend Additional time to scroll in milliseconds.
         * @see #setFinalX(int)
         * @see #setFinalY(int)
         */
        public void extendDuration(int extend) {
            int passed = timePassed();
            mDuration = passed + extend;
            mDurationReciprocal = 1.0f / mDuration;
            mFinished = false;
        }

        /**
         * Returns the time elapsed since the beginning of the scrolling.
         *
         * @return The elapsed time in milliseconds.
         */
        public int timePassed() {
            return (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
        }

        /**
         * Sets the final position (X) for this scroller.
         *
         * @param newX The new X offset as an absolute distance from the origin.
         * @see #extendDuration(int)
         * @see #setFinalY(int)
         */
        public void setFinalX(int newX) {
            mFinalX = newX;
            mDeltaX = mFinalX - mStartX;
            mFinished = false;
        }

        /**
         * Sets the final position (Y) for this scroller.
         *
         * @param newY The new Y offset as an absolute distance from the origin.
         * @see #extendDuration(int)
         * @see #setFinalX(int)
         */
        public void setFinalY(int newY) {
            mFinalY = newY;
            mDeltaY = mFinalY - mStartY;
            mFinished = false;
        }

        /**
         * @hide
         */
        public boolean isScrollingInDirection(float xvel, float yvel) {
            return !mFinished && Math.signum(xvel) == Math.signum(mFinalX - mStartX) &&
                    Math.signum(yvel) == Math.signum(mFinalY - mStartY);
        }
    }
}
//CHECKSTYLE:ON
