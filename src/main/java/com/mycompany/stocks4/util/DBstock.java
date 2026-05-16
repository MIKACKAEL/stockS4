package com.mycompany.stocks4.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBstock {
    private static final String URL = "jdbc:postgresql://localhost:5432/db_stock";
    private static final String USER = "postgres";
    private static final String PASSWORD = "mika";

    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the PostgreSQL database successfully!");
            return connection;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC Driver introuvable dans le classpath.", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Connexion PostgreSQL echouee: " + e.getMessage(), e);
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing the connection.");
                e.printStackTrace();
            }
        }
    }
}
