package fr.vinetos.hellomusic.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import fr.vinetos.hellomusic.R;
import fr.vinetos.hellomusic.adapters.RecyclerViewAdapter;
import fr.vinetos.hellomusic.listeners.CustomTouchListener;
import fr.vinetos.hellomusic.listeners.OnItemClickListener;
import fr.vinetos.hellomusic.manager.AudioManager;
import fr.vinetos.hellomusic.manager.PermissionManager;

import static android.os.Build.VERSION.SDK_INT;

/*
 * ==============================================================================
 *            _    _______   __________________  _____
 *            | |  / /  _/ | / / ____/_  __/ __ \/ ___/
 *            | | / // //  |/ / __/   / / / / / /\__ \
 *            | |/ // // /|  / /___  / / / /_/ /___/ /
 *            |___/___/_/ |_/_____/ /_/  \____//____/
 *
 * ==============================================================================
 *
 * HelloMusic game
 * Copyright (c) Vinetos Software 2017,
 * By Vinetos, juin 2017
 * 
 * ==============================================================================
 * 
 * This file is part of HelloMusic.
 * 
 * HelloMusic is free software: you can redistribute it and/or modify
 * it under the terms of the MIT License.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in 
 * the Software without restriction, including without limitation the rights to 
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies 
 * of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS 
 * FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *==============================================================================
 */
public class MainActivity extends AppCompatActivity {

    private static final String SERVICE_STATUS = "serviceStatus";
    private PermissionManager permissionManager;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        audioManager = new AudioManager(this);
        permissionManager = new PermissionManager(this);

        // M and later use a new permission system
        if (SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionManager.checkAndRequestPermissions()) {
                // App can work correctly
                startApp();
            }
        } else {
            // The new permission system isn't used
            startApp();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (AudioManager.serviceBound) {
            unbindService(audioManager.getServiceConnection());
            //service is active
            audioManager.getPlayer().stopSelf();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(SERVICE_STATUS, AudioManager.serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        AudioManager.serviceBound = savedInstanceState.getBoolean(SERVICE_STATUS);
    }

    private void startApp() {
        audioManager.loadAudio();
        initRecyclerView();
    }

    private void initRecyclerView() {
        if (AudioManager.musicsList != null && AudioManager.musicsList.size() > 0) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
            RecyclerViewAdapter adapter = new RecyclerViewAdapter(AudioManager.musicsList, getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.addOnItemTouchListener(new CustomTouchListener(this, new OnItemClickListener() {
                @Override
                public void onClick(View view, int index) {
                    audioManager.playAudio(index);
                }
            }));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            startApp();
        }
    }

}
