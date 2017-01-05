package com.wzhnsc.testviewdraghelper;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class CustomViewGroup extends LinearLayout {
    private ViewDragHelper mDragger;

    private View mHeaderView;

    private Point mAutoBackOriginPos = new Point();

    public CustomViewGroup(Context context) {
        super(context);

        init();
    }

    public CustomViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CustomViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mDragger = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            // onViewDragStateChanged 当ViewDragHelper状态发生变化时回调（STATE_IDLE，STATE_DRAGGING，STATE_SETTLING）
            // onViewPositionChanged  当捕获的视图的位置发生改变时回调
            // onViewCaptured         当要捕获视图被捕获时回调(ViewDragHelper::captureChildView方法里就有回调)
            // onEdgeTouched          当触摸到边界时回调
            // onEdgeLock             当返回true的时候会锁住边界，false则离开解锁
            // getOrderedChildIndex   改变Z轴上按索引查找视图对应关系

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == mHeaderView;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                return top;
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                //super.onViewReleased(releasedChild, xvel, yvel);

                if (releasedChild == mHeaderView) {
                    // 调用settleCapturedViewAt回到指定的位置
                    mDragger.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y);
                    // 紧随其后的代码是invalidate函数
                    // 因为其内部使用的是mScroller.startScroll函数
                    // 所以别忘了需要使用View::computeScroll方法
                    invalidate();
                }
            }


            // 让子视图可以消耗事件，如：
            // TextView 全部加上 android:clickable="true" 或子视图是 Button
            // 会发现本来可以拖动的子视图拖不动了!
            // 主要是因为：
            // 如果子视图不消耗事件，
            // 那么整个手势（按下->移动->抬起）都是直接进入onTouchEvent方法，
            // 在onTouchEvent方法的按下手势的时候就确定了要捕获的视图。
            //
            // 如果子视图消耗事件，
            // 那么就会先走onInterceptTouchEvent方法，
            // 判断是否可以捕获，而在判断的过程中会去判断如下两个回调的方法，
            // 只有这两个方法返回大于零(0)的值才能正常的捕获！
            // 注：如果只需要一个方向移动，可以只重写一个！
            @Override
            public int getViewHorizontalDragRange(View child) {
                //return super.getViewHorizontalDragRange(child);
                return getMeasuredWidth() - child.getMeasuredWidth();
            }

            @Override
            public int getViewVerticalDragRange(View child) {
                //return super.getViewVerticalDragRange(child);
                return getMeasuredHeight() - child.getMeasuredHeight();
            }

            // 在指定边界拖动时回调
            @Override
            public void onEdgeDragStarted(int edgeFlags, int pointerId) {
                //super.onEdgeDragStarted(edgeFlags, pointerId);

                // 主动通过captureChildView函数对子视图进行捕获
                // 该方法可以绕过tryCaptureView函数
                // 效果为：
                // 在左边界（setEdgeTrackingEnabled 函数指定ViewDragHelper.EDGE_LEFT）
                // 按住移动手指，则 mHeaderView 子视图跟着移动
                mDragger.captureChildView(mHeaderView, pointerId);
            }
        });

        // 注意：如果需要使用边界检测需要添加上如下这句！
        mDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
    }

    @Override
    public void computeScroll() {
        //super.computeScroll();

        // ViewDragHelper::settleCapturedViewAt函数实现动画移动到指定位置
        // 就需要在 View::computeScroll 方法中加上如下处理代码
        if (mDragger.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        Log.d("TestNdk", "onLayout");

        // 获取子视图原始位置坐标
        mAutoBackOriginPos.x = mHeaderView.getLeft();
        mAutoBackOriginPos.y = mHeaderView.getTop();
    }

    @Override
    protected void onFinishInflate() {
        Log.d("TestNdk", "onFinishInflate");

        mHeaderView = getChildAt(0);
    }

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
