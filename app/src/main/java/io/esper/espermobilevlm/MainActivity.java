package io.esper.espermobilevlm;

import static io.esper.espermobilevlm.DownloadActivity.MODEL_FILE_SIZE;
import static io.esper.espermobilevlm.DownloadActivity.PROJ_FILE_SIZE;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;


import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView outputTextView;
    private EditText promptEditText;
    private FileManager fileManager;
    private CommandExecutor commandExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileManager = new FileManager(this);
        commandExecutor = new CommandExecutor(this);

        Button runButton = findViewById(R.id.button_run);
        outputTextView = findViewById(R.id.textView_output);
        promptEditText = findViewById(R.id.editText_prompt);

        runButton.setOnClickListener(view -> {
            if (checkFilesAndExecute()) {
                executeShellCommand();
            }
        });
    }

    private boolean checkFilesAndExecute() {
        if (fileManager.checkFile(getString(R.string.mobile_vlm_model_file_name), MODEL_FILE_SIZE) &&
                fileManager.checkFile(getString(R.string.mm_proj_model_file_name), PROJ_FILE_SIZE)) {
            return true;
        }
        return false;
    }

    private void executeShellCommand() {
        String promptText = promptEditText.getText().toString(); // Get the text from EditText
        String command = "LD_LIBRARY_PATH=" + getApplicationInfo().nativeLibraryDir + " " +
                getApplicationInfo().nativeLibraryDir + "/llava.so -m " +
                fileManager.getFilePath(getString(R.string.mobile_vlm_model_file_name))+ " --mmproj " + fileManager.getFilePath(getString(R.string.mm_proj_model_file_name)) +
                " -t 4 --image " + fileManager.getFilePath(getString(R.string.sample_jpg_name)) + " " +
                "-p \"" + promptText + "\"";
                commandExecutor.executeCommand(command,
                        output -> runOnUiThread(() -> {
                            Log.d("MainActivity", "Output: " + output);
                            outputTextView.append(output + "\n");
                        }),
                        error -> Log.d("MainActivity", "Error running shell command: " + error));
    }
}