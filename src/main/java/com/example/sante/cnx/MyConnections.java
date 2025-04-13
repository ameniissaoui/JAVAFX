package com.example.sante.cnx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyConnections {


    private final String url="jdbc:mysql://localhost:3306/sante";
    private final String login="root";
    private final String pwd="";
    public  static MyConnections instance;

    Connection cnx;

    public MyConnections(){
        try {
            cnx = DriverManager.getConnection(url,login,pwd);
            System.out.println("Connexion active: " + (cnx != null));
            System.out.println("Connexion établie!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }



    }
    public static MyConnections getInstance() {
        if (instance == null) {
            instance = new MyConnections();
        }
        return instance;
    }
    public Connection getCnx(){
        return cnx;

    }
}
