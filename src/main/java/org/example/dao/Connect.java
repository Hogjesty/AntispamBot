package org.example.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {

    private static Connection instance;

    private static final String DATABASE_NAME = "";
    private static final String LOG = "";
    private static final String PASS = "";

    private Connect() {
    }

    public static Connection getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    private static void init() {
        try {
            Connect.instance =  DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + DATABASE_NAME, LOG, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
