package com.example.demo;

import com.example.demo.utils.DatabaseConnection;
import com.example.demo.utils.FXMLScene;
import com.example.demo.utils.TicketGenerator;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javafx.scene.image.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.itextpdf.text.Document;                    // iText PDF document



public class AvailableMoviesController {

    @FXML private Button availableMovies_buyBtn, availableMovies_clearBtn, availableMovies_receiptBtn, availableMovies_selectMovieBtn;
    @FXML private TableColumn<Movie, String> availableMovies_col_movieTitle, availableMovies_col_genre, availableMovies_col_showingDate;
    @FXML private TableView<Movie> availableMovies_tableView;
    @FXML private Label availableMovies_movieTitle, availableMovies_genre, availableMovies_date;
    @FXML private Label availableMovies_normalClass_price, availableMovies_specialClass_price, availableMovies_total;
    @FXML private Spinner<Integer> availableMovies_normalClass_quantity, availableMovies_specialClass_quantity;
    @FXML private ImageView availableMovies_imageView;
    @FXML private AnchorPane availableMovies_form;
    @FXML private Label username;

    private ObservableList<Movie> movieList = FXCollections.observableArrayList();

    private int normalPrice = 150;
    private int specialPrice = 300;

    private Movie lastPurchasedMovie;
    private int lastNormalQty = 0;
    private int lastSpecialQty = 0;
    private int lastTotal = 0;

    private String currentUser;

    private int selectedMovieId;

