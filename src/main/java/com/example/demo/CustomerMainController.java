package com.example.demo;

import com.example.demo.utils.FXMLScene;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CustomerMainController {

    @FXML private Label usernameLabel;
    @FXML private Button dashboardBtn;
    @FXML private Button historyBtn;
    @FXML private Button moviesBtn;
    @FXML private Button signOutBtn;
    @FXML private AnchorPane contentArea;

    private String username;
    private Button currentActiveButton;

    @FXML
    public void initialize() {
        // Set dashboard as default view
        setActiveButton(dashboardBtn);
        loadView("/com/example/demo/customer_dashboard.fxml");
    }

    public void setUsername(String username) {
        this.username = username;
        usernameLabel.setText(username);
    }

    @FXML
    void handleDashboard(ActionEvent event) {
        setActiveButton(dashboardBtn);
        loadView("/com/example/demo/customer_dashboard.fxml");
    }

    @FXML
    void handleHistory(ActionEvent event) {
        setActiveButton(historyBtn);
        loadView("/com/example/demo/customer_history.fxml");
    }

    @FXML
    void handleMovies(ActionEvent event) {
        setActiveButton(moviesBtn);
        loadView("/com/example/demo/availablemovies.fxml");
    }

    @FXML
    void handleSignOut(ActionEvent event) {
        try {
            FXMLScene loginScene = FXMLScene.load("/com/example/demo/login.fxml");
            Scene scene = new Scene(loginScene.getRoot());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button button) {
        // Remove active class from current button
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active");
        }
        
        // Add active class to new button
        button.getStyleClass().add("active");
        currentActiveButton = button;
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Pass username to the controller if it implements UserAware
            Object controller = loader.getController();
            if (controller instanceof UserAware) {
                ((UserAware) controller).setUsername(username);
            }

            // Clear existing content and set new view
            contentArea.getChildren().clear();
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 