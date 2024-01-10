package com.example.deck;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CommandSender {

    private static final String host = "192.168.1.62";
    private static final int port = 8080; // Update with your chosen port number

    public static String sendCommand(String command) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> callable = () -> {
            StringBuilder result = new StringBuilder();
            try {
                Socket socket = new Socket(host, port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println(command);

                String line;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                }

                // Use a Handler to update UI on the main thread if needed
                new Handler(Looper.getMainLooper()).post(() -> {
                    // Process the result or update UI here
                    // Example: textView.setText(result.toString().trim());
                });

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the error if needed
            }
            return result.toString();
        };

        Future<String> future = executor.submit(callable);

        try {
            // Wait for the task to complete and get the result
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception if needed
        } finally {
            // Shutdown the executor when it's no longer needed
            executor.shutdown();
        }

        return null;
    }
}
