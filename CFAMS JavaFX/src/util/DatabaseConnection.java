package util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javafx.scene.control.Alert.AlertType;

import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseConnection {
    
    private static String url;
    private static String user;
    private static String pass;

    static {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);

            String host = props.getProperty("db.host", "localhost");
            String port = props.getProperty("db.port", "3306");
            String dbName = props.getProperty("db.name", "cfams_db");
            user = props.getProperty("db.user", "root");
            pass = props.getProperty("db.pass", "");

            url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?serverTimezone=UTC&connectTimeout=5000";

        } catch (IOException e) {
            System.err.println("Could not load config.properties, using defaults.");
            url = "jdbc:mysql://localhost:3306/cfams_db?useSSL=false&serverTimezone=UTC";
            user = "root";
            pass = "";
        }
    }
    
    public static synchronized Connection getConnection() throws SQLException {
        try {
        	return DriverManager.getConnection(url, user, pass);
        }catch (SQLException e) {
        	FancyAlert alert = new FancyAlert(AlertType.ERROR);
			alert.setTitle("Database Connection");
			alert.setHeaderText("Could not connect to databse...\n" + e);
        	return null;
        }
    }
    
    public static boolean isDatabaseConnected() {
    	Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, user, pass);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	if (conn != null) return true;
    	else return false;
    }
}