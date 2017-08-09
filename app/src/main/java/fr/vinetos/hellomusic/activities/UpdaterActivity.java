package fr.vinetos.hellomusic.activities;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import fr.vinetos.hellomusic.R;
import fr.vinetos.hellomusic.manager.PermissionManager;

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
 * By Vinetos, août 2017
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
public class UpdaterActivity extends AppCompatActivity {

    private static final String URL = "https://api.github.com/repos/vinetos/Hello-music-droid/releases/latest";
    private String version;
    private String browser_download_url;
    private ProgressBar spinner;
    private TextView textView;
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updater);
        spinner = (ProgressBar) findViewById(R.id.updaterBar);
        textView = (TextView) findViewById(R.id.updaterText);
        spinner.setVisibility(View.VISIBLE);

        permissionManager = new PermissionManager(this);
        permissionManager.checkAndRequestPermissions();
    }

    private void checkUpdate() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Parse JSON
                    String tag_name = (String) response.get("tag_name");
                    version = tag_name.substring(1);
                    JSONArray assets = response.getJSONArray("assets");
                    browser_download_url = assets.getJSONObject(0).getString("browser_download_url");

                    // Get App infos
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    String version = pInfo.versionName;

                    // Update available
                    if (!UpdaterActivity.this.version.equals(version)) {
                        // Download the new apk
                        downloadUpdate();
                    } else {
                        startApp();
                    }
                } catch (JSONException | PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        queue.add(jsonObjectRequest);
    }

    private void downloadUpdate() {
        // get destination to update file and set Uri
        // TODO: First I wanted to store my update .apk file on internal storage for my app but apparently android does not allow you to open and install
        // application with existing package from there. So for me, alternative solution is Download directory in external storage. If there is better
        // solution, please inform us in comment
        String fileName = "HelloMusic-" + version + ".apk";
        final String destination = Environment.getExternalStorageDirectory() + "/Download/" + fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists()) {
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();
        }

        //set downloadmanager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(browser_download_url));
        request.setDescription("Downloading update...");
        request.setTitle("HelloMusic");
        //set destination
        request.setDestinationUri(uri);

        textView.setText("Downloading update...");

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);

        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri uri = FileProvider.getUriForFile(UpdaterActivity.this, "fr.vinetos.hellomusic.fileprovider", new File(destination));
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    install.setDataAndType(uri, manager.getMimeTypeForDownloadedFile(downloadId));
                } else {
                    install.setDataAndType(uri, manager.getMimeTypeForDownloadedFile(downloadId));
                }
                startActivity(install);

                unregisterReceiver(this);
                finish();
            }
        };
        //register receiver for when .apk download is compete
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void startApp() {
        spinner.setVisibility(View.GONE);
        startActivity(new Intent(UpdaterActivity.this, WelcomeActivity.class));
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults))
            checkUpdate();
    }
}
