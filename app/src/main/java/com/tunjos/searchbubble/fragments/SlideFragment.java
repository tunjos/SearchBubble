package com.tunjos.searchbubble.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tunjos.searchbubble.models.MyConstants;


public class SlideFragment extends Fragment {
    private int slideLayoutId;

    public static SlideFragment newInstance(int slideLayoutId) {
        SlideFragment fragment = new SlideFragment();
        Bundle args = new Bundle();
        args.putInt(MyConstants.EXTRA_SLIDE_LAYOUT_ID, slideLayoutId);
        fragment.setArguments(args);
        return fragment;
    }
    public SlideFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null && getArguments().containsKey(MyConstants.EXTRA_SLIDE_LAYOUT_ID))
            slideLayoutId = getArguments().getInt(MyConstants.EXTRA_SLIDE_LAYOUT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return  inflater.inflate(slideLayoutId, container, false);
    }
}