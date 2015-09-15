package com.tunjos.searchbubble.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.tunjos.searchbubble.MyApplication;
import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.adapters.ClipListAdapter;
import com.tunjos.searchbubble.adapters.RealmClipAdapter;
import com.tunjos.searchbubble.di.components.DaggerIabComponent;
import com.tunjos.searchbubble.di.components.IabComponent;
import com.tunjos.searchbubble.di.modules.IabModule;
import com.tunjos.searchbubble.fragments.ClipDialogFragment;
import com.tunjos.searchbubble.fragments.DonateDialogFragment;
import com.tunjos.searchbubble.fragments.RateDialogFragment;
import com.tunjos.searchbubble.models.Clip;
import com.tunjos.searchbubble.models.MyConstants;
import com.tunjos.searchbubble.models.MyPreferenceManager;
import com.tunjos.searchbubble.others.IabUtil;
import com.tunjos.searchbubble.others.iabutils.IabHelper;
import com.tunjos.searchbubble.others.iabutils.IabResult;
import com.tunjos.searchbubble.others.itemtouch.SwipeItemTouchHelperCallback;
import com.tunjos.searchbubble.services.ClipboardService;
import com.tunjos.searchbubble.services.FloatingBubbleService;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;


public class MainActivity extends AppCompatActivity implements ClipListAdapter.OnItemClickListener, ClipDialogFragment.ClipSavedListener, ClipListAdapter.OnItemDismissListener {
    @Inject MyPreferenceManager myPreferenceManager;
    @Inject IabHelper mHelper;
    @Inject IabUtil iabUtil;

    @InjectView(R.id.rvClipList) RecyclerView rvClipList;
    @InjectView(R.id.toolbar)Toolbar toolbar;
    @InjectView(R.id.edtxFilter)EditText edtxFilter;
    @InjectView(R.id.fabAddClip)FloatingActionButton fabAddClip;

    private RecyclerView.LayoutManager layoutManager;
    private ClipListAdapter clipListAdapter;
    private RealmClipAdapter realmClipAdapter;

    private Realm realm;
    private RealmResults<Clip> clips;
    private Clip tempClip;

    private IabComponent iabComponent;

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        initializeDependencyInjector();
        checkFirstRun();
        initializeToolbar();
        initializeRecyclerView();
        initializeClipListAdapter();

        realm = Realm.getInstance(getApplicationContext());

        setListeners();
        checkAutoLaunchBubble();

        clips = getAllClips();

        initializeItemTouchHelper();
        initializeRealmAdapter();
        initializeIabHelper();

