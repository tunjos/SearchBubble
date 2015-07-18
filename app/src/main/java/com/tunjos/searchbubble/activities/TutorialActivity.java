package com.tunjos.searchbubble.activities;

import android.graphics.Color;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.fragments.SlideFragment;

public class TutorialActivity extends AppIntro {

    @Override
    public void init(Bundle savedInstanceState) {

        addSlide(SlideFragment.newInstance(R.layout.fragment_slide1));
        addSlide(SlideFragment.newInstance(R.layout.fragment_slide2));
        addSlide(SlideFragment.newInstance(R.layout.fragment_slide3));
        addSlide(SlideFragment.newInstance(R.layout.fragment_slide4));

        setBarColor(Color.parseColor("#3F51B5"));
        setSeparatorColor(Color.parseColor("#2196F3"));

        showSkipButton(false);
    }

    @Override
    public void onSkipPressed() {
    }

    @Override
    public void onDonePressed() {
        finish();
    }
}