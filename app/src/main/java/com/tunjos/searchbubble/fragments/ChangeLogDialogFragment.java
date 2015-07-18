package com.tunjos.searchbubble.fragments;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import com.tunjos.searchbubble.R;

import it.gmariotti.changelibs.library.view.ChangeLogRecyclerView;

/**
 * Created by tunjos on 28/06/2015.
 */
public class ChangeLogDialogFragment extends DialogFragment {

    public ChangeLogDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ChangeLogRecyclerView rvChangeLog = (ChangeLogRecyclerView) layoutInflater.inflate(R.layout.dialog_changelog, null);

        AlertDialog.Builder b =  new  AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(R.string.tx_changelog)
                .setView(rvChangeLog)
                .setPositiveButton(R.string.tx_ok_c,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }
                );
        return b.create();
    }
}