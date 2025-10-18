package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import com.example.demo.utils.FXMLScene;
import com.example.demo.utils.DatabaseConnection;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.stage.Stage;

public class Icustomerpanel {
    @FXML private AnchorPane menu_form;
    @FXML private ScrollPane menu_scroll_pane;
    @FXML private TableView<Movie> menu_table_view1;
    @FXML private TableColumn<Movie, String> menu_col_movie_name1;
    @FXML private TableColumn<Movie, String> menu_col_class;
    @FXML private TableColumn<Movie, String> menu_col_genre;
    @FXML private TableColumn<Movie, String> menu_col_screening_date;
    @FXML private TableColumn<Movie, String> menu_col_scernning_time;
    @FXML private Label menu_total;
    @FXML private Button menu_payBtn;
    @FXML private Button menu_removeBtn;
    @FXML private Button menu_receiptBtn;
    @FXML private Spinner<Integer> menu_special_class_spinner;
    @FXML private Spinner<Integer> menu_normal_class_spinner;
    @FXML private Label menu_special_class_price;
    @FXML private Label menu_normal_class_price;
    @FXML private Label username;
    @FXML private ImageView movie_poster;

    private String currentUser;

    @FXML
    public void initialize() {
        // Initialize the table columns with proper cell factories
        menu_col_movie_name1.setCellValueFactory(new PropertyValueFactory<>("title"));
        menu_col_class.setCellValueFactory(new PropertyValueFactory<>("duration"));
        menu_col_genre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        menu_col_screening_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        menu_col_scernning_time.setCellValueFactory(new PropertyValueFactory<>("duration"));

        // Initialize spinners with default values and step size of 1
        SpinnerValueFactory<Integer> specialClassValueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0, 1);
        menu_special_class_spinner.setValueFactory(specialClassValueFactory);

