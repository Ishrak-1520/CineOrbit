package com.example.demo.chat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatServer {
    private static ChatServer instance;
    private final int port;
    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private volatile boolean running = false;
    private Thread serverThread;

    private ChatServer(int port) {
        this.port = port;
    }

    public static synchronized ChatServer getInstance() {
        if (instance == null) {
            instance = new ChatServer(5000);
        }
        return instance;
    }

    public void start() {
        if (!running) {
            running = true;
            serverThread = new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(port);
                    System.out.println("Chat server started on port " + port);

                    while (running) {
                        Socket clientSocket = serverSocket.accept();
                        new Thread(() -> handleNewConnection(clientSocket)).start();
                    }
                } catch (IOException e) {
                    if (running) {
                        System.out.println("Already running");
                    }
                }
            });
            serverThread.start();
        }
    }

    private void handleNewConnection(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            // First message should be the username
            String username = reader.readLine();
            if (username != null) {
                ClientHandler clientHandler = new ClientHandler(username, clientSocket, writer);
                clients.put(username, clientHandler);

                // Notify admin about the new connection if it's a customer
                if (!username.equals("ADMIN")) {
                    ClientHandler adminHandler = clients.get("ADMIN");
                    if (adminHandler != null) {
                        adminHandler.sendMessage("USER_CONNECTED:" + username);
                    }
                }

                // Start reading messages from this client
                String message;
                while ((message = reader.readLine()) != null) {
                    if (username.equals("ADMIN")) {
                        // Admin messages are private to the selected customer
                        if (message.startsWith("@")) {
                            int spaceIndex = message.indexOf(" ");
                            if (spaceIndex > 1) {
                                String recipient = message.substring(1, spaceIndex);
                                String content = message.substring(spaceIndex + 1);
                                sendPrivateMessage("ADMIN", recipient, content);
                            }
                        }
                    } else {
                        // All customer messages go to admin
                        ClientHandler adminHandler = clients.get("ADMIN");
                        if (adminHandler != null) {
                            // Format: [Customer Name]: Message
                            String formattedMessage = "[" + username + "]: " + message.replace("@ADMIN", "").trim();
                            adminHandler.sendMessage(formattedMessage);
                            // Send confirmation to the customer
                            clientHandler.sendMessage("You: " + message.replace("@ADMIN", "").trim());
                        }
                    }
                }

                // Client disconnected
                handleClientDisconnection(username);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientDisconnection(String username) {
        if (username != null) {
            clients.remove(username);
            // Notify admin about customer disconnection
            if (!username.equals("ADMIN")) {
                ClientHandler adminHandler = clients.get("ADMIN");
                if (adminHandler != null) {
                    adminHandler.sendMessage("USER_DISCONNECTED:" + username);
                }
            }
            System.out.println("Client disconnected: " + username);
        }
    }

    private void sendPrivateMessage(String sender, String recipient, String message) {
        ClientHandler recipientHandler = clients.get(recipient);
        if (recipientHandler != null) {
            recipientHandler.sendMessage("[Admin]: " + message);
        }
    }

    public void stop() {
        running = false;
        clients.values().forEach(ClientHandler::close);
        clients.clear();
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    private static class ClientHandler {
        private final String username;
        private final Socket socket;
        private final PrintWriter writer;

        public ClientHandler(String username, Socket socket, PrintWriter writer) {
            this.username = username;
            this.socket = socket;
            this.writer = writer;
        }

        public void sendMessage(String message) {
            writer.println(message);
        }

        public void close() {
            try {
                writer.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
} 