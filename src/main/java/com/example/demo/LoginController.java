package com.example.demo;

import com.example.demo.utils.DatabaseConnection;
import com.example.demo.utils.FXMLScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField SignUp_email;

    @FXML
    private AnchorPane SignUp_form;

    @FXML
    private Label label;

    @FXML
    private Button signUp_Btn;

    @FXML
    private Hyperlink signUp_alreadyHaveAccount;

    @FXML
    private PasswordField signUp_password;

    @FXML
    private TextField signUp_username;

    @FXML
    private Hyperlink signin_createAccount;

    @FXML
    private AnchorPane signin_form;

    @FXML
    private Button signin_loginBtn;

    @FXML
    private PasswordField signin_password;

    @FXML
    private TextField signin_username;

    private static String loggedInUsername;

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    public static void setLoggedInUsername(String username) {
        loggedInUsername = username;
    }

    @FXML
    void loginHandle(ActionEvent event) {
        loginUser();
    }

    @FXML
    void creatAccount(ActionEvent event) {
        try {
            FXMLScene.switchScene("register.fxml", signin_form);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading register form: " + e.getMessage());
        }
    }

    private void loginUser() {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection connect = new DatabaseConnection().getConnection();
             PreparedStatement prepare = connect.prepareStatement(sql)) {
            
            prepare.setString(1, signin_username.getText());
            prepare.setString(2, signin_password.getText());
            
            ResultSet result = prepare.executeQuery();
            Alert alert;
            
            if (result.next()) {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Message");
                alert.setHeaderText(null);
                alert.setContentText("Successfully Login!");
                alert.showAndWait();
                
                // Store logged in username
                loggedInUsername = signin_username.getText();
                
                // Check role and redirect accordingly
                int role = result.getInt("role");
                String fxmlFile = (role == 1) ? "icustomerpanel.fxml" : "dashboard.fxml";
                
                try {
                    FXMLScene.switchScene(fxmlFile, signin_form);
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error loading dashboard: " + e.getMessage());
                }
                
            } else {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Wrong Username/Password");
                alert.showAndWait();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error during login: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type.toString());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
