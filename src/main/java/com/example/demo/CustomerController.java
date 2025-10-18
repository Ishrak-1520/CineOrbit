package com.example.demo;

import com.example.demo.utils.DatabaseConnection;
import com.example.demo.utils.FXMLScene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.sql.*;

public class CustomerController {

    @FXML private Button customers_clearBtn;
    @FXML private Button customers_deleteBtn;
    @FXML private TextField customers_search;
    @FXML private AnchorPane customers_form;

    @FXML private Label customers_movieTitle;
    @FXML private Label customers_genre;
    @FXML private Label customers_time;
    @FXML private Label customers_date;
    @FXML private Label customers_ticketNumber;
    @FXML private Label username;

    @FXML private TableView<Customer> customers_tableView;
    @FXML private TableColumn<Customer, String> customers_col_tableNo;
    @FXML private TableColumn<Customer, String> customers_col_movieTitle;
    @FXML private TableColumn<Customer, String> customers_col_time;
    @FXML private TableColumn<Customer, String> customers_col_date;
    @FXML private TableColumn<Customer, String> customers_col_totalPayment;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private String currentUser;

    @FXML
    public void initialize() {
        System.out.println("Initializing CustomerController...");
        
        // Initialize table columns
        customers_col_tableNo.setCellValueFactory(new PropertyValueFactory<>("ticketNumber"));
        customers_col_movieTitle.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        customers_col_totalPayment.setCellValueFactory(new PropertyValueFactory<>("totalPayment"));
        customers_col_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        customers_col_time.setCellValueFactory(new PropertyValueFactory<>("time"));

        // Set admin username if available
        String loggedInUser = LoginController.getLoggedInUsername();
        if (loggedInUser != null) {
            username.setText(loggedInUser);
            currentUser = loggedInUser;
        }

        // Load initial data
        loadCustomerData();

        // Setup table selection listener
        customers_tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showCustomerDetails(newSelection);
            }
        });

        // Setup search functionality
        setupSearch();

        // Setup button handlers
        customers_clearBtn.setOnAction(e -> handleClear());
        customers_deleteBtn.setOnAction(e -> handleDelete());
    }

    public void initData(String username) {
        System.out.println("Initializing data for user: " + username);
        this.currentUser = username;
        this.username.setText(username);
        loadCustomerData();
    }

    private void loadCustomerData() {
        System.out.println("Loading customer data...");
        customerList.clear();
        
        try (Connection conn = new DatabaseConnection().getConnection()) {
            System.out.println("Database connection successful");
            
            // First check if the current user is an admin
            String roleCheckSql = "SELECT role FROM users WHERE username = ?";
            boolean isAdmin = false;
            
            try (PreparedStatement ps = conn.prepareStatement(roleCheckSql)) {
                ps.setString(1, currentUser);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    isAdmin = (rs.getInt("role") == 0);
                }
            }
            
            // Use different queries for admin and regular users
            String sql;
            if (isAdmin) {
                sql = "SELECT * FROM customers ORDER BY created_at DESC";
            } else {
                sql = "SELECT * FROM customers WHERE user_id = ? ORDER BY created_at DESC";
            }
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                if (!isAdmin) {
                    ps.setString(1, currentUser);
                }
                ResultSet rs = ps.executeQuery();
                int count = 0;
                
                while (rs.next()) {
                    count++;
                    String ticketNumber = rs.getString("ticket_number");
                    String movieTitle = rs.getString("movie_title");
                    String genre = rs.getString("genre");
                    String tableNo = rs.getString("table_no");
                    String time = rs.getString("time");
                    String date = rs.getString("date");
                    double payment = rs.getDouble("total_payment");
                    
                    System.out.println("Loading ticket: " + ticketNumber + " for movie: " + movieTitle);
                    
                    Customer customer = new Customer(
                        ticketNumber,
                        movieTitle,
                        genre,
                        tableNo,
                        time,
                        date,
                        String.format("₹%.2f", payment)
                    );
                    customerList.add(customer);
                }
                
                System.out.println("Loaded " + count + " tickets");
                customers_tableView.setItems(customerList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Driver Error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database driver error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected Error: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Unexpected error: " + e.getMessage());
        }
    }

    private void setupSearch() {
        FilteredList<Customer> filteredData = new FilteredList<>(customerList, p -> true);
        
        customers_search.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(customer -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                return customer.getMovieTitle().toLowerCase().contains(lowerCaseFilter) ||
                       customer.getTicketNumber().toLowerCase().contains(lowerCaseFilter) ||
                       customer.getDate().toLowerCase().contains(lowerCaseFilter);
            });
        });
        
        SortedList<Customer> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(customers_tableView.comparatorProperty());
        customers_tableView.setItems(sortedData);
    }

    private void showCustomerDetails(Customer customer) {
        customers_movieTitle.setText(customer.getMovieTitle());
        customers_genre.setText(customer.getGenre());
        customers_date.setText(customer.getDate());
        customers_time.setText(customer.getTime());
        customers_ticketNumber.setText(customer.getTicketNumber());
    }

    private void handleClear() {
        customers_tableView.getSelectionModel().clearSelection();
        customers_search.clear();
        customers_movieTitle.setText("");
        customers_genre.setText("");
        customers_date.setText("");
        customers_time.setText("");
        customers_ticketNumber.setText("");
    }

    private void handleDelete() {
        Customer selectedCustomer = customers_tableView.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a ticket to delete");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this ticket?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            String sql = "DELETE FROM customers WHERE ticket_number = ?";
            
            try (Connection conn = new DatabaseConnection().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setString(1, selectedCustomer.getTicketNumber());
                
                int result = ps.executeUpdate();
                if (result > 0) {
                    customerList.remove(selectedCustomer);
                    handleClear();
                    showAlert(Alert.AlertType.INFORMATION, "Ticket deleted successfully");
                }
                
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error deleting ticket: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type.toString());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation methods
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
}

