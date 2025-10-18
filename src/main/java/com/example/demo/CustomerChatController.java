package com.example.demo;

import com.example.demo.utils.FXMLScene;
import com.example.demo.chat.ChatClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import java.io.IOException;

public class CustomerChatController {
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private Label username1;
    
    private ChatClient chatClient;

    @FXML
    public void initialize() {
        // Set username from login
        String currentUser = LoginController.getLoggedInUsername();
        if (currentUser != null) {
            username1.setText(currentUser);
        }
        
        // Initialize chat client
        try {
            chatClient = new ChatClient(currentUser, message -> {
                Platform.runLater(() -> {
                    if (message.startsWith("USER_CONNECTED:") || message.startsWith("USER_DISCONNECTED:")) {
                        // Handle connection messages silently
                        return;
                    }
                    chatArea.appendText(message + "\n");
                });
            });
            chatArea.appendText("Connected to support. Please type your message below.\n");
        } catch (IOException e) {
            e.printStackTrace();
            chatArea.appendText("Failed to connect to chat server: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && chatClient != null) {
            // Always send messages to admin
            chatClient.sendMessage("@ADMIN " + message);
            messageField.clear();
        }
    }

    @FXML
    private void handleCustomerDashboardBtn() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        FXMLScene.switchScene("icustomerpanel.fxml", chatArea);
    }

    @FXML
    private void handleCustomerHistoryBtn() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        FXMLScene.switchScene("customer_history.fxml", chatArea);
    }

    @FXML
    private void handleChatBtn() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        FXMLScene.switchScene("customer_chat.fxml", chatArea);
    }

    @FXML
    private void handleSignoutBtn() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        LoginController.setLoggedInUsername(null);
        FXMLScene.switchScene("login.fxml", chatArea);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 