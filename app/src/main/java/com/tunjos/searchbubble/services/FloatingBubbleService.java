package com.tunjos.searchbubble.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tunjos.searchbubble.MyApplication;
import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.adapters.ClipListAdapter;
import com.tunjos.searchbubble.adapters.RealmClipAdapter;
import com.tunjos.searchbubble.models.Clip;
import com.tunjos.searchbubble.models.MyConstants;
import com.tunjos.searchbubble.models.MyPreferenceManager;
import com.tunjos.searchbubble.others.MyUtils;

import java.util.Date;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.tunjos.searchbubble.others.MyUtils.getClipType;

public class FloatingBubbleService extends Service implements ClipListAdapter.OnItemClickListener {
@Inject MyPreferenceManager myPreferenceManager;

    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private ViewGroup viewGroup;
    private static int MOVE_TOLERANCE;

    private boolean isLongClickable = true;
    private boolean isShortClickable = true;

    @InjectView(R.id.rvClipList) RecyclerView rvClipList;
    @InjectView(R.id.edtxFilter) EditText edtxFilter;
    @InjectView(R.id.imgvSearchBubble) ImageView imgvSearchBubble;

    private RecyclerView.LayoutManager layoutManager;
    private ClipListAdapter clipListAdapter;
    private RealmClipAdapter realmClipAdapter;

    private Realm realm;
    private RealmResults<Clip> clips;

    private TextView.OnEditorActionListener onEditorActionListener;
    private View.OnKeyListener onKeyListener;
    private TextWatcher textWatcher;

    public FloatingBubbleService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((MyApplication)getApplication()).getPersistenceComponent().inject(this);

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.floating_bubble, null, false);
        ButterKnife.inject(this, viewGroup);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);

        //WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        MOVE_TOLERANCE = MyUtils.convertDpToPixel(5.1f, this);

        rvClipList.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        rvClipList.setLayoutManager(layoutManager);

        clipListAdapter = new ClipListAdapter(this);
        rvClipList.setAdapter(clipListAdapter);

        realm = Realm.getInstance(getApplicationContext());

        setListeners();

        clips = getAllClips();

        realmClipAdapter = new RealmClipAdapter(getApplicationContext(), clips, true);
        clipListAdapter.setRealmAdapter(realmClipAdapter);
        clipListAdapter.notifyDataSetChanged();

        windowManager.addView(viewGroup, params);

    }

    private RealmResults<Clip> getAllClips() {
        RealmResults<Clip> clips = realm.where(Clip.class).findAllSorted(MyConstants.FIELD_CREATION_DATE, RealmResults.SORT_ORDER_DESCENDING);
        return clips;
    }

    private void setListeners() {
        clipListAdapter.setOnItemClickListener(this);

        imgvSearchBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShortClickable) {
                    Toast.makeText(FloatingBubbleService.this, "SHORTCLICK", Toast.LENGTH_SHORT).show();
                    switchBubbleView(true);
                }
            }
        });

        imgvSearchBubble.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isLongClickable) return false;

                isShortClickable = false;
                Toast.makeText(FloatingBubbleService.this, "LONGCLICK", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        imgvSearchBubble.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private int deltaX;
            private int deltaY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isLongClickable = true;
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        deltaX = (int) (event.getRawX() - initialTouchX);
                        deltaY = (int) (event.getRawY() - initialTouchY);
                        params.x = initialX + deltaX;
                        params.y = initialY + deltaY;
                        windowManager.updateViewLayout(viewGroup, params);
                        if (deltaX > MOVE_TOLERANCE || deltaY > MOVE_TOLERANCE) {
                            isLongClickable = false;
                            isShortClickable = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        deltaX = (int) (event.getRawX() - initialTouchX);
                        deltaY = (int) (event.getRawY() - initialTouchY);
                        if (deltaX < MOVE_TOLERANCE && deltaY < MOVE_TOLERANCE) {
                            isShortClickable = true;
                        }
                        break;
                }
                return false;
            }
        });

        onEditorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    Toast.makeText(getApplicationContext(),"SEARCG", Toast.LENGTH_SHORT).show();
                    String query =  edtxFilter.getText().toString();
                    myPreferenceManager.getBubbleViewPref();
                    performSearch(query, myPreferenceManager.getStoreSearchesPref());
                }
                return true;
            }
        };

        onKeyListener = new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_SEARCH)) {
                    // Perform action on key press
                    Toast.makeText(getApplicationContext(),"SEARCG", Toast.LENGTH_SHORT).show();
                    String query =  edtxFilter.getText().toString();
                    performSearch(query, myPreferenceManager.getStoreSearchesPref());
                    return true;
                }
                return false;
            }
        };

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        switchBubbleView(false);
    }

    private void filter(String query) {
        if (TextUtils.isEmpty(query)) {
            getAllClips();
        } else {
            clips = realm.where(Clip.class).contains(MyConstants.FIELD_TEXT, query, false).findAllSorted(MyConstants.FIELD_CREATION_DATE, RealmResults.SORT_ORDER_DESCENDING);
        }
        realmClipAdapter.updateRealmResults(clips);
        clipListAdapter.notifyDataSetChanged();
    }

    private void performSearch(String query, boolean storeInHistory) {
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

        if (storeInHistory) {
            storeSearchQuery(query);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);

        if (myPreferenceManager.getAutoClosePref()) {
            stopSelf();
        }
    }

    private void storeSearchQuery(String query) {
        Realm realm = Realm.getInstance(FloatingBubbleService.this);
        int nextId = (int) (realm.where(Clip.class).maximumInt(MyConstants.FIELD_ID) + 1);
        Date dateNow = new Date();
        int clipType = getClipType(query);

        realm.beginTransaction();

        Clip clip = realm.createObject(Clip.class);
        clip.setId(nextId);
        clip.setText(query);
        clip.setType(clipType);
        clip.setCreationDate(dateNow);

        realm.commitTransaction();
        realm.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onItemClick(View view, int position) {
        String query = clipListAdapter.getItem(position).getText();
        int clipType = clipListAdapter.getItem(position).getType();
        switch (clipType) {
            case MyConstants.CLIPTYPE_TEXT:
                performSearch(query, false);
                break;
            case MyConstants.CLIPTYPE_URL:
                break;
            case MyConstants.CLIPTYPE_NO:
                break;
        }
    }

    private void switchBubbleView(boolean checkPreferences) {
        int bubbleView;
        if (checkPreferences) {
            bubbleView = (myPreferenceManager.getBubbleViewPref() + 1) % 2;
        } else {
            bubbleView = myPreferenceManager.getBubbleViewPref();
        }

        switch (bubbleView) {
            case MyConstants.BUBBLE_VIEW_CLIPS:
                rvClipList.setVisibility(View.VISIBLE);
                edtxFilter.setHint(R.string.tx_filter);
                edtxFilter.setOnEditorActionListener(null);
                edtxFilter.setOnKeyListener(null);
                edtxFilter.addTextChangedListener(textWatcher);
                break;
            case MyConstants.BUBBLE_VIEW_SEARCH:
                rvClipList.setVisibility(View.GONE);
                edtxFilter.setHint(R.string.tx_search);
                edtxFilter.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
                edtxFilter.setOnEditorActionListener(onEditorActionListener);
                edtxFilter.setOnKeyListener(onKeyListener);
                edtxFilter.removeTextChangedListener(textWatcher);
                break;
        }

        if (checkPreferences) {
            myPreferenceManager.setBubbleViewPref(bubbleView);
        }
    }
}