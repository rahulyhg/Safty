package com.rvsoft.safty.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Ravi on 11/26/2018.
 * Algante
 * ravikant.vishwakarma@algante.com
 */
public class ViewPagerItemAdapter extends PagerAdapter {
    private Activity mActivity;
    private List<Integer> viewList;
    private List<String> titleList;
    public ViewPagerItemAdapter(Activity activity) {
        mActivity = activity;
        viewList = new ArrayList<>();
        titleList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return mActivity.findViewById(viewList.get(position));
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }

    public void addView(int viewId, String title){
        viewList.add(viewId);
        titleList.add(title);
        notifyDataSetChanged();
    }

    public void removeView(int viewId){
        titleList.remove(viewList.indexOf(viewId));
        viewList.remove(viewList.indexOf(viewId));
        notifyDataSetChanged();
    }
}
