package io.esper.espermobilevlm;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FileManager {
    private Context context;

    public FileManager(Context context) {
        this.context = context;
    }

    public boolean checkFile(String filename, long expectedSize) {
        File file = new File(context.getFilesDir(), filename);
        return file.exists() && file.length() == expectedSize;
    }

    public String getFilePath(String filename) {
        return new File(context.getFilesDir(), filename).getAbsolutePath();
    }

    public void copyUriToFile(Uri uri, File destFile) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        OutputStream outputStream = new FileOutputStream(destFile);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.close();
        inputStream.close();
    }

    public void copyFileFromAssets(String filename) {
        AssetManager assetManager = context.getAssets();
        File outputFile = new File(context.getFilesDir(), filename);

        try (InputStream in = assetManager.open(filename);
             FileOutputStream out = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            Log.d("FileManager", "File copied to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e("FileManager", "Error copying file from assets", e);
        }
    }

    public File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = context.getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}
