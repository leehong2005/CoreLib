/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.widget.state;

import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

/**
 * 用来管理{@link ActivityState}类，内部维护一个栈结构。
 * 
 * @author lihong06
 * @since 2014-6-9
 */
public class StateManager implements ActivityContext {
    /** DEBUG */
    private static final boolean DEBUG = com.lee.sdk.Configuration.DEBUG & true;
    /** TAG */
    private static final String TAG = "StateManager";
    /** KEY_MAIN */
    private static final String KEY_MAIN = "activity-state";
    /** KEY_DATA */
    private static final String KEY_DATA = "data";
    /** KEY_STATE */
    private static final String KEY_STATE = "bundle";
    /** KEY_CLASS */
    private static final String KEY_CLASS = "class";
    
    /** 是否恢复的标志量 */
    private boolean mIsResumed;
    /** 进场动画ID */
    private int mEnterAnim;
    /** 出场动画ID */
    private int mExitAnim;
    /** 标识切换动画是否完成 */
    private boolean mTransitionAnimEnd = true;
    /** 切换动画的动画时间 */
    private long mTransitionAnimDuration = 0;
    /** 结果 */
    private ActivityState.ResultEntry mResult;
    /** 栈结构 */
    private final Stack<StateEntry> mStack = new Stack<StateEntry>();
    /** 页面的容器 */
    private final StateContainer mStateContainer;
    /** Activity context */
    private final ActivityContext mActivityContext;
    /** Android context */
    private final Context mContext;
    /** 标志是最后一个页面出栈后，是否关闭当前activity */
    private final boolean mFinishActivity;
    /** 模拟进入动画的View */
    private AnimateView mEnterAnimView;
    /** 模拟退出动画的View */
    private AnimateView mExitAnimView;
    
    /**
     * 构造方法
     * 
     * @param context context
     * @param stateContainer stateContainer
     */
    public StateManager(Context context, StateContainer stateContainer) {
        this(context, stateContainer, false);
    }
    
    /**
     * 构造方法
     * 
     * @param context context
     * @param stateContainer stateContainer
     * @param finishActivity 当最后一个页面出栈后标识是否关闭activity
     */
    public StateManager(Context context, StateContainer stateContainer, boolean finishActivity) {
        mContext = context;
        mStateContainer = stateContainer;
        mFinishActivity = finishActivity;
        mActivityContext = this;
        
        init(context);
    }
    
    /**
     * 初始化
     * 
     * @param context context
     */
    private void init(Context context) {
        mEnterAnimView = new AnimateView(context);
        mExitAnimView = new AnimateView(context);
        
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        
        FrameLayout container = getRootContainer();
        container.addView(mEnterAnimView, new FrameLayout.LayoutParams(width, height));
        container.addView(mExitAnimView, new FrameLayout.LayoutParams(width, height));
        container.setAnimationCacheEnabled(true);
        mEnterAnimView.setVisibility(View.GONE);
        mExitAnimView.setVisibility(View.GONE);
    }

    @Override
    public Context getAndroidContext() {
        return mContext;
    }

    @Override
    public StateManager getStateManager() {
        return this;
    }
    
