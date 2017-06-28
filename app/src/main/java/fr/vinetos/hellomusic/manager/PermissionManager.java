package fr.vinetos.hellomusic.manager;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class PermissionManager {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final String[] PERMISSIONS_NEEDED = {
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private Activity activity;

    public PermissionManager(Activity activity) {
        this.activity = activity;
    }

    public boolean checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();

        // Check if all permissions are granted
        for (String perm : PERMISSIONS_NEEDED) {
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED)
                listPermissionsNeeded.add(perm);// Add to needed perms (for requests)
        }
        if (listPermissionsNeeded.isEmpty())
            return true;

        ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
        return false;

    }

    public boolean onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                for (String perm : PERMISSIONS_NEEDED) {
                    perms.put(perm, PackageManager.PERMISSION_GRANTED);
                }

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions

                    boolean arePermsGranted = true;
                    for (String perm : PERMISSIONS_NEEDED) {
                        if (perms.get(perm) != PackageManager.PERMISSION_GRANTED) {
                            arePermsGranted = false;
                            break;
                        }
                    }

                    if (arePermsGranted) {
                        // Start app
                        return true;
                    } else {
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        boolean shouldRequest = false;
                        for (String perm : PERMISSIONS_NEEDED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                                shouldRequest = true;
                                break;
                            }
                        }
                        if (shouldRequest) {
                            showDialogOK("We need more permissions !",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by leaving the app.
                                                    activity.finish();
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(activity, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }
        return false;
    }

    public void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

}