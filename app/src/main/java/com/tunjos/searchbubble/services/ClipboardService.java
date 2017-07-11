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
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.tunjos.searchbubble.MyApplication;
import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.models.Clip;
import com.tunjos.searchbubble.models.MyConstants;
import com.tunjos.searchbubble.models.MyPreferenceManager;
import com.tunjos.searchbubble.others.IntentUtils;
import com.tunjos.searchbubble.others.MyUtils;
import com.tunjos.searchbubble.others.PopupUtils;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static com.tunjos.searchbubble.others.MyUtils.getClipType;

public class ClipboardService extends Service {
    @Inject MyPreferenceManager myPreferenceManager;
    private WindowManager windowManager;
    private WindowManager.LayoutParams llPopupBubblesParams;
    private LinearLayout llPopupBubbles;

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

        llPopupBubbles = (LinearLayout) layoutInflater.inflate(R.layout.floating_popup, null, false);
        ButterKnife.inject(this, llPopupBubbles);

        threadPool = Executors.newSingleThreadExecutor();

        setllPopupBubblesParams();
        getClipBoardService();
        getNotificationService();
        setListeners();
        setClipChangedListener();

        if (myPreferenceManager.getPinToNotificationPref()) {
            showPinnedNotification();
        }

        handler = new Handler();
    }

    private void setllPopupBubblesParams() {
        llPopupBubblesParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        llPopupBubblesParams.gravity = Gravity.BOTTOM;
        llPopupBubblesParams.y = MyUtils.convertDpToPixel(33.3f, this);
    }

    private void setListeners() {
        llPopupBubbles.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                removePopup();
                return false;
            }
        });
    }

    @OnClick({R.id.imgvSearch, R.id.imgvTranslate, R.id.imgvSms, R.id.imgvCall, R.id.imgvLocate, R.id.imgvShare, R.id.imgvLaunchBubble})
    public void onClickPopupBubble(View v) {
        String query = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        switch (v.getId()) {
            case R.id.imgvSearch:
                PopupUtils.performSearch(this, query, myPreferenceManager.getDefaultSearchPref());
                removePopup();
                break;
            case R.id.imgvTranslate:
                PopupUtils.perFormTranslate(this, query);
                removePopup();
                break;
            case R.id.imgvSms:
                PopupUtils.performSms(this, query);
                removePopup();
                break;
            case R.id.imgvCall:
                PopupUtils.performCall(this, query);
                removePopup();
                break;
            case R.id.imgvLocate:
                PopupUtils.performLocate(this, query);
                removePopup();
                break;
            case R.id.imgvShare:
                PopupUtils.performShareAction(this, query);
                removePopup();
                break;
            case R.id.imgvLaunchBubble:
                IntentUtils.startFloatingBubbleService(this);
                removePopup();
                break;
        }
    }

    private void showPopup() {
        if (windowManager != null) {
            try {
                windowManager.addView(llPopupBubbles, llPopupBubblesParams);
            } catch (Exception e) {
            }
        }
    }

    private synchronized void removePopup() {
        if (windowManager != null) {
            try {
                windowManager.removeView(llPopupBubbles);
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
                PendingIntent.getService(this, 100, floatingBubbleIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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

            RealmConfiguration config = new RealmConfiguration.Builder().build();
            Realm realm = Realm.getInstance(config);

            Number nextIdNum =  realm.where(Clip.class).max(MyConstants.FIELD_ID);
            int nextId =  nextIdNum != null ? nextIdNum.intValue() + 1 : 0;
            String text = item.getText().toString();
            Date dateNow = new Date();
            int clipType = getClipType(text);

            realm.beginTransaction();

            Clip clip = realm.createObject(Clip.class, nextId);
//            clip.setId(nextId);
            clip.setText(text);
            clip.setType(clipType);
            clip.setCreationDate(dateNow);

            realm.commitTransaction();
            realm.close();
        }
    }

    private void validatePopupBubbles() {
        Set<String> popupSearchPref = myPreferenceManager.getPopupSearchPref();

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_SEARCH_VALUE)) {
            llPopupBubbles.findViewById(R.id.imgvSearch).setVisibility(View.GONE);
        } else {
            llPopupBubbles.findViewById(R.id.imgvSearch).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_TRANSLATE_VALUE)) {
            llPopupBubbles.findViewById(R.id.imgvTranslate).setVisibility(View.GONE);
        } else {
            llPopupBubbles.findViewById(R.id.imgvTranslate).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_SMS_VALUE)) {
            llPopupBubbles.findViewById(R.id.imgvSms).setVisibility(View.GONE);
        } else {
            llPopupBubbles.findViewById(R.id.imgvSms).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_CALL_VALUE)) {
            llPopupBubbles.findViewById(R.id.imgvCall).setVisibility(View.GONE);
        } else {
            llPopupBubbles.findViewById(R.id.imgvCall).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_LOCATE_VALUE)) {
            llPopupBubbles.findViewById(R.id.imgvLocate).setVisibility(View.GONE);
        } else {
            llPopupBubbles.findViewById(R.id.imgvLocate).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_SHARE_VALUE)) {
            llPopupBubbles.findViewById(R.id.imgvShare).setVisibility(View.GONE);
        } else {
            llPopupBubbles.findViewById(R.id.imgvShare).setVisibility(View.VISIBLE);
        }

        if (!popupSearchPref.contains(MyConstants.PREF_POPUP_LAUNCHBUBBLE_VALUE)) {
            llPopupBubbles.findViewById(R.id.imgvLaunchBubble).setVisibility(View.GONE);
        } else {
            llPopupBubbles.findViewById(R.id.imgvLaunchBubble).setVisibility(View.VISIBLE);
        }
    }
}