/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.naman14.timber.subfragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.naman14.timber.utils.Constants;
import com.naman14.timber.utils.NavigationUtils;
import com.naman14.timber.utils.PreferencesUtility;

import fr.vinetos.hellomusic.R;

public class SubStyleSelectorFragment extends Fragment {

    private static final String ARG_PAGE_NUMBER = "pageNumber";
    private static final String WHAT = "what";
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private LinearLayout currentStyle;
    private View foreground;
    private ImageView styleImage, imgLock;

    public static SubStyleSelectorFragment newInstance(int pageNumber, String what) {
        SubStyleSelectorFragment fragment = new SubStyleSelectorFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PAGE_NUMBER, pageNumber);
        bundle.putString(WHAT, what);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_style_selector_pager, container, false);

        TextView styleName = rootView.findViewById(R.id.style_name);
        styleName.setText(String.valueOf(getArguments().getInt(ARG_PAGE_NUMBER) + 1));
        preferences = getActivity().getSharedPreferences(Constants.FRAGMENT_ID, Context.MODE_PRIVATE);

        styleImage = rootView.findViewById(R.id.style_image);
        imgLock = rootView.findViewById(R.id.img_lock);

        styleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPreferences();
            }
        });

        switch (getArguments().getInt(ARG_PAGE_NUMBER)) {
            case 0:
                styleImage.setImageResource(R.drawable.timber_1_nowplaying_x);
                break;
            case 1:
                styleImage.setImageResource(R.drawable.timber_2_nowplaying_x);
                break;
            case 2:
                styleImage.setImageResource(R.drawable.timber_3_nowplaying_x);
                break;
            case 3:
                styleImage.setImageResource(R.drawable.timber_4_nowplaying_x);
                break;
            case 4:
                styleImage.setImageResource(R.drawable.timber_5_nowplaying_x);
                break;
            case 5:
                styleImage.setImageResource(R.drawable.timber_6_nowplaying_x);
                break;
        }

        currentStyle = rootView.findViewById(R.id.currentStyle);
        foreground = rootView.findViewById(R.id.foreground);

        setCurrentStyle();

        return rootView;
    }

    private boolean isUnlocked() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLockedStatus();
    }

    private void updateLockedStatus() {
        if (getArguments().getInt(ARG_PAGE_NUMBER) >= 4 && !isUnlocked()) {
            imgLock.setVisibility(View.VISIBLE);
            foreground.setVisibility(View.VISIBLE);
        } else {
            imgLock.setVisibility(View.GONE);
            foreground.setVisibility(View.GONE);
        }
    }

    public void setCurrentStyle() {
        String fragmentID = preferences.getString(Constants.NOWPLAYING_FRAGMENT_ID, Constants.TIMBER3);

        if (getArguments().getInt(ARG_PAGE_NUMBER) == NavigationUtils.getIntForCurrentNowplaying(fragmentID)) {
            currentStyle.setVisibility(View.VISIBLE);
            foreground.setVisibility(View.VISIBLE);
        } else {
            currentStyle.setVisibility(View.GONE);
            foreground.setVisibility(View.GONE);
        }

    }

    private void setPreferences() {
        if (getArguments().getString(WHAT).equals(Constants.SETTINGS_STYLE_SELECTOR_NOWPLAYING)) {
            editor = getActivity().getSharedPreferences(Constants.FRAGMENT_ID, Context.MODE_PRIVATE).edit();
            editor.putString(Constants.NOWPLAYING_FRAGMENT_ID, getStyleForPageNumber());
            editor.apply();
            if (getActivity() != null)
                PreferencesUtility.getInstance(getActivity()).setNowPlayingThemeChanged(true);
            setCurrentStyle();
            ((StyleSelectorFragment) getParentFragment()).updateCurrentStyle();
        }
    }

    private String getStyleForPageNumber() {
        switch (getArguments().getInt(ARG_PAGE_NUMBER)) {
            case 0:
                return Constants.TIMBER1;
            case 1:
                return Constants.TIMBER2;
            case 2:
                return Constants.TIMBER3;
            case 3:
                return Constants.TIMBER4;
            case 4:
                return Constants.TIMBER5;
            case 5:
                return Constants.TIMBER6;
            default:
                return Constants.TIMBER3;
        }
    }


}
