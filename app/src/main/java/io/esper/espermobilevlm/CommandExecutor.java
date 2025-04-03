package io.esper.espermobilevlm;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CommandExecutor {
    private Process shellProcess;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Context context;

    public CommandExecutor(Context context) {
        this.context = context;
    }

    public void executeCommand(String command, Consumer<String> outputHandler, Consumer<String> errorHandler) {
        executorService.submit(() -> {
            try {
                if (shellProcess == null || !shellProcess.isAlive()) {
                    shellProcess = Runtime.getRuntime().exec("sh");
                    Log.d("CommandExecutor", "Shell process created");
                }

                shellProcess.getOutputStream().write((command + "\n").getBytes(StandardCharsets.UTF_8));
                shellProcess.getOutputStream().flush();

                handleOutput(shellProcess, outputHandler, errorHandler);
                int exitCode = shellProcess.waitFor();
                Log.d("CommandExecutor", "Shell command exit code: " + exitCode);

            } catch (Exception e) {
                Log.e("CommandExecutor", " shell command: " + e.getMessage());
                errorHandler.accept("Error running shell command: " + e.getMessage());
            }
        });
    }

    private void handleOutput(Process process, Consumer<String> outputHandler, Consumer<String> errorHandler) {
        // Output stream
        new Thread(() -> {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = input.readLine()) != null) {
                    outputHandler.accept(line);
                }
            } catch (IOException e) {
                Log.e("CommandExecutor", "Error reading output stream", e);
                errorHandler.accept("Error reading output stream: " + e.getMessage());
            }
        }).start();

        // Error stream
        new Thread(() -> {
            try (BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = error.readLine()) != null) {
                    errorHandler.accept(line);
                }
            } catch (IOException e) {
                Log.e("CommandExecutor", "Error reading error stream", e);
                errorHandler.accept("Error reading error stream: " + e.getMessage());
            }
        }).start();
    }
}