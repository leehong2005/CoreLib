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

package com.lee.sdk.widget.viewpager;

import android.view.View;
import android.view.ViewGroup;

/**
 * 循环的ViewPager的Adapter
 * 
 * @author lihong06
 * @since 2014-10-8
 */
public abstract class CircularPagerAdapter extends PagerAdapterImpl {
    
    /** 循环次数 **/
    public static final int LOOPS_COUNT = 1000;
    
    /**
     * 实际count（转换后的count）
     * @return count
     */
    public abstract int getRealCount();
    
    @Override
    public final int getCount() {
        int count = getRealCount();
        if (1 == count) {
            return count;
        }
        return count * LOOPS_COUNT; // Integer.MAX_VALUE;
    }
    
    @Override
    public View getCurrentView(int position) {
        int pos = toRealPosition(position);
        return super.getCurrentView(pos);
    }
    
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        int pos = toRealPosition(position);
        super.destroyItem(container, pos, object);
    }
    
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int pos = toRealPosition(position);
        return super.instantiateItem(container, pos);
    }
    
    /**
     * 转换pos
     * @param position 隐藏实际的pos
     * @return 转换后的pos
     */
    public final int toRealPosition(int position) {
        int pos = 0;
        
        final int realCount = getRealCount();
        if (realCount <= 0) {
            pos = position;
        } else {
            pos = position % realCount;
        }
        
        return pos;
    }
}
