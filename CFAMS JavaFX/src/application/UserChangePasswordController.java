package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import util.FancyAlert;
import javafx.scene.control.Alert.AlertType;

public class UserChangePasswordController {
	@FXML private PasswordField currentPasswordField, newPasswordField, confirmNewPasswordField;
	
	private util.User user = util.Session.getSession().getUser();
	
	public void goBackToDashboard(ActionEvent e) {
		util.SceneChanger.closeWindowAndRun(e);
	}
	
	public void changePassword(ActionEvent e) {
		FancyAlert errorAlert = new FancyAlert(AlertType.ERROR);
		errorAlert.setTitle("Change Password");
		
		if(!util.DatabaseConnection.isDatabaseConnected()) {
			new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
			return;
		}
		
		if (!checkCurrentPassword(user.getUserId(), currentPasswordField.getText().trim())) {
			errorAlert.setHeaderText("Current password is incorrect.");
			errorAlert.show();
			return;
		}
		
		if (!newPasswordField.getText().trim().equals(confirmNewPasswordField.getText().trim())) {
			errorAlert.setHeaderText("Passwords do not match.");
			errorAlert.show();
			return;
		}
		
		if (newPasswordField.getText().trim().equals(currentPasswordField.getText().trim())) {
			errorAlert.setHeaderText("You cannot use your current password.");
			errorAlert.show();
			return;
		}
		
		FancyAlert profileAlert = new FancyAlert(AlertType.CONFIRMATION);
		profileAlert.setTitle("Change Password");
		profileAlert.setHeaderText("You will be logged out after changing. Continue?");
		if (profileAlert.showAndWait().get() == ButtonType.OK) {
			
			if (!updatePasswordToDB(user.getUserId(), confirmNewPasswordField.getText().trim())) {
				errorAlert.setHeaderText("Something went wrong when updating password.");
				errorAlert.show();
				return;
			}
			
			FancyAlert successAlert = new FancyAlert(AlertType.INFORMATION);
			successAlert.setTitle("Change Password");
			successAlert.setHeaderText("Password has been changed!");
			successAlert.setContentText("You will now be logged out.");
			successAlert.showAndWait();
			Main.logOut();
		}
	}
	
	private boolean checkCurrentPassword(int userId, String currentPassword) {
		String query = "SELECT hash_password FROM users WHERE id = ?";
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setInt(1, userId);
			try(ResultSet rs = ps.executeQuery()){
				if (rs.next()) {
					return util.PasswordUtils.checkPassword(currentPassword, rs.getString("hash_password"));
				}
				return false;
			}
		}catch(SQLException e) { System.out.println(e); return false; }
	}
	
	private boolean updatePasswordToDB(int userId, String newPassword) {
		String query = "UPDATE users SET hash_password = ? WHERE id = ?";
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, util.PasswordUtils.hashPassword(newPassword));
			ps.setInt(2, userId);
			int rowsAdded = ps.executeUpdate();
			System.out.println(rowsAdded);
			return rowsAdded == 1;
		}catch(SQLException e) { System.out.println(e); return false; }
	}
}