        SpinnerValueFactory<Integer> normalClassValueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0, 1);
        menu_normal_class_spinner.setValueFactory(normalClassValueFactory);

        // Add event handlers for buttons
        menu_payBtn.setOnAction(e -> handlePayButton());
        menu_removeBtn.setOnAction(e -> handleRemoveButton());
        menu_receiptBtn.setOnAction(e -> handleReceiptButton());

        // Set username and load data
        currentUser = LoginController.getLoggedInUsername();
        if (currentUser != null) {
            username.setText(currentUser);
        }

        // Add table selection listener for poster display and price updates
        menu_table_view1.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayMoviePoster(newSelection);
                updatePriceLabels();
            }
        });

        // Setup spinner value change listeners
        setupSpinnerListeners();

        // Load initial data
        loadMovieData();

        // Initialize prices to 0
        menu_special_class_price.setText("0.00 BDT");
        menu_normal_class_price.setText("0.00 BDT");
        menu_total.setText("0.00 BDT");
    }

    private void displayMoviePoster(Movie movie) {
        if (movie != null && movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            try {
                String posterPath = movie.getPosterPath();
                File imageFile = new File(posterPath);
                
                // If the file doesn't exist with the direct path, try the images directory
                if (!imageFile.exists()) {
                    imageFile = new File("images/" + new File(posterPath).getName());
                }
                
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    movie_poster.setImage(image);
                    System.out.println("Successfully loaded poster from: " + imageFile.getAbsolutePath());
                } else {
                    System.err.println("Poster file not found: " + imageFile.getAbsolutePath());
                    movie_poster.setImage(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                movie_poster.setImage(null);
            }
        } else {
            movie_poster.setImage(null);
        }
    }

    private void updatePriceLabels() {
        int specialQty = menu_special_class_spinner.getValue();
        int normalQty = menu_normal_class_spinner.getValue();

        double specialTotal = specialQty * 500.0;
        double normalTotal = normalQty * 300.0;
        double total = specialTotal + normalTotal;

        menu_special_class_price.setText(String.format("%.2f BDT", specialTotal));
        menu_normal_class_price.setText(String.format("%.2f BDT", normalTotal));
        menu_total.setText(String.format("%.2f BDT", total));
    }

    // Add spinner value change listeners
    private void setupSpinnerListeners() {
        menu_special_class_spinner.valueProperty().addListener((obs, oldValue, newValue) -> updatePriceLabels());
        menu_normal_class_spinner.valueProperty().addListener((obs, oldValue, newValue) -> updatePriceLabels());
    }

    private void loadMovieData() {
        menu_table_view1.getItems().clear();
        try (Connection conn = new DatabaseConnection().getConnection()) {
            String query = "SELECT * FROM movies WHERE status = 'Now Showing' ORDER BY date DESC";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String posterPath = rs.getString("poster_path");
                // Ensure the poster path is properly formatted
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
                menu_table_view1.getItems().add(movie);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            showError("Error loading movie data: " + e.getMessage());
        }
    }

    @FXML
    private void handleCustomerDashboardBtn() {
        // Already on dashboard, no need to navigate
    }

    @FXML
    private void handleCustomerHistoryBtn() {
        try {
            FXMLScene.switchScene("customer_history.fxml", (Stage) menu_payBtn.getScene().getWindow());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading history page: " + e.getMessage());
        }
    }

    @FXML
    private void handleChatBtn() {
        try {
            FXMLScene.switchScene("customer_chat.fxml", (Stage) menu_payBtn.getScene().getWindow());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading chat page: " + e.getMessage());
        }
    }

    @FXML
    private void handleSignoutBtn() {
        try {
            LoginController.setLoggedInUsername(null);
            FXMLScene.switchScene("login.fxml", (Stage) menu_payBtn.getScene().getWindow());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error signing out: " + e.getMessage());
        }
    }

    private void handlePayButton() {
        // Get selected movie and quantities
        Movie selectedMovie = menu_table_view1.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showError("Please select a movie first");
            return;
        }

        int specialQty = menu_special_class_spinner.getValue();
        int normalQty = menu_normal_class_spinner.getValue();

        if (specialQty == 0 && normalQty == 0) {
            showError("Please select at least one ticket");
            return;
        }

        // Calculate total
        double specialTotal = specialQty * 500.0;
        double normalTotal = normalQty * 300.0;
        double total = specialTotal + normalTotal;
        
        // Confirm purchase
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Purchase");
        confirmAlert.setHeaderText("Purchase Summary");
        confirmAlert.setContentText(String.format("""
            Movie: %s
            Special Class Tickets: %d x 500.00 = %.2f BDT
            Normal Class Tickets: %d x 300.00 = %.2f BDT
            Total Amount: %.2f BDT
            
            Do you want to proceed with the purchase?""",
            selectedMovie.getTitle(), specialQty, specialTotal,
            normalQty, normalTotal, total));

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            // Save purchase to database
            try {
                String ticketNumber = savePurchase(selectedMovie, specialQty, normalQty, total);
                showSuccess("Purchase successful! Your ticket number is: " + ticketNumber);
                generateReceipt(selectedMovie, specialQty, normalQty, total, ticketNumber);
                clearForm();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error processing payment: " + e.getMessage());
            }
        }
    }

    private String savePurchase(Movie movie, int specialQty, int normalQty, double total) throws SQLException, ClassNotFoundException {
        String ticketNumber = generateTicketNumber();
        try (Connection conn = new DatabaseConnection().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Save purchase record
                String sql = "INSERT INTO customers (user_id, movie_title, genre, table_no, time, date, total_payment, ticket_number) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, currentUser);
                    ps.setString(2, movie.getTitle());
                    ps.setString(3, movie.getGenre());
                    ps.setString(4, specialQty + "S," + normalQty + "N");
                    ps.setString(5, movie.getDuration());
                    ps.setString(6, movie.getDate());
                    ps.setDouble(7, total);
                    ps.setString(8, ticketNumber);
                    ps.executeUpdate();
                }
                
                conn.commit();
                return ticketNumber;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void generateReceipt(Movie movie, int specialQty, int normalQty, double total, String ticketNumber) {
        try {
            // Create receipt content
            String receiptContent = String.format("""
                ===================================
                      MOVIE TICKET RECEIPT
                ===================================
                Ticket Number: %s
                Date: %s
                Time: %s
                
                Movie: %s
                Genre: %s
                
                Special Class Tickets: %d x 500.00
                Special Class Total: %.2f BDT
                
                Normal Class Tickets: %d x 300.00
                Normal Class Total: %.2f BDT
                
                Total Amount: %.2f BDT
                ===================================
                Thank you for your purchase!
                ===================================
                """,
                ticketNumber,
                movie.getDate(),
                movie.getDuration(),
                movie.getTitle(),
                movie.getGenre(),
                specialQty,
                specialQty * 500.0,
                normalQty,
                normalQty * 300.0,
                total
            );

            // Show receipt in a dialog
            TextArea textArea = new TextArea(receiptContent);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(20);
            textArea.setPrefColumnCount(40);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Movie Ticket Receipt");
            dialog.getDialogPane().setContent(textArea);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error generating receipt: " + e.getMessage());
        }
    }

    private String generateTicketNumber() {
        return String.format("TKT%d", System.currentTimeMillis());
    }

    private void handleRemoveButton() {
        menu_table_view1.getSelectionModel().clearSelection();
        clearForm();
    }

    private void handleReceiptButton() {
        Movie selectedMovie = menu_table_view1.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showError("Please select a movie first");
            return;
        }

        int specialQty = menu_special_class_spinner.getValue();
        int normalQty = menu_normal_class_spinner.getValue();

        if (specialQty == 0 && normalQty == 0) {
            showError("Please select ticket quantities");
            return;
        }

        double specialTotal = specialQty * 500.0;
        double normalTotal = normalQty * 300.0;
        double total = specialTotal + normalTotal;

        generateReceipt(selectedMovie, specialQty, normalQty, total, "PREVIEW");
    }

    private void clearForm() {
        menu_special_class_spinner.getValueFactory().setValue(0);
        menu_normal_class_spinner.getValueFactory().setValue(0);
        menu_total.setText("0.00 BDT");
        menu_special_class_price.setText("0.00 BDT");
        menu_normal_class_price.setText("0.00 BDT");
        menu_table_view1.getSelectionModel().clearSelection();
        movie_poster.setImage(null);
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
}