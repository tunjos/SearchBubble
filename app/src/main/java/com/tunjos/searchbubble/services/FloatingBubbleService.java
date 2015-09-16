package com.tunjos.searchbubble.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.tunjos.searchbubble.MyApplication;
import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.adapters.ClipListAdapter;
import com.tunjos.searchbubble.adapters.RealmClipAdapter;
import com.tunjos.searchbubble.models.Clip;
import com.tunjos.searchbubble.models.MyConstants;
import com.tunjos.searchbubble.models.MyPreferenceManager;
import com.tunjos.searchbubble.others.IntentUtils;
import com.tunjos.searchbubble.others.MyUtils;
import com.tunjos.searchbubble.others.PopupUtils;

import java.util.Date;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.tunjos.searchbubble.others.MyUtils.getClipType;

public class FloatingBubbleService extends Service implements ClipListAdapter.OnItemClickListener, ClipListAdapter.OnItemLongClickListener, View.OnClickListener {
    @Inject MyPreferenceManager myPreferenceManager;

    private WindowManager windowManager;
    private LinearLayout llPopupBubbles;
    private WindowManager.LayoutParams llPopupBubblesParams;
    private WindowManager.LayoutParams llSearchBubbleParams;
    private WindowManager.LayoutParams imgvCloseBubbleParams;
    private ViewGroup llSearchBubble;
    private ImageView imgvCloseBubble;

    private SpringSystem springSystem;
    private SpringConfig springConfig;

    private Spring searchBubbleSpringX;
    private Spring imgvCloseBubbleSpringY;
    private Spring imgvCloseBubbleSpringScale;

    private int MOVE_TOLERANCE, SPRING_CLAMP_THRESHOLD;
    private float DIM_AMOUNT = 0.8f;
    private double SPRING_TENSION = 400, SPRING_FRICTION = 40;
    private int imgvCloseBubbleWidth;

    private boolean backPressed = false;
    private boolean isShortClickable = true;
    private boolean isLongClickable = true;

    @Optional @InjectView(R.id.rvClipList) RecyclerView rvClipList;
    @Optional @InjectView(R.id.edtxFilter) EditText edtxFilter;

    @Optional @InjectView(R.id.imgvSearchBubble) ImageView imgvSearchBubble;
    private RecyclerView.LayoutManager layoutManager;
    private ClipListAdapter clipListAdapter;

    private RealmClipAdapter realmClipAdapter;
    private Realm realm;

    private RealmResults<Clip> clips;
    private TextView.OnEditorActionListener onEditorActionListener;
    private View.OnKeyListener onKeyListener;
    private TextWatcher textWatcher;

    private Handler handler;
    private static int POPUP_BUBBLE_DELAY = 5000;

    private String query;

    public FloatingBubbleService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((MyApplication) getApplication()).getPersistenceComponent().inject(this);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        llSearchBubble = (ViewGroup) layoutInflater.inflate(R.layout.floating_bubble, null, false);
        imgvCloseBubble = (ImageView) layoutInflater.inflate(R.layout.close_bubble, null, false);
        llPopupBubbles = (LinearLayout) layoutInflater.inflate(R.layout.floating_popup, null, false);


        ButterKnife.inject(this, llSearchBubble);
//        ButterKnife.inject(this, llPopupBubbles);


        llPopupBubblesParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        llPopupBubblesParams.gravity = Gravity.BOTTOM;
        llPopupBubblesParams.y = 100; //TODO set DIP version


        edtxFilter.getBackground().setColorFilter(getResources().getColor(R.color.sb_red), PorterDuff.Mode.SRC_ATOP);

        //Refactor to setLayour llPopupBubblesParams
        llSearchBubbleParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
                        WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT);
        llSearchBubbleParams.gravity = Gravity.TOP | Gravity.LEFT;
        llSearchBubbleParams.dimAmount = DIM_AMOUNT;

        imgvCloseBubbleParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        imgvCloseBubbleParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
        //TODO  getdisplay once
        Display display = windowManager.getDefaultDisplay();
        final int width;
        final int height;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
        } else {
            width = display.getWidth();
            height = display.getHeight();
        }

