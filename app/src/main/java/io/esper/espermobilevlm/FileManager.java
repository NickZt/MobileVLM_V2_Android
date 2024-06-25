package io.esper.espermobilevlm;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
}
