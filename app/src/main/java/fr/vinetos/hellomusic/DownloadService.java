package fr.vinetos.hellomusic;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DownloadService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DownloadService() {
        super("HelloMusic Download Service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null)
            return;

        askForInfos(intent.getStringExtra("apiUrl"));
    }

    private void askForInfos(final String url) {
        // Get the data of the video form the api
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException ignored) {
                sendToast("Download Failed, Code : 1");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    sendToast("Download Failed, Code : 2");
                    return;
                }
                try {
                    final String jsonData = response.body().string();
                    final JSONObject jsonObject = new JSONObject(jsonData);
                    final String error = jsonObject.optString("error");
                    if (!error.isEmpty()) {
                        sendToast(error + ", Code : 3");
                        return;
                    }
                    final String downloadUrl = jsonObject.getString("downloadUrl");
                    final String fileName = jsonObject.getString("fileName");
                    // Launch the download of the video converted in mp3
                    startDownload(downloadUrl, fileName);
                    sendToast("Download started");
                } catch (JSONException ignored) {
                    sendToast("Download Failed, Code : 0");
                }
            }
        });
    }

    private void startDownload(String downloadUrl, String fileName) {
        final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(downloadUrl));
        downloadRequest.allowScanningByMediaScanner();
        downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, fileName);
        downloadManager.enqueue(downloadRequest);
    }

    private void sendToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DownloadService.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
