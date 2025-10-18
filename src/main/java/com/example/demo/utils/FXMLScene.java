package com.example.demo.utils;

import javafx.animation.FadeTransition;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class FXMLScene {
    private Parent root = null;
    private Object controller = null;

    public static FXMLScene load(String fxmlpath) {
        FXMLScene fxmlScene = new FXMLScene();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            // Ensure path starts with /com/example/demo/
            String fullPath = fxmlpath.startsWith("/") ? fxmlpath : "/com/example/demo/" + fxmlpath;
            fxmlLoader.setLocation(FXMLScene.class.getResource(fullPath));
            
            if (fxmlLoader.getLocation() == null) {
                throw new IOException("Could not find FXML file: " + fullPath);
            }

            fxmlScene.root = fxmlLoader.load();
            fxmlScene.controller = fxmlLoader.getController();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load FXML: " + fxmlpath, e);
        }
        return fxmlScene;
    }

    public static void switchScene(String fxmlPath, Node node) throws IOException {
        try {
            FXMLScene fxmlScene = FXMLScene.load(fxmlPath);
            Scene scene = new Scene(fxmlScene.getRoot());
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (RuntimeException e) {
            throw new IOException("Failed to switch scene to " + fxmlPath, e);
        }
    }

    public static void switchScene(FXMLScene fxmlScene, Node node) {
        Scene scene = new Scene(fxmlScene.getRoot());
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    public static void switchScene(String fxmlPath, Stage stage) throws IOException {
        try {
            FXMLScene fxmlScene = FXMLScene.load(fxmlPath);
            Scene scene = new Scene(fxmlScene.getRoot());
            stage.setScene(scene);
            stage.show();
        } catch (RuntimeException e) {
            throw new IOException("Failed to switch scene to " + fxmlPath, e);
        }
    }

    public Parent getRoot() {
        return root;
    }

    public Object getController() {
        return controller;
    }
}