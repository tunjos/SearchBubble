package com.tunjos.searchbubble.activities;

import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.tunjos.searchbubble.R;

public class TutorialActivity extends AppIntro2 {

    @Override
    public void init(Bundle savedInstanceState) {

        int sbRedColor = getResources().getColor(R.color.sb_red);

        addSlide(AppIntroFragment.newInstance(getString(R.string.tx_slide_title_1), getString(R.string.tx_slide_description_1), R.drawable.ic_rate, sbRedColor));
        addSlide(AppIntroFragment.newInstance(getString(R.string.tx_slide_title_2), getString(R.string.tx_slide_description_2), R.drawable.ic_rate, sbRedColor));
        addSlide(AppIntroFragment.newInstance(getString(R.string.tx_slide_title_3), getString(R.string.tx_slide_description_3), R.drawable.ic_rate, sbRedColor));
        addSlide(AppIntroFragment.newInstance(getString(R.string.tx_slide_title_4), getString(R.string.tx_slide_description_4), R.drawable.ic_rate, sbRedColor));
        addSlide(AppIntroFragment.newInstance(getString(R.string.tx_slide_title_5), getString(R.string.tx_slide_description_5), R.drawable.ic_link, sbRedColor));
    }

    @Override
    public void onDonePressed() {
        finish();
    }
}