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
 * By Vinetos, aout 2017
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
public class UpdaterActivity extends AppCompatActivity {

    private static final String URL = "https://api.github.com/repos/vinetos/Hello-music-droid/releases/latest";
    private String version;
    private String browser_download_url;
    private ProgressBar spinner;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updater);
        spinner = (ProgressBar) findViewById(R.id.updaterBar);
        textView = (TextView) findViewById(R.id.updaterText);
        spinner.setVisibility(View.VISIBLE);

        checkUpdate();
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
                        // Stop the activity
                        finish();
                    }
                } catch (JSONException | PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    finish();// End activity if the request failed
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                finish();// End activity if the request failed
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

}
