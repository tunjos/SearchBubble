package com.tunjos.searchbubble.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.tunjos.searchbubble.R;
import com.tunjos.searchbubble.models.Clip;
import com.tunjos.searchbubble.models.MyConstants;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.realm.Realm;

import static com.tunjos.searchbubble.others.MyUtils.getClipType;

/**
 * Created by tunjos on 28/06/2015.
 */
public class ClipDialogFragment extends DialogFragment {
    @InjectView(R.id.edtxClip) EditText edtxClip;

    public ClipDialogFragment() {
    }

    public static ClipDialogFragment newInstance(int type, int clipId, int adapterPosition) {
        ClipDialogFragment fragment = new ClipDialogFragment();
        Bundle args = new Bundle();
        args.putInt(MyConstants.EXTRA_TYPE, type);
        args.putInt(MyConstants.EXTRA_CLIP_ID, clipId);
        args.putInt(MyConstants.EXTRA_ADAPTER_POSITION, adapterPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        edtxClip.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_clip, null);
        ButterKnife.inject(this, view);

        final Realm[] realm = {Realm.getInstance(getActivity())};

        final int type = getArguments().getInt(MyConstants.EXTRA_TYPE);
        int clipId = getArguments().getInt(MyConstants.EXTRA_CLIP_ID);
        final int adapterPosition = getArguments().getInt(MyConstants.EXTRA_ADAPTER_POSITION);
        final Clip[] clip = {null};

        if (type == MyConstants.CLIP_DIALOG_OLD) {
            clip[0] = realm[0].where(Clip.class).equalTo(MyConstants.FIELD_ID, clipId).findFirst();
            edtxClip.setText(clip[0].getText());
        }

        final ClipSavedListener clipSavedListener = (ClipSavedListener)getActivity();

        AlertDialog.Builder b =  new  AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setPositiveButton(R.string.tx_save_c,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                String text = edtxClip.getText().toString();
                                if (text == null || text.equalsIgnoreCase("")) {
                                    return;
                                }

                                if (type == MyConstants.CLIP_DIALOG_NEW) {

                                    int nextId = (int) (realm[0].where(Clip.class).maximumInt(MyConstants.FIELD_ID) + 1);
                                    Date dateNow = new Date();
                                    int clipType = getClipType(text);

                                    realm[0].beginTransaction();

                                    Clip clip = realm[0].createObject(Clip.class);
                                    clip.setId(nextId);
                                    clip.setText(text);
                                    clip.setType(clipType);
                                    clip.setCreationDate(dateNow);

                                    realm[0].commitTransaction();
                                    realm[0].close();
                                    clipSavedListener.onClipSaved(adapterPosition, MyConstants.CLIP_DIALOG_NEW);
                                } else if (type == MyConstants.CLIP_DIALOG_OLD) {
                                    realm[0].beginTransaction();
                                    text = edtxClip.getText().toString();
//                                    Date dateNow = new Date();

                                    clip[0].setText(text);
//                                    clip[0].setCreationDate(dateNow);

                                    realm[0].commitTransaction();
                                    realm[0].refresh();
                                    realm[0].close();
                                    clipSavedListener.onClipSaved(adapterPosition, MyConstants.CLIP_DIALOG_OLD);

                                }
                            }
                        }
                )
                .setNegativeButton(R.string.tx_cancel_c,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );
        if (type == MyConstants.CLIP_DIALOG_OLD) {
            b.setTitle(R.string.tx_editclip);
        } else if (type == MyConstants.CLIP_DIALOG_NEW) {
            b.setTitle(R.string.tx_newclip);
        }

        b.setView(view);
        return b.create();
    }

    public interface ClipSavedListener {
        void onClipSaved(int position, int type);
    }
}