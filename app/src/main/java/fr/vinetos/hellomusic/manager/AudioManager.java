package fr.vinetos.hellomusic.manager;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;

import java.util.ArrayList;

import fr.vinetos.hellomusic.adapters.AudioAdapter;
import fr.vinetos.hellomusic.services.AudioService;

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
 * HelloMusic app
 * Copyright (c) Vinetos Software 2017,
 * By Vinetos, juin 2017
 *
 * ==============================================================================
 *
 * This file is part of HelloMusic app.
 *
 * HelloMusic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HelloMusic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HelloMusic.  If not, see <http://www.gnu.org/licenses/>.
 *
 *==============================================================================
*/
public class AudioManager {

    public static final String BROADCAST_PLAY_NEW_AUDIO = PreferencesManager.PREF_NAME + ".PlayNewAudio";
    public static ArrayList<AudioAdapter> musicsList = new ArrayList<>();
    public static boolean serviceBound;
    private Context context;
    private AudioService player;
    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            AudioService.LocalBinder binder = (AudioService.LocalBinder) service;
            player = binder.getService();
            AudioManager.serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            AudioManager.serviceBound = false;
        }
    };

    public AudioManager(Context context) {
        this.context = context;
    }

    public static boolean isServiceBound() {
        return serviceBound;
    }

    public void loadAudio() {
        if (!musicsList.isEmpty()) return;
        // Search audio into the device
        ContentResolver contentResolver = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                // musicsList to audioList
                musicsList.add(new AudioAdapter(data, title, album, artist));
            }
        }
        cursor.close();
    }

    public void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            PreferencesManager storage = new PreferencesManager(context);
            storage.storeAudio(musicsList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(context, AudioService.class);
            context.startService(playerIntent);
            context.bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            PreferencesManager storage = new PreferencesManager(context);
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(BROADCAST_PLAY_NEW_AUDIO);
            context.sendBroadcast(broadcastIntent);
        }
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public AudioService getPlayer() {
        return player;
    }

    public enum AudioStatus {
        PLAYING,
        PAUSED
    }

}
