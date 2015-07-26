package com.tunjos.searchbubble.others;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.tunjos.searchbubble.models.MyConstants;

/**
 * Created by tunjos on 17/07/2015.
 */
public final class PopupUtils {

    private PopupUtils() {
        // No instances
    }

    public static void performSearch(Context context, String query, String searchProvider) {
        Uri uri;
        switch (searchProvider) {
            case MyConstants.GOOGLE_PREF_VALUE:
                uri = Uri.parse(MyConstants.SEARCH_URL_GOOGLE + query);
                break;
            case MyConstants.BAIDU_PREF_VALUE:
                uri = Uri.parse(MyConstants.SEARCH_URL_BAIDU + query);
                break;
            case MyConstants.YAHOO_PREF_VALUE:
                uri = Uri.parse(MyConstants.SEARCH_URL_YAHOO + query);
                break;
            case MyConstants.DUCKDUCKGO_PREF_VALUE:
                uri = Uri.parse(MyConstants.SEARCH_URL_DUCKDUCKGO + query);
                break;
            case MyConstants.BING_PREF_VALUE:
                uri = Uri.parse(MyConstants.SEARCH_URL_BING + query);
                break;
            case MyConstants.ASK_PREF_VALUE:
                uri = Uri.parse(MyConstants.SEARCH_URL_ASK + query);
                break;
            case MyConstants.YANDEX_PREF_VALUE:
                uri = Uri.parse(MyConstants.SEARCH_URL_YANDEX + query);
                break;
            case MyConstants.WOLFRAMALPHA_PREF_VALUE:
                uri = Uri.parse(MyConstants.SEARCH_URL_WOLFRAMALPHA + query);
                break;
            default:
                uri = Uri.parse(MyConstants.SEARCH_URL_GOOGLE + query);
                break;
        }

    Intent searchIntent = new Intent(Intent.ACTION_VIEW, uri);
    searchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(searchIntent);
    }

    public static boolean perFormTranslate(Context context, String query) {
        Uri uri = new Uri.Builder()
                .scheme("http")
                .authority("translate.google.com")
                .path("/m/translate")
                .appendQueryParameter("q", query)
//                .appendQueryParameter("tl", "it") // target language
//                .appendQueryParameter("sl", "en") // source language
                .build();

        Intent translateIntent = new Intent(Intent.ACTION_VIEW);
        translateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        translateIntent.setPackage("com.google.android.apps.translate");
        translateIntent.setData(uri);

        if (isIntentStartable(context, translateIntent)) {
            context.startActivity(translateIntent);
            return true;
        }
        return false;
    }

    public static void performSms(Context context, String query) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.putExtra("sms_body"  , query);
        context.startActivity(smsIntent);
    }

    public static void performShareAction(Context context, String query) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_TEXT, query);
        shareIntent.setType("text/plain");
        context.startActivity(shareIntent);
    }

    public static void performCall(Context context, String query) {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + query));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callIntent);
    }

    public static boolean performLocate(Context context, String query) {
        Intent locateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(query));
        locateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isIntentStartable(context, locateIntent)) {
            context.startActivity(locateIntent);
            return true;
        }
        return false;
    }

    /**
     * Checks if there is an activity to handle the supplied {@link Intent}.
     */
    public static boolean isIntentStartable(Context context, Intent intent) {
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            return true;
        }
        return false;
    }
}