package com.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

//7-5-2025
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

//private Connection connect;
//private PreparedStatement prepare;
//private Statement statement;
//private ResultSet result;

/*public void signin(){

    String sql = "SELECT * FROM admin WHERE username = ? and password = ?";
    connect = database.connectDb();

    try{
        prepare = connect.prepareStatement(sql);
        prepare.setString(1,signin_username.getText() );
        prepare.setString(2,signin_password.getText() );

        result = prepare.executeQuery();

        Alert alert;

       // CHECK IF THE USERNAME OR PASSWORD IS EMPTY
        if(signin_username.getText().isEmpty() || signin_password.getText().isEmpty()){

            alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all blank fields");
            alert.showAndWait();


        }else{

            if(result.next()){
                alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Information Message");
                alert.setHeaderText(null);
                alert.setContentText("Successfully Login!");
                altert.showAndWait();

                // TO HIDE THE LOGIN FORM :)
                signIn_loginBtn.getScene().getWindow().hide();
                Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));

                Stage stage = new Stage();
                Scene scene = new Scene(root);

                stage.setScene(scene);
                stage.show();
            }else{
                alter=new Alter(AlterType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Wrong Username/Password");
                alert.showAndWait();
            }
        }

    }catch (Exception e) {e.printStackTrace();}
}
*/
public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}