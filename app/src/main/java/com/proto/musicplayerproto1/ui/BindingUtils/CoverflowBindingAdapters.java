package com.proto.musicplayerproto1.ui.BindingUtils;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;

import com.proto.musicplayerproto1.R;
import com.proto.musicplayerproto1.ui.carousel.CarouselViewPager;

public class CoverflowBindingAdapters {
    @BindingAdapter("app:srcUri")
    public static void setSourceUri(ImageView imageView, String uri) {
        if(uri == null || uri.equals(""))
            imageView.setImageResource(R.drawable.no_cover);
        else
            imageView.setImageURI(Uri.parse(uri));
    }

    @BindingAdapter("app:offscreenPageLimit")
    public static void setOffscreenPageLimit(ViewPager viewPager, int pageLimit) {
        viewPager.setOffscreenPageLimit(pageLimit);
    }

    @BindingAdapter("app:currentDataPosition")
    public static void setCurrentDataPosition(ViewPager viewPager, int position) {
        int oldPosition = ((CarouselViewPager)viewPager).getCurrentDataPosition();
        if(oldPosition != position || viewPager.getCurrentItem()==0)// prevent binding loop, viewPager.currentItem==0 is for initiating
            ((CarouselViewPager)viewPager).setCurrentItemByDataPosition(position);
    }

    @InverseBindingAdapter(attribute = "app:currentDataPosition", event = "app:onPageSelected")
    public static int getCurrentDataPosition(ViewPager viewPager) {
        return ((CarouselViewPager)viewPager).getCurrentDataPosition();
    }

    @BindingAdapter("app:onPageSelected")
    public static void setOnPagedSelected(ViewPager viewPager, final InverseBindingListener onPageSelected) {
        //viewPager.clearOnPageChangeListeners();
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) { }

            @Override
            public void onPageSelected(int i) {
                onPageSelected.onChange();
            }

            @Override
            public void onPageScrollStateChanged(int i) { }
        });
    }
}