//        imgvCloseBubbleParams.x = (int) ((width -imgvCloseBubble.findViewById(R.id.imgvCloseBubble).getWidth()) / 2.4);
//        imgvCloseBubbleParams.y = ((int) windowManager.getDefaultDisplay().getHeight()*3) / 4;
        imgvCloseBubbleParams.y = 444;
        Log.d("DHEIGHT", "Y: " + windowManager.getDefaultDisplay().getHeight());
        Log.d("DHEIGHT", "Y3/4: " + imgvCloseBubbleParams.y);

//        imgvCloseBubbleParams.y = imgvCloseBubbleParams.y *2;

        imgvCloseBubble.post(new Runnable() {
            @Override
            public void run() {
                imgvCloseBubbleWidth = imgvCloseBubble.getWidth();
                Toast.makeText(FloatingBubbleService.this, "imgclose.width" + imgvCloseBubble.getWidth(), Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(FloatingBubbleService.this, "REALwidth" + imgvCloseBubbleWidth, Toast.LENGTH_SHORT).show();
//        imgvCloseBubbleParams.x = (int) ((width - imgvCloseBubbleWidth) / 2.4);


//        Toast.makeText(this, "imgclose.width"+imgvCloseBubble.getWidth(), Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "c.x"+imgvCloseBubbleParams.x, Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "c.y"+imgvCloseBubbleParams.y, Toast.LENGTH_SHORT).show();

        //Make Method, initSpringSystem
        springSystem = SpringSystem.create();
        springConfig = new SpringConfig(SPRING_TENSION, SPRING_FRICTION);
        imgvCloseBubbleSpringY = springSystem.createSpring();
        imgvCloseBubbleSpringY.setSpringConfig(springConfig);

        imgvCloseBubbleSpringScale = springSystem.createSpring();
        imgvCloseBubbleSpringScale.setSpringConfig(springConfig);

        searchBubbleSpringX = springSystem.createSpring();
        searchBubbleSpringX.setSpringConfig(springConfig);

        imgvCloseBubble.setVisibility(View.GONE);

        MOVE_TOLERANCE = MyUtils.convertDpToPixel(5.1f, this);
//        SPRING_CLAMP_THRESHOLD = MyUtils.convertDpToPixel(10.1f, this);
        SPRING_CLAMP_THRESHOLD = 50;

        rvClipList.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        rvClipList.setLayoutManager(layoutManager);

        clipListAdapter = new ClipListAdapter(this, true);
        rvClipList.setAdapter(clipListAdapter);

        realm = Realm.getInstance(getApplicationContext());

        setListeners();

        clips = getAllClips();

        realmClipAdapter = new RealmClipAdapter(getApplicationContext(), clips, true);
        clipListAdapter.setRealmAdapter(realmClipAdapter);
        clipListAdapter.notifyDataSetChanged();

        windowManager.addView(llSearchBubble, llSearchBubbleParams);
        windowManager.addView(imgvCloseBubble, imgvCloseBubbleParams);

//        imgvCloseBubbleParams.x = (int) ((width -imgvCloseBubble.getWidth()) / 2);
//        windowManager.updateViewLayout(imgvCloseBubble, imgvCloseBubbleParams);

        imgvCloseBubble.post(new Runnable() {
            @Override
            public void run() {
                imgvCloseBubbleParams.x = (int) ((width - imgvCloseBubble.getWidth()) / 2);
                windowManager.updateViewLayout(imgvCloseBubble, imgvCloseBubbleParams);
                Toast.makeText(FloatingBubbleService.this, "imgclose.width2222" + imgvCloseBubble.getWidth(), Toast.LENGTH_SHORT).show();
            }
        });

        handler = new Handler();
    }

    private RealmResults<Clip> getAllClips() {
        RealmResults<Clip> clips = realm.where(Clip.class).findAllSorted(MyConstants.FIELD_CREATION_DATE, RealmResults.SORT_ORDER_DESCENDING);
        return clips;
    }

    private void setListeners() {
        //Modularize Methods
        clipListAdapter.setOnItemClickListener(this);
        clipListAdapter.setOnItemLongClickListener(this);

        llPopupBubbles.findViewById(R.id.imgvSearch).setOnClickListener(this);
        llPopupBubbles.findViewById(R.id.imgvTranslate).setOnClickListener(this);
        llPopupBubbles.findViewById(R.id.imgvSms).setOnClickListener(this);
        llPopupBubbles.findViewById(R.id.imgvCall).setOnClickListener(this);
        llPopupBubbles.findViewById(R.id.imgvLocate).setOnClickListener(this);
        llPopupBubbles.findViewById(R.id.imgvShare).setOnClickListener(this);
        llPopupBubbles.findViewById(R.id.imgvLaunchBubble).setOnClickListener(this);

        llPopupBubbles.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                removePopup();
                return false;
            }
        });


        imgvSearchBubble.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isLongClickable) return false;
                isShortClickable = false;
                Toast.makeText(FloatingBubbleService.this, "LONGCLICK", Toast.LENGTH_SHORT).show();
                query =  realm.where(Clip.class).findAllSorted(MyConstants.FIELD_CREATION_DATE, RealmResults.SORT_ORDER_DESCENDING).first().getText();
                validatePopupBubbles();
                showPopup();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        removePopup();
                    }
                }, POPUP_BUBBLE_DELAY);
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
                        initialX = llSearchBubbleParams.x;
                        initialY = llSearchBubbleParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        deltaX = (int) (event.getRawX() - initialTouchX);
                        deltaY = (int) (event.getRawY() - initialTouchY);
                        llSearchBubbleParams.x = initialX + deltaX;
                        llSearchBubbleParams.y = initialY + deltaY;
                        windowManager.updateViewLayout(llSearchBubble, llSearchBubbleParams);

                        if (deltaX > MOVE_TOLERANCE || deltaY > MOVE_TOLERANCE) {
                            if (backPressed) {
                                imgvCloseBubbleSpringScale.setEndValue(1.0);
                                imgvCloseBubbleSpringY.setEndValue(1.0);
                                //TODO get some values once..like imgvCloseBubble.getWidth()  etc
                                if (/*llSearchBubbleParams.x >= (imgvCloseBubbleParams.x - SPRING_CLAMP_THRESHOLD) &&
                                        llSearchBubbleParams.x <= (imgvCloseBubbleParams.x + SPRING_CLAMP_THRESHOLD)
                                        &&*/ llSearchBubbleParams.y >= ((windowManager.getDefaultDisplay().getHeight()) -(444 +imgvCloseBubble.getHeight()+ SPRING_CLAMP_THRESHOLD)) &&
                                        llSearchBubbleParams.y <= ((windowManager.getDefaultDisplay().getHeight()) -(444 /*- imgvCloseBubble.getHeight()*/- (SPRING_CLAMP_THRESHOLD)))) {
                                    imgvCloseBubbleSpringScale.setEndValue(1.3);

                                    Log.d("CloseY", "NormalY: " + imgvCloseBubbleParams.y);
                                    Log.d("CloseY","NormalY: "+imgvCloseBubbleParams.y);
                                    Log.d("CloseY","NormalY: "+imgvCloseBubbleParams.y);
                                    Log.d("CloseY", "NormalY: " + imgvCloseBubbleParams.y);
                                    llSearchBubbleParams.x = imgvCloseBubbleParams.x +20;
//                                    llSearchBubbleParams.y = (windowManager.getDefaultDisplay().getHeight())- imgvCloseBubbleParams.y - 20;
                                    llSearchBubbleParams.y = (windowManager.getDefaultDisplay().getHeight())- 444 - imgvCloseBubble.getHeight() -50;

//                                    llSearchBubbleParams.y = 0;

                                    Log.d("CloseAB", "DAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                                    Log.d("CloseAB", "getHeight: " + windowManager.getDefaultDisplay().getHeight());
                                    Log.d("CloseAB", "imgvCloseBubbleParams.y: " + imgvCloseBubbleParams.y);
                                    Log.d("CloseAB", "llSearchBubbleParams.y: " + llSearchBubbleParams.y);



//                                    isClosed = true;
                                } else {
//                                    isClosed = false;
                                    imgvCloseBubbleSpringScale.setEndValue(1.0);
                                }
                                windowManager.updateViewLayout(llSearchBubble, llSearchBubbleParams);
                                windowManager.updateViewLayout(imgvCloseBubble, imgvCloseBubbleParams);
                            }

                            isLongClickable = false;
                            isShortClickable = false;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        deltaX = (int) (event.getRawX() - initialTouchX);
                        deltaY = (int) (event.getRawY() - initialTouchY);
                        if (backPressed) {
                            imgvCloseBubbleSpringY.setEndValue(0.0);
                        }
                        if (deltaX < MOVE_TOLERANCE && deltaY < MOVE_TOLERANCE) {
                            if (isShortClickable) {
                                //Short Click Performed
                                if (backPressed) {
                                    switchBubbleView(false);
                                } else {
                                    switchBubbleView(true);
                                }
                            }
                            isShortClickable = true;
                        }

                        if (/*llSearchBubbleParams.x >= (imgvCloseBubbleParams.x - SPRING_CLAMP_THRESHOLD) &&
                                        llSearchBubbleParams.x <= (imgvCloseBubbleParams.x + SPRING_CLAMP_THRESHOLD)
                                        &&*/ llSearchBubbleParams.y >= ((windowManager.getDefaultDisplay().getHeight()) -(444 +imgvCloseBubble.getHeight()+ SPRING_CLAMP_THRESHOLD)) &&
                                llSearchBubbleParams.y <= ((windowManager.getDefaultDisplay().getHeight()) -(444 /*- imgvCloseBubble.getHeight()*/- (SPRING_CLAMP_THRESHOLD)))) {

                            Toast.makeText(getApplicationContext(), "CLOSE", Toast.LENGTH_SHORT).show();
                            IntentUtils.stopFloatingBubbleService(getApplicationContext());
                        }
                        break;
                }
                return false;
            }
        });

        onEditorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH && keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    Toast.makeText(getApplicationContext(), "SEARCH BY KEYEVENT", Toast.LENGTH_SHORT).show();
                    String query = edtxFilter.getText().toString();
                    myPreferenceManager.getBubbleViewPref();
                    performSearch(query, myPreferenceManager.getStoreSearchesPref());
                    edtxFilter.setText("");

                    return true;
                }
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Toast.makeText(getApplicationContext(), "SEARCH BY NORMAL", Toast.LENGTH_SHORT).show();

                    String query = edtxFilter.getText().toString();
                    myPreferenceManager.getBubbleViewPref();
                    performSearch(query, myPreferenceManager.getStoreSearchesPref());
                    edtxFilter.setText("");

                    return true;
                }
                return false;
            }
        };

        onKeyListener = new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_SEARCH)) {
                    // Perform action on key press
                    Toast.makeText(getApplicationContext(), "SEARCH BY ONKEYLISTENER", Toast.LENGTH_SHORT).show();
                    String query = edtxFilter.getText().toString();
                    performSearch(query, myPreferenceManager.getStoreSearchesPref());
                    edtxFilter.setText("");

                    return true;
                }
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_BACK)) {
                    // Perform action on key press
                    backPressed = true;
                    Toast.makeText(getApplicationContext(), "BACKPRESSED", Toast.LENGTH_SHORT).show();
                    hideAllViews();
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

        edtxFilter.setOnEditorActionListener(onEditorActionListener);

        imgvCloseBubbleSpringY.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                float clampedValue = (float) SpringUtil.clamp(value, 0.0, 1.0);
                imgvCloseBubble.setAlpha(clampedValue);

                //TODO Get this once? yea
 /*               Point point = new Point();
                windowManager.getDefaultDisplay().getSize(point);
                int maxYPosition = point.y / 4;*/

                int maxYPosition = windowManager.getDefaultDisplay().getHeight();
                maxYPosition = maxYPosition /4;
//                maxYPosition = windowManager.getDefaultDisplay().getHeight() - (imgvSearchBubble.getHeight() * 2);
                imgvCloseBubbleParams.y = (int) (SpringUtil.mapValueFromRangeToRange(value, 0.0, 1.0, 0.0, maxYPosition));
                Log.d("CloseY","springY: "+imgvCloseBubbleParams.y);
                Log.d("CloseY","springY: "+imgvCloseBubbleParams.y);
                Log.d("CloseY","springY: "+imgvCloseBubbleParams.y);
                Log.d("CloseY","springY: "+imgvCloseBubbleParams.y);

                windowManager.updateViewLayout(imgvCloseBubble, imgvCloseBubbleParams);
            }

            @Override
            public void onSpringAtRest(Spring spring) {
                if (spring.getCurrentValue() == 0) {
                    imgvCloseBubble.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSpringActivate(Spring spring) {
                imgvCloseBubble.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSpringEndStateChange(Spring spring) {
            }
        });

        imgvCloseBubbleSpringScale.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                float clampedValue = (float) SpringUtil.clamp(value, 0.0, 1.0);//TODO RM
                imgvCloseBubble.setScaleX(value);
                imgvCloseBubble.setScaleY(value);
                windowManager.updateViewLayout(imgvCloseBubble, imgvCloseBubbleParams);
            }

            @Override
            public void onSpringAtRest(Spring spring) {

            }

            @Override
            public void onSpringActivate(Spring spring) {

            }

            @Override
            public void onSpringEndStateChange(Spring spring) {
            }
        });

        searchBubbleSpringX.addListener(new SpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {

            }

            @Override
            public void onSpringAtRest(Spring spring) {

            }

            @Override
            public void onSpringActivate(Spring spring) {

            }

            @Override
            public void onSpringEndStateChange(Spring spring) {
            }
        });

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

        PopupUtils.performSearch(this, query, myPreferenceManager.getDefaultSearchPref());

        if (storeInHistory) {
            storeSearchQuery(query);
        }

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
                performSearch(query, false);
                break;
            case MyConstants.CLIPTYPE_NO:
                performSearch(query, false);
                break;
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        query = clipListAdapter.getItem(position).getText();
        validatePopupBubbles();
        showPopup();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removePopup();
            }
        }, POPUP_BUBBLE_DELAY);
    }

    private void hideAllViews() {
        rvClipList.setVisibility(View.GONE);
        edtxFilter.setVisibility(View.GONE);
        llSearchBubbleParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
//        llSearchBubbleParams.x = (int) llSearchBubble.getX();
//        llSearchBubbleParams.x = (int) llSearchBubble.getY();

        llSearchBubble.invalidate();
        windowManager.updateViewLayout(llSearchBubble, llSearchBubbleParams);
        llSearchBubble.invalidate();
    }

    private void switchBubbleView(boolean checkPreferences) {
        int bubbleView;
        if (checkPreferences) {
            bubbleView = (myPreferenceManager.getBubbleViewPref() + 1) % 2;
        } else {
            bubbleView = myPreferenceManager.getBubbleViewPref();
        }

        llSearchBubbleParams.flags = WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        llSearchBubbleParams.dimAmount = DIM_AMOUNT;

        edtxFilter.setVisibility(View.VISIBLE);

        switch (bubbleView) {
            case MyConstants.BUBBLE_VIEW_CLIPS:
                rvClipList.setVisibility(View.VISIBLE);
                edtxFilter.setHint(R.string.tx_filter);
                edtxFilter.addTextChangedListener(textWatcher);
                edtxFilter.setImeOptions(EditorInfo.IME_ACTION_NONE);
                edtxFilter.requestFocus();
                break;
            case MyConstants.BUBBLE_VIEW_SEARCH:
                rvClipList.setVisibility(View.GONE);
                edtxFilter.setHint(R.string.tx_search);
                edtxFilter.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
                edtxFilter.setOnKeyListener(onKeyListener);
                edtxFilter.removeTextChangedListener(textWatcher);
                edtxFilter.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
                edtxFilter.requestFocus();
                break;
        }

        if (backPressed) {
            //Update llSearchBubbleParams to enable dimming
            windowManager.updateViewLayout(llSearchBubble, llSearchBubbleParams);
            backPressed = false;
        }

        if (checkPreferences) {
            myPreferenceManager.setBubbleViewPref(bubbleView);
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
    public void onClick(View v) {
//      onClickPopupBubble
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