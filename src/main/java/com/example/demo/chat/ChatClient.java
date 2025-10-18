package com.example.demo.chat;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    private final String username;
    private final Consumer<String> messageHandler;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private volatile boolean running;
    private Thread readerThread;

    public ChatClient(String username, Consumer<String> messageHandler) throws IOException {
        this.username = username;
        this.messageHandler = messageHandler;
        connect();
    }

    private void connect() throws IOException {
        socket = new Socket(HOST, PORT);
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send username as first message
        writer.println(username);

        // Start message reading thread
        running = true;
        readerThread = new Thread(this::readMessages);
        readerThread.start();
    }

    private void readMessages() {
        try {
            String message;
            while (running && (message = reader.readLine()) != null) {
                String finalMessage = message;
                messageHandler.accept(finalMessage);
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
                messageHandler.accept("Error reading messages: " + e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        if (writer != null && !socket.isClosed()) {
            writer.println(message);
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (readerThread != null) {
                readerThread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToUser(String targetUser, String message) {
        if (writer != null) {
            writer.println(targetUser + ":" + message);
        }
    }

    public boolean isConnected() {
        return running;
    }
} 