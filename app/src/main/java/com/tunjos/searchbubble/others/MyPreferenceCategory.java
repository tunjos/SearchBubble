package com.tunjos.searchbubble.others;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.tunjos.searchbubble.R;

/**
 * Created by tunjos on 17/07/2015.
 */
public class MyPreferenceCategory extends PreferenceCategory {
    private Context context;
    public MyPreferenceCategory(Context context) {
        super(context);
        this.context = context;
    }

    public MyPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MyPreferenceCategory(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        int sbRedColor = context.getResources().getColor(R.color.sb_red);
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(sbRedColor);
    }
}
