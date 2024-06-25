package io.esper.espermobilevlm;

import static io.esper.espermobilevlm.DownloadActivity.MODEL_FILE_SIZE;
import static io.esper.espermobilevlm.DownloadActivity.PROJ_FILE_SIZE;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private TextView outputTextView;
    private EditText promptEditText;
    private ImageView imageView;
    private FileManager fileManager;
    private CommandExecutor commandExecutor;
    private File photoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileManager = new FileManager(this);
        commandExecutor = new CommandExecutor(this);

        Button runButton = findViewById(R.id.button_run);
        Button galleryButton = findViewById(R.id.button_gallery);
        Button cameraButton = findViewById(R.id.button_camera);
        imageView = findViewById(R.id.imageView);
        outputTextView = findViewById(R.id.textView_output);
        promptEditText = findViewById(R.id.editText_prompt);

        runButton.setOnClickListener(view -> {
            if (checkFilesAndExecute()) {
                executeShellCommand();
            }
        });

        galleryButton.setOnClickListener(view -> pickImageFromGallery());
        cameraButton.setOnClickListener(view -> captureImage());

        displaySampleImage();
    }

    private void displaySampleImage() {
        fileManager.copyFileFromAssets(getString(R.string.sample_jpg_name));
        String imagePath = fileManager.getFilePath(getString(R.string.sample_jpg_name));
        imageView.setImageURI(FileProvider.getUriForFile(this, "io.esper.espermobilevlm.fileprovider", new File(imagePath)));
    }

    private void pickImageFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
    }

    private void captureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = fileManager.createImageFile();
            } catch (IOException ex) {
                Log.e("MainActivity", "Error occurred while creating the file");
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, "io.esper.espermobilevlm.fileprovider", photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK) {
                Uri selectedImageUri = data.getData();
                try {
                    // Create a file in your app's internal storage
                    photoFile = fileManager.createImageFile();
                    // Copy the gallery image to the internal storage
                    fileManager.copyUriToFile(selectedImageUri, photoFile);
                } catch (IOException e) {
                    Log.e("MainActivity", "Error occurred while creating or copying the file", e);
                }
                imageView.setImageURI(selectedImageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imageView.setImageURI(FileProvider.getUriForFile(this, "io.esper.espermobilevlm.fileprovider", photoFile));
            }
        }
    }


    private boolean checkFilesAndExecute() {
        if (!fileManager.checkFile(getString(R.string.sample_jpg_name), 0)) {
            fileManager.copyFileFromAssets(getString(R.string.sample_jpg_name));
        }
        return fileManager.checkFile(getString(R.string.mobile_vlm_model_file_name), MODEL_FILE_SIZE) &&
                fileManager.checkFile(getString(R.string.mm_proj_model_file_name), PROJ_FILE_SIZE);
    }


    private void executeShellCommand() {
        String promptText = promptEditText.getText().toString(); // Get the text from EditText
        String command = "LD_LIBRARY_PATH=" + getApplicationInfo().nativeLibraryDir + " " +
                getApplicationInfo().nativeLibraryDir + "/llava.so -m " +
                fileManager.getFilePath(getString(R.string.mobile_vlm_model_file_name))+ " --mmproj " + fileManager.getFilePath(getString(R.string.mm_proj_model_file_name)) +
                " -t 4 --image " + photoFile.getAbsolutePath() + " " +
                "-p \"" + promptText + "\"";
        commandExecutor.executeCommand(command,
                output -> runOnUiThread(() -> {
                    Log.d("MainActivity", "Output: " + output);
                    outputTextView.append(output + "\n");
                }),
                error -> Log.d("MainActivity", "Error running shell command: " + error));
    }
}