    /**
     * 指示当前栈中是否有指定类型的页面
     * 
     * @param klass class
     * @return true/false
     */
    public boolean hasStateClass(Class<? extends ActivityState> klass) {
        for (StateEntry entry : mStack) {
            if (klass.isInstance(entry.activityState)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 页面个数
     * 
     * @return 页面个数
     */
    public int getStateCount() {
        return mStack.size();
    }
    
    /**
     * 得到顶部的页面
     * 
     * @return top ActivityState
     */
    public ActivityState getTopState() {
        return (!mStack.isEmpty()) ? mStack.peek().activityState : null;
    }
    
    /**
     * 得到最底部的页面
     * 
     * @return bottom ActivityState
     */
    public ActivityState getBottomState() {
        return (!mStack.isEmpty()) ? mStack.get(0).activityState : null;
    }
    
    /**
     * 得到指定页面在栈中的索引，从栈的最顶端开始遍历，一旦找到符合条件的页面，立即返回其索引。
     * 
     * @param klass klass
     * @return 索引，如果不存在返回-1
     */
    public int getStateIndex(Class<? extends ActivityState> klass) {
        for (int i = mStack.size() - 1; i >= 0; --i) {
            if (klass.isInstance(mStack.get(i).activityState)) {
                return i;
            }
        }
        
        return -1;
    }
    
    /**
     * 销毁View时调用
     */
    public void destroyView() {
        if (DEBUG) {
            Log.i(TAG, "StateManager#destroyView()=====");
        }
        
        for (int i = mStack.size() - 1; i >= 0; --i) {
            mStack.get(i).activityState.onDestroyView();
        }
    }
    
    /**
     * 在{@link android.app.Activity#onDestroy()}中调用。
     */
    public void destroy() {
        if (DEBUG) {
            Log.i(TAG, "StateManager#destroy()=====");
        }
        
        final ViewGroup container = getRootContainer();
        ActivityState state;
        while (!mStack.isEmpty()) {
            state = mStack.pop().activityState;
            state.onDestroy();
            // 回调Activity被销毁了
            state.activityDestroy();
            View child = state.getRootView();
            if (null != child) {
                container.removeView(child);
            }
        }
        mStack.clear();
    }
    
    /**
     * 在{@link android.app.Activity#onActivityResult(int, int, Intent)}中调用。
     * 
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param data data
     */
    public void activityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.i(TAG, "StateManager#activityResult()=====");
        }
        
        getTopState().onStateResult(requestCode, resultCode, data);
    }

    /**
     * 在{@link android.app.Activity#onResume()}中调用。
     */
    public void resume() {
        if (mIsResumed) {
            return;
        }
        
        if (DEBUG) {
            Log.i(TAG, "StateManager#resume()=====");
        }
        
        mIsResumed = true;
        if (!mStack.isEmpty()) {
            getTopState().onResume();
        }
    }

    /**
     * 在{@link android.app.Activity#onPause()}中调用。
     */
    public void pause() {
        if (!mIsResumed) {
            return;
        }
        
        if (DEBUG) {
            Log.i(TAG, "StateManager#pause()=====");
        }
        
        mIsResumed = false;
        if (!mStack.isEmpty()) {
            getTopState().onPause();
        }
    }
    
    /**
     * 在{@link android.app.Activity#onBackPressed()}中调用。
     * 
     * @return true表示处理了，false表示未处理
     */
    public boolean backPressed() {
        if (DEBUG) {
            Log.i(TAG, "StateManager#onBackPressed()=====");
        }
        
        if (!mStack.isEmpty()) {
            getTopState().onBackPressed();
            return true;
        }
        
        return false;
    }
    
    /**
     * 在{@link android.app.Activity#onNewIntent(Intent)}中调用。
     * 
     * @param intent intent
     */
    public void newIntent(Intent intent) {
        if (DEBUG) {
            Log.i(TAG, "StateManager#onNewIntent(), intent = " + intent);
        }
        
        if (!mStack.isEmpty()) {
            getTopState().onNewIntent(intent);
        }
    }
    
    /**
     * 指示当前页面
     * 
     * @return 是否指示当前页面
     */
    public boolean isResumed() {
        return mIsResumed;
    }
    
    /**
     * 在{@link android.app.Activity#onRestoreInstanceState(Bundle)}中调用。
     * 
     * @param inState inState
     */
    public void restoreState(Bundle inState) {
        if (DEBUG) {
            Log.v(TAG, "StateManager#onRestoreState(), inState = " + inState);
        }
        
        Parcelable[] list = inState.getParcelableArray(KEY_MAIN);
        for (Parcelable parcelable : list) {
            Bundle bundle = (Bundle) parcelable;
            @SuppressWarnings("unchecked")
            Class<? extends ActivityState> klass = (Class<? extends ActivityState>) bundle.getSerializable(KEY_CLASS);

            Bundle data = bundle.getBundle(KEY_DATA);
            Bundle state = bundle.getBundle(KEY_STATE);

            ActivityState activityState;
            try {
                Log.v(TAG, "    StateManager#onRestoreState(), restoreFromState " + klass);
                activityState = klass.newInstance();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
            activityState.initialize(mActivityContext, data);
            activityState.onCreate(data, state);
            mStack.push(new StateEntry(data, activityState));
            // Push state
            pushStateView(null, activityState);
        }
    }
    
    /**
     * 在{@link android.app.Activity#onSaveInstanceState(Bundle)}中调用。
     * 
     * @param outState outState
     */
    public void saveState(Bundle outState) {
        if (DEBUG) {
            Log.v(TAG, "StateManager#onSaveState(), outState = " + outState);
        }

        Parcelable[] list = new Parcelable[mStack.size()];
        int i = 0;
        for (StateEntry entry : mStack) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(KEY_CLASS, entry.activityState.getClass());
            bundle.putBundle(KEY_DATA, entry.data);
            Bundle state = new Bundle();
            entry.activityState.onSaveState(state);
            bundle.putBundle(KEY_STATE, state);
            if (DEBUG) {
                Log.v(TAG, "StateManager#onSaveState(), " + entry.activityState.getClass());
            }
            
            list[i++] = bundle;
        }
        outState.putParcelableArray(KEY_MAIN, list);
    }
    
    /**
     * 在{@link android.app.Activity#onConfigurationChanged(Configuration)}中调用。
     * 
     * @param config config
     */
    public void configurationChange(Configuration config) {
        if (DEBUG) {
            Log.i(TAG, "StateManager#onConfigurationChange(), config = " + config);
        }
        
        for (StateEntry entry : mStack) {
            entry.activityState.onConfigurationChanged(config);
        }
    }
    
    /**
     * 在{@link android.app.Activity#onWindowFocusChanged(boolean)}中调用。
     * 
     * @param hasFocus hasFocus
     */
    public void windowFocusChanged(boolean hasFocus) {
        if (DEBUG) {
            Log.i(TAG, "StateManager#onWindowFocusChanged(), hasFocus = " + hasFocus);
        }
        
        if (!mStack.isEmpty()) {
            getTopState().onWindowFocusChanged(hasFocus);
        }
    }

    /**
     * 在{@link android.app.Activity#onKeyDown(int, KeyEvent)}中调用。
     * 
     * @param keyCode keyCode
     * @param event event
     * @return true:已经处理，false:未处理
     */
    public boolean keyDown(int keyCode, KeyEvent event) {
        if (DEBUG) {
            Log.d(TAG, "StateManager#keyDown, keyCode = " + keyCode);
        }
        
        if (!mStack.isEmpty()) {
            ActivityState topState = getTopState();
            if (null != topState && topState.isResumed()) {
                return topState.onKeyDown(keyCode, event);
            }
        }
        
        return false;
    }
    
    /**
     * 在{@link android.app.Activity#onKeyUp(int, KeyEvent)}中调用。
     * 
     * @param keyCode keyCode
     * @param event event
     * @return true:已经处理，false:未处理
     */
    public boolean keyUp(int keyCode, KeyEvent event) {
        if (DEBUG) {
            Log.d(TAG, "StateManager#keyDown, keyUp = " + keyCode);
        }
        
        if (!mStack.isEmpty()) {
            ActivityState topState = getTopState();
            if (null != topState && topState.isResumed()) {
                return topState.onKeyUp(keyCode, event);
            }
        }
        
        return false;
    }
    
    /**
     * 需求释放内存是调用
     */
    public void lowMemory() {
        for (StateEntry entry : mStack) {
            entry.activityState.onLowMemory();
        }
    }
    
    /**
     * 设置当前本栈的可见性
     * 
     * @param visibility visibility
     */
    public void setVisibility(int visibility) {
        for (StateEntry entry : mStack) {
            entry.activityState.setVisibility(visibility);
        }
    }
    
    /**
     * 启动指定的页面
     * 
     * @param klass klass
     * @param data data
     */
    public void startState(Class<? extends ActivityState> klass, Bundle data) {
        if (DEBUG) {
            Log.d(TAG, "StateManager#startActivityState(), class = " + klass.getCanonicalName());
        }
        
        // 如果动画未结束，不执行任何逻辑
        if (!isTransitionAnimEnd()) {
            if (DEBUG) {
                Log.e(TAG, "StateManager#startState(),  transition animation does NOT finished ");
            }
            return;
        }
        
        // Handle the launch mode
        boolean handled = handleLaunchMode(klass, data);
        if (handled) {
            return;
        }
        
        ActivityState state = null;
        try {
            state = klass.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        
        startState(state, data);
    }
    
    /**
     * 启动指定的页面
     * 
     * @param state state
     * @param data data
     */
    public void startState(ActivityState state, Bundle data) {
        if (DEBUG) {
            Log.d(TAG, "StateManager#startState(),  begin ========  class = " + state.getClass());
        }
        
        // 如果动画未结束，不执行任何逻辑
        if (!isTransitionAnimEnd()) {
            if (DEBUG) {
                Log.e(TAG, "StateManager#startState(),  transition animation does NOT finished ");
            }
            return;
        }
        
        boolean handled = handleLaunchMode(state.getClass(), data);
        if (handled) {
            return;
        }
        
        ActivityState top = getTopState();
        
        if (DEBUG) {
            Log.d(TAG, "    StateManager#startState(), top state = " + top + ",   mIsResumed = " + mIsResumed);
        }
        
        if (null != top) {
            if (mIsResumed) {
                top.pause();
            }
        }

        state.initialize(mActivityContext, data);
        state.onCreate(data, null);
        // Push state
        pushStateView(top, state);
        // Push to stack
        mStack.push(new StateEntry(data, state));
        
        if (mIsResumed) {
            state.resume();
        }
        
        printStack();
        
        if (DEBUG) {
            Log.d(TAG, "StateManager#startState(),  end ========  class = " + state.getClass());
        }
    }
    
    /**
     * 启动指定的页面
     * 
     * @param klass klass
     * @param requestCode requestCode
     * @param data data
     */
    public void startStateForResult(Class<? extends ActivityState> klass, int requestCode, Bundle data) {
        if (DEBUG) {
            Log.d(TAG, "StateManager#startStateForResult(), class = " + klass.getCanonicalName());
        }
        
        // 如果动画未结束，不执行任何逻辑
        if (!isTransitionAnimEnd()) {
            if (DEBUG) {
                Log.e(TAG, "StateManager#startState(),  transition animation does NOT finished ");
            }
            return;
        }
        
        // Handle the launch mode
        boolean handled = handleLaunchMode(klass, data);
        if (handled) {
            return;
        }
        
        ActivityState state = null;
        try {
            state = klass.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        
        state.initialize(mActivityContext, data);
        state.mResult = new ActivityState.ResultEntry();
        state.mResult.requestCode = requestCode;
        
        ActivityState top = getTopState();
        if (null != top) {
            top.mReceivedResults = state.mResult;
            if (mIsResumed) {
                top.pause();
            }
        } else {
            mResult = state.mResult;
        }
        
        state.onCreate(data, null);
        // Push state
        pushStateView(top, state);
        // Push to stack
        mStack.push(new StateEntry(data, state));
        
        if (mIsResumed) {
            state.resume();
        }
        
        printStack();
    }
    
    /**
     * 关闭指定的页面
     * 
     * @param state state
     */
    public void finishState(ActivityState state) {
        finishState(state, true);
    }
    
    /**
     * 关闭指定的页面
     * 
     * @param state state
     * @param doPost 是否使用post方式来删除View
     */
    public void finishState(ActivityState state, boolean doPost) {
        if (null == state) {
            return;
        }
        
        if (DEBUG) {
            Log.d(TAG, "StateManager#finishState(),  begin ========  class = " + state.getClass());
        }
        
        // 如果动画未结束，不执行任何逻辑
        if (!isTransitionAnimEnd()) {
            if (DEBUG) {
                Log.e(TAG, "StateManager#finishState(),  transition animation does NOT finished ");
            }
            return;
        }
        
        if (mStack.isEmpty()) {
            if (DEBUG) {
                Log.d(TAG, "StateManager#finishState   stack is empty, size = 0");
            }
            return;
        }
        
        if (mFinishActivity) {
            // The finish() request could be rejected (only happens under Monkey),
            // If it is rejected, we won't close the last page.
            if (mStack.size() == 1) {
                Activity activity = (Activity) mActivityContext.getAndroidContext();
                if (mResult != null) {
                    activity.setResult(mResult.resultCode, mResult.resultData);
                }
                activity.finish();
                if (!activity.isFinishing()) {
                    if (DEBUG) {
                        Log.w(TAG, "StateManager#finishState(), finish is rejected, keep the last state");
                    }
                    return;
                }
                
                if (DEBUG) {
                    Log.v(TAG, "no more state, finish activity");
                }
            }
        }

        if (DEBUG) {
            Log.v(TAG, "StateManager#finishState(), finishState " + state);
        }
        
        if (state != mStack.peek().activityState) {
            if (state.isDestroyed()) {
                if (DEBUG) {
                    Log.d(TAG, "StateManager#finishState(), The state is already destroyed");
                }
                return;
            } else {
                if (DEBUG) {
                    throw new IllegalArgumentException("The stateview to be finished"
                            + " is not at the top of the stack. finished state = " + state + ", current top state = "
                            + mStack.peek().activityState);
                }
                return;
            }
        }

        // Remove the top state.
        mStack.pop();
        if (mIsResumed) {
            state.pause();
        }
        
        state.onDestroy();
        
        if (!mStack.isEmpty()) {
            // Restore the immediately previous state
            ActivityState top = mStack.peek().activityState;
            if (DEBUG) {
                Log.d(TAG, "StateManager#finishState(),  after pop, the top state = " + top);
            }
            
            if (mIsResumed) {
                top.resume();
            }
        }
        // Pop state
        popStateView(state, getTopState(), doPost);
        
        printStack();
        
        if (DEBUG) {
            Log.d(TAG, "StateManager#finishState(),  end ========  class = " + state.getClass());
        }
    }
    
    /**
     * 切换到指定的页面
     * 
     * @param oldState oldState
     * @param newState newState
     * @param data data
     */
    @Deprecated
    public void switchState(ActivityState oldState, ActivityState newState, Bundle data) {
        if (DEBUG) {
            Log.d(TAG, "StateManager#switchState, oldState = " 
                    + oldState + ", newState = " + newState + ", data = " + data);
        }
        
        if (null != oldState) {
            ActivityState topState = getTopState();
            if (null != topState) {
                if (topState == oldState) {
                    mStack.pop();
                    if (mIsResumed) {
                        oldState.pause();
                    }
                    oldState.onDestroy();
                }
            }
        }
        
        newState.initialize(mActivityContext, data);
        mStack.push(new StateEntry(data, newState));
        newState.onCreate(data, null);
        // Push state
        pushStateView(oldState, newState);
        
        if (mIsResumed) {
            newState.resume();
        }
    }
    
    /**
     * 设置页面切换的过渡动画效果
     * 
     * @param enterAnim 进入页面的动画
     * @param exitAnim 退出页面的动画
     */
    public final void overridePendingTransition(int enterAnim, int exitAnim) {
        mEnterAnim = enterAnim;
        mExitAnim = exitAnim;
    }

    /**
     * 清除所有State，这个方法调用后，栈会变为空栈
     */
    protected final void clearStates() {
        if (getStateCount() == 0) {
            return;
        }
        
        if (DEBUG) {
            Log.i(TAG, "StateManager#clearStates() begin =========== ");
        }
        
        // 不需要动画效果
        mExitAnim = 0;
        mEnterAnim = 0;
        
        while (!mStack.isEmpty()) {
            StateEntry topEntry = mStack.peek();
            if (DEBUG) {
                Log.i(TAG, "    StateManager#clearStates,  Pop state out stack, state = " + topEntry.activityState);
            }
            popState(topEntry.activityState);
        }
        
        printStack();
        
        if (DEBUG) {
            Log.i(TAG, "StateManager#clearStates() end =========== ");
        }
    }
    
    /**
     * 清除除了指定state之外的所有state
     * 
     * @param exceptState 除这个state之外的所有state将会被清除
     */
    protected final void clearStates(ActivityState exceptState) {
        if (getStateCount() == 0) {
            return;
        }
        
        if (DEBUG) {
            Log.i(TAG, "StateManager#clearStates(exceptState) begin =========== ");
        }
        
        // 不需要动画效果
        mExitAnim = 0;
        mEnterAnim = 0;
        StateEntry exceptEntry = null;
        
        while (!mStack.isEmpty()) {
            StateEntry topEntry = mStack.peek();
            if (null == exceptEntry && topEntry.activityState == exceptState) {
                exceptEntry = topEntry;
                if (DEBUG) {
                    Log.i(TAG, "    StateManager#clearStates,  Pop the except state, do NOT call state's leftcycle");
                }
                mStack.pop();
            } else {
                if (DEBUG) {
                    Log.i(TAG, "    StateManager#clearStates,  Pop state out stack, state = " + topEntry.activityState);
                }
                popState(topEntry.activityState);
            }
        }
        
        if (null != exceptEntry) {
            if (DEBUG) {
                Log.i(TAG, "    StateManager#clearStates,  Push the except state ======");
            }
            mStack.push(exceptEntry);
        }
        
        printStack();
        
        if (DEBUG) {
            Log.i(TAG, "StateManager#clearStates(exceptState) end =========== ");
        }
    }
    
    /**
     * 处理launch mode
     * 
     * @param klass klass
     * @param data data
     * @return true表示已经处理，false表示未处理
     */
    private boolean handleLaunchMode(Class<? extends ActivityState> klass, Bundle data) {
        boolean hasState = hasStateClass(klass);
        if (!hasState) {
            return false;
        }
        
        if (DEBUG) {
            Log.d(TAG, "StateManager#handleLaunchMode, klass = " + klass.getCanonicalName());
        }
        
        ActivityState top = getTopState();
        // Handle the SINGLE TOP mode
        if (null != top && klass.isInstance(top)) {
            // If the launch mode of top state is LAUNCH_SINGLE_TOP, we only call onNewIntent method 
            // instead of starting an new instance.
            int launchMode = top.getLaunchMode();
            if (ActivityInfo.LAUNCH_SINGLE_TOP == launchMode
                    || ActivityInfo.LAUNCH_SINGLE_TASK == launchMode) {
                top.onNewIntent(top.getIntent());
                top.resume();
                if (DEBUG) {
                    Log.i(TAG, "The top state is LAUNCH_SINGLE_TOP or LAUNCH_SINGLE_TASK, state = " + top);
                }
                printStack();
                return true;
            }
        }
        
        // Handle the SINGLE TASK mode
        StateEntry entry = getStateEntry(klass);
        if (null != entry) {
            ActivityState newTopState = entry.activityState;
            int launchMode = newTopState.getLaunchMode();
            if (ActivityInfo.LAUNCH_SINGLE_TASK == launchMode) {
                newTopState.onNewIntent(newTopState.getIntent());
                newTopState.resume();
                
                if (DEBUG) {
                    Log.i(TAG, "The state launch mode is LAUNCH_SINGLE_TASK, state = " 
                            + newTopState + ", pop these states out stack.");
                }
                
                while (!mStack.isEmpty()) {
                    StateEntry topEntry = mStack.peek();
                    if (topEntry == entry) {
                        break;
                    } else {
                        if (DEBUG) {
                            Log.i(TAG, "Pop state out stack, state = " + topEntry.activityState);
                        }
                        popState(topEntry.activityState);
                    }
                }
                
                printStack();
                
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 得到指定类名的页面数据
     * 
     * @param klass klass
     * @return StateEntry对象，可能为null
     */
    private StateEntry getStateEntry(Class<? extends ActivityState> klass) {
        for (StateEntry entry : mStack) {
            if (klass.isInstance(entry.activityState)) {
                return entry;
            }
        }
        return null;
    }
    
    /**
     * 指定的页面弹出栈
     * 
     * @param state state
     */
    private void popState(ActivityState state) {
        if (DEBUG) {
            Log.v(TAG, "StateManager#popState(), state " + state);
        }
        
        if (state != mStack.peek().activityState) {
            if (state.isDestroyed()) {
                if (DEBUG) {
                    Log.d(TAG, "StateManager#finishState(), The state is already destroyed");
                }
                return;
            } else {
                throw new IllegalArgumentException("The stateview to be finished"
                        + " is not at the top of the stack: " + state + ", "
                        + mStack.peek().activityState);
            }
        }

        // Remove the top state.
        mStack.pop();
        if (mIsResumed) {
            state.pause();
        }
        
        state.onDestroy();
        
        // Pop state
        popStateView(state, getTopState(), true);
    }
    
    /**
     * 添加页面的view
     * 
     * @param exitState exitState
     * @param enterState enterState
     */
    private void pushStateView(ActivityState exitState, ActivityState enterState) {
        if (DEBUG) {
            Log.d(TAG, "StateManager#pushStateView  exitState = " + exitState + ",  enterState = " + enterState);
        }
        
        final ViewGroup container = getRootContainer();
        // create view
        final View enterView = enterState.createView(container, enterState.getData());
        final View exitView = (null != exitState) ? exitState.getRootView() : null;
        // state created
        enterState.onStateCreated(null);
        
        final int width = ViewGroup.LayoutParams.MATCH_PARENT;
        final int height = ViewGroup.LayoutParams.MATCH_PARENT;
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);

        if (DEBUG) {
            Log.d(TAG, "StateManager#pushStateView  add view ===== mExitAnim = " 
                    + mExitAnim + ",  mEnterAnim = " + mEnterAnim);
        }
        
        // 先从其Parent上删除
        if (enterView.getParent() != null) {
            ((ViewGroup) enterView.getParent()).removeView(enterView);
        }
        
        // Add view
        container.addView(enterView, params);
        performTransition(new Runnable() {
            @Override
            public void run() {
                if (0 != mExitAnim || 0 != mEnterAnim) { 
                    mExitAnimView.bringToFront();
                    mEnterAnimView.bringToFront();
                    mEnterAnimView.setVisibility(View.GONE);
                    mExitAnimView.setVisibility(View.GONE);
                }
                boolean enterAnim = doAnimation(enterView, mEnterAnim, mEnterAnimView);
                boolean exitAnim =  doAnimation(exitView, mExitAnim, mExitAnimView);
                mExitAnim = 0;
                mEnterAnim = 0;
                // 检查动画结束，防止flag不会被重置
                scheduleTransitionAnimEnd(exitAnim || enterAnim, null);
            }
        }, false);
    }
    
    /**
     * 删除退出页面的view
     * 
     * @param exitState exitState
     * @param enterState enterState
     * @param doPost 是否post执行操作
     */
    private void popStateView(ActivityState exitState, ActivityState enterState, boolean doPost) {
        if (DEBUG) {
            Log.d(TAG, "StateManager#popStateView  exitState = " + exitState + ",  enterState = " + enterState);
        }
        
        final ViewGroup container = getRootContainer();
        final View exitView = exitState.getRootView();
        final View enterView = (null != enterState) ? enterState.getRootView() : null;
        
        // No animation, post execute.
        boolean post = (0 == mExitAnim && 0 == mEnterAnim) && doPost;
        
        if (DEBUG) {
            Log.d(TAG, "    StateManager#popStateView   performTransition  post = " + post);
        }
        
        performTransition(new Runnable() {
            @Override
            public void run() {
                if (0 != mExitAnim || 0 != mEnterAnim) { 
                    mEnterAnimView.bringToFront();
                    mExitAnimView.bringToFront();
                    mEnterAnimView.setVisibility(View.GONE);
                    mExitAnimView.setVisibility(View.GONE);
                }
                
                // 移除View的监听器
                RemoveViewListener listener = new RemoveViewListener(exitView);
                boolean exitAnim = doAnimation(exitView, mExitAnim, mExitAnimView, listener);
                boolean enterAnim = doAnimation(enterView, mEnterAnim, mEnterAnimView, listener);
                // FIX-BUG:
                // 在Android 5.0上，如果WebView在被remove后，再调用draw()方法的话，会导致底层crash，这个问题目前不清楚
                // Android是故意为之还是其bug，因此，我们在scheduleTransitionAnimEnd方法中来执行删除View的操作。如果做了
                // 动画，就等待动画结束之后再删除，否则立即删除。
                //container.removeView(exitView);
                
                // 如果没作动画，直接删除该View，否则只是将View隐藏，在动画结束之后再删除
                if (!exitAnim && !enterAnim) {
                    container.removeView(exitView);
                } else {
                    // 隐藏View，该View会在动画结束后删除
                    exitView.setVisibility(View.INVISIBLE);
                }
                
                mExitAnim = 0;
                mEnterAnim = 0;
                // 检查动画结束，防止flag不会被重置
                scheduleTransitionAnimEnd(exitAnim || enterAnim, null);
            }
        }, post);
    }
    
    /**
     * 执行动画
     * 
     * @param view view
     * @param animId animId
     * @param fakeView fakeView
     * 
     * @return true：开启动画，false：未开启动画
     */
    private boolean doAnimation(final View view, int animId, final AnimateView fakeView) {
        return doAnimation(view, animId, fakeView, null);
    }
    
    /**
     * 执行动画
     * 
     * @param view view
     * @param animId animId
     * @param fakeView fakeView
     * @param listener listener
     * 
     * @return true：开启动画，false：未开启动画
     */
    private boolean doAnimation(final View view, int animId, final AnimateView fakeView, 
            final AnimationListener listener) {
        if (DEBUG) {
            Log.d(TAG, "StateManager#doAnimation, animId = " + animId + ", view = " + view);
        }
        
        if (0 == animId || null == view) {
            return false;
        }
        
        if (DEBUG) {
            Log.d(TAG, "StateManager#doAnimation, do the state transition animation.");
        }
        
        final int oldVisibility = view.getVisibility();
        Animation animation = AnimationUtils.loadAnimation(mActivityContext.getAndroidContext(), animId);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                if (DEBUG) {
                    int curVisibility = view.getVisibility();
                    boolean visible = (oldVisibility == View.VISIBLE);
                    Log.e(TAG, "    StateManager#doAnimation,  onAnimationEnd()  "
                            + ", set view visibility,  visible = " + visible
                            + ", current visibility = " + curVisibility 
                            + ", old visibility = " + oldVisibility + ",   view = " + view);
                }
                
                // 这里可能引起严重的bug，如果退出动画未做完，又再次启动这个页面，如果启动页面执行完成后，再执行onAnimationEnd
                // 的话，那么可能会把这个View再次隐藏，永远无法显示出来。
                //view.setVisibility(oldVisibility);
                fakeView.setVisibility(View.GONE);
                fakeView.setDrawingView(null);
                mTransitionAnimEnd = true;
                
                // Post执行，这里可能会导致一个BUG，如果在listener.onAnimationEnd()中删除要移动的View，将会导致一个
                // crash，onAnimationEnd是从View的绘制流程中回调出来，如果在onAnimationEnd中删除View，相当于在draw()中删除
                // View，那么整体绘制流程就会有问题，因此，我们这里Post执行。
                if (null != listener) {
                    final Animation anim = animation;
                    post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onAnimationEnd(anim);
                        }
                    });
                }
            }
        });
        
