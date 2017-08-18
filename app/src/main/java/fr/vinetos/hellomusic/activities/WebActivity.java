package fr.vinetos.hellomusic.activities;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
public class WebActivity extends Activity {

    public static final String HOME = "http://www.youtubeinmp3.com/fetch/?video=";
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionManager = new PermissionManager(this);
        if (permissionManager.checkAndRequestPermissions()) {
            startApp();
        }
    }

    private void startApp() {
        // Get intent, action and MIME type
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        // Is open from sharing menu
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                final String url = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (url != null) {
                    setContentView(R.layout.activity_web);
                    openWebPage(url); // Handle text being sent
                }

            }
            return;
        }
        // If this activity is open "normally", closing it
        finish();
    }

    /**
     * Open a web page of a specified URL
     *
     * @param url URL to open
     */
    private void openWebPage(String url) {
        WebView webView = (WebView) findViewById(R.id.webview);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.loadUrl(convertUrl(url));
        // Support in-app browser
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        // Support downloads with the DownloadManager
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC,
                        URLUtil.guessFileName(url, contentDisposition, mimetype));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);

                // Close automatically the activity when download is started
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            // Start the loading of the video only if we have all perms
            startApp();
        }
    }

    /**
     * Convert a url to the API to download url with the web api
     *
     * @param url which be shared by the app
     * @return The URL to open
     */
    private String convertUrl(String url) {
        // Youtube share menu
        if (url.contains("youtube.com") || url.contains("youtu.be"))
            return HOME + url;
        return url;
    }

}
