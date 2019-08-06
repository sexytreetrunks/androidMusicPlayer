package com.proto.musicplayerproto1.ui.coverflow;

import android.support.v4.view.ViewPager;
import android.view.View;

public class CoverTransformer implements ViewPager.PageTransformer {
    public static final float SCALE_MIN = 0.3f;
    public static final float SCALE_MAX = 1f;
    public static float scale  = 0f;

    public static float pagerMargin = 0f;
    public static float spaceValue = 0f;
    public static float pivotXOffset = 0f;

    public CoverTransformer(float scale, float pagerMargin, float spaceValue, float pivotXOffset) {
        this.scale = scale;
        this.pagerMargin = pagerMargin;
        this.spaceValue  = spaceValue;
        this.pivotXOffset = pivotXOffset;
    }

    @Override
    public void transformPage(View page, float position) {

        if (scale != 0f) {
            //position 값이 아닌 center냐 아니냐에 따라서 scale값 다르게 주기
            float pos =0;//center와 가까이에있는 놈만 변화가 일어나야함
            if(position < 1 && position > -1)
                pos = position;
            else
                pos = (position > 0)? 1:-1;
            float realScale = Utils.getFloat(1 - Math.abs(pos * scale),SCALE_MIN,SCALE_MAX);
            page.setScaleX(realScale);
            page.setScaleY(realScale);
        }

        if (pagerMargin != 0) {

            float realPagerMargin = position * (pagerMargin);
            float realSpaceValue  = spaceValue;

            if (spaceValue != 0) {
                realSpaceValue = Math.abs(position * spaceValue); //Utils.getFloat(Math.abs(position * spaceValue),MARGIN_MIN,MARGIN_MAX);
                realPagerMargin += (position > 0) ? realSpaceValue : - realSpaceValue;
            }
            page.setTranslationX(realPagerMargin);
        }

        if(position <1 && position > -1)
            page.setElevation(8.0f);
        else
            page.setElevation(0.0f);
    }
}
