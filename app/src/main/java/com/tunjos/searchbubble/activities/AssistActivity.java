package com.tunjos.searchbubble.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tunjos.searchbubble.services.ClipboardService;
import com.tunjos.searchbubble.services.FloatingBubbleService;


public class AssistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startFloatingBubbleService();
        finish();
    }

    private void startFloatingBubbleService() {
        Intent floatingBubbleServiceIntent = new Intent(getApplicationContext(), FloatingBubbleService.class);
        startService(floatingBubbleServiceIntent);
    }

    private void startClipService() {
        Intent clipServiceIntent = new Intent(getApplicationContext(), ClipboardService.class);
        startService(clipServiceIntent);
    }
}