package fr.vinetos.hellomusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class DownloadActivity extends Activity {

    private static final String API = "http://hellomusic.vinetos.fr/download/?url=";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();
        // If we don't get data from intent
        if (!Intent.ACTION_SEND.equals(action) || type == null)
            return;
        // Check the type of data
        if (!"text/plain".equals(type))
            return;
        final String data = intent.getStringExtra(Intent.EXTRA_TEXT);
        // Check if it's open from youtube sharing menu and it's an video URL
        if (!isYoutubeUrl(data))
            return;
        final String apiUrl = convertUrl(data);

        Toast.makeText(this, "Getting video data...", Toast.LENGTH_LONG).show();
        Intent i = new Intent(this, DownloadService.class);
        i.putExtra("apiUrl", apiUrl);
        startService(i);
        finish();
    }

    /**
     * Check if the given url is a youtube url
     *
     * @param url the url
     * @return <code>true</code> if it's a Youtube URL
     */
    private boolean isYoutubeUrl(String url) {
        return url.contains("youtube.com") || url.contains("youtu.be");
    }

    /**
     * Convert a url to the API to download url with the web api
     *
     * @param url which be shared by the app
     * @return The URL to open
     */
    private String convertUrl(String url) {
        return isYoutubeUrl(url) ? API + url : null;
    }
}