        long duration = animation.getDuration();
        // 取到最大的duration
        if (duration >= mTransitionAnimDuration) {
            mTransitionAnimDuration = duration;
        }
        mTransitionAnimEnd = false;
        //view.startAnimation(animation);
        
        fakeView.setVisibility(View.VISIBLE);
        fakeView.setDrawingView(view);
        fakeView.startAnimation(animation);
        
        return true;
    }
    
    /**
     * 标识当前是否在做页面切换动画
     * 
     * @return true表示动画结束，false表示未结束
     */
    private boolean isTransitionAnimEnd() {
        // 如果栈为空的话，则认为动画已经结束
        if (getStateCount() == 0) {
            mTransitionAnimEnd = true;
        }
        
        return mTransitionAnimEnd;
    }
    
    /**
     * 检查页面切换动画是否结束，在某些时候动画的结束的listener不会调用，为了防止 {@link #mTransitionAnimEnd}不会被设置为
     * true，从而再作一次保险作操。
     * 
     * @param doAnim 是否作动画
     * @param action 执行的action
     */
    private void scheduleTransitionAnimEnd(boolean doAnim, final Runnable action) {
        // 如果做了动画，需要post执行行并延迟动画效果的时间
        if (doAnim) {
            getRootContainer().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTransitionAnimEnd = true;
                    mTransitionAnimDuration = 0;
                    if (null != action) {
                        action.run();
                    }
                }
            }, mTransitionAnimDuration);
        } else {
            mTransitionAnimEnd = true;
            mTransitionAnimDuration = 0;
            if (null != action) {
                action.run();
            }
        }
    }
    
    /**
     * 得到栈中页面的容器
     * 
     * @return 页面的容器
     */
    private FrameLayout getRootContainer() {
        return mStateContainer.getStateContainer();
    }
    
    /**
     * 执行过渡动画效果
     * 
     * @param action action
     * @param post 是否post执行
     */
    private void performTransition(Runnable action, boolean post) {
        if (post) {
            getRootContainer().post(action);
        } else {
            action.run();
        }
    }
    
    /**
     * Post to do an action
     * 
     * @param action action
     */
    private void post(Runnable action) {
        getRootContainer().post(action);
    }
    
    /**
     * 打印当前栈信息
     */
    private void printStack() {
        if (DEBUG) {
            Log.e(TAG, "===== Print state stack begin =====");
            Log.e(TAG, "      StateManager = " + this);
            
            int size = mStack.size();
            for (int i = size - 1; i >= 0; --i) {
                Log.i(TAG, "      state #" + (i + 1) + " : " + mStack.get(i).activityState);
            }
            
            Log.e(TAG, "===== Print state stack end =====");
        }
    }
    
    /**
     * 页面数据结构封装
     * 
     * @author lihong06
     * @since 2014-7-6
     */
    private static class StateEntry {
        /** 页面的数据 */
        public final Bundle data;
        /** 页面 */
        public final ActivityState activityState;

        /**
         * 构造方法
         * 
         * @param bundle bundle
         * @param state state
         */
        public StateEntry(Bundle bundle, ActivityState state) {
            this.data = bundle;
            this.activityState = state;
        }
    }
    
    /**
     * 动画实现
     * 
     * @author lihong06
     * @since 2014-11-18
     */
    private static class RemoveViewListener implements AnimationListener {
        /** 将要移除的View */
        private View mRemovedView;
        
        /**
         * 构造方法
         * 
         * @param removedView removedView
         */
        public RemoveViewListener(View removedView) {
            mRemovedView = removedView;
        }
        
        @Override
        public void onAnimationStart(Animation animation) {
            
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (null != mRemovedView) {
                ViewParent parent = mRemovedView.getParent();
                if (parent instanceof ViewGroup) {
                    if (DEBUG) {
                        Log.d(TAG, "    StateManager#popStateView   remove exit view  view = " + mRemovedView);
                    }
                    ((ViewGroup) parent).removeView(mRemovedView);
                }
            }
            
            mRemovedView = null;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            
        }
    }
}
