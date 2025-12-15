package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import util.FancyAlert;

public class AdminAddUserController implements Initializable{
	
	@FXML private TextField firstNameField, middleNameField, surnameField, studentIdField, modUsernameField, adminUsernameField;
	@FXML private ComboBox<util.User.Course> courseComboBox; 
	@FXML private ComboBox<util.User.Year> yearComboBox;
	@FXML private PasswordField passwordField, confirmPasswordField, modPasswordField, adminPasswordField, modConfirmPasswordField, adminConfirmPasswordField;
	@FXML private ImageView profilePreviewImageView, modProfilePreviewImageView, adminProfilePreviewImageView;
	@FXML private TabPane addUserFormContainer;
	
	private File selectedFile = null;
	private String uploadedFilePath = null;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		courseComboBox.getItems().addAll(util.User.Course.values());
		courseComboBox.setConverter(new StringConverter<util.User.Course>() {
			@Override
			public String toString(util.User.Course arg0) {
				return (arg0 != null) ? arg0.getName() : "";
			}
			
			@Override
			public util.User.Course fromString(String arg0) {
				return null;
			}
		});
		yearComboBox.getItems().addAll(util.User.Year.values());
		yearComboBox.setConverter(new StringConverter<util.User.Year>() {
			@Override
			public String toString(util.User.Year arg0) {
				return (arg0 != null) ? arg0.getName() : "";
			}
			
			@Override
			public util.User.Year fromString(String arg0) {
				return null;
			}
		});
	}

	public void goBackToDashboard(ActionEvent e) {
		FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);
		alert.setTitle("Unfinished User");
		alert.setHeaderText("User has not been added. Are you sure you want to exit?");
		((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
		((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
		if (alert.showAndWait().get() == ButtonType.OK){
			util.SceneChanger.closeWindow(e);
		}
	}
	
	public void selectImage(ActionEvent e) {
		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        selectedFile = fileChooser.showOpenDialog((Stage)((Node)e.getSource()).getScene().getWindow());
        if (selectedFile != null) {
            switch (addUserFormContainer.getSelectionModel().getSelectedIndex()) {
			case 0: 
				profilePreviewImageView.setImage(new Image(selectedFile.toURI().toString()));
				break;
			case 1: 
				modProfilePreviewImageView.setImage(new Image(selectedFile.toURI().toString()));
				break;
			case 2: 
				adminProfilePreviewImageView.setImage(new Image(selectedFile.toURI().toString()));
				break;
			default:
				throw new IllegalArgumentException("Unexpected value. Tab does not exist.");
			}
        }
	}
	
	public void addNewUser(ActionEvent e) {
		FancyAlert errorAlert = new FancyAlert(AlertType.ERROR);
		errorAlert.setTitle("Add User");
		
		util.User.UserRole role = util.User.UserRole.USER;
		String firstName = firstNameField.getText().trim();
		String middleName = middleNameField.getText().trim();
		String surname = surnameField.getText().trim();
		String username = formatName(firstName, middleName, surname);
		String studentId = studentIdField.getText().trim();
		util.User.Course course = courseComboBox.getSelectionModel().getSelectedItem();
		util.User.Year year = yearComboBox.getSelectionModel().getSelectedItem();
		String password = passwordField.getText().trim();
		String confirmPassword = confirmPasswordField.getText().trim();
		if (
				firstName.isBlank() ||
				middleName.isBlank() ||
				surname.isBlank() ||
				studentId.isBlank() ||
				course == null ||
				year == null ||
				password.isBlank() ||
				confirmPassword.isBlank() 
			) 
		{
			errorAlert.setHeaderText("Please fill out all fields.");
			errorAlert.show();
			return;
		}
		
		if (!studentId.matches("\\d{2}-\\d{4}")) {
			errorAlert.setHeaderText("Student Id is not valid.");
			errorAlert.show();
			return;
		}
		
		if (password.length() < 8) {
			errorAlert.setHeaderText("Password cannot be less than 8 characters.");
			errorAlert.show();
			return;
		}
		
		if (!password.equals(confirmPassword)) {
			errorAlert.setHeaderText("Passwords do not match.");
			errorAlert.show();
			return;
		}
		
		if (selectedFile == null) {
			FancyAlert profileAlert = new FancyAlert(AlertType.CONFIRMATION);
			profileAlert.setTitle("Add User");
			profileAlert.setHeaderText("You have not selected a profile image for this user. \nIt will be given a default profile image.");
			((Button) profileAlert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
			((Button) profileAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
			if (profileAlert.showAndWait().get() != ButtonType.OK) {
				return;
			}
		}
		
		if (!uploadImage(selectedFile, surname)) {
			errorAlert.setHeaderText("Something went wrong when uploading profile image.");
			errorAlert.show();
			return;
		}
		
		if(!util.DatabaseConnection.isDatabaseConnected()) {
			new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
			return;
		}
		
		if (addUserToDB(role, username, studentId, password, course, year, uploadedFilePath)) {
			FancyAlert successAlert = new FancyAlert(AlertType.INFORMATION);
			successAlert.setTitle("Add User");
			successAlert.setHeaderText("User has successfully been added!");
			successAlert.showAndWait();
			util.SceneChanger.closeWindow(e);
		}else {
			errorAlert.setHeaderText("Something went wrong when adding user.");
			errorAlert.show();
			return;
		}
	}
	
	public void addNewModerator(ActionEvent e) {
		FancyAlert errorAlert = new FancyAlert(AlertType.ERROR);
		errorAlert.setTitle("Add User");
		
		util.User.UserRole role = util.User.UserRole.MODERATOR;
		String username = modUsernameField.getText().trim();
		String password = modPasswordField.getText().trim();
		String confirmPassword = modConfirmPasswordField.getText().trim();
		if (
				username.isBlank() ||
				password.isBlank() ||
				confirmPassword.isBlank() 
			) 
		{
			errorAlert.setHeaderText("Please fill out all fields.");
			errorAlert.show();
			return;
		}
		
		if (password.length() < 8) {
			errorAlert.setHeaderText("Password cannot be less than 8 characters.");
			errorAlert.show();
			return;
		}
		
		if (!password.equals(confirmPassword)) {
			errorAlert.setHeaderText("Passwords do not match.");
			errorAlert.show();
			return;
		}
		
		FancyAlert addAlert = new FancyAlert(AlertType.CONFIRMATION);
		addAlert.setTitle("Add User");
		addAlert.setHeaderText("Are you sure you want to add " + username + " as a new Moderator?");
		((Button) addAlert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
		((Button) addAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
		if (addAlert.showAndWait().get() == ButtonType.OK) {
			if (selectedFile == null) {
				FancyAlert profileAlert = new FancyAlert(AlertType.CONFIRMATION);
				profileAlert.setTitle("Add User");
				profileAlert.setHeaderText("You have not selected a profile image for this user. \nIt will be given a default profile image.");
				((Button) profileAlert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
				((Button) profileAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
				if (profileAlert.showAndWait().get() != ButtonType.OK) {
					return;
				}
			}
			if (!uploadImage(selectedFile, username)) {
				errorAlert.setHeaderText("Something went wrong when uploading profile image.");
				errorAlert.show();
				return;
			}
			
			if(!util.DatabaseConnection.isDatabaseConnected()) {
				new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
				return;
			}
			
			if (addUserToDB(role, username, null, password, null, null, uploadedFilePath)) {
				FancyAlert successAlert = new FancyAlert(AlertType.INFORMATION);
				successAlert.setTitle("Add User");
				successAlert.setHeaderText("User has successfully been added!");
				successAlert.showAndWait();
				util.SceneChanger.closeWindow(e);
			}else {
				errorAlert.setHeaderText("Something went wrong when adding user.");
				errorAlert.show();
				return;
			}
		}
		
		
	}
	
	public void addNewAdmin(ActionEvent e) {
		FancyAlert errorAlert = new FancyAlert(AlertType.ERROR);
		errorAlert.setTitle("Add User");
		
		util.User.UserRole role = util.User.UserRole.ADMIN;
		String username = adminUsernameField.getText().trim();
		String password = adminPasswordField.getText().trim();
		String confirmPassword = adminConfirmPasswordField.getText().trim();
		if (
				username.isBlank() ||
				password.isBlank() ||
				confirmPassword.isBlank() 
			) 
		{
			errorAlert.setHeaderText("Please fill out all fields.");
			errorAlert.show();
			return;
		}
		
		if (password.length() < 8) {
			errorAlert.setHeaderText("Password cannot be less than 8 characters.");
			errorAlert.show();
			return;
		}
		
		if (!password.equals(confirmPassword)) {
			errorAlert.setHeaderText("Passwords do not match.");
			errorAlert.show();
			return;
		}
		
		FancyAlert addAlert = new FancyAlert(AlertType.CONFIRMATION);
		addAlert.setTitle("Add User");
		addAlert.setHeaderText("Are you sure you want to add " + username + " as a new Admin?");
		((Button) addAlert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
		((Button) addAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
		if (addAlert.showAndWait().get() == ButtonType.OK) {
			if (selectedFile == null) {
				FancyAlert profileAlert = new FancyAlert(AlertType.CONFIRMATION);
				profileAlert.setTitle("Add User");
				profileAlert.setHeaderText("You have not selected a profile image for this user. \nIt will be given a default profile image.");
				((Button) profileAlert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
				((Button) profileAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
				if (profileAlert.showAndWait().get() != ButtonType.OK) {
					return;
				}
			}
			
			if (!uploadImage(selectedFile, username)) {
				errorAlert.setHeaderText("Something went wrong when uploading profile image.");
				errorAlert.show();
				return;
			}
			
			if(!util.DatabaseConnection.isDatabaseConnected()) {
				new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
				return;
			}
			
			if (addUserToDB(role, username, null, password, null, null, uploadedFilePath)) {
				FancyAlert successAlert = new FancyAlert(AlertType.INFORMATION);
				successAlert.setTitle("Add User");
				successAlert.setHeaderText("User has successfully been added!");
				successAlert.showAndWait();
				util.SceneChanger.closeWindow(e);
			}else {
				errorAlert.setHeaderText("Something went wrong when adding user.");
				errorAlert.show();
				return;
			}
		}
	}
	
	private String formatName(String firstName, String middleName, String surname) {
		String[] firstNameArray = firstName.split(" ");
		StringBuilder formattedFirstName = new StringBuilder();

		for (String names : firstNameArray) {
		    if (names.isEmpty()) continue;         
		    formattedFirstName.append(
		    		names.substring(0,1).toUpperCase() +
		    		names.substring(1).toLowerCase()
		    ).append(" ");
		}
		formattedFirstName = new StringBuilder(formattedFirstName.toString().trim());
		
		String middleInitial = middleName.substring(0,1).toUpperCase() + ".";
		String formattedSurname = surname.substring(0,1).toUpperCase() + surname.substring(1).toLowerCase();
		String formattedName = formattedFirstName + " " + middleInitial + " " + formattedSurname;
		
		return formattedName;
	}
	
	private synchronized boolean uploadImage(File file, String username) {
		if (file != null) {
			try {
				File targetDir = new File(Main.UPLOAD_DIR);
				if (!targetDir.exists())
					targetDir.mkdirs();
				//set filename here -- username-profile
				String timestamp = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss").format(new Date());
				String extension = getFileExtension(file.getName());
				String newFileName = username.toLowerCase() + "_profile" + timestamp + "." + extension;
				File destFile = new File(targetDir, newFileName);
				Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				uploadedFilePath = Main.DB_UPLOAD_PATH + newFileName;
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}else {
			uploadedFilePath = null;
			return true;
		}
	}
	
	private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
	
	private boolean addUserToDB(util.User.UserRole userRole, String username, String studentId, String password, util.User.Course course, util.User.Year year, String imagePath) {
		String query = "INSERT INTO users(user_role, username, student_id, hash_password, course, student_year, image_url) VALUES(?, ?, ?, ?, ?, ?, ?)";
		String hashPassword = util.PasswordUtils.hashPassword(password);
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			
			ps.setString(1, userRole.toString());
			ps.setString(2, username);
			
			if (studentId != null) ps.setString(3, studentId);
			else ps.setNull(3, java.sql.Types.VARCHAR);
			
			ps.setString(4, hashPassword);
			
			if (course != null) ps.setString(5, course.toString());
			else ps.setNull(5, java.sql.Types.VARCHAR);
			
			if (year != null) ps.setString(6, year.toString());
			else ps.setNull(6, java.sql.Types.VARCHAR);
			
			if (imagePath != null) ps.setString(7, imagePath);
			else ps.setNull(7, java.sql.Types.VARCHAR);
			
			int rowsAdded = ps.executeUpdate();
			System.out.println(rowsAdded);
			return rowsAdded == 1;
		}catch(SQLException e) { System.out.println(e); return false; }
	}
}
