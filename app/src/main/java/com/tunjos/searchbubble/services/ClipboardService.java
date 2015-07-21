package com.tunjos.searchbubble.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.tunjos.searchbubble.MyApplication;
import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.models.Clip;
import com.tunjos.searchbubble.models.MyConstants;
import com.tunjos.searchbubble.models.MyPreferenceManager;
import com.tunjos.searchbubble.others.PopupUtils;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

import static com.tunjos.searchbubble.others.MyUtils.getClipType;

public class ClipboardService extends Service {
    @Inject MyPreferenceManager myPreferenceManager;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private ViewGroup viewGroup;

    private ClipboardManager clipboardManager;
    private OnPrimaryClipChangedListener onPrimaryClipChangedListener;
    private ExecutorService threadPool;

    private NotificationManager notificationManager;

    private Handler handler;
    private static int POPUP_BUBBLE_DELAY = 5000;

    public ClipboardService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((MyApplication)getApplication()).getPersistenceComponent().inject(this);

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

        viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.floating_popup, null, false);
        ButterKnife.inject(this, viewGroup);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.BOTTOM;
        params.y = 100; //TODO set DIP version


        threadPool = Executors.newSingleThreadExecutor();
        getClipBoardService();
        getNotificationService();
        setClipChangedListener();

        if (myPreferenceManager.getPinToNotificationPref()) {
            showPinnedNotification();
        }

        handler = new Handler();
    }

    @OnClick({R.id.imgvSearch, R.id.imgvTranslate, R.id.imgVSms, R.id.imgvCall, R.id.imgvLocate, R.id.imgvShare, R.id.imgvLaunchBubble})
    public void onClickPopupBubble(View v) {
        String query = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
        switch (v.getId()) {
            case R.id.imgvSearch:
                performSearch(query);
                removePopup();
                break;
            case R.id.imgvTranslate:
                perFormTranslate();
                removePopup();
                break;
            case R.id.imgVSms:
                removePopup();
                break;
            case R.id.imgvCall:
                PopupUtils.perFormCall(this, query);
                removePopup();
                break;
            case R.id.imgvLocate:
                removePopup();
                break;
            case R.id.imgvShare:
                removePopup();
                break;
            case R.id.imgvLaunchBubble:
                removePopup();
                break;
        }
    }

    private void showPopup() {
        Log.d("SB", "showpopup"); //REMOVE THIS CODES
        viewGroup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                removePopup();
                return false;
            }
        });
        if (windowManager != null) {
            try {
                windowManager.addView(viewGroup, params);
            } catch (Exception e) {
            }
        }
    }

    private synchronized void removePopup() {
        if (windowManager != null) {
            try {
                windowManager.removeView(viewGroup);
            } catch (Exception e) {
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getClipBoardService() {
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }

    private void getNotificationService() {
    notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    private void setClipChangedListener() {
        onPrimaryClipChangedListener = new OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (clipboardManager.hasPrimaryClip()) {
                    validatePopupBubbles();
                    showPopup();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            removePopup();
                        }
                    }, POPUP_BUBBLE_DELAY);
                    threadPool.execute(new SaveClipboardRunnable());
                }
            }
        };
        clipboardManager.addPrimaryClipChangedListener(onPrimaryClipChangedListener);
    }

    private void showPinnedNotification() {
        Intent floatingBubbleIntent = new Intent(this, FloatingBubbleService.class);
        PendingIntent pendingFloatingBubbleIntent =
                PendingIntent.getService(this, 100, floatingBubbleIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tx_tap_to_show_bubble))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .setColor(getResources().getColor(R.color.sb_red))
                .setContentIntent(pendingFloatingBubbleIntent)
                .setOngoing(true)
                .setAutoCancel(false);

            Notification notification = builder.build();
        notificationManager.notify(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancelAll();
        if (clipboardManager != null && onPrimaryClipChangedListener != null) {
            clipboardManager.removePrimaryClipChangedListener(onPrimaryClipChangedListener);
        }
    }

    private class SaveClipboardRunnable implements Runnable {

        @Override
        public void run() {
            ClipData clipData = clipboardManager.getPrimaryClip();
            ClipData.Item item = clipData.getItemAt(0);

            Realm realm = Realm.getInstance(getApplicationContext());

            int nextId = (int) (realm.where(Clip.class).maximumInt(MyConstants.FIELD_ID) + 1);
            String text = item.getText().toString();
            Date dateNow = new Date();
            int clipType = getClipType(text);

            realm.beginTransaction();

            Clip clip = realm.createObject(Clip.class);
            clip.setId(nextId);
            clip.setText(text);
            clip.setType(clipType);
            clip.setCreationDate(dateNow);

            realm.commitTransaction();
            realm.close();
        }
    }

    private void validatePopupBubbles() {
        Set<String> popupSearchPref= myPreferenceManager.getPopupSearchPref();

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_SEARCH_VALUE)) {
            viewGroup.findViewById(R.id.imgvSearch).setVisibility(View.GONE);
        } else {
            viewGroup.findViewById(R.id.imgvSearch).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_TRANSLATE_VALUE)) {
            viewGroup.findViewById(R.id.imgvTranslate).setVisibility(View.GONE);
        } else {
            viewGroup.findViewById(R.id.imgvTranslate).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_SMS_VALUE)) {
            viewGroup.findViewById(R.id.imgVSms).setVisibility(View.GONE);
        } else {
            viewGroup.findViewById(R.id.imgVSms).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_CALL_VALUE)) {
            viewGroup.findViewById(R.id.imgvCall).setVisibility(View.GONE);
        } else {
            viewGroup.findViewById(R.id.imgvCall).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_LOCATE_VALUE)) {
            viewGroup.findViewById(R.id.imgvLocate).setVisibility(View.GONE);
        } else {
            viewGroup.findViewById(R.id.imgvLocate).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_SHARE_VALUE)) {
            viewGroup.findViewById(R.id.imgvShare).setVisibility(View.GONE);
        } else {
            viewGroup.findViewById(R.id.imgvShare).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_LAUNCHBUBBLE_VALUE)) {
            viewGroup.findViewById(R.id.imgvLaunchBubble).setVisibility(View.GONE);
        } else {
            viewGroup.findViewById(R.id.imgvLaunchBubble).setVisibility(View.VISIBLE);
        }
    }

    private void startFloatingBubbleService() {
        Intent floatingBubbleServiceIntent = new Intent(getApplicationContext(), FloatingBubbleService.class);
        startService(floatingBubbleServiceIntent);
    }

    private void performSearch(String query) {
        if (query == null || query.equalsIgnoreCase("")) {
            return;
        }
        Uri uri;
        switch (myPreferenceManager.getDefaultSearchPref()) {
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
        this.startActivity(intent);
    }

    private void perFormTranslate() {

    }
}