package com.example.demo.utils;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

public class DatabaseConnection {
    private Connection connection;

    public DatabaseConnection() throws SQLException, ClassNotFoundException {
        this.connection = getConnection();

    }

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(
                "jdbc:mysql://" + Dotenv.load().get("DATABASE_HOSTNAME") + ":" + Dotenv.load().get("DATABASE_PORT") + "/" + Dotenv.load().get("DATABASE_NAME"), Dotenv.load().get("DATABASE_USERNAME"), Dotenv.load().get("DATABASE_SECRET")
        );

        return connection;
    }

    public void insideData(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(sql);
        connection.close();
    }

    public ResultSet queryData(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }





}
