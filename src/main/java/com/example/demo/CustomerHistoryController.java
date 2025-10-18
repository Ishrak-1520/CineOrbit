package com.example.demo;

import com.example.demo.utils.DatabaseConnection;
import com.example.demo.utils.FXMLScene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomerHistoryController implements UserAware {

    @FXML private TableView<TicketHistory> historyTable;
    @FXML private TableColumn<TicketHistory, String> dateColumn;
    @FXML private TableColumn<TicketHistory, String> movieColumn;
    @FXML private TableColumn<TicketHistory, String> screeningColumn;
    @FXML private TableColumn<TicketHistory, String> seatsColumn;
    @FXML private TableColumn<TicketHistory, Double> amountColumn;
    @FXML private TableColumn<TicketHistory, String> statusColumn;
    @FXML private TableColumn<TicketHistory, String> actionsColumn;
    @FXML private Label username;
    @FXML private AnchorPane customerHistory_form;

    private ObservableList<TicketHistory> purchaseList = FXCollections.observableArrayList();
    private String currentUsername;

    @FXML
    public void initialize() {
        // Initialize table columns
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        movieColumn.setCellValueFactory(cellData -> cellData.getValue().movieProperty());
        screeningColumn.setCellValueFactory(cellData -> cellData.getValue().screeningProperty());
        seatsColumn.setCellValueFactory(cellData -> cellData.getValue().seatsProperty());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        actionsColumn.setCellValueFactory(cellData -> cellData.getValue().actionProperty());

        // Get current user from LoginController
        currentUsername = LoginController.getLoggedInUsername();
        if (currentUsername != null) {
            loadPurchaseHistory();
        }
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleCustomerDashboardBtn() {
        try {
            FXMLScene.switchScene("icustomerpanel.fxml", (Stage) username.getScene().getWindow());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading history page: " + e.getMessage());
        }
    }

    @FXML
    private void handleCustomerHistoryBtn() {
        try {
            FXMLScene.switchScene("customer_history.fxml", (Stage) username.getScene().getWindow());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading history page: " + e.getMessage());
        }
    }

    @FXML
    private void handleChatBtn() {
        try {
            FXMLScene.switchScene("customer_chat.fxml", (Stage) username.getScene().getWindow());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading chat page: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignoutBtn() {
        try {
            LoginController.setLoggedInUsername(null);
            FXMLScene.switchScene("login.fxml", (Stage) username.getScene().getWindow());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error signing out: " + e.getMessage());
        }
    }

    private boolean isAdmin() {
        String sql = "SELECT role FROM users WHERE username = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, currentUsername);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("role") == 0; // 0 is admin
            }
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadPurchaseHistory() {
        purchaseList.clear();
        try (Connection conn = new DatabaseConnection().getConnection()) {
            String query = "SELECT c.*, DATE_FORMAT(c.created_at, '%Y-%m-%d %H:%i') as formatted_date " +
                          "FROM customers c " +
                          "WHERE c.user_id = ? " +
                          "ORDER BY c.created_at DESC";
            
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, currentUsername);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String ticketNumber = rs.getString("ticket_number");
                    String movieTitle = rs.getString("movie_title");
                    String date = rs.getString("formatted_date");
                    String time = rs.getString("time");
                    String tableNo = rs.getString("table_no");
                    double payment = rs.getDouble("total_payment");
                    
                    // Create a TicketHistory object
                    TicketHistory history = new TicketHistory(
                        date,                   // Purchase date
                        movieTitle,             // Movie title
                        time,                   // Screening time
                        tableNo,                // Seat information
                        payment,                // Amount paid
                        "Completed",            // Status
                        ticketNumber            // Ticket number as action reference
                    );
                    purchaseList.add(history);
                }

                historyTable.setItems(purchaseList);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading purchase history: " + e.getMessage());
        }
    }

    @Override
    public void setUsername(String username) {
        this.currentUsername = username;
        this.username.setText(username);
        loadPurchaseHistory(); // Reload purchase history when username is set
    }
} 