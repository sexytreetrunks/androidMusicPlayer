package com.proto.musicplayerproto1.ui.carousel;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proto.musicplayerproto1.databinding.CoverItemBinding;

import java.util.List;

//circular인지 아닌지 선택할수있도록, 여기서 선택해서 적용할건지 아니면 애초에 외부에서 선택하고난뒤 선택에따라 다른 adapter 만들어서 적용할건지 고민필요
public class CarouselPagerAdapter extends PagerAdapter {
    private List<String> imgResourceList;

    public CarouselPagerAdapter() {}

    public CarouselPagerAdapter(List<String> imgResourceList) {
        this.imgResourceList = imgResourceList;
    }

    public void setImgResourceList(List<String> imgResourceList) {
        this.imgResourceList = imgResourceList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(getDataCount() == 0)
            return 0;
        else
            return Short.MAX_VALUE;
    }

    public int getDataCount() {
        return imgResourceList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        //여기에 바인딩생성해서 바인딩할 데이터 set하면 됨
        CoverItemBinding binding = CoverItemBinding.inflate(LayoutInflater.from(container.getContext()), container, false);
        int realPosition = position % getDataCount();
        String uri = imgResourceList.get(realPosition);
        binding.setCoverUri(uri);
        View coverItem = binding.getRoot();
        container.addView(coverItem);
        return coverItem;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view==((View)o);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        //container.removeViewAt(position);
        container.removeView((View)object);
    }


}
