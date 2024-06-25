package io.esper.espermobilevlm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class DownloadActivity extends AppCompatActivity {
    public static final long MODEL_FILE_SIZE = 791817856L; // MobileVLM_V2-1.7B-2B-Q4_K.gguf
    public static final long PROJ_FILE_SIZE = 595103072L;  // mmproj-model-f16.gguf

    private File modelFile;
    private File projFile;
    private ProgressBar progressBarModel;
    private Button buttonProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        progressBarModel = findViewById(R.id.progressBarModel);
        ProgressBar progressBarProj = findViewById(R.id.progressBarProj);
        TextView statusModel = findViewById(R.id.statusModel);
        TextView statusProj = findViewById(R.id.statusProj);
        buttonProceed = findViewById(R.id.buttonProceed);

        Log.d("DownloadActivity", "onCreate: Starting DownloadActivity");
        // Initialize modelFile and projFile
        modelFile = new File(getFilesDir(), getString(R.string.mobile_vlm_model_file_name));
        projFile = new File(getFilesDir(), getString(R.string.mm_proj_model_file_name));

        manageFileDownload(modelFile, MODEL_FILE_SIZE, R.string.mobile_vlm_model_url, progressBarModel, statusModel);
        manageFileDownload(projFile, PROJ_FILE_SIZE, R.string.mm_proj_model_url, progressBarProj, statusProj);
        checkAllFilesReady();
    }

    private void manageFileDownload(File file, long expectedSize, int urlResourceId, ProgressBar progressBar, TextView status) {
        Log.d("DownloadActivity", "manageFileDownload: Checking file " + file.getName());
        if (file.exists() && file.length() == expectedSize) {
            Log.d("DownloadActivity", "File exists and is correct size: " + file.getName());
            updateDownloadStatus(status, "Ready", 100, progressBar);
        } else {
            if (file.exists()) {
                Log.e("DownloadActivity", "Deleting incorrect file: " + file.getName());
                Log.e("DownloadActivity", "File size: " + file.length() + " Expected size: " + expectedSize);
                file.delete();
            }
            startDownload(file, getString(urlResourceId), expectedSize, progressBar, status);
        }
    }

    private void updateDownloadStatus(TextView status, String message, int progress, ProgressBar progressBar) {
        runOnUiThread(() -> {
            Log.d("DownloadActivity", "updateDownloadStatus: " + message + " Progress: " + progress);
            status.setText(message);
            progressBar.setProgress(progress);
        });
    }

    private void startDownload(File file, String url, long expectedSize, ProgressBar progressBar, TextView status) {
        updateDownloadStatus(status, "Downloading...", 0);
        DownloadUtils.downloadWithCheck(url, file, expectedSize, new DownloadUtils.DownloadListener() {
            @Override
            public void onDownloadComplete(File file) {
                updateDownloadStatus(status, "Download complete", 100);
                progressBar.setProgress(100);
                checkAllFilesReady();
            }

            @Override
            public void onDownloadError(Exception e) {
                updateDownloadStatus(status, "Error: " + e.getMessage(), 0);
            }

            @Override
            public void onDownloadProgress(int progress) {
                progressBar.setProgress(progress);
                Log.d("DownloadActivity", "onDownloadProgress: " + progress);
                updateDownloadStatus(status, "Downloading... " + progress + "%", progress);
            }
        });
    }

    private void updateDownloadStatus(TextView status, String message, int progress) {
        runOnUiThread(() -> {
            status.setText(message);
            progressBarModel.setProgress(progress);
        });
    }

    private void checkAllFilesReady() {
        if (modelFile != null && projFile != null && modelFile.length() == MODEL_FILE_SIZE && projFile.length() == PROJ_FILE_SIZE) {
            runOnUiThread(() -> {
                buttonProceed.setVisibility(View.VISIBLE);
                buttonProceed.setOnClickListener(v -> {
                    Intent intent = new Intent(DownloadActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            });
        }
    }
}
