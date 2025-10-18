package com.example.demo;

import com.example.demo.chat.ChatServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Start the chat server
        ChatServer.getInstance().start();

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Movie Ticket Booking System");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Stop the chat server when the application closes
        ChatServer.getInstance().stop();
    }

    public static void main(String[] args) {
        launch();
    }
}