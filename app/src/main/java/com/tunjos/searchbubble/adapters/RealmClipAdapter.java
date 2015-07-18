package com.tunjos.searchbubble.adapters;

import android.content.Context;

import com.tunjos.searchbubble.models.Clip;

import io.realm.RealmResults;

/**
 * Created by tunjos on 22/06/2015.
 */
public class RealmClipAdapter extends RealmModelAdapter<Clip> {

    public RealmClipAdapter(Context context, RealmResults<Clip> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }
}