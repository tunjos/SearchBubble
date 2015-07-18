package com.tunjos.searchbubble.others;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;

import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.models.MyConstants;

/**
 * Created by tunjos on 24/06/2015.
 */
public class MyUtils {
    public static int convertDpToPixel(float dp, Context context){
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return (int)px;
    }

    public static int convertPixelsToDp(float px, Context context){
        Resources r = context.getResources();
        DisplayMetrics metrics = r.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return (int)dp;
    }

    public static int getClipType(String text) {
        if (Patterns.WEB_URL.matcher(text.toLowerCase()).matches()) {
            return MyConstants.CLIPTYPE_URL;
        } else if (Patterns.PHONE.matcher(text).matches()) {
            return MyConstants.CLIPTYPE_NO;
        } else {
            return MyConstants.CLIPTYPE_TEXT;
        }
    }

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();


        return toolbarHeight;
    }
}
