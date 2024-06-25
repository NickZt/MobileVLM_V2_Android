package io.esper.espermobilevlm;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtils {

    public interface DownloadListener {
        void onDownloadComplete(File file);
        void onDownloadError(Exception e);
        void onDownloadProgress(int progress);

    }

    public static void downloadWithCheck(final String urlString, final File outputFile, final long expectedSize, final DownloadListener listener) {
        Thread thread = new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                Log.d("DownloadUtils", "Starting download: " + urlString);
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("DownloadUtils", "HTTP error response: " + connection.getResponseCode());
                    throw new IOException("HTTP error code: " + connection.getResponseCode());
                }

                int fileLength = connection.getContentLength();
                BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
                FileOutputStream fos = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1024];
                long total = 0;
                int count;
                while ((count = bis.read(buffer)) != -1) {
                    total += count;
                    if (fileLength > 0) { // only if total length is known
                        int progress = (int) (total * 100 / fileLength);
                        Log.d("DownloadUtils", "Download progress: " + progress + "%");
                        listener.onDownloadProgress(progress);
                    }
                    fos.write(buffer, 0, count);
                }
                fos.flush();
                fos.close();
                bis.close();

                if (outputFile.length() != expectedSize) {
                    Log.e("DownloadUtils", "File size mismatch after download. Deleting: " + outputFile.getName());
                    outputFile.delete();
                    throw new IOException("Downloaded file size does not match expected size.");
                }

                Log.d("DownloadUtils", "Download complete: " + outputFile.getName());
                listener.onDownloadComplete(outputFile);
            } catch (Exception e) {
                Log.e("DownloadUtils", "Download error: " + e.getMessage());
                listener.onDownloadError(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
        thread.start();
    }
}