        tempClip = new Clip();
    }

    private void initializeClipListAdapter() {
        clipListAdapter = new ClipListAdapter(this, false);
        rvClipList.setAdapter(clipListAdapter);
    }

    private void checkAutoLaunchBubble() {
        if (myPreferenceManager.getBubbleActivePref()) {
            startClipService();
            if (myPreferenceManager.getAutoLaunchBubblePref()) {
                startFloatingBubbleService();
            }
        }
    }

    private void initializeItemTouchHelper() {
        ItemTouchHelper.Callback callback =
                new SwipeItemTouchHelperCallback(clipListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvClipList);
    }

    private void initializeRealmAdapter() {
        realmClipAdapter = new RealmClipAdapter(this, clips, true);
        clipListAdapter.setRealmAdapter(realmClipAdapter);
        clipListAdapter.notifyDataSetChanged();
    }

    private void initializeIabHelper() {
        mHelper.enableDebugLogging(true);//TODO disable logging
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d("TAG", "Problem setting up In-app Billing: " + result);
                    return;
                } else {
                    Log.d("TAG", "Done setting up In-app Billing: " + result);
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                iabUtil.retrieveData(mHelper);
            }
        });
    }

    private void initializeRecyclerView() {
        layoutManager = new LinearLayoutManager(this);
        rvClipList.setHasFixedSize(true);
        rvClipList.setLayoutManager(layoutManager);
    }

    private void initializeDependencyInjector() {
        iabComponent = DaggerIabComponent.builder()
                .iabModule(new IabModule(MyConstants.BASE64_ENCODED_PUBLIC_KEY))
                .persistenceComponent(((MyApplication)getApplication()).getPersistenceComponent()).build();
        iabComponent.inject(this);
    }

    private void initializeToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
    }

    private void checkFirstRun() {
        if (myPreferenceManager.getFirstRunPref()) {
            myPreferenceManager.setFirstRunPref(false);
            Intent tutorialIntent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(tutorialIntent);
        }
    }

    private void setListeners() {
        clipListAdapter.setOnItemClickListener(this);
        edtxFilter.addTextChangedListener(new TextWatcher() {
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
        });
    }

    private void startFloatingBubbleService() {
        Intent floatingBubbleServiceIntent = new Intent(getApplicationContext(), FloatingBubbleService.class);
        startService(floatingBubbleServiceIntent);
    }

    private RealmResults<Clip> getAllClips() {
        clips = realm.where(Clip.class).findAllSorted(MyConstants.FIELD_CREATION_DATE, RealmResults.SORT_ORDER_DESCENDING);
        return clips;
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

    private void startClipService() {
        Intent clipServiceIntent = new Intent(getApplicationContext(), ClipboardService.class);
        startService(clipServiceIntent);
    }

    @OnClick(R.id.fabAddClip)
    public void onClickFabAddClip(View view) {
        FragmentManager fm = getFragmentManager();
        ClipDialogFragment clipDialogFragment = ClipDialogFragment.newInstance(MyConstants.CLIP_DIALOG_NEW, -1, 0);
        clipDialogFragment.show(fm, "fragment_clipDialog");
    }

    @Override
    protected void onResume() {
        super.onResume();
        clipListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();

        if (mHelper != null) mHelper.dispose();
            mHelper = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (myPreferenceManager.getAppRatedPref()) {
            menu.findItem(R.id.action_rate).setVisible(false);
        }
        if (myPreferenceManager.getDonateCompletePref()) {
            menu.findItem(R.id.action_donate).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                rvClipList.scrollToPosition(0);
                break;
            case R.id.action_donate:
                FragmentManager fm = getFragmentManager();
                DonateDialogFragment donateDialogFragment = new DonateDialogFragment();
                donateDialogFragment.show(fm, "fragment_donateDialog");
            break;
            case R.id.action_rate:
                FragmentManager fm1 = getFragmentManager();
                RateDialogFragment rateDialogFragment = new RateDialogFragment();
                rateDialogFragment.show(fm1, "fragment_rateDialog");
            break;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        FragmentManager fm = getFragmentManager();
        int clipId = clipListAdapter.getItem(position).getId();
        ClipDialogFragment clipDialogFragment = ClipDialogFragment.newInstance(MyConstants.CLIP_DIALOG_OLD, clipId, position);
        clipDialogFragment.show(fm, "fragment_clipDialog");
    }

    @Override
    public void onClipSaved(int position, int type) {
        switch (type) {
            case MyConstants.CLIP_DIALOG_OLD:
                clipListAdapter.notifyItemChanged(position);
                break;
            case MyConstants.CLIP_DIALOG_NEW:
                clipListAdapter.notifyItemInserted(position);
                rvClipList.scrollToPosition(position);
                break;
        }
    }

    @Override
    public void onItemDismiss(final int position) {
        final Clip clip = clipListAdapter.getItem(position);

        tempClip.setId(clip.getId());
        tempClip.setText(clip.getText());
        tempClip.setCreationDate(clip.getCreationDate());
        tempClip.setType(clip.getType());

        realm.beginTransaction();
        clip.removeFromRealm();
        realm.commitTransaction();

        Snackbar.make(fabAddClip, R.string.tx_clip_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.tx_undo_c, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        realm.beginTransaction();
                        realm.copyToRealm(tempClip);
                        realm.commitTransaction();

                        clipListAdapter.notifyItemInserted(position);
                      }
                })
                .setActionTextColor(getResources().getColor(R.color.sb_red))
                .show();
    }

    public void showThankYouMsg() {
        Snackbar.make(fabAddClip, R.string.tx_thank_you, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.tx_life_is_easy_c, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.sb_red))
                .show();
    }

    public void showErrorMsg() {
        Snackbar.make(fabAddClip, R.string.tx_error, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.tx_try_later_c, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.sb_red))
                .show();
    }

    public IabComponent getIabComponent() {
        return iabComponent;
    }
}