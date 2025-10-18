package com.example.demo;

import com.example.demo.utils.DatabaseConnection;
import com.example.demo.utils.FXMLScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField SignUp_email;
    @FXML private AnchorPane SignUp_form;
    @FXML private TextField name;
    @FXML private PasswordField signUp_password;
    @FXML private TextField signUp_username;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    @FXML
    void handleAlreadyAccount(ActionEvent event) {
        switchScene("/com/example/demo/login.fxml", event);
    }

    @FXML
    void handleClose() {
        System.exit(0);
    }

    @FXML
    void handleMinimize(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    void handleSignup(ActionEvent event) {
        String username = signUp_username.getText().trim();
        String password = signUp_password.getText().trim();
        String email = SignUp_email.getText().trim();
        String fullName = name.getText().trim();

        // Input validation
        if (!validateInputs(username, password, email, fullName)) {
            return;
        }

        try (Connection conn = new DatabaseConnection().getConnection()) {
            // Check if username or email already exists
            if (isUserExists(conn, username, email)) {
                showAlert(Alert.AlertType.ERROR, "Username or email already exists");
                return;
            }

            // Create user account
            if (createUserAccount(conn, username, password, email, fullName)) {
                showAlert(Alert.AlertType.INFORMATION, "Registration successful!");
                clearForm();
                switchScene("/com/example/demo/login.fxml", event);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        } catch (ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateInputs(String username, String password, String email, String fullName) {
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill all blank fields");
            return false;
        }

        if (username.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Username must be at least 3 characters long");
            return false;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Password must be at least 6 characters long");
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.ERROR, "Please enter a valid email address");
            return false;
        }

        if (fullName.length() < 2) {
            showAlert(Alert.AlertType.ERROR, "Please enter your full name");
            return false;
        }

        return true;
    }

    private boolean isUserExists(Connection conn, String username, String email) throws SQLException {
        String checkSql = "SELECT username FROM users WHERE username = ? OR email = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, username);
            checkStmt.setString(2, email);
            ResultSet rs = checkStmt.executeQuery();
            return rs.next();
        }
    }

    private boolean createUserAccount(Connection conn, String username, String password, String email, String fullName) throws SQLException {
        String insertSql = "INSERT INTO users (username, password, name, email, role) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, password); // In a real app, password should be hashed
            insertStmt.setString(3, fullName);
            insertStmt.setString(4, email);
            return insertStmt.executeUpdate() > 0;
        }
    }

    private void handleDatabaseError(SQLException e) {
        String errorMessage = "Registration failed: ";
        if (e.getMessage().contains("Duplicate entry")) {
            errorMessage += "Username or email already exists";
        } else {
            errorMessage += e.getMessage();
        }
        showAlert(Alert.AlertType.ERROR, errorMessage);
        e.printStackTrace();
    }

    private void clearForm() {
        signUp_username.setText("");
        signUp_password.setText("");
        SignUp_email.setText("");
        name.setText("");
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error Message" : "Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.showAndWait();
    }

    private void switchScene(String fxmlPath, ActionEvent event) {
        try {
            FXMLScene fxmlScene = FXMLScene.load(fxmlPath);
            Scene scene = new Scene(fxmlScene.getRoot());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error loading scene: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
