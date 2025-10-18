package com.example.demo;

import com.example.demo.utils.FXMLScene;
import com.example.demo.chat.ChatClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;

public class AdminChatController {
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private ListView<String> customerListView;
    @FXML private Label username;
    @FXML private Label customerCountLabel;
    @FXML private Label selectedCustomerLabel;
    @FXML private Label customerStatusLabel;
    
    private String currentUser;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private ChatClient chatClient;
    private String currentCustomer;
    private String selectedCustomer;
    private final ObservableList<String> customers = FXCollections.observableArrayList();
    private final Set<String> activeCustomers = new HashSet<>();
    private Pattern usernamePattern = Pattern.compile("^\\[(\\d{2}:\\d{2}:\\d{2})\\] (.*?): .*$");

    @FXML
    public void initialize() {
        currentUser = "admin";
        username.setText("Admin");
        
        customerListView.setItems(customers);
        customerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedCustomer = newVal;
            if (selectedCustomer != null) {
                selectedCustomerLabel.setText("Chatting with: " + selectedCustomer);
                customerStatusLabel.setText("Status: Online");
                customerStatusLabel.setTextFill(javafx.scene.paint.Color.valueOf("#2ecc71"));
            } else {
                selectedCustomerLabel.setText("Select a customer to start chatting");
                customerStatusLabel.setText("Status: No customer selected");
                customerStatusLabel.setTextFill(javafx.scene.paint.Color.valueOf("#95a5a6"));
            }
        });

        try {
            chatClient = new ChatClient("ADMIN", message -> {
                Platform.runLater(() -> {
                    if (message.startsWith("USER_CONNECTED:")) {
                        String user = message.substring("USER_CONNECTED:".length());
                        if (!activeCustomers.contains(user) && !user.equals("ADMIN")) {
                            activeCustomers.add(user);
                            customers.add(user);
                            updateCustomerCount();
                            chatArea.appendText("Customer " + user + " has connected.\n");
                        }
                    } else if (message.startsWith("USER_DISCONNECTED:")) {
                        String user = message.substring("USER_DISCONNECTED:".length());
                        activeCustomers.remove(user);
                        customers.remove(user);
                        updateCustomerCount();
                        if (selectedCustomer != null && selectedCustomer.equals(user)) {
                            selectedCustomer = null;
                            selectedCustomerLabel.setText("Select a customer to start chatting");
                            customerStatusLabel.setText("Status: Customer disconnected");
                            customerStatusLabel.setTextFill(javafx.scene.paint.Color.valueOf("#e74c3c"));
                            chatArea.appendText("Customer " + user + " has disconnected.\n");
                        }
                    } else {
                        chatArea.appendText(message + "\n");
                    }
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
            chatArea.appendText("Error connecting to chat server: " + e.getMessage() + "\n");
        }
        
        // Initialize customer count
        updateCustomerCount();
    }

    private void updateCustomerCount() {
        int count = activeCustomers.size();
        customerCountLabel.setText(count + (count == 1 ? " customer" : " customers") + " online");
    }

    private void handleIncomingMessage(String message) {
        // Extract username from message format: "[timestamp] username: message"
        Matcher matcher = usernamePattern.matcher(message);
        if (matcher.find()) {
            currentCustomer = matcher.group(2);
        }
        appendMessage("", message); // Message already contains sender info
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && chatClient != null) {
            if (selectedCustomer != null) {
                chatClient.sendMessage("@" + selectedCustomer + " " + message);
                chatArea.appendText("You to " + selectedCustomer + ": " + message + "\n");
            } else {
                chatArea.appendText("Please select a customer to chat with.\n");
            }
            messageField.clear();
        }
    }

    private void appendMessage(String sender, String message) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String formattedMessage = sender.isEmpty() ? 
            String.format("[%s] %s\n", timestamp, message) :
            String.format("[%s] %s: %s\n", timestamp, sender, message);
        chatArea.appendText(formattedMessage);
    }

    @FXML
    private void handleDashboardBtn() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        FXMLScene.switchScene("dashboard.fxml", chatArea);
    }

    @FXML
    private void handleAddMovieBtn() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        FXMLScene.switchScene("addmovies.fxml", chatArea);
    }

    @FXML
    private void handleAvailableMovieBtn() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        FXMLScene.switchScene("availablemovies.fxml", chatArea);
    }

    @FXML
    private void handleEditScreeningBtn() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        FXMLScene.switchScene("editscreening.fxml", chatArea);
    }

    @FXML
    private void handleCustomersBtn() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        FXMLScene.switchScene("customers.fxml", chatArea);
    }

    @FXML
    private void handleChatBtn() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        FXMLScene.switchScene("admin_chat.fxml", chatArea);
    }

    @FXML
    private void handleSignoutBtn() throws IOException {
        if (chatClient != null) {
            chatClient.disconnect();
        }
        FXMLScene.switchScene("login.fxml", chatArea);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type.toString());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 