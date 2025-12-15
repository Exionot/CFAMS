package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import util.FancyAlert;
import javafx.scene.control.Alert.AlertType;

public class LoginController implements Initializable{
	@FXML private TextField usernameField;
	@FXML private PasswordField passwordField;
	@FXML private Button loginButton;
	@FXML private ImageView iconImageView;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		iconImageView.setImage(new Image(new File(Main.RESOURCE_DIR + "icon.png").toURI().toString()));
	}
	
	public synchronized void loginAttempt(ActionEvent e) {
		String username = usernameField.getText();
		String password = passwordField.getText();
		
		if(!util.DatabaseConnection.isDatabaseConnected()) {
			new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
			return;
		}
		
		if (login(username, password)) {
			util.User user = createUser(username);
			if (user != null) {
				util.Session.getSession().setUser(user);
				
				FancyAlert alert = new FancyAlert(AlertType.INFORMATION);
				alert.setTitle("Login");
				alert.setHeaderText("You've successfully logged in!");
				alert.showAndWait();
				try {
					util.SceneChanger.changeScene(e, util.SceneChanger.SceneName.USER_DASHBOARD.getName());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				util.SystemLog.logAction(
						util.SystemLog.Type.ACTION, 
						util.Session.getSession().getUser().getUserId(), 
						util.SystemLog.ActionMessage.LOGIN
						);
			}
		}else {
			FancyAlert alert = new FancyAlert(AlertType.ERROR);
			alert.setTitle("Login");
			alert.setHeaderText("Invalid username or password!");
			alert.show();
		}
	}
	
	private boolean login(String username, String password) {
		String query = "SELECT hash_password FROM users WHERE username = ? OR student_id = ?";
		try {
			Connection conn = util.DatabaseConnection.getConnection();
			if (conn != null) {
				PreparedStatement ps = conn.prepareStatement(query);
				ps.setString(1, username);
				ps.setString(2, username);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						String storedHash = rs.getString("hash_password");
						if (!util.PasswordUtils.checkPassword(password, storedHash)) {
							return false;
						}
						return true;
					}else return false;
				}
			}else {
				return false;
			}
		}catch (SQLException e) { e.printStackTrace(); return false;}
	}
	
	private synchronized util.User createUser(String usernameId){
		String query = "SELECT id, username, student_id, course, student_year, image_url FROM users WHERE username = ? OR student_id = ?";

		try (Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, usernameId);
			ps.setString(2, usernameId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return new util.User(
							rs.getInt("id"),
							rs.getString("username"),
							rs.getString("student_id"),
							rs.getString("course"),
							rs.getString("student_year"),
							rs.getString("image_url")
							);
				} 
				return null;
			}

		}catch (SQLException e) {return null;}
	}
}
