package com.rvsoft.safty.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Ravi on 11/26/2018.
 * Algante
 * ravikant.vishwakarma@algante.com
 */
public class ViewPagerStatic extends ViewPager {
    public ViewPagerStatic(@NonNull Context context) {
        super(context);
    }

    public ViewPagerStatic(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
