package com.wzhnsc.testviewdraghelper;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class CustomViewGroup2 extends LinearLayout {
    // build.gradle 里 dependencies 中
    // 增加 compile 'com.android.support:appcompat-v7:版本号与compileSdkVersion对应.+'
    // 官方的DrawerLayout就是用此类实现的
    private ViewDragHelper mDragger;

    private View mHeaderView;

    public CustomViewGroup2(Context context) {
        super(context);

        init();
    }

    public CustomViewGroup2(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CustomViewGroup2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        // 第一步：创建实例
        mDragger = ViewDragHelper.create(this, // 当前的ViewGroup实例
                                         // 主要用于设置mTouchSlop
                                         // helper.mTouchSlop = (int)(helper.mTouchSlop * (1 / sensitivity));
                                         // 可见传入越大，mTouchSlop的值就会越小
                                         1.0f,
                                         // 第二步：编写ViewDragHelper.Callback实例
                                         new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                // 返回ture则表示可以捕获该view，根据传入的第一个参数child决定是否捕获
                return child == mHeaderView;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                final int leftBound  = getPaddingLeft();
                final int rightBound = getWidth() - mHeaderView.getWidth();

                // 对child移动的左边界（X轴）进行控制 - 此例为在ViewGroup的内部移动
                return Math.min(Math.max(left, leftBound), rightBound);
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                final int topBound    = getPaddingTop();
                final int bottomBound = getHeight() - mHeaderView.getHeight() - mHeaderView.getPaddingBottom();

                // 对child移动的上边界（Y轴）进行控制 - 此例为在ViewGroup的内部移动
                return Math.min(Math.max(top, topBound), bottomBound);
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        Log.d("TestNdk", "onFinishInflate");

        mHeaderView = getChildAt(0);
    }

    // 第三步：在 onInterceptTouchEvent 和 onTouchEvent 里接管触摸方法
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //return super.onInterceptTouchEvent(ev);
        return mDragger.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //return super.onTouchEvent(event);
        mDragger.processTouchEvent(event);
        return true;
    }
}
