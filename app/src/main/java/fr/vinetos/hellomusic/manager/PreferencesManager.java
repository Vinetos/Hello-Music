package fr.vinetos.hellomusic.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import fr.vinetos.hellomusic.adapters.AudioAdapter;
import fr.vinetos.hellomusic.utils.NetworkUtils;

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
public class PreferencesManager {

    // Shared preferences file name
    public static final String PREF_NAME = "fr.vinetos.hellomusic.HelloMusic";
    private static final String AUDIO_CACHE_STORAGE = " AudioCacheStorage";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String LAST_UPDATE_TIME = "LastUpdateTime";
    // shared pref mode
    private static final int PRIVATE_MODE = 0;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public PreferencesManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = preferences.edit();
    }

    public boolean isFirstTimeLaunch() {
        return preferences.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean canCheckUpdate(Context context) {
        long lastUpdateTime = preferences.getLong(LAST_UPDATE_TIME, -1);
        if (lastUpdateTime == -1)
            return true;
        // 1000 * 60 * 60 * 24 = 86400000 = 1 day
        return System.currentTimeMillis() - lastUpdateTime >= 86_400_000 && NetworkUtils.isNetworkAvailable(context);
    }

    public void updateLastUpdateTime() {
        editor.putLong(LAST_UPDATE_TIME, System.currentTimeMillis());
        editor.commit();
    }

    public void storeAudio(ArrayList<AudioAdapter> arrayList) {
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(AUDIO_CACHE_STORAGE, json);
        editor.apply();
    }

    public ArrayList<AudioAdapter> loadAudio() {
        Gson gson = new Gson();
        String json = preferences.getString(AUDIO_CACHE_STORAGE, null);
        Type type = new TypeToken<ArrayList<AudioAdapter>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeAudioIndex(int index) {
        editor.putInt(AUDIO_CACHE_STORAGE, index);
        editor.apply();
    }

    public int loadAudioIndex() {
        return preferences.getInt(AUDIO_CACHE_STORAGE, -1);//return -1 if no data found
    }

    public void clearCachedAudioPlaylist() {
        editor.clear();
        editor.commit();
    }


}
