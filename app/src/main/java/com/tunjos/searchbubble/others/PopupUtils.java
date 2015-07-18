package com.tunjos.searchbubble.others;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.tunjos.searchbubble.models.MyConstants;

/**
 * Created by tunjos on 17/07/2015.
 */
public class PopupUtils {

    public void perFormSearch(Context ctx, String query, String searchProvider) {
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

    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    ctx.startActivity(intent);
    }

    public static void perFormTranslate(String query) {

    }

    public static void openUrl(String query) {

    }

    public static void perFormShareAction(String query) {

    }

    public static void perFormCall(Context ctx, String query) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + query));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    public static void perFormLocate(String query) {

    }

}