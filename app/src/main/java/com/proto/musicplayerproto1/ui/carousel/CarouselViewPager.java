package com.proto.musicplayerproto1.ui.carousel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class CarouselViewPager extends ViewPager {

    public CarouselViewPager(@NonNull Context context) {
        super(context);
    }

    public CarouselViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        super.setAdapter(adapter);
        setCurrentItem(0);
    }

    public void setCurrentItemByDataPosition(int dataPosition) {
        setCurrentItemByDataPosition(dataPosition, true);
    }

    public void setCurrentItemByDataPosition(int dataPosition, boolean scrollBy) {
        //현재 position에서 가까운 destPosition으로 이동
        int base = getBaseAmount(getCurrentItem());
        int offset = dataPosition;
        setCurrentItem(base+offset);
    }

    public int getCurrentDataPosition() {
        int position = super.getCurrentItem();
        int count = ((CarouselPagerAdapter)getAdapter()).getDataCount();
        return position % count;
    }

    public int getBaseAmount(int position) {
        int count = ((CarouselPagerAdapter)getAdapter()).getDataCount();
        if(position==0) {
            int q = (Short.MAX_VALUE / count) / 2;
            return (count * q);
        } else {
            return position - (position % count);
        }
    }
}
