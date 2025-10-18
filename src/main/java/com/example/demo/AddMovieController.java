package com.example.demo;

import com.example.demo.utils.DatabaseConnection;
import com.example.demo.utils.FXMLScene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class AddMovieController {

    @FXML private TextField addMovies_movieTitle, addMovies_genre, addMovies_duration, addMovies_date, addMovies_search;
    @FXML private TableView<Movie> addMovies_tableView;
    @FXML private TableColumn<Movie, String> addMovies_col_movieTitle, addMovies_col_genre, addMovies_col_duration, addMovies_col_date;
    @FXML private Button addMovies_insertBtn, addMovies_updateBtn, addMovies_deleteBtn, addMovies_clearBtn, addMovies_importBtn;
    @FXML private ImageView addMovies_imageView;
    @FXML private AnchorPane addMovies_form;

    @FXML
    private Label username;

    @FXML
    void handleAddMovieBtn(ActionEvent event) { switchScene(event, "/com/example/demo/addmovies.fxml"); }
    @FXML
    void handleAvailableMovieBtn(ActionEvent event) { switchScene(event, "/com/example/demo/availablemovies.fxml"); }
    @FXML
    void handleCustomersBtn(ActionEvent event) { switchScene(event, "/com/example/demo/customers.fxml"); }
    @FXML
    void handleDashboardBtn(ActionEvent event) { switchScene(event, "/com/example/demo/dashboard.fxml"); }
    @FXML
    void handleEditScreeningBtn(ActionEvent event) { switchScene(event, "/com/example/demo/editscreening.fxml"); }
    @FXML
    void handleSignoutBtn(ActionEvent event) { switchScene(event, "/com/example/demo/login.fxml"); }
    @FXML
    void handleChatBtn(ActionEvent event) { switchScene(event, "/com/example/demo/admin_chat.fxml"); }

    private void switchScene(ActionEvent event, String path) {
        FXMLScene fxmlScene = FXMLScene.load(path);
        Scene scene = new Scene(fxmlScene.getRoot());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private ObservableList<Movie> movieList = FXCollections.observableArrayList();
    private String selectedPosterPath = null;
    private File posterFile;

    @FXML
    public void initialize() {
        addMovies_col_movieTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        addMovies_col_genre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        addMovies_col_duration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        addMovies_col_date.setCellValueFactory(new PropertyValueFactory<>("date"));

        addMovies_tableView.setItems(movieList);

        addMovies_insertBtn.setOnAction(e -> insertMovie());
        addMovies_updateBtn.setOnAction(e -> updateMovie());
        addMovies_deleteBtn.setOnAction(e -> deleteMovie());
        addMovies_clearBtn.setOnAction(e -> clearFields());
        addMovies_importBtn.setOnAction(e -> handleImportPoster());

        // Listen for table row selection
        addMovies_tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                addMovies_movieTitle.setText(newSel.getTitle());
                addMovies_genre.setText(newSel.getGenre());
                addMovies_duration.setText(newSel.getDuration());
                addMovies_date.setText(newSel.getDate());
                selectedPosterPath = newSel.getPosterPath();
                if (selectedPosterPath != null) {
                    addMovies_imageView.setImage(new Image("file:" + selectedPosterPath));
                }
            }
        });

        // Restrict duration to numbers only
        addMovies_duration.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,2}:?\\d{0,2}:?\\d{0,2}")) {
                addMovies_duration.setText(oldVal); // Reject invalid change
            }
        });

        setupSearchBar();
        loadMoviesFromDatabase();
    }

    private void setupSearchBar() {
        FilteredList<Movie> filteredData = new FilteredList<>(movieList, b -> true);

        addMovies_search.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(movie -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return movie.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                        movie.getGenre().toLowerCase().contains(lowerCaseFilter) ||
                        movie.getDate().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<Movie> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(addMovies_tableView.comparatorProperty());
        addMovies_tableView.setItems(sortedData);
    }

    private void loadMoviesFromDatabase() {
        movieList.clear();
        String sql = "SELECT * FROM movies";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movieList.add(new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("genre"),
                        rs.getString("duration"),
                        rs.getString("date"),
                        rs.getString("poster_path"),
                        rs.getString("status")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertMovie() {
        String title = addMovies_movieTitle.getText();
        String genre = addMovies_genre.getText();
        String duration = addMovies_duration.getText();
        String date = addMovies_date.getText();

        if (title.isEmpty() || genre.isEmpty() || duration.isEmpty() || date.isEmpty()) {
            showAlert("Please fill all fields.");
            return;
        }

        if (!duration.matches("\\d{0,2}:?\\d{0,2}:?\\d{0,2}")) {
            showAlert("Duration must be a number.");
            return;
        }

        String sql = "INSERT INTO movies (title, genre, duration, date, poster_path, status) VALUES (?, ?, ?, ?, ?, 'Now Showing')";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, genre);
            ps.setString(3, duration);
            ps.setString(4, date);
            ps.setString(5, selectedPosterPath);
            ps.executeUpdate();

            loadMoviesFromDatabase();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMovie() {
        Movie selected = addMovies_tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a movie to update.");
            return;
        }

        String sql = "UPDATE movies SET title = ?, genre = ?, duration = ?, date = ?, poster_path = ?, status = ? WHERE id = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, addMovies_movieTitle.getText());
            ps.setString(2, addMovies_genre.getText());
            ps.setString(3, addMovies_duration.getText());
            ps.setString(4, addMovies_date.getText());
            ps.setString(5, selectedPosterPath);
            ps.setString(6, selected.getStatus());
            ps.setInt(7, selected.getId());
            ps.executeUpdate();

            loadMoviesFromDatabase();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteMovie() {
        Movie selected = addMovies_tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a movie to delete.");
            return;
        }

        String sql = "DELETE FROM movies WHERE id = ?";
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selected.getId());
            ps.executeUpdate();

            loadMoviesFromDatabase();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        addMovies_movieTitle.clear();
        addMovies_genre.clear();
        addMovies_duration.clear();
        addMovies_date.clear();
        addMovies_imageView.setImage(null);
        selectedPosterPath = null;
        addMovies_tableView.getSelectionModel().clearSelection();
    }

    private void handleImportImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Poster");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) addMovies_form.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            posterFile = file;
            Image image = new Image(file.toURI().toString());
            addMovies_imageView.setImage(image);
        }
        selectedPosterPath = file.getAbsolutePath();
    }

    private void handleImportPoster() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Poster Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(addMovies_form.getScene().getWindow());

        if (file != null) {
            try {
                File dir = new File("images");
                if (!dir.exists()) dir.mkdirs();

                String ext = file.getName().substring(file.getName().lastIndexOf("."));
                String newFileName = "poster_" + System.currentTimeMillis() + ext;
                File dest = new File(dir, newFileName);

                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                selectedPosterPath = "images/" + newFileName;
                
                Image image = new Image(dest.toURI().toString());
                addMovies_imageView.setImage(image);
                System.out.println("Saved poster to: " + selectedPosterPath);
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Failed to save image.");
            }
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
