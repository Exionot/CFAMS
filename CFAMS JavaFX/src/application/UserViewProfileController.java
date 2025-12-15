package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.FancyAlert;

public class UserViewProfileController implements Initializable {
	
	@FXML private Label usernameLabel, studentIdLabel, courseLabel, yearLabel, userTypeLabel, studentIdTitleLabel, courseTitleLabel, yearTitleLabel;
	@FXML private ImageView userProfileImageView;
	
	private util.User user = util.Session.getSession().getUser();
	private File selectedFile = null;
	private String uploadedFilePath = null;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		System.out.println(grabSurname(user.getUsername()));
		
		Rectangle clip = new Rectangle(userProfileImageView.getFitWidth(), userProfileImageView.getFitHeight());
		clip.setArcWidth(20);  
		clip.setArcHeight(20); 
		userProfileImageView.setClip(clip);
		Path imagePath = Paths.get(user.getImagePath());
		Path defaultPath = Paths.get(Main.RESOURCE_DIR + "default-profile.jpg");
		Path pathToUse = Files.exists(imagePath)
		        ? imagePath
		        : defaultPath;
		userProfileImageView.setImage(new Image(pathToUse.toUri().toString()));
		if (user.getRole() != util.User.UserRole.USER) {
			((Pane)studentIdTitleLabel.getParent()).getChildren().remove(studentIdTitleLabel);
			((Pane)courseTitleLabel.getParent()).getChildren().remove(courseTitleLabel);
			((Pane)yearTitleLabel.getParent()).getChildren().remove(yearTitleLabel);
			((Pane)studentIdLabel.getParent()).getChildren().remove(studentIdLabel);
			((Pane)courseLabel.getParent()).getChildren().remove(courseLabel);
			((Pane)yearLabel.getParent()).getChildren().remove(yearLabel);
			usernameLabel.setText(user.getUsername());
			userTypeLabel.setText(user.getRole().toString());
			return;
		}
		usernameLabel.setText(user.getUsername());
		userTypeLabel.setText(user.getRole().toString());
		studentIdLabel.setText(user.getStudentId());
		courseLabel.setText(user.getCourse().getName());
		yearLabel.setText(user.getYear().getName());
	}
	
	public void goBackToDashboard (ActionEvent e) {
		util.SceneChanger.closeWindowAndRun(e);
	}
	
	public void showChangePasswordPane(ActionEvent e) throws IOException {
		util.SceneChanger.newWindow(e, util.SceneChanger.SceneName.CHANGE_PASSWORD.getName());
	}
	
	public void changeProfile (ActionEvent e) {
		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        selectedFile = fileChooser.showOpenDialog((Stage)((Node)e.getSource()).getScene().getWindow());
        System.out.println(selectedFile);
        if (selectedFile != null) {
        	userProfileImageView.setImage(new Image(selectedFile.toURI().toString()));
        	
        	FancyAlert profileAlert = new FancyAlert(AlertType.CONFIRMATION);
			profileAlert.setTitle("Change Profile Image");
			profileAlert.setHeaderText("Save changes?");
			if (profileAlert.showAndWait().get() == ButtonType.OK) {
				FancyAlert errorAlert = new FancyAlert(AlertType.ERROR);
				errorAlert.setTitle("Change Profile Image");
				
				if(!util.DatabaseConnection.isDatabaseConnected()) {
					new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
					return;
				}
				
				if (!uploadImage(selectedFile, grabSurname(user.getUsername()))) {
					errorAlert.setHeaderText("Something went wrong when uploading profile image.");
					errorAlert.show();
					return;
				}
				
				if (!updateProfileToDB(user.getUserId(), uploadedFilePath)) {
					errorAlert.setHeaderText("Something went wrong when adding user profile to database.");
					errorAlert.show();
					return;
				}
				
				FancyAlert successAlert = new FancyAlert(AlertType.INFORMATION);
				successAlert.setTitle("Change Profile Image");
				successAlert.setHeaderText("Profile image updated!");
				successAlert.show();
			}else {
				userProfileImageView.setImage(new Image(new File(user.getImagePath()).toURI().toString()));
			}
        }
	}
	
	private synchronized boolean uploadImage(File file, String username) {
		if (file != null) {
			try {
				File targetDir = new File(Main.UPLOAD_DIR);
				System.out.println(targetDir);
				if (!targetDir.exists())
					targetDir.mkdirs();
				//set filename here -- username-profile
				String timestamp = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss").format(new Date());
				String extension = getFileExtension(file.getName());
				String newFileName = username + "_profile" + timestamp + "." + extension;
				File destFile = new File(targetDir, newFileName);
				Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				uploadedFilePath = Main.DB_UPLOAD_PATH + newFileName;
				System.out.println(uploadedFilePath);
				return true;
			} catch (IOException e) {
				FancyAlert successAlert = new FancyAlert(AlertType.INFORMATION);
				successAlert.setTitle("Change Profile Image");
				successAlert.setHeaderText("Returned false\n" + e);
				successAlert.show();
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
	
	private String grabSurname(String name) {
		int index = name.lastIndexOf('.');
		String lastName = ((name.toLowerCase()).substring(index + 1).trim()).replace(" ", "_");
		return lastName;
	}	
	
	private boolean updateProfileToDB(int userId, String imagePath) {
		String query = "UPDATE users SET image_url = ? WHERE id = ?";
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, imagePath);
			ps.setInt(2, userId);
			int rowsAdded = ps.executeUpdate();
			System.out.println(rowsAdded);
			return rowsAdded == 1;
		}catch(SQLException e) { System.out.println(e); return false; }
	}
}
