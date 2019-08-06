package com.proto.musicplayerproto1.ui.coverflow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * PagerContainer: A layout that displays a ViewPager with its children that are outside
 * the typical pager bounds.
 * Origin from "devunwired"
 * @see(<a href = "https://gist.github.com/devunwired/8cbe094bb7a783e37ad1"></>)
 */
public class PagerContainer extends FrameLayout{

    private ViewPager mPager;
    boolean mNeedsRedraw = false;
    private long pressStartTime;
    private float startX, endX;
    private boolean stayedWithinClickDistance;
    private int prevPosition = 0;

    public PagerContainer(Context context) {
        super(context);
        init();
    }

    public PagerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PagerContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //Disable clipping of children so non-selected pages are visible
        setClipChildren(false);

        //Child clipping doesn't work with hardware acceleration in Android 3.x/4.x
        //You need to set this value here if using hardware acceleration in an
        // application targeted at these releases.
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onFinishInflate() {
        try {
            mPager = (ViewPager) getChildAt(0);
            mPager.addOnPageChangeListener(new PageItemChangeListner());
            prevPosition = mPager.getCurrentItem();
            //해당 pager에 translation적용
        } catch (Exception e) {
            throw new IllegalStateException("The root child of PagerContainer must be a ViewPager");
        }
    }

    public ViewPager getViewPager() {
        return mPager;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //We capture any touches not already handled by the ViewPager
        // to implement scrolling from a touch outside the pager bounds.

        int minPressDuration = 500;
        int minDistance = 10;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                pressStartTime = System.currentTimeMillis();
                stayedWithinClickDistance = true;
                startX = event.getX();
            }
            case MotionEvent.ACTION_MOVE: {
                if (stayedWithinClickDistance && Math.abs(event.getX() - startX) > Utils.convertDpToPixel(10, getContext())) {
                    stayedWithinClickDistance = false;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                float range = mPager.getWidth() / 2;
                float middle = (this.getLeft() + this.getRight()) / 2;
                long pressDuration = System.currentTimeMillis() - pressStartTime;
                if (pressDuration < minPressDuration && stayedWithinClickDistance) {
                    endX = event.getX();
                    float deltaX = endX - startX;
                    if(Math.abs(deltaX) < minDistance) {
                        int currentItem = mPager.getCurrentItem();
                        if(endX > middle + range)
                            mPager.setCurrentItem(currentItem + 1);
                        else if(endX < middle - range )
                            mPager.setCurrentItem(currentItem - 1);
                    }
                }
            }
        }
        return mPager.dispatchTouchEvent(event);
    }

    public int getSelectedPagePosition() {
        return mPager.getCurrentItem();
    }

    private class PageItemChangeListner implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //Force the container to redraw on scrolling.
            //Without this the outer pages render initially and then stay static
            if (mNeedsRedraw) invalidate();
            //invalidate();
        }

        @Override
        public void onPageSelected(int position) {
            int deltaPosition = position - prevPosition;
            Log.d("**PageChanged","delta: " + deltaPosition);
            prevPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mNeedsRedraw = (state != ViewPager.SCROLL_STATE_IDLE);
        }
    }

    public interface OnPageClickListener {
        public void onPageClick(int position);
    }
}