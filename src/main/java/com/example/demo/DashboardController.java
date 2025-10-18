package com.example.demo;

import com.example.demo.utils.DatabaseConnection;
import com.example.demo.utils.FXMLScene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    @FXML
    private Label dashboard_availableMovies;

    @FXML
    private AnchorPane dashboard_form;

    @FXML
    private Label dashboard_totalEarnToday;

    @FXML
    private Label dashboard_totalSoldTicket;

    @FXML
    private Label username;

    @FXML
    private ImageView currentPoster;

    @FXML
    private Label movieTitle;

    private List<Movie> availableMovies = new ArrayList<>();
    private int currentMovieIndex = 0;
    private Timeline slideTimer;

    @FXML
    public void initialize() {
        loadStatistics();
        loadAvailableMovies();
        setupSlideshow();
        
        // Set admin username
        username.setText(LoginController.getLoggedInUsername());
    }

    private void loadStatistics() {
        try (Connection conn = new DatabaseConnection().getConnection()) {
            // Load total sold tickets today
            String ticketQuery = "SELECT COUNT(*) as count FROM customers WHERE DATE(created_at) = CURDATE()";
            try (PreparedStatement stmt = conn.prepareStatement(ticketQuery)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    dashboard_totalSoldTicket.setText(String.valueOf(rs.getInt("count")));
                }
            }

            // Load total earnings today
            String earningsQuery = "SELECT COALESCE(SUM(total_payment), 0) as total FROM customers WHERE DATE(created_at) = CURDATE()";
            try (PreparedStatement stmt = conn.prepareStatement(earningsQuery)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    dashboard_totalEarnToday.setText(String.format("%.2f", rs.getDouble("total")));
                }
            }

            // Load available movies count
            String moviesQuery = "SELECT COUNT(*) as count FROM movies WHERE status = 'Now Showing'";
            try (PreparedStatement stmt = conn.prepareStatement(moviesQuery)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    dashboard_availableMovies.setText(String.valueOf(rs.getInt("count")));
                }
            }

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void loadAvailableMovies() {
        availableMovies.clear();
        try (Connection conn = new DatabaseConnection().getConnection()) {
            String query = "SELECT * FROM movies WHERE status = 'Now Showing' ORDER BY title";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String posterPath = rs.getString("poster_path");
                    if (posterPath != null && !posterPath.isEmpty() && !posterPath.startsWith("images/")) {
                        posterPath = "images/" + new File(posterPath).getName();
                    }
                    
                    Movie movie = new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getString("duration"),
                        rs.getString("date"),
                        posterPath,
                        rs.getString("status")
                    );
                    availableMovies.add(movie);
                }
            }
            showCurrentMovie();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setupSlideshow() {
        // Stop any existing slideshow
        if (slideTimer != null) {
            slideTimer.stop();
        }

        // Create new slideshow timer
        slideTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> handleNextMovie()));
        slideTimer.setCycleCount(Timeline.INDEFINITE);
        slideTimer.play();
    }

    @FXML
    void handlePrevMovie() {
        if (availableMovies.isEmpty()) return;
        
        currentMovieIndex--;
        if (currentMovieIndex < 0) {
            currentMovieIndex = availableMovies.size() - 1;
        }
        showCurrentMovie();
        resetSlideTimer();
    }

    @FXML
    void handleNextMovie() {
        if (availableMovies.isEmpty()) return;
        
        currentMovieIndex++;
        if (currentMovieIndex >= availableMovies.size()) {
            currentMovieIndex = 0;
        }
        showCurrentMovie();
        resetSlideTimer();
    }

    private void resetSlideTimer() {
        if (slideTimer != null) {
            slideTimer.stop();
            slideTimer.play();
        }
    }

    private void showCurrentMovie() {
        if (availableMovies.isEmpty()) {
            currentPoster.setImage(null);
            movieTitle.setText("No movies available");
            return;
        }

        Movie movie = availableMovies.get(currentMovieIndex);
        movieTitle.setText(movie.getTitle());

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            try {
                String posterPath = movie.getPosterPath();
                File imageFile = new File(posterPath);
                
                // If the file doesn't exist with the direct path, try the images directory
                if (!imageFile.exists()) {
                    imageFile = new File("images/" + new File(posterPath).getName());
                }
                
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    currentPoster.setImage(image);
                    System.out.println("Successfully loaded image from: " + imageFile.getAbsolutePath());
                } else {
                    System.err.println("Image file not found: " + imageFile.getAbsolutePath());
                    currentPoster.setImage(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                currentPoster.setImage(null);
            }
        } else {
            currentPoster.setImage(null);
        }
    }

    @FXML
    void handleAddMovieBtn(ActionEvent event) {
        switchScene("/com/example/demo/addmovies.fxml", event);
    }

    @FXML
    void handleAvailableMovieBtn(ActionEvent event) {
        switchScene("/com/example/demo/availablemovies.fxml", event);
    }

    @FXML
    void handleCustomersBtn(ActionEvent event) {
        switchScene("/com/example/demo/customers.fxml", event);
    }

    @FXML
    void handleDashboardBtn(ActionEvent event) {
        switchScene("/com/example/demo/dashboard.fxml", event);
    }

    @FXML
    void handleEditScreeningBtn(ActionEvent event) {
        switchScene("/com/example/demo/editscreening.fxml", event);
    }

    @FXML
    void handleChatBtn(ActionEvent event) {
        switchScene("/com/example/demo/admin_chat.fxml", event);
    }

    @FXML
    void handleSignoutBtn(ActionEvent event) {
        switchScene("/com/example/demo/login.fxml", event);
    }

    private void switchScene(String fxmlPath, ActionEvent event) {
        if (slideTimer != null) {
            slideTimer.stop();
        }
        FXMLScene fxmlScene = FXMLScene.load(fxmlPath);
        Scene scene = new Scene(fxmlScene.getRoot());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