    @FXML
    public void initialize() {
        // Initialize spinners with value factories
        availableMovies_normalClass_quantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        availableMovies_specialClass_quantity.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));

        // Set initial prices
        availableMovies_normalClass_price.setText("₹" + normalPrice);
        availableMovies_specialClass_price.setText("₹" + specialPrice);

        // Initialize table columns
        availableMovies_col_movieTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        availableMovies_col_genre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        availableMovies_col_showingDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // Add selection listener to table
        availableMovies_tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMovieId = newSelection.getId();
                loadMovieDetails(newSelection);
            }
        });

        // Setup quantity change listeners
        setupQuantityListeners();

        // Setup button handlers
        availableMovies_buyBtn.setOnAction(e -> handleBuyBtn());
        availableMovies_receiptBtn.setOnAction(e -> showReceipt());
        availableMovies_clearBtn.setOnAction(e -> handleClear());

        // Load movies
        loadMoviesFromDatabase();
    }

    private void loadMoviesFromDatabase() {
        String sql = "SELECT * FROM movies WHERE status = 'Now Showing'";
        movieList.clear();
        
        try (Connection conn = new DatabaseConnection().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
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
                    rs.getString("date"),  // This is the showing date
                    posterPath,            // This is the poster path
                    rs.getString("status")
                );
                movieList.add(movie);
            }
            availableMovies_tableView.setItems(movieList);
            
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error loading movies: " + e.getMessage());
        }
    }

    private void setupQuantityListeners() {
        availableMovies_normalClass_quantity.valueProperty().addListener((obs, oldValue, newValue) -> updateTotal());
        availableMovies_specialClass_quantity.valueProperty().addListener((obs, oldValue, newValue) -> updateTotal());
    }

    private void updateTotal() {
        int normalQty = availableMovies_normalClass_quantity.getValue();
        int specialQty = availableMovies_specialClass_quantity.getValue();
        double total = (normalQty * normalPrice) + (specialQty * specialPrice);
        availableMovies_total.setText(String.format("₹%.2f", total));
    }

    @FXML
    private void handleBuyBtn() {
        if (selectedMovieId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Please select a movie first");
            return;
        }

        if (currentUser == null || currentUser.isEmpty()) {
            currentUser = LoginController.getLoggedInUsername();
            if (currentUser == null || currentUser.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Please log in first");
                return;
            }
        }

        int normalQty = availableMovies_normalClass_quantity.getValue();
        int specialQty = availableMovies_specialClass_quantity.getValue();

        if (normalQty == 0 && specialQty == 0) {
            showAlert(Alert.AlertType.ERROR, "Please select at least one ticket");
            return;
        }

        // Calculate total
        double total = (normalQty * normalPrice) + (specialQty * specialPrice);

        // Confirm purchase
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Purchase");
        confirm.setHeaderText(null);
        confirm.setContentText(String.format("Total Amount: ₹%.2f%nProceed with purchase?", total));

        if (confirm.showAndWait().get() == ButtonType.OK) {
            // Save the current movie and quantities for receipt
            lastPurchasedMovie = availableMovies_tableView.getSelectionModel().getSelectedItem();
                lastNormalQty = normalQty;
                lastSpecialQty = specialQty;
            lastTotal = (int)total;

            savePurchaseToDatabase(selectedMovieId, normalQty, specialQty, total);
            showReceipt();

            // Clear selections
                availableMovies_normalClass_quantity.getValueFactory().setValue(0);
                availableMovies_specialClass_quantity.getValueFactory().setValue(0);
            availableMovies_total.setText("₹0.00");
        }
    }

    private void savePurchaseToDatabase(int movieId, int normalQty, int specialQty, double total) {
        try (Connection conn = new DatabaseConnection().getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            
            try {
                // First get the movie details
                String movieSql = "SELECT title, genre FROM movies WHERE id = ?";
                String movieTitle = null;
                String movieGenre = null;
                
                try (PreparedStatement ps = conn.prepareStatement(movieSql)) {
                    ps.setInt(1, movieId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        movieTitle = rs.getString("title");
                        movieGenre = rs.getString("genre");
                    }
                }

                if (movieTitle != null) {
                    // Save to purchases table first
                    String purchaseSql = "INSERT INTO purchases (movie_id, username, normal_qty, special_qty, total_amount) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(purchaseSql)) {
                        ps.setInt(1, movieId);
                        ps.setString(2, currentUser);
                        ps.setInt(3, normalQty);
                        ps.setInt(4, specialQty);
                        ps.setDouble(5, total);
                        ps.executeUpdate();
                    }

                    // Generate and save individual tickets
                    String ticketSql = "INSERT INTO customers (user_id, movie_title, genre, table_no, time, date, total_payment, ticket_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    
                    // Generate tickets for normal class
                    for (int i = 0; i < normalQty; i++) {
                        String ticketNumber = "TICKET_" + System.currentTimeMillis() + "_N" + i;
                        try (PreparedStatement ps = conn.prepareStatement(ticketSql)) {
                            ps.setString(1, currentUser);
                            ps.setString(2, movieTitle);
                            ps.setString(3, movieGenre);
                            ps.setString(4, "N" + (i + 1));  // Normal class seat number
                            ps.setString(5, java.time.LocalTime.now().toString());
                            ps.setString(6, java.time.LocalDate.now().toString());
                            ps.setDouble(7, normalPrice);
                            ps.setString(8, ticketNumber);
                            ps.executeUpdate();
                            
                            // Generate QR code and barcode for the ticket
                            generateTicket(ticketNumber);
                            Thread.sleep(10); // Small delay to ensure unique timestamps
                        }
                    }

                    // Generate tickets for special class
                    for (int i = 0; i < specialQty; i++) {
                        String ticketNumber = "TICKET_" + System.currentTimeMillis() + "_S" + i;
                        try (PreparedStatement ps = conn.prepareStatement(ticketSql)) {
                            ps.setString(1, currentUser);
                            ps.setString(2, movieTitle);
                            ps.setString(3, movieGenre);
                            ps.setString(4, "S" + (i + 1));  // Special class seat number
                            ps.setString(5, java.time.LocalTime.now().toString());
                            ps.setString(6, java.time.LocalDate.now().toString());
                            ps.setDouble(7, specialPrice);
                            ps.setString(8, ticketNumber);
                            ps.executeUpdate();
                            
                            // Generate QR code and barcode for the ticket
                            generateTicket(ticketNumber);
                            Thread.sleep(10); // Small delay to ensure unique timestamps
                        }
                    }

                    conn.commit(); // Commit transaction
                    showAlert(Alert.AlertType.INFORMATION, "Purchase successful!");
                }
            } catch (Exception e) {
                conn.rollback(); // Rollback on error
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error saving purchase: " + e.getMessage());
        }
    }

    private void generateTicket(String ticketNumber) {
        try {
            // Generate QR Code
            String qrCodePath = "qrcodes/" + ticketNumber + "_qrcode.png";
            TicketGenerator.generateQRCode(ticketNumber, qrCodePath);

            // Generate Barcode
            String barcodePath = "barcodes/" + ticketNumber + "_barcode.png";
            TicketGenerator.generateBarcode(ticketNumber, barcodePath);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error generating ticket: " + e.getMessage());
        }
    }

    private void showReceipt() {
        if (lastPurchasedMovie == null) {
            showAlert(Alert.AlertType.WARNING, "No recent purchase found.");
            return;
        }

        showTicketView(lastPurchasedMovie,lastNormalQty,lastSpecialQty,lastTotal);

    }

    private void printNode(Node node) {
        Printer printer = Printer.getDefaultPrinter();
        PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT, Printer.MarginType.DEFAULT);
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(node.getScene().getWindow())) {
            job.printPage(pageLayout, node);
            job.endJob();
        }
    }

    private void exportReceiptToPDF(Node node) {
        try {
            WritableImage snapshot = node.snapshot(new SnapshotParameters(), null);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save PDF Receipt");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                File tempImage = new File("temp_receipt.png");
                ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, null), "png", tempImage);

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                com.itextpdf.text.Image img = com.itextpdf.text.Image.getInstance(tempImage.getAbsolutePath());
                img.scaleToFit(500, 700);
                document.add(img);
                document.close();
                tempImage.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type.toString());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleClear() {
        // Clear the selection in table
        availableMovies_tableView.getSelectionModel().clearSelection();
        
        // Reset the spinners
        availableMovies_normalClass_quantity.getValueFactory().setValue(0);
        availableMovies_specialClass_quantity.getValueFactory().setValue(0);
        
        // Clear the labels
        availableMovies_movieTitle.setText("");
        availableMovies_genre.setText("");
        availableMovies_date.setText("");
        availableMovies_total.setText("₹0.00");
        
        // Clear the image
        availableMovies_imageView.setImage(null);
    }

    // Navigation
    @FXML void handleAddMovieBtn(ActionEvent event) { switchScene(event, "/com/example/demo/addmovies.fxml"); }
    @FXML void handleAvailableMovieBtn(ActionEvent event) { switchScene(event, "/com/example/demo/availablemovies.fxml"); }
    @FXML void handleCustomersBtn(ActionEvent event) { switchScene(event, "/com/example/demo/customers.fxml"); }
    @FXML void handleDashboardBtn(ActionEvent event) { switchScene(event, "/com/example/demo/dashboard.fxml"); }
    @FXML void handleEditScreeningBtn(ActionEvent event) { switchScene(event, "/com/example/demo/editscreening.fxml"); }
    @FXML void handleSignoutBtn(ActionEvent event) { switchScene(event, "/com/example/demo/login.fxml"); }
    @FXML void handleChatBtn(ActionEvent event) { switchScene(event, "/com/example/demo/admin_chat.fxml"); }

    private void switchScene(ActionEvent event, String path) {
        FXMLScene fxmlScene = FXMLScene.load(path);
        Scene scene = new Scene(fxmlScene.getRoot());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void showTicketView(Movie movie, int normalQty, int specialQty, int total) {
        VBox ticketContainer = new VBox(10);
        ticketContainer.setStyle("-fx-padding: 16; -fx-background-color: white;");

        int seatNo = 1;

        try {
            // Generate Normal tickets
            for (int i = 0; i < normalQty; i++) {
                String ticketId = "TICKET_" + System.currentTimeMillis() + "_N" + i;
                String qrData = String.format("Movie: %s\nDate: %s\nTime: %s\nHall: %s\nRow: %s\nSeat: %d\nClass: %s\nPrice: %d\nLocation: %s",
                        movie.getTitle(), movie.getDate(), movie.getDate(), "Blue", "D", seatNo, "Normal", 150, "POV Cinema Hall, Dhaka");
                String qrPath = TicketGenerator.generateQRCode(qrData, ticketId);

                AnchorPane ticketPane = createSingleTicket(movie.getTitle(), movie.getDate(), movie.getDate(), "BDT150", "Blue", "D", String.valueOf(seatNo), "Normal", "POV Cinema Hall, Dhaka", movie.getPosterPath(), qrPath);
                ticketContainer.getChildren().add(ticketPane);

                if (i < normalQty - 1 || specialQty > 0) {
                    Separator separator = new Separator();
                    separator.setStyle("-fx-background-color: #ccc;");
                    ticketContainer.getChildren().add(separator);
                }

                seatNo++;
            }

            // Generate Special tickets
            for (int i = 0; i < specialQty; i++) {
                String ticketId = "TICKET_" + System.currentTimeMillis() + "_S" + i;
                String qrData = String.format("Movie: %s\nDate: %s\nTime: %s\nHall: %s\nRow: %s\nSeat: %d\nClass: %s\nPrice: %d\nLocation: %s",
                        movie.getTitle(), movie.getDate(), movie.getDate(), "Hall", "Row", seatNo, "Special", 300, "POV Cinema Hall, Dhaka");
                String qrPath = TicketGenerator.generateQRCode(qrData, ticketId);

                AnchorPane ticketPane = createSingleTicket(movie.getTitle(), movie.getDate(), movie.getDate(), "BDT300", "Red", "D", String.valueOf(seatNo), "Special", "POV Cinema Hall, Dhaka", movie.getPosterPath(), qrPath);
                ticketContainer.getChildren().add(ticketPane);

                if (i < specialQty - 1) {
                    Separator separator = new Separator();
                    separator.setStyle("-fx-background-color: #ccc;");
                    ticketContainer.getChildren().add(separator);
                }

                seatNo++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Wrap tickets in ScrollPane
        ScrollPane scrollPane = new ScrollPane(ticketContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);

        // Buttons for Print and Export PDF
        Button printBtn = new Button("Print All");
        Button exportPdfBtn = new Button("Export PDF");

        HBox buttonBox = new HBox(10, printBtn, exportPdfBtn);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10, scrollPane, buttonBox);
        root.setPadding(new Insets(10));

        Stage ticketStage = new Stage();
        ticketStage.setTitle("🎟️ Movie Tickets");
        ticketStage.setScene(new Scene(root, 620, 500));
        ticketStage.show();

        // Print button action
        printBtn.setOnAction(evt -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(ticketStage)) {
                boolean printed = job.printPage(ticketContainer);
                if (printed) {
                    job.endJob();
                }
            }
        });

        // Export PDF button action
        exportPdfBtn.setOnAction(evt -> {
            try {
                exportReceiptToPDF(ticketContainer);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "PDF exported successfully!");
                alert.showAndWait();
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to export PDF.");
                alert.showAndWait();
            }
        });
    }


    private AnchorPane createSingleTicket(String title, String date, String time, String price,
                                    String hall, String row, String seats, String classText, String location,
                                    String posterPath, String qrPath) {
        FXMLScene fxmlScene = FXMLScene.load("/com/example/demo/recipt.fxml");
        // Get the controller for the loaded FXML
        ReceiptController controller = (ReceiptController) fxmlScene.getController();

        // Call the setData method to pass all ticket info
        controller.setData(title, date, time, price, hall, row, seats, classText, location, posterPath, qrPath);

        // Return the populated HBox node
        return (AnchorPane) fxmlScene.getRoot();
    }

    public void initData(int movieId, String username) {
        this.selectedMovieId = movieId;
        this.currentUser = username;
        if (username != null) {
            this.username.setText(username);
        }
        if (movieId > 0) {
            loadMovieDetails(movieList.stream()
                    .filter(movie -> movie.getId() == movieId)
                    .findFirst()
                    .orElse(null));
        }
    }

    private void loadMovieDetails(Movie movie) {
        if (movie != null) {
            availableMovies_movieTitle.setText(movie.getTitle());
            availableMovies_genre.setText(movie.getGenre());
            availableMovies_date.setText(movie.getDate());

            // Load movie poster if available
            if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
                try {
                    String posterPath = movie.getPosterPath();
                    File imageFile = new File(posterPath);
                    
                    // If the file doesn't exist with the direct path, try the images directory
                    if (!imageFile.exists()) {
                        // Try with images/ prefix
                        imageFile = new File("images/" + new File(posterPath).getName());
                        
                        // If still not found, try without images/ prefix
                        if (!imageFile.exists() && posterPath.startsWith("images/")) {
                            imageFile = new File(posterPath);
                        }
                    }
                    
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        availableMovies_imageView.setImage(image);
                        availableMovies_imageView.setFitWidth(135);
                        availableMovies_imageView.setFitHeight(175);
                        availableMovies_imageView.setPreserveRatio(true);
                        System.out.println("Successfully loaded image from: " + imageFile.getAbsolutePath());
                    } else {
                        System.err.println("Image file not found: " + imageFile.getAbsolutePath());
                        availableMovies_imageView.setImage(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Failed to load image from path: " + movie.getPosterPath());
                    availableMovies_imageView.setImage(null);
                }
            } else {
                availableMovies_imageView.setImage(null);
            }

            // Reset quantities and total
            availableMovies_normalClass_quantity.getValueFactory().setValue(0);
            availableMovies_specialClass_quantity.getValueFactory().setValue(0);
            availableMovies_total.setText("₹0.00");
        }
    }

}
